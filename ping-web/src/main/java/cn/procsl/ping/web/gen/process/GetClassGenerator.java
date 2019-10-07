package cn.procsl.ping.web.gen.process;

import cn.procsl.ping.web.gen.ClassGenerator;
import cn.procsl.ping.web.http.annotation.Get;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class GetClassGenerator implements ClassGenerator {

    @Override
    public String support() {
        return Get.class.getName();
    }

    @Override
    public void process(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, TypeElement element) {
        Set<? extends Element> list = roundEnv.getElementsAnnotatedWith(element);
        if (list.isEmpty()) {
            return;
        }

        try {

            CompilationUnit compilationUnit = StaticJavaParser.parse("class A { }");
            // 获取包名
            String packageName = "";

            // 创建类名
            String className = "";

            Filer filer = processingEnv.getFiler();
            JavaFileObject classFile = filer.createClassFile(packageName + "." + className);

            // 创建方法, 参数为Vert.x的Router, 返回值为void
            HashMap<String, String> methods = new HashMap<>();
            for (Element method : list) {
                this.createMethod(element, method, methods);
            }
        } catch (IOException e) {
            // 处理失败
        }
    }


    /**
     * 创建源文件
     * 文件类型 class package interface = getKind();
     * 文件全路径: asType
     *
     * @param element
     * @param item
     * @return
     */
    private void createMethod(TypeElement element, Element item, Map<String, String> methods) {

        if (item instanceof ExecutableElement) {

        }

    }


}
