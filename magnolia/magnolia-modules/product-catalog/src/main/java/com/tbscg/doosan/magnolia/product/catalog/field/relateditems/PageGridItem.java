package com.tbscg.doosan.magnolia.product.catalog.field.relateditems;

import info.magnolia.jcr.util.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

/**
 * Class holds page data to display as single row in Grid component.
 */
@Getter
@Setter
@Slf4j
public class PageGridItem extends GridItem {

    private static final String PAGE_RELATED = "pageRelated";
    private static final String URL = "url";
    private static final String PATH = "path";
    private String pageUrl;
    private String magnoliaPagePath;

    public PageGridItem(String lang, Node item) {
        super();
        this.pageUrl = getLocalisedUrl(lang, item);
        this.magnoliaPagePath = this.getPath();
    }

    protected String getLocalisedUrl(String lang, Node item) {
        try {
            Node pageRelatedNode = item.hasNode(PAGE_RELATED) ? item.getNode(PAGE_RELATED) : null;
            if (Strings.isNotBlank(lang) && pageRelatedNode != null) {
                setPath(getStringProperty(pageRelatedNode, PATH));
                Node localizedPage = pageRelatedNode.getNode(lang);
                return getStringProperty(localizedPage, URL);
            }
        } catch (Exception e) {
            log.error("Error while parsing localised url from associated page {}", this.getPath(), e.getMessage());
        }
        return Strings.EMPTY;
    }

    private String getStringProperty(Node node, String relativePath) {
        return Optional.ofNullable(PropertyUtil.getPropertyOrNull(node, relativePath))
                .map(PropertyUtil::getValueString)
                .orElse(Strings.EMPTY);
    }
}
