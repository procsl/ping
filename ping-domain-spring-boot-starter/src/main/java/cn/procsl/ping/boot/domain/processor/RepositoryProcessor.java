package cn.procsl.ping.boot.domain.processor;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.persistence.Entity;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * 1. 扫描所有的实体引用
 * 2. 根据各项条件过滤
 * 3. 根据过滤出的实体对应的 Repository 生成代码
 *
 * @author procsl
 * @date 2020/05/18
 */
public class RepositoryProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private Messager messager;

    private Filer filer;

    private String packageName;

    private List<RepositoryBuilder> builders;

    private List<RepositoryBuilder> singletonBuilders;

    private String prefix;

    private List<String> includes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();

        try {
            this.initPackageName(processingEnv);

            this.initPrefix(processingEnv);

            this.initIncludes(processingEnv);
        } catch (Exception e) {
            messager.printMessage(ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(CreateRepository.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);

        try {
            for (Element entity : entities) {
                String name = this.createPackageName(entity, roundEnv);
                // 生成多继承源文件
                this.generateSourceCode(entity, name, roundEnv);
                // 生成单继承源文件
                this.generateSingletonSourceCode(entity, name, roundEnv);
            }
        } catch (IOException e) {
            messager.printMessage(ERROR, "Source code output error:" + e.getMessage());
            return false;
        } catch (Exception e) {
            messager.printMessage(ERROR, "Error:" + e.getMessage());
            return false;
        }
        return true;
    }

    private void generateSingletonSourceCode(Element entity, String packageName, RoundEnvironment roundEnv) throws IOException {

        List<RepositoryBuilder> matcher = this.matcher(this.singletonBuilders, entity);
        if (matcher.isEmpty()) {
            return;
        }

        int count = 0;
        for (RepositoryBuilder builder : matcher) {
            String className = this.createClassName(entity, String.valueOf(count), roundEnv);

            TypeSpec typeSpec = this.buildRepository(className, entity, roundEnv)
                    .addSuperinterface(builder.build(entity, roundEnv))
                    .build();
            count++;
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
     *
     * @param processingEnv 编译上下文环境
     */
    private void initIncludes(ProcessingEnvironment processingEnv) {

        ClassLoader currentClassLoad = this.getClass().getClassLoader();
        ServiceLoader<RepositoryBuilder> services = ServiceLoader.load(RepositoryBuilder.class, currentClassLoad);

        String tmp = processingEnv.getOptions().get("creator.repository.includes");

//        processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH,"","repository.build.properties");

        if (StringUtils.isEmpty(tmp)) {
            messager.printMessage(WARNING, "Create default repositories");
            includes = asList(
                    "org.springframework.data.jpa.repository.JpaRepository",
                    "org.springframework.data.querydsl.QuerydslPredicateExecutor"
            );
        } else {
            includes = asList(tmp.split(","));
        }

        this.builders = new LinkedList<>();
        this.singletonBuilders = new LinkedList<>();
        Iterator<RepositoryBuilder> itor = services.iterator();
        if (itor.hasNext()) {
            RepositoryBuilder curr = itor.next();
            if (curr.isSingleton()) {
                this.singletonBuilders.add(curr);
            } else {
                this.builders.add(curr);
            }
        }
    }

    /**
     * 初始化包名
     *
     * @param processingEnv 编译上下文
     */
    private void initPackageName(ProcessingEnvironment processingEnv) {
        this.packageName = processingEnv.getOptions().get("creator.repository.package.name");
        if (StringUtils.isEmpty(packageName)) {
            messager.printMessage(WARNING, "Use default package name");
        }
    }

    /**
     * 初始化repository前缀
     *
     * @param processingEnv 编译上下文
     */
    private void initPrefix(ProcessingEnvironment processingEnv) {
        String tmp = processingEnv.getOptions().get("create.repository.prefix");
        if (StringUtils.isEmpty(tmp)) {
            this.prefix = "Ping";
            return;
        }
        this.prefix = tmp;
    }

    /**
     * 代码生成
     *
     * @param entity      实体文件
     * @param roundEnv    上下文
     * @param packageName 包名
     */
    private void generateSourceCode(Element entity, String packageName, RoundEnvironment roundEnv) throws IOException {

        // 类名
        String className = this.createClassName(entity, "", roundEnv);

        Iterable<? extends TypeName> inter = this.getMultipleInterfaceType(entity, roundEnv);
        if (inter == null || !inter.iterator().hasNext()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Not matcher repository:" + className + ":" + entity.getSimpleName());
            return;
        }

        // 获取待生成的Repository
        TypeSpec repository = this.buildRepository(className, entity, roundEnv)
                .addSuperinterfaces(inter).build();

        this.writer(packageName, repository);
    }

    /**
     * 创建当前的repository类名
     *
     * @param entity   对应的实体
     * @param roundEnv 上下文环境
     * @return 类名
     */
    private String createClassName(Element entity, String sign, RoundEnvironment roundEnv) {
        return this.prefix + entity.getSimpleName() + sign + "Repository";
    }

    /**
     * 创建当前的repository的包名
     *
     * @param entity      对应的实体
     * @param environment 上下文环境
     * @return 包名
     */
    private String createPackageName(Element entity, RoundEnvironment environment) {
        boolean isOk = StringUtils.hasText(this.packageName);
        if (isOk) {
            return this.packageName;
        }

        Element packageType = entity.getEnclosingElement();
        if (packageType == null) {
            return "";
        }

        // 返回当前的实体的包名
        String fullName = packageType.asType().toString();

        if (packageType.getSimpleName() == null) {
            return fullName;
        }
        String simpleName = packageType.getSimpleName().toString();

        if (simpleName.isEmpty()) {
            return fullName;
        }

        if (fullName.length() > simpleName.length()) {
            return fullName.substring(0, fullName.length() - simpleName.length()) + "repository";
        }

        return fullName;
    }


    /**
     * 创建Repository对象
     *
     * @param className   类名称
     * @param entity      对应的entity名称
     * @param environment 编译器上下文
     * @return 返回repository对象
     */
    private TypeSpec.Builder buildRepository(String className, Element entity, RoundEnvironment environment) {
        return TypeSpec
                .interfaceBuilder(className)
                .addAnnotation(Repository.class)
                .addAnnotation(Indexed.class)
                .addModifiers(Modifier.PUBLIC);
    }

    /**
     * 循环创建superInterface
     *
     * @param entity      对应的实体
     * @param environment 编译器上下文
     * @return 返回创建的TypeNames
     */
    private Iterable<? extends TypeName> getMultipleInterfaceType(Element entity, RoundEnvironment environment) {

        List<RepositoryBuilder> matcher = this.matcher(this.builders, entity);

        // 如果没有匹配到, 直接退出
        if (matcher.isEmpty()) {
            return null;
        }

        HashSet<TypeName> types = new HashSet<>();
        for (RepositoryBuilder builder : matcher) {
            TypeName type = builder.build(entity, environment);
            if (type != null) {
                types.add(type);
            }
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
        if (CollectionUtils.isEmpty(builders)) {
            return Collections.emptyList();
        }

        List<RepositoryBuilder> matcher = new LinkedList();
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

        String[] currentBuilders = entity.getAnnotation(CreateRepository.class).builders();
        if (currentBuilders == null || currentBuilders.length == 0) {
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
