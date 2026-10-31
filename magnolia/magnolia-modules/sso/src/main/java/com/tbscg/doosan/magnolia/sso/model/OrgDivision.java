package com.tbscg.doosan.magnolia.sso.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrgDivision {
    EMEA,
    APE,
    PORTABLEPOWER,
    UNKNOWN,
    BOBCAT,
    DOOSAN;

    @JsonCreator
    public static OrgDivision fromString(String value) {
        try {
            return OrgDivision.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
