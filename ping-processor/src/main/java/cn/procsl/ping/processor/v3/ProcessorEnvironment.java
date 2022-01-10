package cn.procsl.ping.processor.v3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;

public interface ProcessorEnvironment {

    /**
     * 通过类名获取 element
     *
     * @param elementName 全限定名
     * @return 全限定名对应的Element
     */
    @Nullable
    TypeElement getTypeElementByName(String elementName);

    ProcessingEnvironment getProcessingEnvironment();

    RoundEnvironment getRoundEnvironment();

    /**
     * @return 获取编译时日志组件
     */
    Messager getMessager();

    /**
     * @return 获取编译时文件
     */
    Filer getFiler();


    /**
     * @param key 系统配置key
     * @return 返回找到的系统配置
     */
    String getConfig(@Nonnull String key);

    /**
     * 获取指定的类型的包装器
     *
     * @param realTypeName 类型名称
     * @return 返回包装器名称
     */
    String getWrapperType(TypeMirror realTypeName);

    boolean isPublic(ExecutableElement element);

    boolean isStatic(ExecutableElement element);

    AnnotationMirror findAnnotationMirror(Element element, String name);

    AnnotationMirror findAnnotationMirror(Element element, Class<? extends Annotation> name);

}
