package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.processor.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.processor.repository.processor.RepositoryBuilder;
import com.google.auto.service.AutoService;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;


@AutoService(RepositoryBuilder.class)
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
    protected String getSupportRepositoryClass() {
        return "cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository";
    }

    @Override
    public TypeMirror generator(TypeElement entity, RoundEnvironment roundEnv) {
        Types typeUtils = this.processingEnvironment.getTypeUtils();

        TypeMirror tmp = typeUtils.erasure(entity.asType());
        boolean bool = typeUtils.isSubtype(tmp, booleanStatefulType);
        if (bool) {
            return super.generator(entity, roundEnv);
        }
        return null;
    }
}
