package cn.procsl.ping.boot.data.processor;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
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
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.EMPTY_SET;

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

    private boolean enable;

    private String packageName;

    private Set<RepositoryBuilder> builders;

    private String prefix;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();

        Map<String, String> options = this.processingEnv.getOptions();
        try {
            String is = options.getOrDefault("creator.repository", "false");
            this.enable = Boolean.parseBoolean(is);
            if (!enable) {
                messager.printMessage(Diagnostic.Kind.NOTE, "repository processor is disabled!");
                return;
            }

            this.packageName = options.get("creator.repository.package.name");
            if (packageName == null || packageName.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Use default package name");
            }

            this.prefix = options.getOrDefault("create.repository.prefix", "Ping");

            String tmp = options.get("creator.repository.includes");
            List<String> includes;
            if (tmp == null || tmp.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Create default repositories");
                includes = Collections.singletonList(
                        "org.springframework.data.jpa.repository.JpaRepository"
                );
            } else {
                includes = Arrays.asList(tmp.split(","));
            }

            this.initBuilder(includes);

        } catch (Exception e) {
            enable = false;
            messager.printMessage(Diagnostic.Kind.ERROR, "Error: " + e.getMessage());
        }

    }

    protected void initBuilder(List<String> includes) {
        HashSet<String> copy = new HashSet<>(includes);

        Iterator<RepositoryBuilder> builders = ServiceLoader.load(RepositoryBuilder.class).iterator();
        this.builders = new HashSet<>();

        while (builders.hasNext()) {
            RepositoryBuilder current = builders.next();
            this.testAndPushAndRemove(copy.iterator(), current);
        }
    }

    private void testAndPushAndRemove(Iterator<String> i, RepositoryBuilder current) {
        while (i.hasNext()) {
            if (current.support(i.next())) {
                this.builders.add(current);
                i.remove();
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (enable) {
            return Collections.singleton(CreateRepository.class.getName());
        }
        return EMPTY_SET;
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
            messager.printMessage(Diagnostic.Kind.ERROR, "Source code output error:" + e.getMessage());
            return false;
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error:" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 代码生成
     *
     * @param entity   实体文件
     * @param roundEnv 上下文
     */
    protected void generateSourceCode(Element entity, RoundEnvironment roundEnv) throws IOException {

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
    protected String createClassName(Element entity, RoundEnvironment roundEnv) {
        return this.prefix + entity.getSimpleName() + "Repository";
    }

    /**
     * 创建当前的repository的包名
     *
     * @param entity      对应的实体
     * @param environment 上下文环境
     * @return 包名
     */
    protected String createPackageName(Element entity, RoundEnvironment environment) {
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
    protected TypeSpec buildRepository(String className, Element entity, RoundEnvironment environment) {

        Iterable<? extends TypeName> superInterFaces = this.reduce(entity, environment);
        return TypeSpec
                .interfaceBuilder(className)
                .addAnnotation(Repository.class)
                .addAnnotation(Indexed.class)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterfaces(superInterFaces)
                .build();

    }

    /**
     * 循环创建superInterface
     *
     * @param entity      对应的实体
     * @param environment 编译器上下文
     * @return 返回创建的TypeNames
     */
    protected Iterable<? extends TypeName> reduce(Element entity, RoundEnvironment environment) {
        HashSet<TypeName> types = new HashSet<>();
        for (RepositoryBuilder builder : this.builders) {
            TypeName type = builder.build(entity, environment);
            types.add(type);
        }
        return types;
    }

}
