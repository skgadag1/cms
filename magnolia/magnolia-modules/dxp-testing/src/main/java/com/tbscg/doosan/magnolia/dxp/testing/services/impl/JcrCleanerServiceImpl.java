package com.tbscg.doosan.magnolia.dxp.testing.services.impl;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.testing.DxpTestingModule;
import com.tbscg.doosan.magnolia.dxp.testing.services.JcrCleanerService;
import com.tbscg.doosan.magnolia.dxp.testing.setup.WorkspaceConfig;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.annotation.LazySingleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Default implementation for {@link JcrCleanerService}.
 *
 * @author Giovanni José López Reza
 */
@Slf4j
@LazySingleton
public class JcrCleanerServiceImpl implements JcrCleanerService {
    private final DxpTestingModule dxpTestingModule;
    private final String SQL2_QUERY_STATEMENT = "SELECT * FROM [${nodeType}]";
    private final String NODE_TYPE_PN = "${nodeType}";

    @Inject
    public JcrCleanerServiceImpl(DxpTestingModule dxpTestingModule) {
        this.dxpTestingModule = dxpTestingModule;
    }

    @Override
    public void cleanSelenideItems() {
        log.info("Cleaning Selenide Items start.");

        Preconditions.checkNotNull(this.dxpTestingModule,"Unable to retrieve DXP Testing module.");
        Preconditions.checkNotNull(this.dxpTestingModule.getSelenideCleanerConfig(), "No configuration found for selenide cleaner.");
        Preconditions.checkArgument(this.dxpTestingModule.getPatterns().size() > 0, "There has to be at least one configured pattern.");
        Preconditions.checkArgument(this.dxpTestingModule.getWorkspaceConfigs().size() > 0, "There has to be at least one configured workspace.");

        // Cleaning all the defined workspaces
        for (WorkspaceConfig workspaceConfig: dxpTestingModule.getWorkspaceConfigs()) {
            this.cleanWorkspace(workspaceConfig.getWorkspace(), workspaceConfig.getNodeType());
        }

        log.info("Cleaning Selenide Items end.");
    }

    private void cleanWorkspace(String workspace, String nodeType) {
        log.debug("Cleaning Workspace: {}", workspace);

        Preconditions.checkArgument(StringUtils.isNotBlank(workspace), "Workspace can not be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(nodeType), "Node Type can not be empty.");

        try {
            Session session = MgnlContext.getJCRSession(workspace);
            List<Node> selenideItems = searchSelenideItems(session, nodeType);
            log.info("Found a total of {} selenide items.", selenideItems.size());

            for (Node node : selenideItems) {
                NodeUtil.remove(node);
            }

            session.save();

            if (session.isLive()) {
                session.logout();
            }
        } catch (RepositoryException e) {
            log.error("Unable to get JCR session: {}", e.getMessage(), e);
        }
    }

    private List<Node> searchSelenideItems(Session websiteSession, String nodeType){
        try {
            if (websiteSession != null) {
                QueryManager queryManager = websiteSession.getWorkspace().getQueryManager();

                if (queryManager != null) {
                    String statement = StringUtils.replace(SQL2_QUERY_STATEMENT, NODE_TYPE_PN, nodeType);
                    Query query = queryManager.createQuery(statement, Query.JCR_SQL2);

                    if (query != null) {
                        QueryResult queryResult = query.execute();

                        if (queryResult != null) {
                            RowIterator rowIterator = queryResult.getRows();
                            List<Node> selenideItems = new ArrayList<>();

                            while (rowIterator.hasNext()) {
                                Row currentRow = rowIterator.nextRow();
                                Node node = currentRow.getNode();

                                if (isSelenideItem(node)) {
                                    log.debug("Found selenide item: {}", node.getPath());
                                    selenideItems.add(node);
                                }
                            }
                            return selenideItems;
                        }
                    }
                }
            }
        } catch (InvalidQueryException e) {
            log.error("Error while querying selenide items: {}", e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error("Repository error: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private boolean isSelenideItem(Node node){
        if (node != null) {
            return this.handleIsSelenideItem(node);
        }
        return false;
    }

    private boolean handleIsSelenideItem(Node node) {
        try {
            // Trying all the defined patterns
            for (String pattern: dxpTestingModule.getPatterns()) {
                // Checking if at least one of them matches
                if (Pattern.matches(pattern, node.getName())) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            log.error("Unable to get node name: {}", e.getMessage(), e);
        }
        return false;
    }
}
