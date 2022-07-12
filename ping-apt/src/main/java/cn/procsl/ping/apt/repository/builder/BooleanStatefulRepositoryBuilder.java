package cn.procsl.ping.apt.repository.builder;

import cn.procsl.ping.apt.repository.EntityAndIdRepositoryBuilder;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;


//@AutoService(RepositoryBuilder.class)
public class BooleanStatefulRepositoryBuilder extends EntityAndIdRepositoryBuilder {

    private TypeMirror booleanStatefulType;

    /**
     * 内部初始化
     */
    @Override
    protected void innerInit() {
        CharSequence name = "cn.procsl.ping.boot.domain.business.state.model.BooleanStateful";
        TypeElement tmp = processingEnvironment.getElementUtils().getTypeElement(name);
        if (tmp == null) {
            return;
        }
        booleanStatefulType = processingEnvironment.getTypeUtils().erasure(tmp.asType());
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
    public Map<String, List<TypeMirror>> generator(TypeElement entity, RoundEnvironment roundEnv) {
        Types typeUtils = this.processingEnvironment.getTypeUtils();

        TypeMirror tmp = typeUtils.erasure(entity.asType());
        boolean bool = typeUtils.isSubtype(tmp, booleanStatefulType);
        if (bool) {
            return super.generator(entity, roundEnv);
        }
        return Collections.emptyMap();
    }
}
