package com.tbscg.doosan.magnolia.dxp.workflow.service.configuration;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

public class PathMappingServiceConfiguration {

    protected static final String PATH_MAPPING_CONFIG_PATH = "/modules/dxp-workflow/pathMapping";

    public List<String> getMappableSites() throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(WorkflowConstant.CONFIG_WORKSPACE);
        final Property property = session.getProperty(PATH_MAPPING_CONFIG_PATH + "/mappableSites");

        return Arrays.stream(property.getString().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

}
