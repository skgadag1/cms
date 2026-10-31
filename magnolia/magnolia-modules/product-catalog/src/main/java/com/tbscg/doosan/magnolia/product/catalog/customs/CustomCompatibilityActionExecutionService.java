package com.tbscg.doosan.magnolia.product.catalog.customs;

import com.vaadin.data.BinderValidationStatus;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.ioc.UiContextScoped;
import info.magnolia.ui.contentapp.browser.ActionExecutionService;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.observation.DatasourceObservation;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@UiContextScoped
public class CustomCompatibilityActionExecutionService extends ActionExecutionService {

    private final ComponentProvider componentProvider;
    private final EventBus eventBus;
    public CustomCompatibilityActionExecutionService(ActionExecutor actionExecutor, MessagesManager messagesManager, ComponentProvider componentProvider, EventBus eventBus) {
        super(actionExecutor, messagesManager);
        this.componentProvider = componentProvider;
        this.eventBus = eventBus;
    }

    @Override
    public void executeAction(String actionName, Object... parameters) {
        Collection<Object> compatibilityParameters = new ArrayList(Arrays.asList(parameters));
        Optional<ValueContextProvider> valueContextProviderOptional = this.invokeComponentProviderSafely(() -> {
            return (ValueContextProvider)this.componentProvider.newInstance(ValueContextProvider.class, new Object[0]);
        });
        Optional<Collection> selectedItems = valueContextProviderOptional.map(InjectedComponentProvider::get).map(ValueContext::get).map((nodeStream) -> {
            return (List)nodeStream.collect(Collectors.toList());
        });
        Optional<EditorViewProvider> editorView = this.invokeComponentProviderSafely(() -> {
            return (EditorViewProvider)this.componentProvider.newInstance(EditorViewProvider.class, new Object[0]);
        });
        Set<Node> jcrNodes = this.findJcrNodes(selectedItems);
        if (jcrNodes.size() > 0) {
            compatibilityParameters.add(this.wrapForLegacyActionExecution(jcrNodes.iterator().next(), editorView));
            compatibilityParameters.add(jcrNodes.stream().map((node) -> {
                return this.wrapForLegacyActionExecution(node, editorView);
            }).collect(Collectors.toList()));
            this.mimicContentConnector(compatibilityParameters);
            this.mimicEditor(compatibilityParameters, jcrNodes, editorView, this.invokeComponentProviderSafely(() -> {
                return (ValueContextProvider)this.componentProvider.newInstance(ValueContextProvider.class, new Object[0]);
            }), this.invokeComponentProviderSafely(() -> {
                return (CloseHandlerProvider)this.componentProvider.newInstance(CloseHandlerProvider.class, new Object[0]);
            }));
        } else {
            selectedItems.ifPresent((o) -> {
                o.stream().findFirst().ifPresent(compatibilityParameters::add);
            });
        }
        super.executeAction(actionName, compatibilityParameters.toArray(new Object[0]));
    }

    private void mimicEditor(Collection<Object> compatibilityParameters, Set<Node> jcrNodes, Optional<EditorViewProvider> editorView, Optional<ValueContextProvider> valueContextProviderOptional, final Optional<CloseHandlerProvider> closeHandler) {
        editorView.map(InjectedComponentProvider::get).ifPresent((nodeEditorView) -> {
            compatibilityParameters.add(new EditorValidator() {
                public void showValidation(boolean visible) {
                }

                public boolean isValid() {
                    List<BinderValidationStatus> validate = nodeEditorView.validate();
                    return validate.stream().allMatch(BinderValidationStatus::isOk);
                }
            });
        });
        compatibilityParameters.add(new EditorCallback() {
            public void onCancel() {
                closeHandler.map(InjectedComponentProvider::get).ifPresent(CloseHandler::close);
            }

            public void onSuccess(String actionName) {
                this.onCancel();
            }
        });
        valueContextProviderOptional.map(InjectedComponentProvider::get).ifPresent((context) -> {
            this.eventBus.addHandler(ContentChangedEvent.class, (event) -> {
                context.set(jcrNodes);
            });
        });
    }

