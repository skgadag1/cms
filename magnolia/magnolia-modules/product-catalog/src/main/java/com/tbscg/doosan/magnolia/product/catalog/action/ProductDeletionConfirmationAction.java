package com.tbscg.doosan.magnolia.product.catalog.action;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.vaadin.ui.*;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.action.ConfirmationAction;
import info.magnolia.ui.contentapp.action.ConfirmationActionDefinition;
import info.magnolia.ui.contentapp.browser.ActionExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ProductDeletionConfirmationAction<ProductDeletionConfirmationActionDefinition extends ConfirmationActionDefinition> extends ConfirmationAction<ProductDeletionConfirmationActionDefinition, Node> {

    private static final String JCR_PRODUCT_NODE_TYPE = "dxp:product";
    private static final String ATTACHMENT_PRODUCT_NODE_TYPE = "mgnl:page";
    private static final String WORKSPACE = "website";
    private static final String SQL_LANGUAGE = "JCR-SQL2";
    private static final String UUID = "${UUID}";
    private static final String SQL2_QUERY_STATEMENT = "SELECT [mgnl:page] FROM [nt:base] WHERE CONTAINS ('model', '" + UUID + "')";
    private static final String NO_RELATED_PAGES_TITLE = "Are you sure you want to delete this product?";
    private static final String SOME_RELATED_PAGES_TITLE = "Before deletion you should remove products from pages:";
    private static final String PATH = "${PATH}";
    private static final String LINK_BASE_URI = "<a href=\" /.magnolia/admincentral#app:pages-app:browser;" + PATH + "\" target=\"_blank\">" + PATH + "</a>" ;
    private static final String NEW_LINE = "<br>";
    private static String message;
    @Inject
    public ProductDeletionConfirmationAction(ProductDeletionConfirmationActionDefinition definition, UiContext uiContext, ActionExecutionService actionExecutionService, SimpleTranslator i18n, ValueContext valueContext) {
        super(definition, uiContext, actionExecutionService, i18n, valueContext);
    }

    @Override
    public void execute() throws ActionExecutionException {
        createMessage();
        Window alertWindow = createWindow();
        alertWindow.addStyleName("product-deletion");
        buildAndOpen(alertWindow);
    }

    private Window createWindow(){
        return AlertBuilder.confirmDialog(getTitle())
                .withLevel(Notification.Type.WARNING_MESSAGE)
                .withBody(message)
                .withConfirmButtonCaption("Delete")
                .withDeclineButtonCaption("Cancel")
                .withConfirmationHandler(() -> {
                    if (getDefinition().getSuccessActionName() != null) {
                        getActionExecutionService().executeAction(getDefinition().getSuccessActionName(), getValueContext().current().nullableValue());
                    }
                }).build();
    }

    private String getTitle() {
        return !message.isEmpty() ? SOME_RELATED_PAGES_TITLE : NO_RELATED_PAGES_TITLE;
    }

    private void createMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        resolveTargetItems().forEach(node -> {
            try {
                getRelatedPages(node.getIdentifier()).forEach(path ->
                    messageBuilder.append(buildUrl(path)).append(NEW_LINE)
                );
            } catch (Exception e) {
                log.error("Error while retrieving related pages", e);
                throw new RuntimeException(e);
            }
        });
        message = messageBuilder.toString();
    }

    public String buildUrl(String parameter) {
        return StringUtils.replace(LINK_BASE_URI, PATH, parameter);
    }

    private List<String> getRelatedPages(final String uuid) throws RepositoryException {
        final String statement = StringUtils.replace(SQL2_QUERY_STATEMENT, UUID, uuid);
        return NodeUtil.asPathsList(getQueryResult(statement));
    }

    private NodeIterator getQueryResult(final String statement) throws RepositoryException {
        log.debug("Executing query: " + statement);
        return QueryUtil.search(WORKSPACE, statement, SQL_LANGUAGE, ATTACHMENT_PRODUCT_NODE_TYPE);
    }

    protected Collection<Node> resolveTargetItems() {
        return getValueContext().get()
                .map(node -> {
                    try {
                        return NodeUtil.getNodesWithParent(node, JCR_PRODUCT_NODE_TYPE);
                    } catch (RepositoryException e) {
                        log.error("Error while getting nodes with parent", e);
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void buildAndOpen(Window window) {
        UI.getCurrent().addWindow(window);
        window.focus();
    }
}
