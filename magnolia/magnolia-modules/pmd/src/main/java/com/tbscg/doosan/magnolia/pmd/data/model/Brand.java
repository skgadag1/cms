package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Brand extends ConnectTarget {

    private String code;
    private String locale;
    private String name;

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }
}
