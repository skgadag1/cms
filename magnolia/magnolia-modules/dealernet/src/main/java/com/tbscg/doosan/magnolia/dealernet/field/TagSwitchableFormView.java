package com.tbscg.doosan.magnolia.dealernet.field;

import com.google.common.base.Objects;
import com.tbscg.doosan.magnolia.dealernet.field.clonableNamed.CloneableNamedField;
import com.tbscg.doosan.magnolia.dealernet.util.DealernetUtils;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.config.NamedDefinition;
import info.magnolia.ui.datasource.PropertySetFactory;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import info.magnolia.ui.editor.*;
import info.magnolia.ui.field.*;
import info.magnolia.ui.framework.ioc.UiComponentProvider;
import info.magnolia.ui.framework.layout.ConfiguredFieldLayoutDefinition;
import info.magnolia.ui.framework.layout.FieldLayoutProducer;
import info.magnolia.ui.framework.layout.field.FieldComponentDecorator;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagSwitchableFormView<T> implements EditorView<T> {
    private final TagSwitchableFieldDefinition<T> definition;
    private final PropertySetFactory<T> propertySetFactory;
    private Binder<T> binder;
    private final Map<String, EditorView<T>> representations;
    private final Locale locale;
    private final VerticalLayout layout;
    private final FieldComponentDecorator fieldComponentDecorator;
    private static final String IS_TAG_SWITCHABLE = "isTagSwitchable";

    @Inject
    TagSwitchableFormView(TagSwitchableFieldDefinition<T> definition, PropertySetFactory<T> propertySetFactory, LocaleContext localeContext, FieldComponentDecorator fieldComponentDecorator) {
        this.layout = new VerticalLayout();
        this.definition = definition;
        this.propertySetFactory = propertySetFactory;
        this.representations = (Map) definition.getForms().stream()
                .collect(Collectors.toMap(
                        NamedDefinition::getName,
                        (childEditor) -> (EditorView) this.create(new CompositeFormDefinition(childEditor, definition.getPropertyNameDecorator()), new Object[0])));
        this.locale = localeContext.getDefault();
        this.fieldComponentDecorator = fieldComponentDecorator;
        this.binder = this.bindOptionField();
        this.layout();
    }

    public void populate(T item) {
        List<Node> contentVisibility = Stream.concat(
                        DealernetUtils.getComponentVisibilityNodes((Node) item, this.definition.getRoot()).stream(),
                        DealernetUtils.getContentNodeVisibilityNodes((Node) item, this.definition.getName()).stream())
                .distinct()
                .collect(Collectors.toList());
        List<String> contentVisibilityNames = contentVisibility.stream()
                .map(NodeUtil::getName)
                .collect(Collectors.toList());
        contentVisibilityNames.add(this.definition.getDefaultValue());
        this.populateRepresentations(item, contentVisibilityNames);
        this.populateOptions(contentVisibility);
        this.binder = this.bindOptionField();
        this.binder.readBean(item);
        this.applyDefaults();
        this.layout();
    }

    private void populateOptions(List<Node> componentVisibility) {
        Set<Option> options = componentVisibility.stream()
                .map(DealernetUtils::mapNodeToOption)
                .collect(Collectors.toSet());
        options.addAll(this.definition.getField().getDatasource().getOptions());
        this.definition.getField().getDatasource().setOptions(new ArrayList<>(options));
    }

    private void populateRepresentations(T item, List<String> componentVisibilityNames) {
        Optional<FormDefinition<T>> defaultFormOptional = definition.getForms().stream().findAny();
        if (defaultFormOptional.isPresent()) {
            componentVisibilityNames.forEach(tag -> this.representations.put(tag, (EditorView) this.create(new CompositeFormDefinition(tag, defaultFormOptional.get(), definition.getPropertyNameDecorator()), new Object[0])));
            this.representations.values().forEach((representation) -> representation.populate(item));
        }
    }

    public List<BinderValidationStatus<?>> validate() {
        Option currentRepresentation = this.getOptionField().getValue();
        List<BinderValidationStatus<?>> validationStatuses = new ArrayList();
        validationStatuses.add(this.binder.validate());
        this.subForm(currentRepresentation).map(EditorView::validate).ifPresent(validationStatuses::addAll);
        return validationStatuses;
    }

    public void write(T item) {
        this.binder.writeBeanIfValid(item);
        this.subForm(this.getOptionField().getValue()).ifPresent((form) -> {
            PropertyUtil.setProperty(((Node) item), IS_TAG_SWITCHABLE, "true");
            form.write(item);
        });
    }

    public void applyDefaults() {
        this.definition.getField().getDatasource().getOptions().stream()
                .filter((option) -> Objects.equal(option.getValue(), String.valueOf(this.definition.getField().getDefaultValue())))
                .findFirst()
                .ifPresent((defaultOption) -> this.getOptionField().setValue(defaultOption));
        this.representations.values().forEach(EditorView::applyDefaults);
    }

    private void layout() {
        this.layout.removeAllComponents();
        this.layout.setMargin(false);
        Component optionField = (Component) this.getOptionField();
        optionField = this.fieldComponentDecorator.decorateField(optionField, this.definition.getField());
        this.layout.addComponent(optionField);
        optionField.setCaption(null);
        this.getOptionField().getOptionalValue().flatMap(this::subForm).ifPresent((view) -> {
            this.layout.addComponent(view.asVaadinComponent());
        });
    }

    private Binder<T> bindOptionField() {
        AbstractSelectFieldDefinition<Option, OptionListDefinition> fieldDefinition = this.definition.getField();
        UiComponentProvider componentProvider = this.getComponentProvider().inChildContext(fieldDefinition);
        Binder<T> binder = Binder.withPropertySet(this.propertySetFactory.fromFieldDefinitions(Collections.singletonList(fieldDefinition), this.locale));
        FieldBinder<Option> fieldBinder = componentProvider.newInstance(fieldDefinition.getFieldBinderClass(), new Object[0]);
        HasValue<Option> optionsField = ((FieldFactory) componentProvider.newInstance(fieldDefinition.getFactoryClass(), new Object[0])).createField();
        fieldBinder.configureBinding(fieldDefinition, binder.forField(optionsField)).withValidationStatusHandler(new ValidationStatusHandler(optionsField)).bind(fieldDefinition.getName());
        optionsField.addValueChangeListener(e -> {
            this.subForm(e.getOldValue()).ifPresent(oldView -> this.layout.removeComponent(oldView.asVaadinComponent()));
            this.subForm(e.getValue()).ifPresent(newView -> this.layout.addComponent(newView.asVaadinComponent()));
        });
        return binder;
    }

    private HasValue<Option> getOptionField() {
        return (HasValue) this.binder.getBinding(this.definition.getField().getName()).map((binding) -> binding.getField()).orElse(null);
    }

    private Optional<EditorView<T>> subForm(Option selectedOption) {
        return Optional.ofNullable(selectedOption).map((option) -> this.representations.get(option.getValue()));
    }

    public Component asVaadinComponent() {
        return this.layout;
    }

    private static class VerticalLayoutProducer implements FieldLayoutProducer<VerticalLayoutProducer.Definition> {
        private final FieldComponentDecorator fieldComponentDecorator;

        @Inject
        VerticalLayoutProducer(FieldComponentDecorator fieldComponentDecorator) {
            this.fieldComponentDecorator = fieldComponentDecorator;
        }

        public Component createLayout(Definition definition, Map<EditorPropertyDefinition, Component> mappings) {
            return new VerticalLayout(mappings.entrySet().stream().map((entry) -> this.fieldComponentDecorator.decorateField(entry.getValue(), entry.getKey())).toArray((x$0) -> new Component[x$0]));
        }

        static class Definition extends ConfiguredFieldLayoutDefinition<VerticalLayoutProducer> {
            Definition() {
                this.setImplementationClass(VerticalLayoutProducer.class);
            }
        }
    }

    private static class CompositeFormDefinition<T> extends ConfiguredFormDefinition<T> implements WithPropertyNameDecorator {
        private final Class<WithPropertyNameDecorator.PropertyNameDecorator> propertyNameDecorator;

        public CompositeFormDefinition(String name, FormDefinition<T> definition, Class<PropertyNameDecorator> propertyNameDecorator) {
            this.setLayout(new VerticalLayoutProducer.Definition());
            this.setName(name);
            this.setProperties(getProperties(name, definition));
            this.propertyNameDecorator = propertyNameDecorator;
        }

        public CompositeFormDefinition(FormDefinition<T> definition, Class<WithPropertyNameDecorator.PropertyNameDecorator> propertyNameDecorator) {
            this.setLayout(new TagSwitchableFormView.VerticalLayoutProducer.Definition());
            this.setName(definition.getName());
            this.setProperties(definition.getProperties());
            this.propertyNameDecorator = propertyNameDecorator;
        }

        private List<EditorPropertyDefinition> getProperties(String name, FormDefinition<T> definition) {
            List<CloneableNamedField> editorPropertyDefinitions = definition.getProperties().stream()
                    .filter(CloneableNamedField.class::isInstance)
                    .map(CloneableNamedField.class::cast)
                    .map(CloneableNamedField::clone)
                    .collect(Collectors.toList());
            editorPropertyDefinitions.forEach(def -> def.setName(name));
            return editorPropertyDefinitions.stream()
                    .filter(EditorPropertyDefinition.class::isInstance)
                    .map(EditorPropertyDefinition.class::cast)
                    .collect(Collectors.toList());
        }

        public Class<WithPropertyNameDecorator.PropertyNameDecorator> getPropertyNameDecorator() {
            return this.propertyNameDecorator;
        }
    }
}
