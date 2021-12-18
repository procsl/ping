package cn.procsl.ping.processor.repo.builder;

import cn.procsl.ping.processor.repo.AbstractRepositoryBuilder;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;


//@AutoService(RepositoryBuilder.class)
public class AdjacencyTreeRepositoryBuilder extends AbstractRepositoryBuilder {

    final static String supportName = "cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository";
    final static String adjacencyNodeName = "cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode";
    final static String adjacencyPathNodeName = "cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode";
    final static String domainEntityName = "cn.procsl.ping.business.domain.DomainEntity";
    final static String domainIdName = "cn.procsl.ping.business.domain.DomainId";
    final static String domainEventsName = "cn.procsl.ping.business.domain.DomainEvents";
    private Types typeUtils;
    private Elements elementUtils;
    private TypeMirror node;
    private TypeMirror path;
    private TypeMirror domainEntity;
    private TypeMirror domainId;
    private TypeMirror domainEvents;
    private boolean init = false;

    /**
     * 内部初始化
     */
    @Override
    protected void innerInit() {
        typeUtils = this.processingEnvironment.getTypeUtils();
        elementUtils = this.processingEnvironment.getElementUtils();
        TypeElement adjacencyType = elementUtils.getTypeElement(adjacencyNodeName);
        if (adjacencyType == null) {
            return;
        }
        // 删除泛型
        node = this.typeUtils.erasure(adjacencyType.asType());
        path = this.typeUtils.erasure(elementUtils.getTypeElement(adjacencyPathNodeName).asType());

        domainEntity = this.typeUtils.erasure(elementUtils.getTypeElement(domainEntityName).asType());
        domainId = this.typeUtils.erasure(elementUtils.getTypeElement(domainIdName).asType());
        domainEvents = this.typeUtils.erasure(elementUtils.getTypeElement(domainEventsName).asType());
        init = true;
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
            TypeMirror notType = this.typeUtils.erasure(mirror);
            boolean bool = this.typeUtils.isSubtype(notType, node);
            if (bool && mirror instanceof DeclaredType) {
                return ((DeclaredType) mirror);
            }
        }

        mirrorsInf.removeIf(element -> {
            element = typeUtils.erasure(element);
            return element.equals(domainId) || element.equals(domainEntity) || element.equals(domainEvents);
        });
        for (TypeMirror mirror : mirrorsInf) {
            TypeMirror notType = this.typeUtils.erasure(mirror);
            boolean bool = this.typeUtils.isSubtype(notType, node);
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
    protected void findSuperClass(TypeMirror element,
                                  List<TypeElement> elements,
                                  List<TypeMirror> mirrors) {
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

    protected void findInterfaces(TypeMirror mirro,
                                  List<TypeElement> elements,
                                  List<TypeMirror> mirrors) {
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
    protected String getSupportRepositoryClass() {
        return supportName;
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
    public Map<String, List<TypeMirror>> generator(TypeElement entity, RoundEnvironment roundEnv) {
        if (!init) {
            return null;
        }

        TypeMirror idType = findIdType(entity);
        if (idType == null) {
            return null;
        }

        DeclaredType tmp = this.getSubType(entity);
        if (tmp == null) {
//            log.info("Not implement {} interface, skip!", adjacencyNodeName);
            return null;
        }

        TypeMirror pathType = null;
        for (TypeMirror mirror : tmp.getTypeArguments()) {
            boolean bool = this.typeUtils.isSubtype(this.typeUtils.erasure(mirror), path);
            if (bool) {
                pathType = mirror;
                break;
            }
        }

        if (pathType == null) {
//            log.warn("Not find: {} subType", adjacencyNodeName);
            return null;
        }

        if (pathType instanceof TypeVariable) {
            pathType = ((TypeVariable) pathType).getUpperBound();
        }

        return Map.of(this.getSupportRepositoryClass(), Arrays.asList(entity.asType(), idType, pathType));
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
    public Collection<String> getName() {
        return Collections.singletonList("AdjacencyTreeRepository");
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

        Element type = this.typeUtils.asElement(mirror);
        if (type instanceof TypeElement) {
            return (TypeElement) type;
        }
        return null;
    }
}
