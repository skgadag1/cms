package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.Data;

@Data
public class MediatrackerFilters {

    private Division division;
    private Status[] status;
    private StatusChanged statusChanged;

    //pagination
    private Sort sort;
    private Integer size;
    private String[] searchAfter;
    private String[] searchBefore;
    private boolean fetchAll;

    public void setStatus(Status... status) {
        this.status = status;
    }
}
