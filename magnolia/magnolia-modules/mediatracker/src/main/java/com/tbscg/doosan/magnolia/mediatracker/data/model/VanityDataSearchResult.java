package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class VanityDataSearchResult<T extends BaseVanityData> {

    public VanityDataSearchResult(List<T> items, Integer total) {
        this.items = items;
        this.total = total;
    }

    private List<T> items;

    private Integer total;

    private String[] searchAfterSort;

    private String[] searchBeforeSort;
}
