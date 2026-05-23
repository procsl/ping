import java.io.File

// 1. 获取当前项目 resources 目录的绝对路径
String resourcesPath = project.basedir.absolutePath + "/src/main/resources"

// 2. 获取通过 -P 激活的所有 Maven Profile ID
def activeIds = project.activeProfiles.collect { it.id }

// 3. 过滤：只有当 resources 下存在对应的 application-xxx.properties 时才保留
def validProfiles = activeIds.findAll { id ->
    File propFile = new File("${resourcesPath}/application-${id}.properties")
    File ymlFile = new File("${resourcesPath}/application-${id}.yml")
    File yamlFile = new File("${resourcesPath}/application-${id}.yaml")

    boolean exists = propFile.exists() || ymlFile.exists() || yamlFile.exists()

    if (!exists) {
        log.warn("【Profile 过滤警告】检测到 -P 激活了 [${id}]，但未找到对应的 application-${id} 配置文件，已从 spring.profiles.active 中剔除。")
    }
    return exists
}

// 4. 用逗号拼装合法项并注入
def profilesStr = validProfiles.join(",")
project.properties.setProperty('dynamic.spring.profiles', profilesStr)
log.info("【Profile 动态组装成功】最终 spring.profiles.active = [${profilesStr}]")
