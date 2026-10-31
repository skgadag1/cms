package com.tbscg.doosan.magnolia.product.catalog.field.relateditems;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Class holds data to display as single row in Grid component.
 */
@Getter
@Setter
public class GridItem {

    private static final String TITLE = "title";

    private String title;
    private String path;

    public GridItem() {
        this.title = Strings.EMPTY;
        this.path = Strings.EMPTY;
    }

    public GridItem(Node node, String lang) {
        try {
            this.title = getLocalisedProperty(node, TITLE, lang);
            this.path = node.getPath();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    private String getLocalisedProperty(Node node, String name, String lang) {
        try {
            if(Strings.isNotBlank(lang)) {
                String lName = name + "_" + lang;
                if (node.hasProperty(lName)) {
                    return node.getProperty(lName).getString();
                }
            }
            if(node.hasProperty(name)) {
                return node.getProperty(name).getString();
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return Strings.EMPTY;
    }

}
