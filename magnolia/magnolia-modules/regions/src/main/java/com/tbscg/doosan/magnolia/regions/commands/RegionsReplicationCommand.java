package com.tbscg.doosan.magnolia.regions.commands;

import com.tbscg.doosan.magnolia.dxp.core.util.EventConstants;
import com.tbscg.doosan.magnolia.dxp.core.util.JcrConstants;
import com.tbscg.doosan.magnolia.regions.RegionsModule;
import com.tbscg.doosan.magnolia.regions.replication.RegionReplicator;
import com.tbscg.doosan.magnolia.regions.replication.ReplicationException;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

/**
 * {@link MgnlCommand} to be executed exclusively by observation listeners that "replicate" an action from regions
 * workspace (given an event action) using configured replicators.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class RegionsReplicationCommand extends MgnlCommand {

    private static final String EVENT_KEY = "event";
    private final RegionsModule regionsConfig;

    @Inject
    public RegionsReplicationCommand(RegionsModule regionsConfig) {
        this.regionsConfig = regionsConfig;
    }

    @Override
    public boolean execute(Context context) {

        Event event = context.getAttribute(EVENT_KEY);

        String eventNodeType = this.getEventNodeType(event);

        if (eventNodeType != null) {
            boolean allExecSuccess = true;

            for (RegionReplicator regionReplicator : regionsConfig.getReplicators()) {

                allExecSuccess = this.executeReplicator(regionReplicator, event, eventNodeType);

            }

            return allExecSuccess;
        }

        return false;
    }

    private boolean executeReplicator(RegionReplicator regionReplicator, Event event, String eventNodeType) {

        if (regionReplicator !=  null) {

            switch (eventNodeType) {
                case RegionsConstants.NodeTypes.Website.NAME:
                    return this.executeSiteReplication(regionReplicator, event);
                case RegionsConstants.NodeTypes.Region.NAME:
                    return this.executeRegionReplication(regionReplicator, event);
                case RegionsConstants.NodeTypes.Language.NAME:
                    return this.executeLanguageReplication(regionReplicator, event);
                default:
                    return this.executeSharedPropertyReplication(regionReplicator, event);
            }
        }

        return false;
    }

    private boolean executeSharedPropertyReplication(RegionReplicator regionReplicator, Event event) {
        try {

            regionReplicator.updateSite(StringUtils.substringBeforeLast(event.getPath(), NodeUtil.PATH_SEPARATOR));

        } catch (Exception e) {
            log.error("Error when replicating shared properties", e);
            return false;
        }

        return true;
    }

    private boolean executeRegionReplication(RegionReplicator regionReplicator, Event event) {
        try {
            switch (event.getType()) {
                case Event.NODE_ADDED:
                    regionReplicator.addRegion(event.getPath());
                    break;
                case Event.NODE_MOVED:
                    regionReplicator.renameRegion((String) event.getInfo().get(EventConstants.SRC_ABS_PATH),
                            (String) event.getInfo().get(EventConstants.DEST_ABS_PATH));
                    break;

                case Event.NODE_REMOVED:
                    regionReplicator.removeRegion(event.getPath());
                    break;

                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_REMOVED:
                case Event.PROPERTY_ADDED:
                    regionReplicator.updateRegion(StringUtils.substringBeforeLast(event.getPath(), NodeUtil.PATH_SEPARATOR));
            }
        } catch (Exception e) {
            log.error("Error when replicating regions", e);
            return false;
        }

        return true;
    }

    private boolean executeSiteReplication(RegionReplicator regionReplicator, Event event) {
        try {
            switch (event.getType()) {

                case Event.NODE_ADDED:
                    regionReplicator.addSite(event.getPath());
                    break;

                case Event.NODE_MOVED:
                    regionReplicator.renameSite((String) event.getInfo().get(EventConstants.SRC_ABS_PATH),
                            (String) event.getInfo().get(EventConstants.DEST_ABS_PATH));
                    break;

                case Event.NODE_REMOVED:
                    regionReplicator.removeSite(event.getPath());
                    break;

                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_REMOVED:
                case Event.PROPERTY_ADDED:
                    regionReplicator.updateSite(StringUtils.substringBeforeLast(event.getPath(), NodeUtil.PATH_SEPARATOR));
            }
        } catch (Exception e) {
            log.error("Error when replicating website", e);
            return false;
        }

        return true;
    }

    private boolean executeLanguageReplication(RegionReplicator regionReplicator, Event event) throws ReplicationException {
        try {

            regionReplicator.updateRegion(StringUtils.substringBeforeLast(event.getPath(), NodeUtil.PATH_SEPARATOR));

        } catch (Exception e) {
            log.error("Error when replicating languages", e);
            return false;
        }

        return true;
    }


    private String getEventNodeType(Event event) {

        try {

            switch (event.getType()) {

                case Event.PROPERTY_REMOVED:
                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_ADDED:
                    return this.getPropertyNodeType(event.getPath());

                case Event.NODE_REMOVED:
                    return this.getNodeTypeByHierarchy(event.getPath());
            }

            String path = event.getPath();
            Node targetNode = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE).getNode(path);

            return targetNode.getPrimaryNodeType().getName();

        } catch (RepositoryException e) {
            log.error("Unable to get node type for event", e);
        }

        return null;
    }

    private String getPropertyNodeType(String propertyPath) {

        try {

            String parentNodePath = StringUtils.substringBeforeLast(propertyPath, NodeUtil.PATH_SEPARATOR);
            Node parentNode = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE).getNode(parentNodePath);

            return parentNode.getPrimaryNodeType().getName();

        } catch (Exception e) {
            log.error("Unable to get parent node type for property [{}]", propertyPath, e);
        }

        return null;
    }

    private String getNodeTypeByHierarchy(String nodePath) {

        String parentNodePath = StringUtils.substringBeforeLast(nodePath, NodeUtil.PATH_SEPARATOR);
        parentNodePath = parentNodePath.equals(StringUtils.EMPTY) ? NodeUtil.PATH_SEPARATOR : parentNodePath;

        try {

            Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
            Node parentNode = regionsSession.getNode(parentNodePath);

            switch (parentNode.getPrimaryNodeType().getName()) {

                case RegionsConstants.NodeTypes.Website.NAME:
                    return RegionsConstants.NodeTypes.Region.NAME;

                case RegionsConstants.NodeTypes.Region.NAME:
                    return RegionsConstants.NodeTypes.Language.NAME;

                case JcrConstants.NodeTypes.Root.NAME:
                    return RegionsConstants.NodeTypes.Website.NAME;

                default:
                    return NodeTypes.Content.NAME;
            }

        } catch (Exception e) {
            log.error("Unable to get node type", e);
        }

        return null;
    }
}
