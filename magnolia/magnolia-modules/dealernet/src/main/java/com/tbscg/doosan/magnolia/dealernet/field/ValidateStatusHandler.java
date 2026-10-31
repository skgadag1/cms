package com.tbscg.doosan.magnolia.dealernet.field;

import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.BindingValidationStatusHandler;
import com.vaadin.data.HasValue;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.framework.layout.field.FieldLayoutComponent;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class ValidationStatusHandler implements BindingValidationStatusHandler {
    private final HasValue<?> hasValue;

    ValidationStatusHandler(HasValue<?> hasValue) {
        this.hasValue = hasValue;
    }

    public void statusChange(BindingValidationStatus<?> statusChange) {
        this.findParent((Component)this.hasValue, VerticalLayout.class).flatMap((components) -> {
            Stream componentStream = StreamSupport.stream(components.spliterator(), false);
            FieldLayoutComponent.ValidationLabel.class.getClass();
            componentStream = componentStream.filter(FieldLayoutComponent.ValidationLabel.class::isInstance);
            FieldLayoutComponent.ValidationLabel.class.getClass();
            return componentStream.map(FieldLayoutComponent.ValidationLabel.class::cast).findFirst();
        }).ifPresent(validationLabel -> ((FieldLayoutComponent.ValidationLabel)validationLabel).setValue(statusChange.getMessage().orElse("")));
    }

    private <V extends Component> Optional<V> findParent(Component field, Class<V> parentClass) {
        for(ClientConnector connector = field; connector != null; connector = connector.getParent()) {
            if (parentClass.isInstance(connector)) {
                return (Optional<V>) Optional.of((Component)connector);
            }
        }

        return Optional.empty();
    }
}
