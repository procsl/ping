package cn.procsl.ping.admin.web;

import cn.procsl.ping.admin.utils.QueryBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.QueryResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 分页从第一页开始
 *
 * @param <T>
 */
public class FormatPage<T> extends PageImpl<T> {


    private FormatPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public static <T> FormatPage<T> page(List<T> content, Pageable pageable, long total) {
        return new FormatPage<>(content, pageable, total);
    }

    public static <T> FormatPage<T> page(QueryBuilder<T> query, Pageable pageable) {
        QueryResults<T> result = query.build(pageable).fetchResults();
        return new FormatPage<>(result.getResults(), pageable, result.getTotal());
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @Schema(example = "100", description = "总页数")
    public Long getTotal() {
        return super.getTotalElements();
    }

    @Override
    @Schema(example = "10", description = "每页大小", hidden = true)
    @JsonIgnore
    public int getSize() {
        return super.getSize();
    }

    @Schema(example = "10", description = "每页大小")
    public int getLimit() {
        return super.getSize();
    }

    @Schema(example = "1", description = "页码偏移量")
    public long getOffset() {
        return this.getPageable().getOffset() + 1;
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isLast() {
        return super.isLast();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getNumber() {
        return super.getNumber();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getNumberOfElements() {
        return super.getNumberOfElements();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable nextPageable() {
        return super.nextPageable();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable previousPageable() {
        return super.previousPageable();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable getPageable() {
        return super.getPageable();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Sort getSort() {
        return super.getSort();
    }


}
