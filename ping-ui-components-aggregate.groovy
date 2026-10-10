import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile

/**
 * 抽象组件构建期聚合
 *
 * 把本模块与依赖构件中的抽象组件描述聚合为单一索引，落固定路径
 * META-INF/ping/ui/components.json —— 运行时只读该固定路径，杜绝 classpath* 通配扫描。
 *
 * 输入（按依赖在前、本模块在后的顺序处理，后者覆盖前者）：
 *   1. 构件内的 META-INF/ping/ui/types/*.json          组件类型定义（ping-ui 提供）
 *   2. 构件内的 ui/<page>/compose.json                     页面装配描述（各业务模块提供）
 *   3. 构件内的 META-INF/ping/ui/components.json        上游已聚合的索引
 *
 * 输出：
 *   ${project.build.outputDirectory}/META-INF/ping/ui/components.json
 *   { "schema_version": 1, "types": [...], "pages": [ { "key": ..., "tree": {...} } ] }
 *
 * 失败策略：索引缺失、文件非法一律告警并跳过，绝不抛出，保证构建不因描述问题中断。
 */

final String INDEX_REL = "META-INF/ping/ui/components.json"
final String TYPES_PREFIX = "META-INF/ping/ui/types/"
final String PAGES_ROOT = "ui/"

def slurper = new JsonSlurper()
def typeDefs = new TreeMap<String, Object>()
def pages = new TreeMap<String, Object>()
def missing = []
def invalid = 0

def warn = { String msg -> log.warn("[抽象组件聚合] ${msg}") }

/** 解析 JSON 文本；失败返回 null 并计数 */
def parseJson = { byte[] bytes, String where ->
    try {
        return slurper.parseText(new String(bytes, StandardCharsets.UTF_8))
    } catch (Exception e) {
        invalid++
        warn("描述文件无法解析，已跳过：${where}（${e.message}）")
        return null
    }
}

/** 合并一份索引（上游产物） */
def mergeIndex = { Object raw, String where ->
    if (!(raw instanceof Map)) {
        if (raw != null) {
            invalid++
            warn("聚合索引不是 JSON 对象，已跳过：${where}")
        }
        return
    }
    Map idx = (Map) raw
    if (idx.types instanceof List) {
        idx.types.each { t ->
            if (t instanceof Map && t.type) {
                typeDefs[t.type.toString()] = t
            }
        }
    }
    if (idx.pages instanceof List) {
        idx.pages.each { p ->
            if (p instanceof Map && p.key && p.tree instanceof Map) {
                pages[p.key.toString()] = p
            }
        }
    }
}

/** 从本地 ui/<page>/compose.json 收集页面 */
def collectPages = { File baseDir, File uiDir ->
    if (!uiDir.isDirectory()) {
        return
    }
    try {
        uiDir.eachFileRecurse { file ->
            if (!file.isFile() || file.name != "compose.json") {
                return
            }
            def rel = baseDir.toPath().relativize(file.toPath()).toString().replace(File.separator, '/')
            def segs = rel.tokenize('/')
            if (segs.size() < 3 || segs[0] != "ui") {
                warn("装配描述路径不符合 ui/<page>/compose.json，已跳过：${rel}")
                return
            }
            def key = segs[1..-2].join('/')
            def tree = parseJson(file.bytes, rel)
            if (!(tree instanceof Map)) {
                if (tree != null) {
                    invalid++
                    warn("装配描述不是 JSON 对象，已跳过：${rel}")
                }
                return
            }
            if (!tree.type) {
                invalid++
                warn("装配描述缺少 type，已跳过：${rel}")
                return
            }
            pages[key] = [key: key, tree: tree]
        }
    } catch (Exception e) {
        warn("遍历装配描述失败：${uiDir}（${e.message}）")
    }
}

/** 从本地 META-INF/ping/ui/types/ 收集类型定义 */
def collectTypes = { File typesDir ->
    if (!typesDir.isDirectory()) {
        return
    }
    def files = typesDir.listFiles()
    if (files == null) {
        return
    }
    files.findAll { it.isFile() && it.name.endsWith(".json") }.each { file ->
        def defn = parseJson(file.bytes, file.path)
        if (!(defn instanceof Map)) {
            if (defn != null) {
                invalid++
                warn("组件类型定义不是 JSON 对象，已跳过：${file.path}")
            }
            return
        }
        if (!defn.type) {
            invalid++
            warn("组件类型定义缺少 type，已跳过：${file.path}")
            return
        }
        typeDefs[defn.type.toString()] = defn
    }
}

