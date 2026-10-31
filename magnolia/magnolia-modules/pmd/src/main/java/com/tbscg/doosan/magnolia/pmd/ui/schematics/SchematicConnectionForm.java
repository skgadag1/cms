package com.tbscg.doosan.magnolia.pmd.ui.schematics;

import com.tbscg.doosan.magnolia.pmd.data.model.ConnectTarget;
import com.tbscg.doosan.magnolia.pmd.data.model.Prefix;
import com.tbscg.doosan.magnolia.pmd.data.model.Range;
import com.tbscg.doosan.magnolia.pmd.data.model.SchematicConnectionData;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.PmiSelectorTree;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.RangeEditForm;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class SchematicConnectionForm extends EditForm<SchematicConnectionData> {

    private final FormLayout layout = new FormLayout();
    private final PmiSelectorTree prefix;

    private final Range customRange = new Range();
    private final TextField suffixStart = new TextField("From suffix");
    private final TextField suffixEnd = new TextField("To suffix");

    private final RadioButtonGroup<Range> options = new RadioButtonGroup<>();

    private final ApiService apiService;

    public SchematicConnectionForm(ApiService apiService) {
        super(new SchematicConnectionData());
        this.apiService = apiService;

        VerticalLayout rangeOptionsPart = new VerticalLayout();
        rangeOptionsPart.setCaption("Select range");

        this.prefix = new PmiSelectorTree(apiService, Prefix.class);
        this.prefix.setCaption("Select prefix");
        this.prefix.setWidth(300, Sizeable.Unit.PIXELS);
        this.prefix.setHeight(300, Sizeable.Unit.PIXELS);
        this.prefix.addValueChangeListener(e -> {
            ConnectTarget value = e.getValue();
            if (value instanceof Prefix) {
                Prefix p = (Prefix) value;
                List<Range> optionItems = new ArrayList<>(apiService.getRanges(p));
                optionItems.add(this.customRange);
                options.setItems(optionItems);
                rangeOptionsPart.setVisible(true);
            } else {
                rangeOptionsPart.setVisible(false);
                options.setItems(Collections.emptyList());
            }
        });

        this.suffixStart.setWidth(150, Sizeable.Unit.PIXELS);
        this.suffixEnd.setWidth(150, Sizeable.Unit.PIXELS);

        HorizontalLayout customRangeFields = new HorizontalLayout(this.suffixStart, this.suffixEnd);
        customRangeFields.setVisible(false);

        this.options.setItemCaptionGenerator(r -> {
            return r == customRange ? "Other:" : r.getSuffixStart() + " - " + r.getSuffixEnd();
        });
        this.options.addSelectionListener(e -> customRangeFields.setVisible(isCustomRangeSelected()));

        rangeOptionsPart.addComponent(options);
        rangeOptionsPart.addComponent(customRangeFields);
        rangeOptionsPart.setVisible(false);

        layout.addComponent(this.prefix);
        layout.addComponent(rangeOptionsPart);
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    private boolean isCustomRangeSelected() {
        return this.options.getValue() == this.customRange;
    }

    @Override
    protected void bind(Binder<SchematicConnectionData> binder) {
        binder.forField(this.prefix)
                .withValidator(Objects::nonNull, "Prefix is required")
                .bind(data -> data.getPrefixId() != null ? this.apiService.getPrefix(data.getPrefixId()) : null,
                        (data, prefix) -> data.setPrefixId(prefix.getId()));

        binder.forField(this.suffixStart)
                .withValidator(suffix -> !isCustomRangeSelected()
                                || RangeEditForm.validateSuffixRange(suffix, this.suffixEnd.getValue()),
                        "Invalid format")
                .bind(data -> null,
                        (data, suffix) -> {
                            if (isCustomRangeSelected()) data.setSuffixStart(suffix);
                        });

        binder.forField(this.suffixEnd)
                .withValidator(suffix -> !isCustomRangeSelected()
                                || RangeEditForm.validateSuffixRange(this.suffixStart.getValue(), suffix),
                        "Invalid format")
                .bind(data -> null,
                        (data, suffix) -> {
                            if (isCustomRangeSelected()) data.setSuffixEnd(suffix);
                        });

        binder.forField(this.options)
                .withValidator(Objects::nonNull, "Range not selected")
                .bind(data -> null,
                        (data, range) -> {
                            if (!isCustomRangeSelected()) {
                                data.setSuffixStart(range.getSuffixStart());
                                data.setSuffixEnd(range.getSuffixEnd());
                            }
                        });

    }
}
