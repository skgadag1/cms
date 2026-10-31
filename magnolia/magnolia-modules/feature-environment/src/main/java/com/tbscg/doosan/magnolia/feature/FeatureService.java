package com.tbscg.doosan.magnolia.feature;

import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import lombok.extern.slf4j.Slf4j;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;

@Slf4j
public class FeatureService {

	private static final String FLAG_PROPERTY_NAME = "installed";
	private static final Boolean FLAG_PROPERTY_VALUE = true;

	public void markFeatureInstalled(InstallContext ctx, String featureId) throws RepositoryException {
		final Node moduleNode = ctx.getOrCreateCurrentModuleNode();
		final Node featureNode = NodeUtil.createPath(moduleNode, featureId, NodeTypes.Content.NAME);
		PropertyUtil.setProperty(featureNode, FLAG_PROPERTY_NAME, FLAG_PROPERTY_VALUE);
		log.debug("feature flag has been set for {}", featureId);
	}

	public boolean featureNeedsBootstrapping(InstallContext ctx, String featureId) {
		if (isBlank(featureId)) {
			log.debug("feature environment not specified, no need for additional bootstrapping");
			return false;
		}
		try {
			return !isFeatureInstalled(ctx, featureId);
		} catch (RepositoryException e) {
			throw new RuntimeException("error occurred when checking feature environment installation flag", e);
		}
	}

	private boolean isFeatureInstalled(InstallContext ctx, String featureId) throws RepositoryException {
		if (ctx.hasModulesNode()) {
			final Node moduleNode = ctx.getOrCreateCurrentModuleNode();
			if (moduleNode.hasNode(featureId)) {
				log.debug("feature node for {} is present", featureId);
				Node featureNode = moduleNode.getNode(featureId);
				Object flag = PropertyUtil.getPropertyValueObject(featureNode, FLAG_PROPERTY_NAME);
				log.debug("feature install flag value: {}", flag);
				return FLAG_PROPERTY_VALUE.equals(flag);
			} else {
				log.debug("feature node not present");
			}
		}
		return false;
	}
}
