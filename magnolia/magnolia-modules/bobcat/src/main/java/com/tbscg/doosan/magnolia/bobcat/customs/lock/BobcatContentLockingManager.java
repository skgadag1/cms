package com.tbscg.doosan.magnolia.bobcat.customs.lock;

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.locking.ContentLockingModule;
import info.magnolia.locking.core.DefaultContentLockingManager;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;

@Log4j2
public class BobcatContentLockingManager extends DefaultContentLockingManager {

    private final ContentLockingModule contentLockingModule;

    @Inject
    public BobcatContentLockingManager(ContentLockingModule contentLockingModule) {
        super(contentLockingModule);
        this.contentLockingModule = contentLockingModule;
    }

    @Override
    public synchronized String unlock(Node node, String currentUser) throws RepositoryException {
        Workspace workspace = node.getSession().getWorkspace();
        LockManager lockManager = workspace.getLockManager();
        Lock lock = lockManager.getLock(node.getPath());
        String lockOwner = lock.getLockOwner();
        Node lockStoreNode = this.getLockStoreNode(workspace.getName(), node.getIdentifier());
        String lockToken = PropertyUtil.getString(lockStoreNode, "lockToken");
        try {
            this.contentLockingModule.removeLockToken(workspace.getName(), lockToken);
        } catch (LockException e) {
            log.trace("Lock token {} was not found in {} lock manager.", lockToken, workspace.getName());
        }
        lockManager.addLockToken(lockToken);
        lockManager.unlock(node.getPath());
        log.debug("Node {} unlocked with token {}", node.getPath(), lock.getLockToken());
        lockStoreNode.remove();
        lockStoreNode.getSession().save();
        return lockOwner;
    }
}
