package com.tbscg.doosan.magnolia.forms.configuration.validator;

import com.tbscg.doosan.magnolia.forms.configuration.validator.definition.SubPathPropertyEqualDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;

@Slf4j
public class SubPathPropertyEqual implements AvailabilityRule {
    private final String propertyValue;
    private final String subPath;
    private final String propertyName;

    @Inject
    public SubPathPropertyEqual(SubPathPropertyEqualDefinition isNestedPropertyEqualDefinition) {
        this.propertyName = isNestedPropertyEqualDefinition.getPropertyName();
        this.propertyValue = isNestedPropertyEqualDefinition.getPropertyValue();
        this.subPath = isNestedPropertyEqualDefinition.getSubPath();
    }


    @Override
    public boolean isAvailable(Collection<?> collection) {
        return collection.stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .allMatch(node -> {
                    try {
                        Node subNode = node.getNode(subPath);
                        String property = subNode.getProperty(propertyName).getValue().getString();
                        return propertyValue.equals(property);
                    } catch (RepositoryException e) {
                        log.warn("SubPath {} not exists", subPath);
                        return false;
                    }
                });
    }
}
