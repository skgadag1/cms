package com.tbscg.doosan.magnolia.bobcat.field.filter;

import lombok.Data;

import java.util.Map;

@Data
public class NodeFilter {

    public static final String PROPERTY_NAME = "propertyName";
    public static final String PROPERTY_VALUE = "propertyValue";
    public static final String PROPERTY_DEFAULT_VALUE = "propertyDefaultValue";
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_NAME = "nodeName";
    public static final String HIDE_EMPTY = "hideEmpty";
    public static final String USE_PREFILTER_QUERY = "prefilterQuery";

    private String propertyName;

    private String propertyValue;

    private String propertyDefaultValue = "";

    private boolean hideEmpty = false;

    private String nodeType;

    private String nodeName;

    private boolean prefilterQuery = false;

    public static NodeFilter fromPropertyMap(final Map<String, ?> nodeFilterProperties) {
        if (nodeFilterProperties == null) {
            return null;
        }

        final NodeFilter nf = new NodeFilter();
        nf.setPropertyName((String) nodeFilterProperties.get(PROPERTY_NAME));
        nf.setPropertyValue((String) nodeFilterProperties.get(PROPERTY_VALUE));
        nf.setPropertyDefaultValue((String) nodeFilterProperties.get(PROPERTY_DEFAULT_VALUE));
        nf.setNodeType((String) nodeFilterProperties.get(NODE_TYPE));
        nf.setNodeName((String) nodeFilterProperties.get(NODE_NAME));

        final Object hideEmpty = nodeFilterProperties.get(HIDE_EMPTY);
        nf.setHideEmpty(hideEmpty != null ? (Boolean) hideEmpty : false);

        final Object prefilterQuery = nodeFilterProperties.get(USE_PREFILTER_QUERY);
        nf.setPrefilterQuery(prefilterQuery != null ? (Boolean) prefilterQuery : false);

        return nf;
    }
}
