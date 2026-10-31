package com.tbscg.doosan.magnolia.product.catalog.field;

import com.google.common.collect.Streams;
import com.tbscg.doosan.magnolia.product.catalog.field.relateditems.GridItem;
import com.tbscg.doosan.magnolia.product.catalog.field.relateditems.RelatedItemType;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class RelatedItemsField<T> extends FormView<T> {

    private final String UUID = "${UUID}";
    private final String NODE_TYPE = "${NODE_TYPE}";
    private final String STORE_PROPERTY = "${STORE_PROPERTY}";
    protected final String SUB_APP_NAME = "${SUB_APP_NAME}";
    private final String SQL2_QUERY_STATEMENT = "SELECT [" + NODE_TYPE + "] FROM [nt:base] WHERE CONTAINS ('" + STORE_PROPERTY + "', '" + UUID + "')";
    protected final String LINK_BASE_URI = "/.magnolia/admincentral#app:product-catalog:" + SUB_APP_NAME + ";";

    private final LocaleContext localeContext;
    protected final RelatedItemsFieldDefinition<T> formDefinition;

    public RelatedItemsField(FormDefinition<T> formDefinition, LocaleContext localeContext) {
        super(formDefinition, localeContext);
        this.localeContext = localeContext;
        this.formDefinition = (RelatedItemsFieldDefinition<T>) formDefinition;
    }

    @Override
    public void populate(T item) {
        super.populate(item);
        final Layout layout = (Layout) super.asVaadinComponent();
        RelatedItemType relatedItemType = getRelatedItemTypeFromConfiguration();
        if(relatedItemType == null) {
            layout.addComponents(getErrorMsg("Related items type is not set in configuration"));
        }
        String storeProperty = getStorePropertyFromConfiguration();
        if(Strings.isBlank(storeProperty)) {
            layout.addComponents(getErrorMsg("Related items store property is not set in configuration"));
        }
        List<GridItem> gridItems = findAssociatedItems(item, storeProperty, relatedItemType);
        if (!gridItems.isEmpty()) {
            layout.addComponents(getGrid(gridItems, relatedItemType));
        } else {
            layout.addComponents(getErrorMsg("No related items found"));
        }
        layout.setSizeFull();
    }

    protected RelatedItemType getRelatedItemTypeFromConfiguration() {
        return RelatedItemType.fromString(Optional.ofNullable(formDefinition.getItemType()).orElse(Strings.EMPTY));
    }

    private String getStorePropertyFromConfiguration() {
        return Optional.ofNullable(formDefinition.getStoreProperty()).orElse(Strings.EMPTY);
    }

    private Component getErrorMsg(String msg) {
        return new Label(msg, ContentMode.HTML);
    }

    private String getItemUuid(T item) {
        try {
            return ((JcrNodeWrapper) item).getUUID();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Component getGrid(List<GridItem> gridItems, RelatedItemType relatedItemType) {
        Grid grid = createGrid();
        grid.setItems(gridItems);
        grid.setWidthFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addItemClickListener(event -> grid.getUI().getPage().open(buildUrl(((GridItem)event.getItem()).getPath(), relatedItemType), "_blank"));
        return grid;
    }

    protected Grid createGrid() {
        Grid grid = new Grid(GridItem.class);
        grid.setColumns("title", "path");
        return grid;
    }

    private String buildUrl(String path, RelatedItemType relatedItemType) {
        return getLinkBaseUri().replace(this.SUB_APP_NAME, relatedItemType.getSubAppName()) + path;
    }

    protected String getLinkBaseUri() {
        return LINK_BASE_URI;
    }

    protected List<GridItem> findAssociatedItems(T item, String storeProperty, RelatedItemType relatedItemType) {
        try {
            String uuid = getItemUuid(item);
            String statement = buildQueryStatement(uuid, storeProperty, relatedItemType);
            NodeIterator queryResult = getQueryResult(statement, relatedItemType);
            if (queryResult != null) {
                return (List<GridItem>) Streams.stream(queryResult)
                        .map(node -> mapResultToGridItem(node, item))
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("Error while Associating Items : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private GridItem mapResultToGridItem(Object node, T item) {
        return new GridItem((Node) node, getLang());
    }

    private NodeIterator getQueryResult(String statement, RelatedItemType relatedItemType) {
        NodeIterator result = null;
        try {
            Session session = MgnlContext.getJCRSession(relatedItemType.getWorkspace());

            if (session == null) {
                return null;
            }
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            if (queryManager == null) {
                return null;
            }
            log.debug("Executing query: " + statement);
            result = QueryUtil.search(relatedItemType.getWorkspace(), statement, "JCR-SQL2", relatedItemType.getNodeType());
            if (result == null) {
                return null;
            }

        } catch (InvalidQueryException e) {
            log.error("Error while querying items: {}", e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error("Repository error: {}", e.getMessage(), e);
        }
        return result;
    }

    private String buildQueryStatement(String uuid, String storeProperty, RelatedItemType relatedItemType) {
        String statement = StringUtils.replace(SQL2_QUERY_STATEMENT, UUID, uuid)
                .replace(STORE_PROPERTY, storeProperty)
                .replace(NODE_TYPE, relatedItemType.getNodeType());
        return statement;
    }

    protected String getLang() {
        if (Strings.isNotBlank(localeContext.getCurrent().getLanguage())) {
            return localeContext.getCurrent().getLanguage();
        } else {
            return Strings.EMPTY;
        }
    }

}
