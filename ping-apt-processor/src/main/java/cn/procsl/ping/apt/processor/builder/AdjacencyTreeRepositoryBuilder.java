package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.AbstractRepositoryBuilder;
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

    private TypeMirror node;

    private TypeMirror path;

    private TypeMirror domainEntity;

    private TypeMirror domainId;

    private TypeMirror domainEvents;

    final static String adjacencyNodeName = "cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode";
    final static String adjacencyPathNodeName = "cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode";
    final static String domainEntityName = "cn.procsl.ping.business.domain.DomainEntity";
    final static String domainIdName = "cn.procsl.ping.business.domain.DomainId";
    final static String domainEventsName = "cn.procsl.ping.business.domain.DomainEvents";

    /**
     * 内部初始化
     */
    @Override
    protected void innerInit() {
        type = this.processingEnvironment.getTypeUtils();
        Elements ele = this.processingEnvironment.getElementUtils();
        TypeElement adjacencyType = ele.getTypeElement(adjacencyNodeName);
        // 删除泛型
        node = this.type.erasure(adjacencyType.asType());
        path = this.type.erasure(ele.getTypeElement(adjacencyPathNodeName).asType());

        domainEntity = this.type.erasure(ele.getTypeElement(domainEntityName).asType());
        domainId = this.type.erasure(ele.getTypeElement(domainIdName).asType());
        domainEvents = this.type.erasure(ele.getTypeElement(domainEventsName).asType());
    }

    protected DeclaredType getSubType(TypeElement entity) {
        // jb 代码写的太垃圾了
        List<TypeElement> elementsClass = new LinkedList<>();
        List<TypeMirror> mirrorsClass = new LinkedList<>();

        List<TypeElement> elementsInf = new LinkedList<>();
        List<TypeMirror> mirrorsInf = new LinkedList<>();
        this.findSuperClass(entity.asType(), elementsClass, mirrorsClass);

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
    protected void findSuperClass(@NonNull TypeMirror element,
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
        this.findSuperClass(tmp.getSuperclass(), elements, mirrors);

    }

    protected void findInterfaces(@NonNull TypeMirror mirro,
                                  @NonNull List<TypeElement> elements,
                                  @NonNull List<TypeMirror> mirrors) {
        TypeElement tmp = this.convert(mirro);
        if (mirro == null) {
            return;
        }
        if (tmp.getInterfaces() == null || tmp.getInterfaces().isEmpty()) {
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
    protected Class getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository");
    }

    /**
     * 构建接口元素
     * 则直接返回空,表示不支持
     * 如果实体中没有找到 NodePath 或者实体不是继承于  cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode
     *
     * @param entity   当前的实体
     * @param roundEnv 当前编译器上下文
     * @return 返回接口标识
     */
    @Override
    public TypeName build(TypeElement entity, RoundEnvironment roundEnv) throws ClassNotFoundException {

        TypeName idType = createIdType(entity);
        if (idType == null) {
            return null;
        }

        DeclaredType tmp = this.getSubType(entity);
        if (tmp == null) {
            log.info("Not implement {} interface, skip!", adjacencyNodeName);
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
            log.warn("Not find: {} subType", adjacencyNodeName);
            return null;
        }

        if (pathType instanceof TypeVariable) {
            pathType = ((TypeVariable) pathType).getUpperBound();
        }

        TypeName pathNode = createPathNodeType(pathType);

        TypeName entityType = createEntityType(entity);

        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());
        return ParameterizedTypeName.get(repositoryType, entityType, idType, pathNode);
    }


    /**
     * 需要查找当前实体上的PathNode范型对应的类
     *
     * @return TypeName
     */
    protected TypeName createPathNodeType(TypeMirror mirror) {
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
     * @param mirror TypeMirror
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
