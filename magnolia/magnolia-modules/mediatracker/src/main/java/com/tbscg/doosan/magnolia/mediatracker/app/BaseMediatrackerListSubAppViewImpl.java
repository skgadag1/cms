package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.ui.pagination.ButtonType;
import com.tbscg.doosan.magnolia.mediatracker.ui.pagination.ExtendedPagination;
import com.tbscg.doosan.magnolia.mediatracker.ui.pagination.ExtendedPaginationResource;
import com.vaadin.addon.pagination.PaginationResource;
import com.vaadin.ui.Grid;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseMediatrackerListSubAppViewImpl<T extends BaseVanityData> {

    protected final ExtendedPagination pagination;

    public static final int[] ITEMS_PER_PAGE = {5, 10, 20, 50, 100};
    private final int page = 1;
    private final int limit = 5;

    private static final String modifiedTime = "modifiedTime.keyword";
    private final Sort modifiedTimeDescSort = new Sort(modifiedTime, SortOrder.DESC.name());
    private final Sort modifiedTimeASCSort = new Sort(modifiedTime, SortOrder.ASC.name());

    public BaseMediatrackerListSubAppViewImpl() {
        this.pagination = createPagination(page, limit);
        this.pagination.addPageChangeListener(event -> {
            ButtonType buttonType = event.getButtonType();
            VanityDataSearchResult<T> result = fetchForPagination(buttonType);
            pagination.setTotalCount(result.getTotal());
            pagination.setCurrentSearchAfter(result.getSearchAfterSort());
            pagination.setCurrentSearchBefore(result.getSearchBeforeSort());
            pagination.getPaginationResource().setButtonType(null);
            getGrid().setItems(result.getItems().stream()
                    .sorted(Comparator.comparing(BaseVanityData::getModifiedTime).reversed())
                    .collect(Collectors.toList()));
            getGrid().scrollToStart();
        });
    }

    protected abstract VanityDataSearchResult<T> fetchUpdatedItems(boolean shouldFetchAll, StatusChanged statusChanged);
    protected abstract VanityDataSearchResult<T> fetchForPagination(ButtonType buttonType);
    protected abstract VanityDataSearchResult<T> fetchAll();

    protected abstract void refreshUpdatedItems(StatusChanged statusChanged);
    protected abstract void refresh();

    protected ExtendedPagination createPagination(int page, int limit) {
        final PaginationResource paginationResource = PaginationResource.newBuilder()
                .setPage(page)
                .setLimit(limit)
                .build();
        final ExtendedPaginationResource extendedPaginationResource = ExtendedPaginationResource.builder()
                .paginationResource(paginationResource)
                .buttonType(ButtonType.FIRST)
                .build();
        final ExtendedPagination pagination = new ExtendedPagination(extendedPaginationResource);
        pagination.setItemsPerPage(ITEMS_PER_PAGE);
        return pagination;
    }

    protected Integer refreshPagination(VanityDataSearchResult<BaseVanityData> result) {
        Integer total = result.getTotal();
        Integer itemsPerPage = pagination.getLimit();
        Integer endIndex = itemsPerPage > total ? total : itemsPerPage;
        pagination.setTotalCount(total);
        pagination.setItemsRange();
        pagination.setCurrentPage(1);
        pagination.setCurrentSearchAfter(result.getSearchAfterSort());
        pagination.setCurrentSearchBefore(result.getSearchBeforeSort());
        return endIndex;
    }

    protected abstract Grid<T> getGrid();

    protected Sort getModifiedTimeDescSort() {
        return modifiedTimeDescSort;
    }

    protected Sort getModifiedTimeAscSort() {
        return modifiedTimeASCSort;
    }

    protected void addPaginationToFilters(boolean shouldFetchAll, MediatrackerFilters filters) {
        filters.setSort(this.getModifiedTimeDescSort());
        if (!shouldFetchAll) {
            filters.setSize(this.pagination.getLimit());
        }
        filters.setFetchAll(shouldFetchAll);
    }

    protected void setFiltersForButtonEvent(ButtonType buttonType, MediatrackerFilters filters) {
        switch (Optional.ofNullable(buttonType).orElse(ButtonType.FIRST)) {
            case FIRST: {
                filters.setSort(this.getModifiedTimeDescSort());
                filters.setSearchAfter(new String[]{});
                filters.setSearchBefore(new String[]{});
                break;
            }
            case NEXT: {
                filters.setSort(this.getModifiedTimeDescSort());
                filters.setSearchAfter(pagination.getCurrentSearchAfter());
                filters.setSearchBefore(new String[]{});
                break;
            }
            case PREVIOUS: {
                filters.setSort(this.getModifiedTimeAscSort());
                filters.setSearchAfter(new String[]{});
                filters.setSearchBefore(pagination.getCurrentSearchBefore());
                break;
            }
            case LAST: {
                filters.setSort(this.getModifiedTimeAscSort());
                filters.setSearchAfter(new String[]{});
                filters.setSearchBefore(new String[]{});
                break;
            }
        }
    }
}
