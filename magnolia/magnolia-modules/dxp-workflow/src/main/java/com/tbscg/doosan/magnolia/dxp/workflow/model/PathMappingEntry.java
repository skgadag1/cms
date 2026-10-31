package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class PathMappingEntry {

    private String uuid;

    private String jcrPath;

    private String customPath;

    /**
     * Subtree completely hidden
     */
    private boolean hidden;

    /**
     * Node not in children path
     */
    private boolean removed;

    private boolean activated;
}
