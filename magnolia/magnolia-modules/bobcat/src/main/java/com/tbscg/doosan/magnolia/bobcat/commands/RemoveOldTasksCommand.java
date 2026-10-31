package com.tbscg.doosan.magnolia.bobcat.commands;

import com.tbscg.doosan.magnolia.bobcat.util.TaskNodesPredicate;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.*;
import java.time.LocalDate;

@Setter
@RequiredArgsConstructor
@Slf4j
public class RemoveOldTasksCommand extends BaseRepositoryCommand {

    private static final String TASKS_WORKSPACE = "tasks";
    private static final String ROOT_FOLDER_PATH = "/tasks";
    private int daysToExpire;

    @Override
    public boolean execute(Context context) throws Exception {
        try {
            Session session = MgnlContext.getJCRSession(TASKS_WORKSPACE);
            Node rootNode = session.getNode(ROOT_FOLDER_PATH);
            Iterable<Node> expiredTasksNodes = getExpiredTasksNodes(rootNode);
            removeNodes(expiredTasksNodes);
            session.save();
            return true;
        } catch (Exception e) {
            log.error("Error while removing old tasks", e);
            return false;
        }
    }

    private Iterable<Node> getExpiredTasksNodes(Node rootNode) throws Exception {
        LocalDate expirationDate = getFirstDateBeforeExpiration(daysToExpire);
        return info.magnolia.jcr.util.NodeUtil.collectAllChildren(rootNode, new TaskNodesPredicate(expirationDate));
    }

    private LocalDate getFirstDateBeforeExpiration(int daysToExpire) {
        return LocalDate.now().minusDays(daysToExpire);
    }

    private void removeNodes(Iterable<Node> nodeList) {
        log.info("Start of removing old tasks");
        nodeList.forEach(NodeUtil::remove);
    }
}
