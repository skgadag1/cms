package com.tbscg.doosan.magnolia.dxp.workflow;

import com.tbscg.doosan.magnolia.dxp.core.action.ui.*;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.config.PublishingCommandsConfig;
import com.tbscg.doosan.magnolia.dxp.workflow.event.*;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is optional and represents the configuration for the dxp-workflow module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/dxp-workflow</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
@Slf4j
@Singleton
public class DxpWorkflow implements ModuleLifecycle {
    private static final String MODULE_NAME = "workflow";
    private final SsmClient ssmClient;
    @Getter
    private final SqsClient sqsClient;
    private final EventBus eventBus;
    @Getter
    private String cmsIndexerTableName;
    @Getter
    private String indexerTableParam;
    @Getter
    private String sqsQueueParamName;
    @Getter
    private String sqsQueueUrl;
    @Setter
    @Getter
    private Map<String, PublishingCommandsConfig> publishingCommandsConfig = new LinkedHashMap<>();

    @Inject
    public DxpWorkflow(final Provider<SsmClient> awsSimpleSystemsManagementProvider, @Named(SystemEventBus.NAME) EventBus eventBus) {
        this.ssmClient = awsSimpleSystemsManagementProvider.get();
        this.eventBus = eventBus;
        this.sqsClient = SqsClient.create();
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", DxpWorkflow.MODULE_NAME);
        NavigationEventHandler eventHandler = new NavigationEventHandler();
        DxpActionEventHandler bobcatPagesEventHandler = new BobcatPagesEventHandler();
        DxpActionEventHandler dealernetPageEventHandler = new DealernetPagesEventHandler();
        DxpActionEventHandler defaultEventHandler = new DefaultEventHandler();
        DxpActionEventHandler configEventHandler = new ConfigEventHandler();
        addHandlersToEventBus(eventHandler);
        addHandlersToEventBus(bobcatPagesEventHandler);
        addHandlersToEventBus(dealernetPageEventHandler);
        addHandlersToEventBus(defaultEventHandler);
        addHandlersToEventBus(configEventHandler);
    }

    private void addHandlersToEventBus(DxpActionEventHandler eventHandler) {
        this.eventBus.addHandler(DxpActionCommittedEvent.class, eventHandler);
        this.eventBus.addHandler(DxpActionDeletedEvent.class, eventHandler);
        this.eventBus.addHandler(DxpActionPublishedEvent.class, eventHandler);
        this.eventBus.addHandler(DxpActionUnpublishedEvent.class, eventHandler);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} stopping.", DxpWorkflow.MODULE_NAME);
    }

    public void setIndexerTableParam(final String indexerTableParam) {
        this.indexerTableParam = indexerTableParam;
        resolveCmsIndexerTableName();
    }

    public void setSqsQueueParamName(final String sqsQueueParamName) {
        this.sqsQueueParamName = sqsQueueParamName;
        resolveSqsQueueUrl();
    }

    private void resolveSqsQueueUrl() {
        this.sqsQueueUrl = resolveStringParameter(this.sqsQueueParamName);
        log.info("SQS queue URL resolved: {}", this.sqsQueueUrl);
    }

    private void resolveCmsIndexerTableName() {
        this.cmsIndexerTableName = resolveStringParameter(this.indexerTableParam);
    }

    private String resolveStringParameter(String parameterName) {
        log.debug("Resolving SSM parameter: {}", parameterName);
        final GetParameterRequest request = GetParameterRequest.builder().name(parameterName)
                .build();
        final GetParameterResponse result = TracingUtil.callWithTracing(traceID -> this.ssmClient.getParameter(request));
        final String value = result.parameter().value();
        log.debug("Param {} resolved with value: {}", parameterName, value);
        return value;
    }
}
