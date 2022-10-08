package cn.procsl.ping.boot.common.web;

import cn.procsl.ping.boot.common.utils.QueryBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.QueryResults;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public <E> FormatPage<E> transform(Function<T, E> convert) {
        List<E> content = this.getContent().stream().map(convert).collect(Collectors.toList());
        return page(content, this.getPageable(), this.getTotal());
    }

    @SuppressWarnings("unused")
    public <E> Collection<E> convert(Function<T, E> converter) {
        return this.getContent().stream().map(converter).collect(Collectors.toList());
    }

    @Override
    @NonNull
    public List<T> getContent() {
        return super.getContent();
    }

    @Override
    @NonNull
    @Schema(example = "true", description = "是否为第一页数据")
    public boolean isFirst() {
        return super.isFirst();
    }

    @Override
    @NonNull
    @Schema(example = "true", description = "Content是否为空")
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @NonNull
    @Schema(example = "100", description = "总页数")
    public Long getTotal() {
        return super.getTotalElements();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getSize() {
        return super.getSize();
    }

    @NonNull
    @SuppressWarnings("unused")
    @Schema(example = "10", description = "每页大小")
    public int getLimit() {
        return super.getSize();
    }

    @NonNull
    @SuppressWarnings("unused")
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


    @NonNull
    @SuppressWarnings("unused")
    @Schema(example = "true", description = "是否存在后续数据")
    public boolean isNext() {
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
