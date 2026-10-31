package com.tbscg.doosan.magnolia.dxp.workflow.filters;

import com.tbscg.doosan.magnolia.dxp.workflow.DxpWorkflow;
import com.tbscg.doosan.magnolia.dxp.workflow.config.PublishingCommandsConfig;
import info.magnolia.commands.CommandsManager;
import info.magnolia.publishing.dispatcher.Dispatcher;
import info.magnolia.publishing.receiver.filter.PublicationFilter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
public class DxpPublicationFilter extends PublicationFilter {

    private final DxpWorkflow dxpWorkflow;
    private final CommandsManager commandsManager;

    @Inject
    public DxpPublicationFilter(Dispatcher dispatcher, DxpWorkflow dxpWorkflow, CommandsManager commandsManager) {
        super(dispatcher);
        this.dxpWorkflow = dxpWorkflow;
        this.commandsManager = commandsManager;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        super.doFilter(request, response, chain);

        if (response.getStatus() != HttpServletResponse.SC_NO_CONTENT) {
            return;
        }

        try {
            handleRequest(request);
        } catch (Exception e) {
            log.error("Error during DXP custom command execution", e);
        }
    }

    private void handleRequest(HttpServletRequest request) {
        String path = request.getHeader("mgnlexchangepath");
        String operation = request.getHeader("mgnlexchangeaction");

        if (path == null || operation == null) {
            log.warn("Missing required headers: mgnlexchangepath or mgnlexchangeaction");
            return;
        }

        String app = extractAppFromPath(path);

        dxpWorkflow.getPublishingCommandsConfig().values().stream()
                .filter(config -> isSupported(config, app, operation))
                .forEach(config -> executeCommand(config, path, app, operation));
    }

    private String extractAppFromPath(String path) {
        String[] pathSegments = path.split("/");
        if (pathSegments.length > 1) {
            return pathSegments[1];
        } else {
            log.warn("Invalid path format: {}", path);
            return "";
        }
    }

    private boolean isSupported(PublishingCommandsConfig config, String app, String operation) {
        return config.getSupportedApps().contains(app) && config.getSupportedOperations().contains(operation);
    }

    private void executeCommand(PublishingCommandsConfig config, String path, String app, String operation) {
        try {
            log.debug("Executing command: {} for app {} and operation {}", config.getCommandName(), app, operation);
            commandsManager.executeCommand(config.getCommandCatalog(), config.getCommandName(), Collections.singletonMap("path", path));
        } catch (Exception e) {
            log.error("Error executing command for app {} and operation {}", app, operation, e);
            throw new RuntimeException(e);
        }
    }
}
