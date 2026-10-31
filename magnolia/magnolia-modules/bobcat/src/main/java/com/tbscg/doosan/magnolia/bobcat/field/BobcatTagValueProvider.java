package com.tbscg.doosan.magnolia.bobcat.field;

import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.html.mapper.MapToTokensElement;
import com.vaadin.data.ValueProvider;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;


/**
 * Value Provider for facet types used in column definition.
 */
@Slf4j
public class BobcatTagValueProvider implements ValueProvider<JcrNodeWrapper, Object> {
    public static final String ITEM_TYPE = "nt:base";
    private final String JCR_LANGUAGE = "JCR-SQL2";
    private final String UUID = "${UUID}";
    private final String WORKSPACE = "dxpTags";
    private final String SQL2_QUERY_STATEMENT = "SELECT * FROM ['"+ITEM_TYPE+"'] WHERE [jcr:uuid] = '" + UUID + "'";

    @Override
    public Object apply(final JcrNodeWrapper jcrNode) {
        try {
            Collection<String> tagNames = new LinkedList<>();

                NodeIterator pageNodeItr = jcrNode.getNodes();
                while(pageNodeItr.hasNext()) {

                    Node pageNode = pageNodeItr.nextNode();
                    if (!pageNode.getName().equals("dxpTags")) {
                        continue;
                    }
                    NodeIterator tagNodes =  pageNode.getNodes();
                    while(tagNodes.hasNext()){
                        Node tagChild = tagNodes.nextNode();
                        String tagUuid = PropertyUtil.getString(tagChild, "dxpTag");
                        log.debug("dxp tagUuid : {}", tagUuid);
                        String statement = buildQueryStatement(tagUuid);
                        NodeIterator result = getQueryResult(statement);

                        while(result.hasNext()) {
                            String tagName = result.nextNode().getName();
                            if (Objects.equals(tagName, "")) {
                                continue;
                            }
                            tagNames.add(tagName);
                        }
                    }
                }

            return MapToTokensElement.create(tagNames);
        } catch (RepositoryException e) {
            log.error("Failed to get dxp Tags for the page. {}", e);
            return null;
        }
    }

    private String buildQueryStatement(String uuid) {
        return StringUtils.replace(SQL2_QUERY_STATEMENT, UUID, uuid);
    }
    private NodeIterator getQueryResult(String statement) {
        NodeIterator result = null;
        try {
            Session session = MgnlContext.getJCRSession(WORKSPACE);
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            log.debug("Executing query: {}", statement);
            result = QueryUtil.search(WORKSPACE, statement, JCR_LANGUAGE, ITEM_TYPE);

        } catch (InvalidQueryException e) {
            log.error("Error while querying items: {}", e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error("Repository error: {}", e.getMessage(), e);
        }
        return result;
    }

}