// ------------------------------------------------------------------
// 1. 解析 classpath（构建期枚举依赖构件，属允许范围；禁令只约束运行时）
// ------------------------------------------------------------------
def ownOutput = project.build.outputDirectory
def classpath = []
try {
    classpath.addAll(project.compileClasspathElements)
} catch (Exception e) {
    warn("解析 classpath 失败，仅聚合本模块描述（${e.message}）")
    // 不抛出，继续处理本模块
}

def ordered = classpath.findAll { it != ownOutput } + [ownOutput]

// ------------------------------------------------------------------
// 2. 逐个构件收集
// ------------------------------------------------------------------
ordered.each { path ->
    File artifact = new File(path)
    if (artifact.isDirectory()) {
        // 类型定义与页面（本模块与依赖目录形态的产物）
        collectTypes(new File(artifact, "META-INF/ping/ui/types"))
        collectPages(artifact, new File(artifact, PAGES_ROOT))

        // 上游聚合索引：本模块自己的一份是即将重写的产物，读取会引入陈旧数据，跳过
        File idx = new File(artifact, INDEX_REL)
        if (artifact.absolutePath == new File(ownOutput).absolutePath) {
            return
        }
        if (idx.isFile()) {
            mergeIndex(parseJson(idx.bytes, INDEX_REL), INDEX_REL)
        } else {
            missing << artifact.name
        }
        return
    }

    if (!artifact.isFile() || !artifact.name.endsWith(".jar")) {
        return
    }

    boolean indexFound = false
    try {
        JarFile jar = new JarFile(artifact)
        try {
            jar.entries().each { entry ->
                if (entry.directory) {
                    return
                }
                String name = entry.name
                if (name == INDEX_REL) {
                    def stream = jar.getInputStream(entry)
                    try {
                        mergeIndex(parseJson(stream.bytes, "${artifact.name}!/${name}"), name)
                        indexFound = true
                    } finally {
                        stream.close()
                    }
                    return
                }
                if (name.startsWith(TYPES_PREFIX) && name.endsWith(".json")) {
                    def stream = jar.getInputStream(entry)
                    try {
                        def defn = parseJson(stream.bytes, "${artifact.name}!/${name}")
                        if (defn instanceof Map && defn.type) {
                            typeDefs[defn.type.toString()] = defn
                        }
                    } finally {
                        stream.close()
                    }
                    return
                }
                if (name.startsWith(PAGES_ROOT) && name.endsWith("/compose.json")) {
                    def segs = name.tokenize('/')
                    if (segs.size() < 3) {
                        return
                    }
                    def key = segs[1..-2].join('/')
                    def stream = jar.getInputStream(entry)
                    try {
                        def tree = parseJson(stream.bytes, "${artifact.name}!/${name}")
                        if (tree instanceof Map && tree.type) {
                            pages[key] = [key: key, tree: tree]
                        }
                    } finally {
                        stream.close()
                    }
                }
            }
        } finally {
            jar.close()
        }
        if (!indexFound && artifact.name.startsWith("ping-")) {
            missing << artifact.name
        }
    } catch (Exception e) {
        warn("读取依赖构件失败，已跳过：${artifact.name}（${e.message}）")
    }
}

// ------------------------------------------------------------------
// 3. 写出固定索引
// ------------------------------------------------------------------
if (missing) {
    warn("以下依赖构件缺少聚合索引，已跳过（共 ${missing.size()} 个）：${missing.join(', ')}")
}
if (invalid > 0) {
    warn("共跳过 ${invalid} 个不合法的描述文件")
}

def output = [
    schema_version: 1,
    types         : typeDefs.values().toList(),
    pages         : pages.values().toList()
]

try {
    File outFile = new File(ownOutput, INDEX_REL)
    outFile.parentFile.mkdirs()
    def json = JsonOutput.prettyPrint(JsonOutput.toJson(output))
    outFile.setText(json, StandardCharsets.UTF_8.name())
    log.info("[抽象组件聚合] 已生成 ${INDEX_REL}：types=${typeDefs.size()}，pages=${pages.size()}")
} catch (Exception e) {
    warn("写出聚合索引失败（${e.message}）")
}
