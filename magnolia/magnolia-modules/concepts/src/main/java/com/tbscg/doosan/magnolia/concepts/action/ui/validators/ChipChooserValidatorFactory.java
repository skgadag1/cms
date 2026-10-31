package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Item;
import javax.jcr.Node;


@Slf4j
public class ChipChooserValidatorFactory extends AbstractFieldValidatorFactory<ChipChooserValidatorDefinition, JcrNodeWrapper> {

    private final info.magnolia.ui.ValueContext<Item> valueContext;

    public ChipChooserValidatorFactory(ChipChooserValidatorDefinition definition, info.magnolia.ui.ValueContext<Item> valueContext) {
        super(definition);
        this.valueContext = valueContext;
    }

    @Override
    public Validator<JcrNodeWrapper> createValidator() {
        return (value, ctx) -> {
            try {
                String chipPath = value.getPath();
                if (isChipCommon(chipPath)) {
                    return ValidationResult.ok();
                }
                if (!isChipPathValid(chipPath)) {
                    return ValidationResult.error("Choose chips from bobcat folder");
                }
                String chipRegion = getRegionFromPath(chipPath);
                Node featureNode = getCurrentNode();
                String featurePath = featureNode.getPath();
                String featureRegion = getRegionFromPath(featurePath);
                if (!chipRegion.equals(featureRegion)) {
                    return ValidationResult.error("Chip region must be the same as feature region");
                }
                return ValidationResult.ok();
            } catch (Exception e) {
                log.error("Error during validation", e);
                return ValidationResult.error("Error during validation, contact administrator");
            }
        };
    }


    private Node getCurrentNode() {
        return (Node) valueContext.getSingleOrThrow();
    }

    private String getRegionFromPath(String path) {
        return path.split("/")[2];
    }

    private boolean isChipPathValid(String path) {
        if (!path.startsWith("/bobcat")) return false;
        String[] parts = path.split("/");
        return parts[3].equals("chips");
    }

    private boolean isChipCommon(String chipPath) {
        return chipPath.startsWith("/bobcat/common/chips");
    }
}
