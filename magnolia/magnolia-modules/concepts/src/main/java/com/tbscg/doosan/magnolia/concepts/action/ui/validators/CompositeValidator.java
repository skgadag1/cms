package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import info.magnolia.ui.form.EditorValidator;

import java.util.Arrays;

public class CompositeValidator implements EditorValidator {
    private final EditorValidator[] validators;

    public CompositeValidator(EditorValidator[] validators) {
        this.validators = validators;
    }

    @Override
    public void showValidation(boolean b) {
        Arrays.stream(validators).forEach(validator -> validator.showValidation(b));
    }

    @Override
    public boolean isValid() {
        return Arrays.stream(validators).allMatch(EditorValidator::isValid);
    }
}
