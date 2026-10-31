package com.tbscg.doosan.magnolia.dxp.testing;

import com.tbscg.doosan.magnolia.dxp.testing.setup.*;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * DXP Testing module class.
 *
 * @author Giovanni José López Reza
 */
@Slf4j
@Singleton
public class DxpTestingModule implements ModuleLifecycle {

    public static final String MODULE_NAME = "dxp-testing";
    private SelenideCleanerConfig selenideCleanerConfig;

    private List<WorkspaceConfig> workspaceConfigs;
    private List<String> patterns;

    @Inject
    public DxpTestingModule() {
        workspaceConfigs = new ArrayList<>();
        patterns = new ArrayList<>();
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", MODULE_NAME);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} stopping.", MODULE_NAME);
    }

    private void retrieveConfigProperties(){
        if (selenideCleanerConfig != null) {
            retrieveWorkspaces();
            retrievePatterns();
        }
    }


    private void retrieveWorkspaces() {

        WorkspaceConfigs workspaceConfigs = selenideCleanerConfig.getWorkspaceConfigs();
        if (workspaceConfigs != null) {

            List<WorkspaceConfig> workspaceConfigList = workspaceConfigs.getWorkspaceConfigs();
            if (workspaceConfigList != null) {
                this.workspaceConfigs.clear();
                this.workspaceConfigs.addAll(workspaceConfigList);
            }
        }
    }

    private void retrievePatterns() {

        PatternConfigs patternConfigs = selenideCleanerConfig.getPatternConfigs();
        if (patternConfigs != null) {

            List<String> patternConfigList = patternConfigs.getPatterns();
            if (patternConfigList != null) {
                patterns.clear();
                patterns.addAll(patternConfigList);
            }
        }
    }

    public List<WorkspaceConfig> getWorkspaceConfigs() {
        return workspaceConfigs;
    }

    public void setWorkspaceConfigs(List<WorkspaceConfig> workspaces) {
        this.workspaceConfigs = workspaces;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public SelenideCleanerConfig getSelenideCleanerConfig() {
        return selenideCleanerConfig;
    }

    public void setSelenideCleanerConfig(SelenideCleanerConfig selenideCleanerConfig) {
        this.selenideCleanerConfig = selenideCleanerConfig;
        this.retrieveConfigProperties();
    }

    public void addWorkspaceConfig(WorkspaceConfig workspaceConfig){
        if (this.workspaceConfigs != null && workspaceConfig != null) {
            this.workspaceConfigs.add(workspaceConfig);
        }
    }
}
