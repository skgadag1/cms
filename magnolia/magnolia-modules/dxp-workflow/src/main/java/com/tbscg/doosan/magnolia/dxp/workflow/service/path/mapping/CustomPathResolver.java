package com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping;

import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMapping;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.CustomPathMappingException;

import javax.jcr.Node;

public interface CustomPathResolver {
    PathMapping resolveCustomPaths(Node node) throws CustomPathMappingException;
}
