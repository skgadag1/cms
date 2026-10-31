package com.tbscg.doosan.magnolia.dealernet.field;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import lombok.Data;

import javax.jcr.Node;

/**
 * Class holds data to display as single row in Grid component.
 */
@Data
public class GridItem {

    private static final String TITLE = "@name";

    private String title;
    private String path;

    public GridItem(Node node) {
        this.title = NodeUtil.getName(node);
        this.path = NodeUtil.getPath(node);
    }
}
