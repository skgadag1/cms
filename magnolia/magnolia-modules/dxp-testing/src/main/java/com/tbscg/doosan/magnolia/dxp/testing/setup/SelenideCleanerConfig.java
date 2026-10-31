package com.tbscg.doosan.magnolia.dxp.testing.setup;

public class SelenideCleanerConfig {

    private WorkspaceConfigs workspaceConfigs;
    private PatternConfigs patternConfigs;

    public SelenideCleanerConfig() {
    }

    public WorkspaceConfigs getWorkspaceConfigs() {
        return workspaceConfigs;
    }

    public void setWorkspaceConfigs(WorkspaceConfigs workspaceConfigs) {
        this.workspaceConfigs = workspaceConfigs;
    }

    public PatternConfigs getPatternConfigs() {
        return patternConfigs;
    }

    public void setPatternConfigs(PatternConfigs patternConfigs) {
        this.patternConfigs = patternConfigs;
    }
}
