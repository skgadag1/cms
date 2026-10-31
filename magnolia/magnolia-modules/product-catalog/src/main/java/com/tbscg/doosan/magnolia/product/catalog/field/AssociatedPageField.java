package com.tbscg.doosan.magnolia.product.catalog.field;

import com.tbscg.doosan.magnolia.product.catalog.field.relateditems.GridItem;
import com.tbscg.doosan.magnolia.product.catalog.field.relateditems.PageGridItem;
import com.tbscg.doosan.magnolia.product.catalog.field.relateditems.RelatedItemType;
import com.vaadin.ui.Grid;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AssociatedPageField<T> extends RelatedItemsField<T> {
    protected final String LINK_BASE_URI = "/.magnolia/admincentral#app:pages-app:" + SUB_APP_NAME + ";";

    public AssociatedPageField(FormDefinition<T> formDefinition, LocaleContext localeContext) {
        super(formDefinition, localeContext);
    }

    protected RelatedItemType getRelatedItemTypeFromConfiguration() {
        return RelatedItemType.PAGE;
    }

    protected List<GridItem> findAssociatedItems(T item, String storeProperty, RelatedItemType relatedItemType) {
        try {
            PageGridItem pageGridItem = new PageGridItem(getLang(), (Node) item);
            if (pageGridItem != null && StringUtils.isNotEmpty(pageGridItem.getPageUrl())) {
                return Collections.singletonList(new PageGridItem(getLang(), (Node) item));
            }
        } catch (Exception e) {
            log.error("Error while Associating Items : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    protected Grid createGrid() {
        Grid grid = new Grid(PageGridItem.class);
        grid.setColumns("pageUrl", "magnoliaPagePath");
        return grid;
    }

    protected String getLinkBaseUri() {
        return LINK_BASE_URI;
    }
}
