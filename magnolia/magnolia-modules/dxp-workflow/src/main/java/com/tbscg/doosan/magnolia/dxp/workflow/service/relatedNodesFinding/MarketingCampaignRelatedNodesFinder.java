package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MarketingCampaignRelatedNodesFinder extends RelatedNodesFinder {

    @Override
    protected List<Node> findRelatedNodes(final String nodeUuid) {
        try {
            Node campaignNode = NodeUtil.getNodeByIdentifier(WorkflowConstant.MARKETING_CAMPAIGNS_WORKSPACE, nodeUuid);
            return Arrays.stream(campaignNode.getProperty("pagesField").getValues())
                    .map(this::getStringOfValueNoThrow)
                    .map(this::findWebsiteNode)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (Exception e) {
            log.error("Error while searching for related nodes", e);
            return Collections.emptyList();
        }
    }

    private Optional<Node> findWebsiteNode(final String pageId) {
        try {
            return Optional.of(NodeUtil.getNodeByIdentifier(WorkflowConstant.WEBSITE_WORKSPACE, pageId));
        } catch (Exception e) {
            log.error("Error while searching for related nodes", e);
            return Optional.empty();
        }
    }
}
