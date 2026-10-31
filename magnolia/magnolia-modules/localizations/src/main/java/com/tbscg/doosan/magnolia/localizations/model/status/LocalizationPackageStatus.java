package com.tbscg.doosan.magnolia.localizations.model.status;

import lombok.Getter;

@Getter
public enum LocalizationPackageStatus {
    NOT_PUBLISHED("icon-status-orange", "Not published"),
    ERROR("icon-status-red", "Error while publishing"),
    PUBLISHED_TO_SQS("icon-status-green", "Published to SQS");

    private String color;
    private String description;

    LocalizationPackageStatus(String color, String description) {
        this.color = color;
        this.description = description;
    }
}
