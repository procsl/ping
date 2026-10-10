package cn.procsl.ping.boot.ui.component.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个请求处理方法是「可被抽象组件装配描述引用的接口」。
 *
 * <p>装配描述（{@code ui/<page>/compose.json}）以 {@code @ResourceReference:<name>} 引用该接口，
 * 启动期由 {@code UIComponentRegistry} 结合打包期导出的 OpenAPI 文档，
 * 把引用就地替换为含方法、路径、入参结构、出参结构的完整接口描述。
 *
 * <p>未标注的方法不参与引用解析 —— 即使同名接口存在，引用也不会被解析。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResourceReference {

    String name() default "";

}
