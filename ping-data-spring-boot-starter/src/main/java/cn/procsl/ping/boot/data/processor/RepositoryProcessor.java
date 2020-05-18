package cn.procsl.ping.boot.data.processor;

import cn.procsl.ping.boot.data.annotation.CreateRepository;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
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
    protected Filer filer;

    protected boolean enable;

    protected String packageName;

    protected List<String> includes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();

        Map<String, String> options = this.processingEnv.getOptions();
        try {
            String is = options.getOrDefault("creator.repository", "false");
            enable = Boolean.parseBoolean(is);
            if (!enable) {
                messager.printMessage(Diagnostic.Kind.NOTE, "repository processor is disabled!");
                return;
            }

            packageName = options.get("creator.repository.package.name");
            if (packageName == null || packageName.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Use default package name");
            }

            String tmp = options.get("creator.repository.includes");
            if (tmp == null || tmp.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Create default repositories");
                this.includes = Collections.singletonList(
                        "org.springframework.data.jpa.repository.JpaRepository"
                );
                return;
            }

            this.includes = Arrays.asList(tmp.split(","));
        } catch (Exception e) {
            enable = false;
            messager.printMessage(Diagnostic.Kind.ERROR, "Error: " + e.getMessage());
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
        return true;
    }
}
