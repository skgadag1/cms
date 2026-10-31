package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import com.vaadin.ui.DateField;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class FormRequest {
    private String formName;
    private String region;
    private DateField from;
    private DateField to;
}
