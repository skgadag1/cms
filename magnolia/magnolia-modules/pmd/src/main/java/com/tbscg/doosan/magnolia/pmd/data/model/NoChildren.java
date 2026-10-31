package com.tbscg.doosan.magnolia.pmd.data.model;

public class NoChildren extends ConnectTarget {

    public NoChildren(ConnectTarget parent) {
        super(parent != null ? parent.getId() : -1);
    }

    @Override
    public String getLabel() {
        return "- none -";
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public boolean hasChildren() {
        return false;
    }
}
