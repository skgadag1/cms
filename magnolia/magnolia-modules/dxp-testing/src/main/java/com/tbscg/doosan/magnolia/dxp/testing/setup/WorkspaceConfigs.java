package com.tbscg.doosan.magnolia.dxp.testing.setup;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceConfigs {

    private List<WorkspaceConfig> workspaceConfigs;

    public WorkspaceConfigs() {
        this.workspaceConfigs = new ArrayList<>();
    }

    public WorkspaceConfigs(List<WorkspaceConfig> workspaceConfigs) {
        this.workspaceConfigs = workspaceConfigs;
    }

    public List<WorkspaceConfig> getWorkspaceConfigs() {
        return workspaceConfigs;
    }

    public void setWorkspaceConfigs(List<WorkspaceConfig> workspaceConfigs) {
        this.workspaceConfigs = workspaceConfigs;
    }

    public void addWorkspaceConfig(WorkspaceConfig workspaceConfig){
        if (this.workspaceConfigs != null && workspaceConfig != null) {
            this.workspaceConfigs.add(workspaceConfig);
        }
    }
}
