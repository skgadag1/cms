package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.DxpActionProcessor;
import com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping.DxpCustomPathMappingService;
import com.vaadin.ui.Notification;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import info.magnolia.ui.editor.EditorDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.ConfiguredSwitchableFieldDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.JcrMultiFieldDefinition;
import info.magnolia.ui.observation.DatasourceObservation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DxpCommitAction<T> extends CommitAction<T> {

    private final DxpComponentToSendAction<T> componentToSendAction;
    private final DxpCustomPathMappingService dxpCustomPathMappingService;
    private final LazyInitializer<List<DxpActionProcessor>> lazyCustomOnSaveProcComponents;

    @Inject
    public DxpCommitAction(CommitActionDefinition definition, CloseHandler closeHandler,
                           ValueContext<T> valueContext, EditorView<T> form, Datasource<T> datasource,
                           DatasourceObservation.Manual datasourceObservation,
                           @Named(SystemEventBus.NAME) EventBus eventBus,
                           DxpCustomPathMappingService dxpCustomPathMappingService,
                           ComponentProvider componentProvider) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        this.componentToSendAction = new DxpComponentToSendAction<>(eventBus, valueContext);
        this.dxpCustomPathMappingService = dxpCustomPathMappingService;

        final List<String> customOnSaveProcessorClasses = ((DxpCommitActionDefinition) definition).getCustomOnSaveProcessors();
        lazyCustomOnSaveProcComponents = createLazyCustomOnSaveProcComponents(componentProvider, customOnSaveProcessorClasses);
    }

    private static LazyInitializer<List<DxpActionProcessor>> createLazyCustomOnSaveProcComponents(
            final ComponentProvider componentProvider,
            final List<String> customOnSaveProcessorClasses) {
        return new LazyInitializer<>() {
            @Override
            protected List<DxpActionProcessor> initialize() {
                return getOnSaveProcessorsComponentList(componentProvider, customOnSaveProcessorClasses);
            }
        };
    }

    private static List<DxpActionProcessor> getOnSaveProcessorsComponentList(
            final ComponentProvider componentProvider,
            final List<String> processorClassNames) {
        try {
            if (processorClassNames.isEmpty()) {
                log.debug("No custom processor classes defined for save action.");
                return emptyList();
            }

            final List<DxpActionProcessor> processors =
                    new ArrayList<>(processorClassNames.size());

            if (log.isDebugEnabled()) {
                log.debug("Custom OnSaveProcessors: {}", String.join(", ", processorClassNames));
            }

            for (String processorClassName : processorClassNames) {
                processors.add((DxpActionProcessor) componentProvider.getComponent(Class.forName(processorClassName)));
            }
            return processors;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createMissingNodes(Node wrappedNode) {
        try {
            NodeUtil.getOrAddNode(wrappedNode, "header", NodeTypes.Area.NAME);
            NodeUtil.getOrAddNode(wrappedNode, "main", NodeTypes.Area.NAME);
            NodeUtil.getOrAddNode(wrappedNode, "footer", NodeTypes.Area.NAME);
        } catch (RepositoryException e) {
            log.error("Error while creating missing nodes", e);
        }
    }

    @SneakyThrows
    private static FormDefinition<?> getFormDefinition(FormView<?> formView) {
        // FormViewWithChangeListener (used by javascriptField) extends FormView but does not redeclare
        // "formDefinition"; getDeclaredField only sees the subclass, so walk the hierarchy.
        Class<?> clazz = formView.getClass();
        while (clazz != null) {
            try {
                Field formDefinitionField = clazz.getDeclaredField("formDefinition");
                formDefinitionField.setAccessible(true);
                return (FormDefinition<?>) formDefinitionField.get(formView);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new IllegalStateException("formDefinition field not found for " + formView.getClass().getName());
    }

    private static void handlePropertyDefinition(Object propertyDefinition) {
        if (!(propertyDefinition instanceof JcrMultiFieldDefinition jcrMultiFieldDefinition)) {
            return;
        }

        final EditorDefinition<Node> editorDefinition = jcrMultiFieldDefinition.getEditorDefinition();

        handleEditorDefinition(editorDefinition);
    }

    private static void handleEditorDefinition(EditorDefinition<Node> editorDefinition) {
        log.debug("editorDefinition class is: {}", editorDefinition.getClass());
        if (editorDefinition instanceof JcrMultiFieldDefinition jcrMultiFieldDefinition) {
            handleFieldDefinition(jcrMultiFieldDefinition.getField());
        } else {
            log.debug("Unhandled field definition: {}", editorDefinition.getClass());
        }
    }

    private static void handleFieldDefinition(EditorPropertyDefinition field) {
        log.debug("Field type: {}", field.getClass());

        if (field instanceof CompositeFieldDefinition<?> compositeFieldDefinition) {
            log.debug("Processing CompositeFieldDefinition: {}", compositeFieldDefinition);

            compositeFieldDefinition.getProperties().forEach(property -> {
                log.debug("Property: {}", property);
                if (!(property instanceof ConfiguredSwitchableFieldDefinition<?> switchableFieldDefinition)) {
                    return;
                }

                final OptionListDefinition datasourceDefinition = switchableFieldDefinition.getField().getDatasource();
                if (datasourceDefinition == null) {
                    return;
                }

                final List<Option> options = datasourceDefinition.getOptions();

                options.forEach(option -> log.debug("Got option: {}", option));
            });
        } else {
            log.debug("Unhandled field type: {}", field.getClass());
        }
    }

    @Override
    public void execute() throws ActionExecutionException {
        Optional<T> optionalItem = this.getValueContext().getSingle();

        if (optionalItem.isPresent() && optionalItem.get() instanceof Node wrappedNode) {
            try {
                if (isNodeLocked(wrappedNode)) {
                    log.warn("Attempted to rename a locked node or its parent: {}", wrappedNode.getPath());
                    AlertBuilder.alert("Parent lock")
                            .withBody("This item or its parent is locked.")
                            .withLevel(Notification.Type.WARNING_MESSAGE)
                            .withOkButtonCaption("OK")
                            .withConfirmationHandler(() -> {})
                            .buildAndOpen();
                    return;
                }

                String path = NodeUtil.getPath(wrappedNode);
                String nodeType = NodeUtil.getPrimaryNodeType(wrappedNode);
                String workspace = NodeUtil.tryGetNodeWorkspace(wrappedNode).orElse(EMPTY);
                createMissingNodes(wrappedNode);
                this.dxpCustomPathMappingService.updatePageWithCustomName(wrappedNode, path, nodeType, workspace);

                Optional.of(this.getForm())
                        .map(editorView -> editorView instanceof FormView<?> formView ? formView : null)
                        .ifPresent(formView -> runAllCustomProcessors(wrappedNode, getFormDefinition(formView)));

            } catch (RepositoryException e) {
                log.error("Error processing node: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process the node.", e);
            }
        }

        super.execute();

        this.componentToSendAction.execute();
    }

    private boolean isNodeLocked(Node node) throws RepositoryException {
        if (node.isLocked()) {
            return true;
        }
        try {
            if (node.getDepth() > 0) {
                return isNodeLocked(node.getParent());
            }
        } catch (RepositoryException e) {
            log.error("Error checking lock status for node or its parent: {}", e.getMessage(), e);
        }

        return false;
    }

    @SneakyThrows
    private void runAllCustomProcessors(Node wrappedNode, FormDefinition<?> formDefinition) {
        for (DxpActionProcessor proc : lazyCustomOnSaveProcComponents.get()) {
            proc.process(wrappedNode, formDefinition);
        }
    }
}
