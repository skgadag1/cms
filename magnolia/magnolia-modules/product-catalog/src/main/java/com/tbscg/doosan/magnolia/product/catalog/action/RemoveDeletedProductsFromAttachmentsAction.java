package com.tbscg.doosan.magnolia.product.catalog.action;

import com.machinezoo.noexception.Exceptions;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.CommandAction;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RemoveDeletedProductsFromAttachmentsAction extends CommandAction<Node, RemoveDeletedProductsFromAttachmentsActionDefinition> {

    private static final String JCR_PRODUCT_NODE_TYPE = "dxp:product";
    private static final String ATTACHMENT_PRODUCT_NODE_TYPE = "mgnl:contentNode";
    private static final String WORKSPACE = "attachments";
    private static final String SQL_LANGUAGE = "JCR-SQL2";
    private static final String UUID = "${UUID}";
    private static final String SQL2_QUERY_STATEMENT = "SELECT [mgnl:contentNode] FROM [nt:base] WHERE CONTAINS ('product', '" + UUID + "')";

    @Inject
    RemoveDeletedProductsFromAttachmentsAction(RemoveDeletedProductsFromAttachmentsActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor);
    }

    @Override
    public boolean executeCommand(Node node) {
        try {
            return deleteProductFromAttachments(node.getIdentifier());
        } catch (Exception e) {
            log.error("Error while deleting product from attachments", e);
            return false;
        }
    }

    private boolean deleteProductFromAttachments(final String uuid) throws Exception {
        final List<Node> productsToDelete = getProductsToDelete(uuid);
        productsToDelete.forEach(this::removeNode);
        return true;
    }

    private void removeNode(Node node) {
        try{
            log.info("Removing node: " + node.getPath());
            Session session = node.getSession();
            node.remove();
            session.save();
        }catch (Exception e){
            log.error("Error while removing node: " + node, e);
        }
    }

    private List<Node> getProductsToDelete(final String uuid) throws RepositoryException {
        final String statement = StringUtils.replace(SQL2_QUERY_STATEMENT, UUID, uuid);
        return NodeUtil.asList(getQueryResult(statement));
    }

    private NodeIterator getQueryResult(final String statement) throws RepositoryException {
        log.debug("Executing query: " + statement);
        return QueryUtil.search(WORKSPACE, statement, SQL_LANGUAGE, ATTACHMENT_PRODUCT_NODE_TYPE);
    }

    @Override
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
}
