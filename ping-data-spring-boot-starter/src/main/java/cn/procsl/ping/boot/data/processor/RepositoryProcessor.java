package cn.procsl.ping.boot.data.processor;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.processor.impl.*;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.persistence.Entity;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private String prefix;

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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);

        try {
            for (Element entity : entities) {
                this.generateSourceCode(entity, roundEnv);
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

    /**
     * 初始化包含的类型
     *
     * @param processingEnv 编译上下文环境
     */
    private void initIncludes(ProcessingEnvironment processingEnv) {

        String tmp = processingEnv.getOptions().get("creator.repository.includes");

        List<String> includes;
        if (StringUtils.isEmpty(tmp)) {
            messager.printMessage(WARNING, "Create default repositories");
            includes = asList(
                    "org.springframework.data.jpa.repository.JpaRepository",
                    "org.springframework.data.querydsl.QuerydslPredicateExecutor"
            );
        } else {
            includes = asList(tmp.split(","));
        }

        this.initBuilder(includes);
    }

    /**
     * 初始化生成器
     *
     * @param includes 包含的类型
     */
    private void initBuilder(List<String> includes) {
        List<RepositoryBuilder> tmp = Arrays.asList(
                new JpaRepositoryBuilder(),
                new PagingAndSortingRepositoryBuilder(),
                new JpaSpecificationExecutorBuilder(),
                new CrudRepositoryBuilder(),
                new QueryDslPredicateExecutorBuilder(),
                new ReactiveQueryDslPredicateExecutorBuilder());

        // 过滤掉不支持的
        this.builders = tmp.stream().filter(item -> {
            for (String s : includes) {
                boolean isSupport = item.support(s);
                if (isSupport) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());

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
     * @param entity   实体文件
     * @param roundEnv 上下文
     */
    private void generateSourceCode(Element entity, RoundEnvironment roundEnv) throws IOException {

        // 包名
        String packageName = this.createPackageName(entity, roundEnv);

        // 类名
        String className = this.createClassName(entity, roundEnv);

        // 获取待生成的Repository
        TypeSpec repository = this.buildRepository(className, entity, roundEnv);

        // java 源文件表示
        JavaFile file = JavaFile.builder(packageName, repository).build();

        // 写入class
        file.writeTo(filer);
    }

    /**
     * 创建当前的repository类名
     *
     * @param entity   对应的实体
     * @param roundEnv 上下文环境
     * @return 类名
     */
    private String createClassName(Element entity, RoundEnvironment roundEnv) {
        return this.prefix + entity.getSimpleName() + "Repository";
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

        // 返回当前的实体的包名
        return entity.getEnclosingElement().asType().toString();
    }


    /**
     * 创建Repository对象
     *
     * @param className   类名称
     * @param entity      对应的entity名称
     * @param environment 编译器上下文
     * @return 返回repository对象
     */
    private TypeSpec buildRepository(String className, Element entity, RoundEnvironment environment) {
        return TypeSpec
                .interfaceBuilder(className)
                .addAnnotation(Repository.class)
                .addAnnotation(Indexed.class)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterfaces(this.reduce(entity, environment))
                .build();
    }

    /**
     * 循环创建superInterface
     *
     * @param entity      对应的实体
     * @param environment 编译器上下文
     * @return 返回创建的TypeNames
     */
    private Iterable<? extends TypeName> reduce(Element entity, RoundEnvironment environment) {
        HashSet<TypeName> types = new HashSet<>();
        for (RepositoryBuilder builder : this.builders) {
            TypeName type = builder.build(entity, environment);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

}
