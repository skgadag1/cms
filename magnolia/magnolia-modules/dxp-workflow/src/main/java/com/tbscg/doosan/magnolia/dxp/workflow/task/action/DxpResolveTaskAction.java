package com.tbscg.doosan.magnolia.dxp.workflow.task.action;

import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.admincentral.shellapp.pulse.task.action.ResolveTaskAction;
import info.magnolia.ui.admincentral.shellapp.pulse.task.action.ResolveTaskActionDefinition;
import info.magnolia.ui.api.shell.Shell;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DxpResolveTaskAction extends ResolveTaskAction {
    private final Set<Task> tasks;
    private final TasksManager taskManager;

    public DxpResolveTaskAction(ResolveTaskActionDefinition definition, TasksManager taskManager, Shell shell, ValueContext<Task> valueContext) {
        super(definition, taskManager, shell, valueContext);
        this.taskManager = taskManager;
        this.tasks = valueContext.get().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    protected void executeTask(TasksManager taskManager, Task _task) {
        log.debug("About to execute Tasks [{}]", this.tasks);
        for (Task task : this.tasks) {
            try {
                super.executeTask(this.taskManager, task);
            } catch (Exception error) {
                log.error("An error occurred while trying to execute task [{}]", task, error);
                this.getShell().showError("Error: " + error.getMessage(), error);
            }
        }
    }
}
