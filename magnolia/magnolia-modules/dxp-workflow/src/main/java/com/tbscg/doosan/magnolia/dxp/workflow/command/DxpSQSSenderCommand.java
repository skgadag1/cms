package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.workflow.DxpWorkflow;
import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsServiceImpl;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DxpSQSSenderCommand extends MgnlCommand {

    private static final String PATH = "path";

    private final SqsService sqsService;

    public DxpSQSSenderCommand(DxpWorkflow regionsConfig) {
        this.sqsService = new SqsServiceImpl(regionsConfig.getSqsClient(), regionsConfig.getSqsQueueUrl());
    }

    @Override
    public boolean execute(Context context) throws Exception {
        String path = (String) context.getAttributes().get(PATH);
        if (path == null) {
            log.warn("No path attribute found in context");
            return false;
        }

        try {
            sqsService.sendMessage(path);
            return true;
        } catch (Exception e) {
            log.error("Error sending message to SQS", e);
            throw e;
        }
    }
}
