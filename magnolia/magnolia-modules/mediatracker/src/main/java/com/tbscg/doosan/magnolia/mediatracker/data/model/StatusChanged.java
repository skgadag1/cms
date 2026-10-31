package com.tbscg.doosan.magnolia.mediatracker.data.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StatusChanged {

    private boolean isDeleted;

    private String code;

    private OffsetDateTime lastUpdatedTime;

    public StatusChanged(String code, OffsetDateTime lastUpdatedTime, boolean isDeleted) {
        this.isDeleted = isDeleted;
        this.code = code;
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
