package com.tbscg.doosan.magnolia.dxp.core.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.jcr.Item;
import javax.jcr.Node;
import java.util.Collection;

/**
 * Utility class to wrap some methods that throw checked exceptions.
 */
@UtilityClass
public class SneakyWrappers {
    /**
     * Sneaky wrapper for {@link Node#getPath()}.
     * @param item item to get path from.
     * @return path of the item.
     */
    @SneakyThrows
    public static String getNodePath(final Item item) {
        return item.getPath();
    }

    /**
     * Sneaky wrapper for {@link NodeUtil#getDirectNodes(Node)}.
     * @param item item to get direct nodes from.
     * @return collection of direct nodes.
     */
    @SneakyThrows
    public static Collection<Node> getDirectNodes(final Node item) {
        return NodeUtil.getDirectNodes(item);
    }

}
