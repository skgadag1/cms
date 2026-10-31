/**
 * This file Copyright (c) 2013-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.localizations.compatibility.app.action;

import com.vaadin.v7.data.Item;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.importexport.command.JcrImportCommand;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Call Import Command in order to perform the import action.
 *
 * @deprecated since 6.2 - use {@link info.magnolia.ui.contentapp.action.CommitAction} instead.
 *
 * @see <a href="https://documentation.magnolia-cms.com/display/DOCS62/Upgrading+to+Magnolia+6.2.x">Upgrading to Magnolia 6.2.x</a>
 */
@Deprecated
public class SaveImportDialogAction extends AbstractAction<SaveImportDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveImportDialogAction.class);

    private final Item item;
    private final CommandsManager commandsManager;
    private final EditorValidator validator;
    private final EditorCallback callback;
    private final EventBus eventBus;

    @Inject
    public SaveImportDialogAction(SaveImportDialogActionDefinition definition, Item item, CommandsManager commandsManager, EditorValidator validator, EditorCallback callback, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.item = item;
        this.commandsManager = commandsManager;
        this.validator = validator;
        this.callback = callback;
        this.eventBus = eventBus;
    }

    /**
     * @deprecated since 5.4.9 - use {@link #SaveImportDialogAction(SaveImportDialogActionDefinition, Item, CommandsManager, EditorValidator, EditorCallback, EventBus)} instead.
     */
    @Deprecated
    public SaveImportDialogAction(SaveImportDialogActionDefinition definition, Item item, CommandsManager commandsManager, EditorValidator validator, EditorCallback callback) {
        this(definition, item, commandsManager, validator, callback, null);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            try {
                JcrNodeAdapter parent = (JcrNodeAdapter) item;
                JcrNodeAdapter importXml = (JcrNodeAdapter) parent.getChild("import");
                if (importXml == null) {
                    throw new IllegalArgumentException("Nothing to import, given item does not contain any child named 'import'.");
                }

                if (eventBus != null) {
                    List<Node> nodesBeforeImport = NodeUtil.asList(NodeUtil.asIterable(parent.getJcrItem().getNodes()));
                    executeCommand(parent);

                    List<Node> nodesAfterImport = NodeUtil.asList(NodeUtil.asIterable(parent.getJcrItem().getNodes()));
                    Set<JcrNodeItemId> importedNodeItemIds = getImportedNodeItemIds(nodesBeforeImport, nodesAfterImport);

                    /**
                     * HACK: Historically the 'import' action is partially mis-configured: instead of starting the import dialog from within a specialised action,
                     * we typically hijack the OpenCreateDialogAction, which presets a callback that attempts to send ContentChangedEvent for some item
                     * supposedly created with a dialog. However, the import action acts differently and we need to send the CCE for potentially many items (for all the imported ones).
                     * To prevent the OpenCreateDialogAction's callback from firing the un-desired CCE, we invoke onCancel() method (effectively just closes a dialog) and send the proper event
                     * right here in this action.
                     */
                    eventBus.fireEvent(new ContentChangedEvent(importedNodeItemIds));
                    callback.onCancel();

                } else {
                    executeCommand(parent);
                    callback.onSuccess(getDefinition().getName());
                }

            } catch (RepositoryException e) {
                throw new ActionExecutionException(e);
            }
        } else {
            log.info("Validation error(s) occurred. No Import performed.");
        }
    }

    private Set<JcrNodeItemId> getImportedNodeItemIds(List<Node> nodesBeforeImport, List<Node> nodesAfterImport) throws RepositoryException {
        Set<JcrNodeItemId> importedNodeItemIds = new HashSet<>();
        for (Node nodeAfterImport : nodesAfterImport) {
            boolean existedBeforeImport = false;
            for (Node nodeBeforeImport : nodesBeforeImport) {
                if (NodeUtil.isSame(nodeAfterImport, nodeBeforeImport)) {
                    existedBeforeImport = true;
                    break;
                }
            }
            if (!existedBeforeImport) {
                importedNodeItemIds.add(new JcrNodeItemId(nodeAfterImport.getIdentifier(), nodeAfterImport.getSession().getWorkspace().getName()));
            }
        }
        return importedNodeItemIds;
    }

    public void executeCommand(JcrNodeAdapter itemChanged) throws ActionExecutionException {

        String commandName = getDefinition().getCommand();
        String catalog = getDefinition().getCatalog();
        long start = System.currentTimeMillis();
        try {
            JcrImportCommand command = (JcrImportCommand) this.commandsManager.getCommand(catalog, commandName);
            if (command == null) {
                throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", commandName));
            }
            final JcrNodeAdapter node = (JcrNodeAdapter) itemChanged.getChild("import");
            command.setStream(((Binary)node.getItemProperty(JcrConstants.JCR_DATA).getValue()).getStream());
            command.setFileName((String) node.getItemProperty(File.PROPERTY_FILENAME).getValue());
            command.setRepository(itemChanged.getWorkspace());
            command.setPath(itemChanged.getJcrItem().getPath());
            command.setIdentifierBehavior(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
            log.debug("Executing command [{}] from catalog [{}]...", commandName, catalog);
            commandsManager.executeCommand(command, null);
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start);
            throw new ActionExecutionException(e);
        }
    }

    protected CommandsManager getCommandsManager() {
        return commandsManager;
    }
}
