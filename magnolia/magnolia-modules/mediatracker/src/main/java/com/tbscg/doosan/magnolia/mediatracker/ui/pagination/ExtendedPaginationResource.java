package com.tbscg.doosan.magnolia.mediatracker.ui.pagination;

import com.vaadin.addon.pagination.PaginationResource;
import lombok.*;


@Data
@AllArgsConstructor
@Builder
public class ExtendedPaginationResource {

    private PaginationResource paginationResource;
    private ButtonType buttonType;

    public long total() {
        return this.paginationResource.total();
    }

    public int limit() {
        return this.paginationResource.limit();
    }

    public int page() {
        return this.paginationResource.page();
    }

    public int totalPage() {
        return this.paginationResource.totalPage();
    }

    public int initIndex() {
        return this.paginationResource.initIndex();
    }

    public void setTotal(long total) {
        this.paginationResource.setTotal(total);
    }

    public void setPage(int page) {
        this.paginationResource.setPage(page);
    }

    public void setLimit(int limit) {
        this.paginationResource.setLimit(limit);
    }

    public void setInitIndex(int initIndex) {
        this.paginationResource.setInitIndex(initIndex);
    }


    public ExtendedPaginationResource first() {
        return ExtendedPaginationResource.builder()
                .paginationResource(paginationResource.first())
                .buttonType(ButtonType.FIRST)
                .build();
    }

    public ExtendedPaginationResource previous() {
        return ExtendedPaginationResource.builder()
                .paginationResource(paginationResource.previous())
                .buttonType(ButtonType.PREVIOUS)
                .build();
    }

    public ExtendedPaginationResource next() {
        return ExtendedPaginationResource.builder()
                .paginationResource(paginationResource.next())
                .buttonType(ButtonType.NEXT)
                .build();
    }

    public ExtendedPaginationResource last() {
        return ExtendedPaginationResource.builder()
                .paginationResource(paginationResource.last())
                .buttonType(ButtonType.LAST)
                .build();
    }

    public boolean isFirst() {
        return this.paginationResource.isFirst();
    }

    public boolean hasPrevious() {
        return this.paginationResource.hasPrevious();
    }

    public boolean hasNext() {
        return this.paginationResource.hasNext();
    }

    public boolean isLast() {
        return this.paginationResource.isLast();
    }
}
