package cn.procsl.ping.boot.domain.processor.builder;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.processor.AbstractRepositoryBuilder;
import cn.procsl.ping.business.domain.DomainEntity;
import cn.procsl.ping.business.domain.DomainEvents;
import cn.procsl.ping.business.domain.DomainId;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.LinkedList;
import java.util.List;

import static com.squareup.javapoet.TypeName.get;

@Slf4j
public class AdjacencyTreeRepositoryBuilder extends AbstractRepositoryBuilder {

    private Types type;

    private Elements ele;

    private TypeElement adjacencyType;

    private TypeMirror node;

    private TypeMirror path;

    private TypeMirror domainEntity;

    private TypeMirror domainId;

    private TypeMirror domainEvents;


    /**
     * 内部初始化
     */
    @Override
    protected void innerInit() {
        type = this.processingEnvironment.getTypeUtils();
        ele = this.processingEnvironment.getElementUtils();
        adjacencyType = ele.getTypeElement(AdjacencyNode.class.getName());
        // 删除泛型
        node = this.type.erasure(adjacencyType.asType());
        path = this.type.erasure(this.ele.getTypeElement(AdjacencyPathNode.class.getName()).asType());

        domainEntity = this.type.erasure(this.ele.getTypeElement(DomainEntity.class.getName()).asType());
        domainId = this.type.erasure(this.ele.getTypeElement(DomainId.class.getName()).asType());
        domainEvents = this.type.erasure(this.ele.getTypeElement(DomainEvents.class.getName()).asType());
    }

    protected DeclaredType getSubType(TypeElement entity) {
        // jb 代码写的太垃圾了
        List<TypeElement> elementsClass = new LinkedList<>();
        List<TypeMirror> mirrorsClass = new LinkedList<>();

        List<TypeElement> elementsInf = new LinkedList<>();
        List<TypeMirror> mirrorsInf = new LinkedList<>();
        this.findSuperClass(entity, entity.asType(), elementsClass, mirrorsClass);

        // 查找所有的接口
        for (TypeElement element : elementsClass) {
            List<? extends TypeMirror> infs = element.getInterfaces();
            for (TypeMirror inf : infs) {
                this.findInterfaces(inf, elementsInf, mirrorsInf);
            }
        }

        mirrorsClass.removeIf(element -> element.equals(entity.asType()));
        // 过滤
        for (TypeMirror mirror : mirrorsClass) {
            TypeMirror notType = this.type.erasure(mirror);
            boolean bool = this.type.isSubtype(notType, node);
            if (bool && mirror instanceof DeclaredType) {
                return ((DeclaredType) mirror);
            }
        }

        mirrorsInf.removeIf(element -> {
            element = type.erasure(element);
            return element.equals(domainId) || element.equals(domainEntity) || element.equals(domainEvents);
        });
        for (TypeMirror mirror : mirrorsInf) {
            TypeMirror notType = this.type.erasure(mirror);
            boolean bool = this.type.isSubtype(notType, node);
            if (bool && mirror instanceof DeclaredType) {
                return ((DeclaredType) mirror);
            }
        }

        // 查询是否存在指定的接口
        return null;
    }

    /**
     * 查找父类
     *
     * @param element  指定的元素
     * @param elements 容器
     */
    protected void findSuperClass(TypeElement entity, @NonNull TypeMirror element,
                                  @NonNull List<TypeElement> elements,
                                  @NonNull List<TypeMirror> mirrors) {
        TypeElement tmp = this.convert(element);
        if (element == null) {
            return;
        }

        boolean bool = tmp.getSuperclass() instanceof NoType;
        if (bool) {
            return;
        }

        bool = tmp.getSuperclass() instanceof NullType;
        if (bool) {
            return;
        }

        mirrors.add(element);
        elements.add(tmp);
        this.findSuperClass(entity, tmp.getSuperclass(), elements, mirrors);

    }

    protected void findInterfaces(@NonNull TypeMirror mirro,
                                  @NonNull List<TypeElement> elements,
                                  @NonNull List<TypeMirror> mirrors) {
        TypeElement tmp = this.convert(mirro);
        if (mirro == null) {
            return;
        }
        if (CollectionUtils.isEmpty(tmp.getInterfaces())) {
            return;
        }

        elements.add(tmp);
        mirrors.add(mirro);

        for (TypeMirror mirror : tmp.getInterfaces()) {
            this.findInterfaces(mirror, elements, mirrors);
        }
    }

    /**
     * 获取支持的repository的class对象
     *
     * @return 返回支持的Repository 对象
     */
    @Override
    protected Class<AdjacencyTreeRepository> getSupportRepositoryClass() {
        return AdjacencyTreeRepository.class;
    }

    /**
     * 构建接口元素
     * 则直接返回空,表示不支持
     * 如果实体中没有找到 NodePath 或者实体不是继承于 {@link cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode}
     *
     * @param entity   当前的实体
     * @param roundEnv 当前编译器上下文
     * @return 返回接口标识
     */
    @Override
    public TypeName build(TypeElement entity, RoundEnvironment roundEnv) {

        TypeName idType = createIdType(entity);
        if (idType == null) {
            return null;
        }

        DeclaredType tmp = this.getSubType(entity);
        if (tmp == null) {
            log.info("Not implement {} interface, skip!", AdjacencyNode.class.getName());
            return null;
        }

        TypeMirror pathType = null;
        for (TypeMirror mirror : tmp.getTypeArguments()) {
            boolean bool = this.type.isSubtype(this.type.erasure(mirror), path);
            if (bool) {
                pathType = mirror;
                break;
            }
        }

        if (pathType == null) {
            log.warn("Not find: {} subType", AdjacencyPathNode.class.getName());
            return null;
        }

        if (pathType instanceof TypeVariable) {
            pathType = ((TypeVariable) pathType).getUpperBound();
        }

        TypeName pathNode = createPathNodeType(entity, pathType);

        TypeName entityType = createEntityType(entity);

        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());
        return ParameterizedTypeName.get(repositoryType, entityType, idType, pathNode);
    }


    /**
     * 需要查找当前实体上的PathNode范型对应的类
     *
     * @param element
     * @return
     */
    protected TypeName createPathNodeType(TypeElement element, TypeMirror mirror) {
        return get(mirror);
    }

    /**
     * 是否独立实现, 不同时继承多个(某些接口有冲突的情况下)
     *
     * @return 如果独立创建, 则返回true
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public String getName() {
        return "AdjacencyTree";
    }

    /**
     * 转化节点
     *
     * @param mirror
     * @return 如果可以转换为TypeElement则返回, 否则返回null
     */
    public TypeElement convert(TypeMirror mirror) {
        if (mirror == null) {
            return null;
        }

        Element type = this.type.asElement(mirror);
        if (type instanceof TypeElement) {
            return (TypeElement) type;
        }
        return null;
    }
}
