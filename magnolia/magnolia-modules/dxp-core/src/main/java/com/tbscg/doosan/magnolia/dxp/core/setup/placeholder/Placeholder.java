package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import org.apache.commons.lang3.builder.ToStringBuilder;

class Placeholder {
    final String nodePath;
    final String propertyName;
    final String placeholderName;

    Placeholder(String nodePath, String propertyName, String placeholderName) {
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.placeholderName = placeholderName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("placeholder", this.placeholderName)
                .append("node", this.nodePath)
                .append("property", this.propertyName)
                .toString();
    }
}
