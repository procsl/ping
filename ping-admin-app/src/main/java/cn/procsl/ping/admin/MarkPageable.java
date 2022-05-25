package cn.procsl.ping.admin;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(in = ParameterIn.QUERY, description = "基于从1开始的分页 (1..N)", name = "${spring.data.web.pageable.page-parameter:offset}", schema = @Schema(type = "integer", defaultValue = "1"))
@Parameter(in = ParameterIn.QUERY, description = "每页大小", name = "${spring.data.web.pageable.size-parameter:size}", schema = @Schema(type = "integer", defaultValue = "10"))
@Parameter(in = ParameterIn.QUERY, description = "Sorting criteria in the format: property,(asc|desc). " + "Default sort order is ascending. " + "Multiple sort criteria are supported.", name = "sort", array = @ArraySchema(schema = @Schema(type = "String", example = "id")))
@Parameter(name = "pageable", hidden = true)
public @interface MarkPageable {


}