    private void mimicContentConnector(Collection<Object> compatibilityParameters) {
        this.invokeComponentProviderSafely(() -> {
            return (JcrDatasourceDefinitionProvider)this.componentProvider.newInstance(JcrDatasourceDefinitionProvider.class, new Object[0]);
        }).map(InjectedComponentProvider::get).ifPresent((datasourceDefinition) -> {
            ConfiguredJcrContentConnectorDefinition contentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
            contentConnectorDefinition.setWorkspace(datasourceDefinition.getWorkspace());
            contentConnectorDefinition.setRootPath(datasourceDefinition.getRootPath());
            contentConnectorDefinition.setNodeTypes((List)datasourceDefinition.getAllowedNodeTypes().stream().map((nodeType) -> {
                ConfiguredNodeTypeDefinition nodeTypeDefinition = new ConfiguredNodeTypeDefinition();
                nodeTypeDefinition.setName(nodeType);
                return nodeTypeDefinition;
            }).collect(Collectors.toList()));
            compatibilityParameters.add(new JcrContentConnector((VersionManager)this.componentProvider.getComponent(VersionManager.class), contentConnectorDefinition));
        });
    }

    private Set<Node> findJcrNodes(Optional<Collection> selectedItems) {
        //CUSTOM CODE BEGIN
        return (Set<Node>) selectedItems.map((items) -> items.stream()
                        .filter(Node.class::isInstance)
                        .map(Node.class::cast)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .orElse(new LinkedHashSet<>());
        //CUSTOM CODE END
    }

    private JcrNodeAdapter wrapForLegacyActionExecution(Object potentialJcrItem, final Optional<EditorViewProvider> editorView) {
        return new JcrNodeAdapter((Node)potentialJcrItem) {
            public Node applyChanges() throws RepositoryException {
                editorView.map(InjectedComponentProvider::get).ifPresent((editorViewx) -> {
                    editorViewx.write(this.getJcrItem());
                });
                Optional<ManualObservationProvider> manualObservationProviderOptional = CustomCompatibilityActionExecutionService.this.invokeComponentProviderSafely(() -> {
                    return (ManualObservationProvider)CustomCompatibilityActionExecutionService.this.componentProvider.newInstance(ManualObservationProvider.class, new Object[0]);
                });
                manualObservationProviderOptional.map(InjectedComponentProvider::get).ifPresent(DatasourceObservation.Manual::trigger);
                return super.applyChanges();
            }
        };
    }

    private <C> Optional<C> invokeComponentProviderSafely(Callable<C> callable) {
        try {
            return Optional.of(callable.call());
        } catch (Exception var3) {
            return Optional.empty();
        }
    }

    private static class InjectedComponentProvider<T> implements Provider<T> {
        private final T component;

        private InjectedComponentProvider(T component) {
            this.component = component;
        }

        public T get() {
            return this.component;
        }
    }

    private static class CloseHandlerProvider extends InjectedComponentProvider<CloseHandler> {
        @Inject
        private CloseHandlerProvider(CloseHandler closeHandler) {
            super(closeHandler);
        }
    }

    private static class JcrDatasourceDefinitionProvider extends InjectedComponentProvider<JcrDatasourceDefinition> {
        @Inject
        private JcrDatasourceDefinitionProvider(JcrDatasourceDefinition datasourceDefinition) {
            super(datasourceDefinition);
        }
    }

    private static class ManualObservationProvider extends InjectedComponentProvider<DatasourceObservation.Manual> {
        @Inject
        private ManualObservationProvider(DatasourceObservation.Manual component) {
            super(component);
        }
    }

    private static class ValueContextProvider extends InjectedComponentProvider<ValueContext<Node>> {
        @Inject
        private ValueContextProvider(ValueContext<Node> valueContext) {
            super(valueContext);
        }
    }

    private static class EditorViewProvider extends InjectedComponentProvider<EditorView> {
        @Inject
        private EditorViewProvider(EditorView<?> strategy) {
            super(strategy);
        }
    }
}
