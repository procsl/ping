package cn.procsl.ping.web.gen.process;

import cn.procsl.ping.web.gen.AnnotationProcessor;
import cn.procsl.ping.web.http.annotation.Get;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/10/13
 */
public class GetProcessor extends AnnotationProcessor {

    protected Set<String> created;


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> tmp = new HashSet<>(1);
        tmp.add(Get.class.getName());
        return tmp;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return true;
        }

        // 获取被处理的类的包名,每一项都是一个类,可能是接口或者类
        for (Element rootElement : roundEnv.getRootElements()) {
            String packageName = rootElement.getEnclosingElement().asType().toString();

            String fullPackageName = packageName + "." + this.PACKAGE_ITEM_NAME;

            String className = fullPackageName + "." + rootElement.getSimpleName();

            //
            try {
                // 创建包
                JavaFileObject sourceFile = this.javacFiler.createSourceFile(className + "Gen");

                CompilationUnit compilationUnit = new CompilationUnit();
                ClassOrInterfaceDeclaration source = compilationUnit
                        .addClass(rootElement.getSimpleName() + "Gen")
                        .setPublic(true);
                sourceFile.openOutputStream().write(source.toString().getBytes());

            } catch (IOException e) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "写入文件错误:" + e.getMessage());
            }
        }
        return false;
    }
}
