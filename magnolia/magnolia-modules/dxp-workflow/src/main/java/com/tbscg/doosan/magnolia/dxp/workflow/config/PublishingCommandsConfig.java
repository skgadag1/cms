package com.tbscg.doosan.magnolia.dxp.workflow.config;

import lombok.Data;

import java.util.List;

@Data
public class PublishingCommandsConfig {
    private String commandName;
    private String commandCatalog;
    private List<String> supportedOperations;
    private List<String> supportedApps;
}