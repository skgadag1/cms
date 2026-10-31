package com.tbscg.doosan.magnolia.dxp.core.setup.task;

import com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.tbscg.doosan.magnolia.dxp.core.util.DxpPreconditions.checkNotBlank;

@Slf4j
public class EnvironmentAwareTask extends AbstractTask {
    private final String targetInstanceType;
    private final String targetEnvironment;
    private final Task task;

    public EnvironmentAwareTask(String taskName,
                                String taskDescription,
                                String targetInstanceType,
                                String targetEnvironment,
                                Task task) {
        super(taskName, taskDescription);
        this.targetInstanceType = targetInstanceType;
        this.targetEnvironment = targetEnvironment;
        this.task = task;
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        String instanceType = System.getProperty(EnvironmentConstants.INSTANCE_TYPE, EnvironmentConstants.COMMON_INSTANCE);
        checkNotBlank(instanceType, "System property '%s' must be set".formatted(EnvironmentConstants.INSTANCE_TYPE));

        String environment = System.getProperty(EnvironmentConstants.ENVIRONMENT, EnvironmentConstants.SHARED_ENVIRONMENT);
        checkNotBlank(environment, "System property '%s' must be set".formatted(EnvironmentConstants.ENVIRONMENT));

        checkNotNull(task, "Task must not be null");

        if (isTargetEnvironment(environment) && isTargetInstanceType(instanceType)) {
            task.execute(installContext);
        } else {
            log.info("Skipping task [{}] as it is not applicable for instance type [{}] and environment [{}]",
                    getName(), instanceType, environment);
        }
    }

    private boolean isTargetEnvironment(String environment) {
        return environment.equals(targetEnvironment) || EnvironmentConstants.SHARED_ENVIRONMENT.equals(targetEnvironment);
    }

    private boolean isTargetInstanceType(String instanceType) {
        return instanceType.equals(targetInstanceType) || EnvironmentConstants.COMMON_INSTANCE.equals(targetInstanceType);
    }
}
