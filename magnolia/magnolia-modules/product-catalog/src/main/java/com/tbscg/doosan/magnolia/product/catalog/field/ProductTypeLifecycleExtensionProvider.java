package com.tbscg.doosan.magnolia.product.catalog.field;

import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.vaadin.data.ValueProvider;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.HashMap;

/**
 * Value Provider for product and feature types used in column definition.
 */
@Slf4j
public class ProductTypeLifecycleExtensionProvider implements ValueProvider<JcrNodeWrapper, String> {

    private static final HashMap<String, Integer> supportedJcrTypesMap;

    static {
        supportedJcrTypesMap = new HashMap<>();
        supportedJcrTypesMap.put("dxp:productGroup", 0);
        supportedJcrTypesMap.put("dxp:subcategory", 1);
        supportedJcrTypesMap.put("dxp:category", 2);
    }

    @Override
    public String apply(final JcrNodeWrapper jcrNode) {
        try {
            final String jcrType = jcrNode.getPrimaryNodeType().getName();
            String typeName = ProductType.getNameByType(jcrType);

            if (supportedJcrTypesMap.containsKey(jcrType)) {
                return prepareTypeName(typeName, hasHistoricalAndCurrent(supportedJcrTypesMap.get(jcrType), jcrNode.getNodes()));
            }

            return typeName;
        } catch (RepositoryException e) {
            log.error("Failed to product typeName with lifecycle information.", e);
            return "Unknown";
        }
    }

    private String prepareTypeName(String typeName, ProductLifecycleType kind) {
        String extension = kind.historical && kind.current ? "MIX" : kind.historical ? "HIS" : kind.current ? "CUR" : "";

        return extension.equals("") ? typeName : typeName + " ( " + extension + " )";
    }

    private ProductLifecycleType hasHistoricalAndCurrent(int depth, NodeIterator nodes) throws RepositoryException {
        boolean hasHistorical = false;
        boolean hasCurrent = false;

        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            String lifecycle = PropertyUtil.tryGetStringValue(node, "lifecycle");

            if (!hasCurrent && lifecycle.equals("Current")) {
                hasCurrent = true;
            }

            if (!hasHistorical && lifecycle.equals("Historical")) {
                hasHistorical = true;
            }

            if (hasCurrent && hasHistorical) {
                break;
            }

            if (depth > 0) {
                ProductLifecycleType kind = hasHistoricalAndCurrent(depth - 1, node.getNodes());

                if (kind.historical) {
                    hasHistorical = true;
                }

                if (kind.current) {
                    hasCurrent = true;
                }
            }
        }

        return new ProductLifecycleType(hasHistorical, hasCurrent);
    }

    public static class ProductLifecycleType {
        public final boolean historical;
        public final boolean current;

        public ProductLifecycleType(boolean historical, boolean current) {
            this.historical = historical;
            this.current = current;
        }
    }
}

