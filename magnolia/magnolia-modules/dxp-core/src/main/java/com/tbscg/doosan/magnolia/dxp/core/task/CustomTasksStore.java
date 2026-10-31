package com.tbscg.doosan.magnolia.dxp.core.task;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.task.Task;
import info.magnolia.task.persistence.TaskPathGenerator;
import info.magnolia.task.persistence.TasksStoreImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CustomTasksStore extends TasksStoreImpl {
    @Inject
    public CustomTasksStore(SecuritySupport securitySupport, TaskPathGenerator taskPathGenerator) {
        super(securitySupport, taskPathGenerator);
    }

    @Override
    public List<Task> findTasksByUserAndStatus(String userName, List<Task.Status> statuses, Map<String, Boolean> sortCriteria, int limit, int offset) {
        final List<Task> items = super.findTasksByUserAndStatus(userName, statuses, sortCriteria, limit, offset);
        return items.stream()
                .filter(NodeUtil.distinctByKey(Task::getPath))
                .collect(Collectors.toList());
    }
}
