package cn.procsl.ping.boot.jpa.domain.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * 分页从第一页开始
 *
 * @param <T>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FormatPage<T> implements Page<T> {

    final Page<T> page;

    protected List<T> content;

    public static <T> FormatPage<T> copy(Page<T> page) {
        if (page instanceof FormatPage) {
            return (FormatPage<T>) page;
        }
        return new FormatPage<>(page);
    }


    @Override
    @NonNull
    public List<T> getContent() {
        if (this.content != null) {
            return this.content;
        }
        return page.getContent();
    }

    @Override
    public boolean hasContent() {
        return page.hasContent();
    }

    @Override
    @NonNull
    @Schema(example = "true", description = "是否为第一页数据")
    public boolean isFirst() {
        return page.isFirst();
    }

    @Override
    @NonNull
    @Schema(example = "true", description = "Content是否为空")
    public boolean isEmpty() {
        return page.isEmpty();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public long getTotalElements() {
        return page.getTotalElements();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return page.map(converter);
    }

    @NonNull
    @Schema(example = "100", description = "总页数")
    public Long getTotal() {
        return page.getTotalElements();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getSize() {
        return page.getSize();
    }

    @NonNull
    @SuppressWarnings("unused")
    @Schema(example = "10", description = "每页大小")
    public int getLimit() {
        return page.getSize();
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
        return page.getTotalPages();
    }


    @NonNull
    @SuppressWarnings("unused")
    @Schema(example = "true", description = "是否存在后续数据")
    public boolean isNext() {
        return page.hasNext();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isLast() {
        return page.isLast();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public boolean hasNext() {
        return page.hasNext();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public boolean hasPrevious() {
        return page.hasPrevious();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getNumber() {
        return page.getNumber();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public int getNumberOfElements() {
        return page.getNumberOfElements();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable nextPageable() {
        return page.nextPageable();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable previousPageable() {
        return page.previousPageable();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Pageable getPageable() {
        return page.getPageable();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NonNull Sort getSort() {
        return page.getSort();
    }


    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public Iterator<T> iterator() {
        return page.iterator();
    }
}
