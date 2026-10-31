package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Engine;
import com.tbscg.doosan.magnolia.pmd.data.model.Prefix;
import com.tbscg.doosan.magnolia.pmd.data.model.Range;
import com.tbscg.doosan.magnolia.pmd.data.model.RangeData;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.EditForm;
import com.tbscg.doosan.magnolia.pmd.ui.EditWindow;
import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class RangeEditForm extends EditForm<RangeData> {

    private final VerticalLayout layout = new VerticalLayout();

    private final PmiSelectorTree prefix;
    private final TextField suffixStart = new TextField("Suffix start");
    private final TextField suffixEnd = new TextField("Suffix end");
    private final ComboBox<Engine> engine = new ComboBox<>("Engine");
    private final List<Engine> availableEngines;
    private final ApiService apiService;

    private RangeEditForm(ApiService apiService, RangeData data) {
        super(data);
        this.apiService = apiService;

        this.prefix = new PmiSelectorTree(apiService, Prefix.class);
        this.prefix.setCaption("Select prefix");
        this.prefix.setWidth(300, Sizeable.Unit.PIXELS);
        this.prefix.setHeight(300, Sizeable.Unit.PIXELS);

        this.suffixStart.setWidthFull();
        this.suffixEnd.setWidthFull();

        this.availableEngines = apiService.getEngines();
        this.engine.setItems(this.availableEngines);
        this.engine.setWidthFull();
        this.engine.setItemCaptionGenerator(Engine::getName);

        this.layout.addComponent(new FormLayout(
                this.prefix,
                this.suffixStart,
                this.suffixEnd,
                this.engine));
    }

    @Override
    public Component asComponent() {
        return this.layout;
    }

    public static final Pattern SUFFIX_PATTERN = Pattern.compile("^[0-9]{5,8}$");

    private static boolean validateSuffix(String s) {
        return StringUtils.isNotBlank(s) && SUFFIX_PATTERN.matcher(s).matches();
    }

    public static boolean validateSuffixRange(String from, String to) {
        return validateSuffix(from) && validateSuffix(to) && from.length() == to.length();
    }

    @Override
    protected void bind(Binder<RangeData> binder) {
        binder.forField(this.prefix)
                .withValidator(Objects::nonNull, "Prefix is required")
                .bind(data -> this.apiService.getPrefix(data.getPrefixId()),
                        (data, prefix) -> data.setPrefixId(prefix.getId()));

        binder.forField(this.engine)
                .bind(data -> this.availableEngines.stream()
                                .filter(e -> Long.valueOf(e.getId()).equals(data.getEngineId()))
                                .findFirst().orElse(null),
                     (data, engine) -> data.setEngineId(engine != null ? engine.getId() : null)
                );

        binder.forField(this.suffixStart)
                .withValidator(suffix -> validateSuffixRange(suffix, this.suffixEnd.getValue()),
                        "Invalid format")
                .bind(RangeData::getSuffixStart, RangeData::setSuffixStart);

        binder.forField(this.suffixEnd)
                .withValidator(suffix -> validateSuffixRange(this.suffixStart.getValue(), suffix),
                        "Invalid format")
                .bind(RangeData::getSuffixEnd, RangeData::setSuffixEnd);
    }

    public static EditWindow<RangeData> newRange(ApiService apiService, Prefix prefix, Consumer<Range> callback) {
        RangeData rangeData = new RangeData().withPrefixId(prefix.getId());
        RangeEditForm editForm = new RangeEditForm(apiService, rangeData);
        return new EditWindow<>("New range", editForm, data -> {
            Range newRange = apiService.addRange(data);
            if (callback != null) {
                callback.accept(newRange);
            }
        });
    }

    public static EditWindow<RangeData> editRange(ApiService apiService, Range range, Consumer<Range> callback) {
        RangeData rangeData = new RangeData(range);
        RangeEditForm editForm = new RangeEditForm(apiService, rangeData);
        return new EditWindow<>("Edit range attributes", editForm, data -> {
            Range updated = apiService.updateRange(range.getId(), data);
            if (callback != null) {
                callback.accept(updated);
            }
        });
    }
}
