package com.tbscg.doosan.magnolia.dxp.core.setup.task;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.util.ExceptionUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.DefaultRepositoryManager;
import info.magnolia.repository.RepositoryManager;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for registering Content apps.
 * It is split into 2 main functionalities
 *  - Registering a new workspace (i.e. for a new content of the Content App).
 *  - Adding "ALL" permissions to superuser role.
 *  TODO: can be extended so to register custom nodeTypes.
 *
 * @author Alejandro Puig Escobar
 * */

public class RegisterWorkspaceTask extends AbstractTask {

    private static final String MGNL_REPOSITORY = "magnolia";
    private String workspaceName;

    public RegisterWorkspaceTask(final String workspaceName) {
        super("Register workspace: " + workspaceName, "Registers workspace " + workspaceName
                + " and adds default permissions to superuser");
        this.workspaceName = workspaceName;
    }

    protected void doExecute(InstallContext ctx) throws UnsupportedOperationException, Exception {
        //create application workspace
        createWorkspace(workspaceName);

        //add permissions to superuser role
        long permissions = Permission.ALL;
        List<String> paths = Arrays.asList("/", "/*");
        addRolePermissions(AccessDefinition.DEFAULT_SUPERUSER_ROLE, workspaceName, permissions, paths);
    }

    private void createWorkspace(String workspaceName)throws RepositoryException{
        DefaultRepositoryManager repositoryManager =
                (DefaultRepositoryManager) Components.getComponent(RepositoryManager.class);
        //check if Magnolia repository is registered
        if(repositoryManager.hasRepository(MGNL_REPOSITORY)){
            //check if there's an already registered workspace with @workspaceName
            if (!repositoryManager.hasWorkspace(workspaceName)) {
                repositoryManager.createWorkspace(MGNL_REPOSITORY, workspaceName);
            }
        }
    }

    private void addRolePermissions(String roleName, String workspaceName, long permission, List<String> paths) {
        RoleManager roleManager = Components.getComponent(SecuritySupport.class).getRoleManager();
        Role role = roleManager.getRole(roleName);
        for (String path : paths) {
            roleManager.addPermission(role, workspaceName, path, permission);
        }
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            this.doExecute(installContext);
        } catch (Exception e) {
            throw new TaskExecutionException("Could not execute task: " + ExceptionUtil.exceptionToWords(e), e);
        }
    }
}
