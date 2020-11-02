package cn.procsl.ping.processor.repository.processor;

import cn.procsl.ping.processor.repository.annotation.RepositoryCreator;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.persistence.Entity;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static javax.tools.Diagnostic.Kind.WARNING;


/**
 * 1. 扫描所有的实体引用
 * 2. 根据各项条件过滤
 * 3. 根据过滤出的实体对应的 Repository 生成代码
 *
 * @author procsl
 * @date 2020/05/18
 */
@AutoService(Processor.class)
@Slf4j
public class RepositoryProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer filer;

    private List<RepositoryBuilder> builders;

    private List<RepositoryBuilder> singletonBuilders;

    private List<String> includes;

    private Map<Object, Object> config;

    private boolean init = true;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();

        try {
            initConfig();

            initIncludes();
        } catch (Exception e) {
            messager.printMessage(WARNING, "Initializing the annotation processor failed for a number of reasons: " + e.getMessage());
            log.warn("Initializing the annotation processor failed for a number of reasons: ", e);
            init = false;
        }
    }

    /**
     * 加载基础配置
     */
    private void initConfig() {
        try (
            InputStream is = filer
                .getResource(StandardLocation.CLASS_PATH, "", RepositoryBuilder.processor)
                .openInputStream()
        ) {

            Properties properties = new Properties();
            properties.load(is);
            this.config = properties;
            return;

        } catch (IOException e) {
            messager.printMessage(WARNING, "The profile could not be found: '" + RepositoryBuilder.processor + "'. by error:" + e.getMessage());
            log.warn("The profile could not be found: '{}'. by error:", RepositoryBuilder.processor, e);
        }
        this.config = emptyMap();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(RepositoryCreator.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (init && roundEnv.processingOver()) {
            return false;
        }

        Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);

        try {
            for (Element entity : entities) {
                if (!(entity instanceof TypeElement)) {
                    String str = entity.asType().toString();
                    messager.printMessage(WARNING, "The element of the annotation label is not a class type: '" + str + "'", entity);
                    log.warn("The element of the annotation label is not a class type: {}", str);
                    continue;
                }

                String name = this.createPackageName((TypeElement) entity);
                log.info("The entity corresponds to the name of the repository package:{} - {}", entity.toString(), name);
                // 生成多继承源文件
                this.generateSourceCode((TypeElement) entity, name, roundEnv);
                // 生成单继承源文件
                this.generateSingletonSourceCode((TypeElement) entity, name, roundEnv);
            }

        } catch (Exception e) {
            messager.printMessage(WARNING, "The build of the source code failed:" + e.getClass().getName() + ":" + e.getMessage());
            log.error("The build of the source code failed:", e);
            return false;
        }
        log.info("The annotation processing is complete");
        return true;
    }


    /**
     * 生成单文件继承
     *
     * @param entity      指定的实体
     * @param packageName 包名
     * @throws IOException 当文件写入失败时发生
     */
    private void generateSingletonSourceCode(TypeElement entity, String packageName, RoundEnvironment roundEnv) throws IOException, ClassNotFoundException {

        List<RepositoryBuilder> matcher = this.matcher(this.singletonBuilders, entity);
        if (matcher.isEmpty()) {
            return;
        }

        for (RepositoryBuilder builder : matcher) {
            String className = this.createClassName(entity, builder.getName());

            TypeMirror superInf = builder.generator(entity, roundEnv);
            if (superInf == null) {
                log.warn("This interface generation failed because the generator returned null:{}", builder.getClass().getName());
                messager.printMessage(WARNING, "This interface generation failed because the generator returned null:" + builder.getClass().getName());
                continue;
            }

            TypeSpec typeSpec = this.buildRepository(className)
                .addSuperinterface(TypeName.get(superInf))
                .build();
            writer(packageName, typeSpec);
        }
    }

    private void writer(String packageName, TypeSpec typeSpec) throws IOException {
        // java 源文件表示
        JavaFile file = JavaFile.builder(packageName, typeSpec).build();

        // 写入class
        file.writeTo(filer);
    }

    /**
     * 初始化包含的类型
     */
    private void initIncludes() {

        ClassLoader currentClassLoad = this.getClass().getClassLoader();
        ServiceLoader<RepositoryBuilder> services = ServiceLoader.load(RepositoryBuilder.class, currentClassLoad);

        String tmp = this.getConfig(RepositoryBuilder.include);

        if (tmp == null || tmp.isEmpty()) {
            messager.printMessage(WARNING, "Only the default repositories will be created: [org.springframework.data.jpa.repository.JpaRepository]");
            log.warn("Only the default repositories will be created: [org.springframework.data.jpa.repository.JpaRepository]");
            includes = Collections.singletonList("org.springframework.data.jpa.repository.JpaRepository");
        } else {
            includes = Arrays.stream(tmp.split(","))
                .filter(Objects::nonNull)
                .filter(item -> !item.isEmpty())
                .map(String::trim)
                .distinct()
                .filter(this::isAvailable)
                .sorted()
                .collect(Collectors.toList());
        }

        this.builders = new LinkedList<>();
        this.singletonBuilders = new LinkedList<>();
        for (RepositoryBuilder curr : services) {
            curr.init(processingEnv, this::getConfig);
            if (curr.isSingleton()) {
                this.singletonBuilders.add(curr);
            } else {
                this.builders.add(curr);
            }
        }
    }

    private boolean isAvailable(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            messager.printMessage(WARNING, "Class not found:" + clazz);
            log.warn("Class not found:{}", clazz);
        }
        return false;
    }

    /**
     * 获取系统配置
     *
     * @param key 系统配置key
     * @return 返回找到的系统配置
     */
    protected String getConfig(String key) {
        // 首先从config加载
        Object prop = this.config.get(key);
        if (prop == null) {
            return this.processingEnv.getOptions().get(key);
        }

        if (prop instanceof String) {
            return (String) prop;
        }

        if (prop instanceof Number) {
            return String.valueOf(prop);
        }

        messager.printMessage(WARNING, "This property is not a simple type: " + key);
        log.warn("This property is not a simple type: {}", key);
        return null;
    }

    /**
     * 代码生成
     *
     * @param entity      实体文件
     * @param roundEnv    上下文
     * @param packageName 包名
     */
    private void generateSourceCode(TypeElement entity, String packageName, RoundEnvironment roundEnv) throws IOException, ClassNotFoundException {

        // 类名
        String className = this.createClassName(entity, "");

        Iterable<? extends TypeName> inter = this.getMultipleInterfaceType(entity, roundEnv);
        if (inter == null || !inter.iterator().hasNext()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Not matched to repository:" + className + ":" + entity.getSimpleName(), entity);
            log.info("Not matched to repository:{}:{}", className, entity.getSimpleName());
            return;
        }

        // 获取待生成的Repository
        TypeSpec repository = this.buildRepository(className)
            .addSuperinterfaces(inter).build();

        this.writer(packageName, repository);
    }

    /**
     * 创建当前的repository类名
     *
     * @param entity 对应的实体
     * @return 类名
     */
    private String createClassName(Element entity, String sign) {
        String tmp = this.getConfig(RepositoryBuilder.prefix);
        if (tmp == null || tmp.isEmpty()) {
            tmp = "";
        }
        return tmp + entity.getSimpleName() + sign + "Repository";
    }

    /**
     * 创建当前的repository的包名
     *
     * @param entity 对应的实体
     * @return 包名
     */
    private String createPackageName(TypeElement entity) {

        do {
            RepositoryCreator repo = entity.getAnnotation(RepositoryCreator.class);
            if (repo == null) {
                break;
            }
            // 绝对包名
            String pgName = repo.packageName();
            if (!pgName.isEmpty()) {
                return pgName;
            }

            // 相对位置包名
            int indexOf = repo.indexOf();
            if (indexOf <= -1) {
                String tmp = this.getConfig(RepositoryBuilder.index);
                try {
                    indexOf = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    log.warn("This property cannot be formatted as a number:{}", tmp);
                }
            }
            Elements utils = this.processingEnv.getElementUtils();
            PackageElement packName = utils.getPackageOf(entity);
            String name = packName.asType().toString();
            String[] seg = name.split("\\.");
            if (indexOf > -1 && indexOf < seg.length) {
                List<String> join = Arrays.stream(seg).limit(indexOf).collect(Collectors.toList());
                name = String.join(".", join);
            }

            if (!name.isEmpty()) {
                return name.concat(".repository");
            }
        }
        while (false);

        // 全局配置包名
        String packageName = this.getConfig(RepositoryBuilder.pageName);
        if (packageName != null && (!packageName.isEmpty())) {
            return packageName;
        }

        // 默认的包名
        return "repository";
    }


    /**
     * 创建Repository对象
     *
     * @param className 类名称
     * @return 返回repository对象
     */
    private TypeSpec.Builder buildRepository(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName("org.springframework.stereotype.Repository");
        return TypeSpec
            .interfaceBuilder(className)
            .addAnnotation(clazz)
            .addModifiers(Modifier.PUBLIC);
    }

    /**
     * 创建多继承接口
     *
     * @param entity      对应的实体
     * @param environment 编译器上下文
     * @return 返回创建的TypeNames
     */
    private Iterable<? extends TypeName> getMultipleInterfaceType(TypeElement entity, RoundEnvironment environment) {

        List<RepositoryBuilder> matcher = this.matcher(this.builders, entity);

        // 如果没有匹配到, 直接退出
        if (matcher.isEmpty()) {
            return null;
        }

        HashSet<TypeName> types = new HashSet<>();
        for (RepositoryBuilder builder : matcher) {
            TypeMirror type = builder.generator(entity, environment);
            if (type == null) {
                log.warn("This interface generation failed because the generator returned null:{}", builder.getClass().getName());
                messager.printMessage(WARNING, "This interface generation failed because the generator returned null:" + builders.getClass().getName());
                continue;
            }

            types.add(TypeName.get(type));
        }
        return types;
    }


    /**
     * 查找匹配的构建器
     *
     * @param builders 构建器列表
     * @param entity   对应实体
     * @return 返回匹配成功的构建器
     */
    private List<RepositoryBuilder> matcher(List<RepositoryBuilder> builders, Element entity) {
        if (builders == null || builders.isEmpty()) {
            return Collections.emptyList();
        }

        List<RepositoryBuilder> matcher = new LinkedList<>();
        for (RepositoryBuilder builder : builders) {
            for (String include : this.includes) {
                try {
                    if (builder.support(include)) {
                        matcher.add(builder);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Class not fount:", e);
                }
            }
        }

        // 如果没有匹配到, 直接退出
        if (matcher.isEmpty()) {
            return matcher;
        }

        RepositoryCreator repo = entity.getAnnotation(RepositoryCreator.class);
        if (repo == null) {
            return matcher;
        }

        String[] currentBuilders = repo.builders();
        if (currentBuilders.length == 0) {
            return matcher;
        }

        // 如果有单独指定, 则再次匹配
        List<RepositoryBuilder> tmp = new LinkedList<>();
        for (String builder : currentBuilders) {
            for (RepositoryBuilder repositoryBuilder : matcher) {
                try {
                    if (repositoryBuilder.support(builder)) {
                        tmp.add(repositoryBuilder);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Class not fount:", e);
                }
            }
        }
        return tmp;
    }
}
