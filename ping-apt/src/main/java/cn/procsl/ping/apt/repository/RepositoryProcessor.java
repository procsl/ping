package cn.procsl.ping.apt.repository;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Entity;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;


/**
 * 1. 扫描所有的实体引用
 * 2. 根据各项条件过滤
 * 3. 根据过滤出的实体对应的 Repository 生成代码
 * TODO 需要重构, 抽离出 javapoet 的依赖, 防止processor初始化失败
 *
 * @author procsl
 * @date 2020/05/18
 */
@AutoService(Processor.class)
public class RepositoryProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer filer;

    private List<RepositoryBuilder> builders;

    private List<RepositoryBuilder> singletonBuilders;

    private List<String> includes;

    private Map<Object, Object> config;

    private Map<String, RepositoryNamingStrategy> namingStrategy;

    private boolean init = true;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();

        try {
            initConfig();

            initIncludes();

            initNamingStrategy();

        } catch (Exception e) {
            messager.printMessage(WARNING, "Initializing the annotation processor failed for a number of reasons: " + e.getMessage());
            init = false;
        }

    }

    void initNamingStrategy() {
        ClassLoader currentClassLoad = this.getClass().getClassLoader();
        ServiceLoader<RepositoryNamingStrategy> services = ServiceLoader.load(RepositoryNamingStrategy.class, currentClassLoad);
        this.namingStrategy = new HashMap<>();
        services.forEach(item -> this.namingStrategy.put(item.getClass().getName(), item));
    }

    private void initConfig() {
        try (InputStream is = filer.getResource(StandardLocation.CLASS_PATH, "", RepositoryBuilder.processor).openInputStream()) {

            Properties properties = new Properties();
            properties.load(is);
            this.config = properties;
            return;

        } catch (IOException e) {
            messager.printMessage(WARNING, "The profile could not be found: '" + RepositoryBuilder.processor + "'. by error:" + e.getMessage());
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
        if (!init || roundEnv.processingOver()) {
            return false;
        }

        Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);

        try {
            for (Element entity : entities) {
                if (!(entity instanceof TypeElement)) {
                    String str = entity.asType().toString();
                    messager.printMessage(WARNING, "The element of the annotation label is not a class type: '" + str + "'", entity);
                    continue;
                }
                RepositoryCreator repo = entity.getAnnotation(RepositoryCreator.class);
                if (repo == null) {
                    continue;
                }

                String name = this.createPackageName((TypeElement) entity);
                // 生成多继承源文件
                this.generateSourceCode((TypeElement) entity, name, roundEnv);
                // 生成单继承源文件
                this.generateSingletonSourceCode((TypeElement) entity, name, roundEnv);
            }

        } catch (Exception e) {
            messager.printMessage(ERROR, "The build of the source code failed:" + e.getClass().getName() + ":" + e.getMessage());
            return false;
        }
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

            Map<String, List<TypeMirror>> type = builder.generator(entity, roundEnv);
            if (type == null) {
                messager.printMessage(WARNING, "This interface generation failed because the generator returned null:" + builder.getClass().getName());
                continue;
            }

            TypeSpec.Builder typeSpecBuilder = this.buildRepository(className);
            type.forEach((k, v) -> {
                TypeName[] mirrors = v.stream().map(TypeName::get).toArray(value -> new TypeName[v.size()]);
                ClassName repositoryType = ClassName.get(this.processingEnv.getElementUtils().getTypeElement(k));
                ParameterizedTypeName interfaceType = ParameterizedTypeName.get(repositoryType, mirrors);
                typeSpecBuilder.addSuperinterface(interfaceType);
            });
            writer(packageName, typeSpecBuilder.build());
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
            includes = Collections.singletonList("org.springframework.data.jpa.repository.JpaRepository");
        } else {
            includes = Arrays.stream(tmp.split(",")).filter(Objects::nonNull).filter(item -> !item.isEmpty()).map(String::trim).distinct().filter(this::isAvailable).sorted().collect(Collectors.toList());
        }

        this.builders = new LinkedList<>();
        this.singletonBuilders = new LinkedList<>();
        for (RepositoryBuilder curr : services) {
            try {
                curr.init(processingEnv, this::getConfig);
            } catch (Exception e) {
                this.messager.printMessage(WARNING, curr.getClass().getName() + "初始化失败: " + e.getMessage());
            }
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

        List<RepositoryBuilder> matcher = this.matcher(this.builders, entity);
        // 如果没有匹配到, 直接退出
        if (matcher.isEmpty()) {
            messager.printMessage(WARNING, "Not matched to repository:" + entity.getSimpleName(), entity);
            return;
        }

        Set<? extends TypeName> inter = this.getMultipleInterfaceType(entity, roundEnv, matcher);

        if (inter.isEmpty()) {
            messager.printMessage(WARNING, "Not matched to repository:" + entity.getSimpleName(), entity);
            return;
        }
        Collection<String> repositories = new HashSet<>();
        matcher.forEach(item -> repositories.addAll(item.getName()));
        // 类名
        String className = this.createClassName(entity, repositories);

        // 获取待生成的Repository
        TypeSpec repository = this.buildRepository(className).addSuperinterfaces(inter).build();

        this.writer(packageName, repository);
    }

    /**
     * 创建当前的repository类名
     *
     * @param entity 对应的实体
     * @return 类名
     */
    private String createClassName(TypeElement entity, Collection<String> repository) {
        String tmp = this.getConfig(RepositoryBuilder.prefix);
        if (tmp == null || tmp.isEmpty()) {
            tmp = "";
        }

        RepositoryCreator repositoryCreator = entity.getAnnotation(RepositoryCreator.class);
        RepositoryNamingStrategy strategy = this.namingStrategy.get(repositoryCreator.strategy());
        if (strategy != null) {
            String name = strategy.repositoryName(entity, tmp, repository);
            if (name != null && (!name.isEmpty())) {
                return name;
            }
        }

        return tmp + entity.getSimpleName() + "Repository";
    }

    /**
     * 创建当前的repository的包名
     *
     * @param entity 对应的实体
     * @return 包名
     */
    private String createPackageName(TypeElement entity) {

        RepositoryCreator repo = entity.getAnnotation(RepositoryCreator.class);

        RepositoryNamingStrategy strategy = this.namingStrategy.get(repo.strategy());
        if (strategy != null) {
            String packageName = strategy.repositoryPackageName(entity);
            if (packageName != null && (!packageName.isEmpty())) {
                return packageName;
            }
        }

        // 全局配置包名
        String packageName = this.getConfig(RepositoryBuilder.pageName);
        if (packageName != null && (!packageName.isEmpty())) {
            return packageName;
        }

        // 默认的包名
        return entity.getEnclosingElement().toString();
    }


    /**
     * 创建Repository对象
     *
     * @param className 类名称
     * @return 返回repository对象
     */
    private TypeSpec.Builder buildRepository(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName("org.springframework.stereotype.Repository");
        return TypeSpec.interfaceBuilder(className).addAnnotation(clazz).addModifiers(Modifier.PUBLIC);
    }

    /**
     * 创建多继承接口
     *
     * @param entity      对应的实体
     * @param environment 编译器上下文
     * @return 返回创建的TypeNames
     */
    Set<? extends TypeName> getMultipleInterfaceType(TypeElement entity, RoundEnvironment environment, List<RepositoryBuilder> matcher) {
        HashSet<TypeName> types = new HashSet<>();
        for (RepositoryBuilder builder : matcher) {
            Map<String, List<TypeMirror>> type = builder.generator(entity, environment);
            if (type == null || type.isEmpty()) {
                messager.printMessage(WARNING, "This interface generation failed because the generator returned null:" + builders.getClass().getName());
                continue;
            }

            type.forEach((k, v) -> {
                TypeName[] mirrors = v.stream().map(TypeName::get).toArray(value -> new TypeName[v.size()]);
                ClassName repositoryType = ClassName.get(this.processingEnv.getElementUtils().getTypeElement(k));
                ParameterizedTypeName interfaceType = ParameterizedTypeName.get(repositoryType, mirrors);
                types.add(interfaceType);
            });
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
                if (builder.support(include)) {
                    matcher.add(builder);
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
                if (repositoryBuilder.support(builder)) {
                    tmp.add(repositoryBuilder);
                }
            }
        }
        return tmp;
    }
}
