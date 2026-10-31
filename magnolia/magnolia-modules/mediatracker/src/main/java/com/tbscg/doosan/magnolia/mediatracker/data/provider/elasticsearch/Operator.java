package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

/**
 * Enum containing ElasticSearch boolean logic operators
 * used to interpret text in the query value
 */
public enum Operator {
    OR("or"), AND("and");

    private final String value;

    Operator(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
