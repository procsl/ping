package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.EntityAndIdRepositoryBuilder;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;


public class BooleanStatefulRepositoryBuilder extends EntityAndIdRepositoryBuilder {


    private TypeMirror booleanStatefulType;


    /**
     * 内部初始化
     */
    @Override
    protected void innerInit() {
        CharSequence name = "cn.procsl.ping.boot.domain.business.state.model.BooleanStateful";
        TypeMirror tmp = processingEnvironment.getElementUtils().getTypeElement(name).asType();
        booleanStatefulType = processingEnvironment.getTypeUtils().erasure(tmp);
    }

    /**
     * 获取支持的repository的class对象
     *
     * @return 返回支持的Repository 对象
     */
    @Override
    protected Class<?> getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository");
    }

    @Override
    public TypeName build(TypeElement entity, RoundEnvironment roundEnv) throws ClassNotFoundException {
        Types typeUtils = this.processingEnvironment.getTypeUtils();

        TypeMirror tmp = typeUtils.erasure(entity.asType());
        boolean bool = typeUtils.isSubtype(tmp, booleanStatefulType);
        if (bool) {
            return super.build(entity, roundEnv);
        }
        return null;
    }
}
