package com.tbscg.doosan.magnolia.dxp.testing.setup;

public class WorkspaceConfig {

    private String workspace;
    private String nodeType;

    public WorkspaceConfig() {
    }

    public WorkspaceConfig(String workspace, String nodeType) {
        this.workspace = workspace;
        this.nodeType = nodeType;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

}
