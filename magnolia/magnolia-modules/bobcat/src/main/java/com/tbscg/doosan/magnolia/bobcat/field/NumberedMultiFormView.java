package com.tbscg.doosan.magnolia.bobcat.field;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.editor.MultiFormDefinition;
import info.magnolia.ui.editor.MultiFormView;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.MultiFieldDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

/**
 * Multifield view that prefixes every entry with a running "&lt;label&gt; N"
 * caption (e.g. "Tile/Element 1", "Tile/Element 2"), so editors can tell
 * repeated composite rows apart. Stock {@link MultiFormView} renders the same
 * static label on each row with no running index.
 *
 * Wire it on a (jcr)MultiField via {@code implementationClass}. The inner
 * field's own label should be left blank in YAML so only the numbered caption
 * shows; the base text below is used as the prefix.
 */
public class NumberedMultiFormView<T> extends MultiFormView<T> {

    private static final String ENTRY_STYLE = "numbered-multi-entry";
    private static final String DEFAULT_LABEL = "Tile/Element";

    private final MultiFormDefinition<T> definition;

    @Inject
    public NumberedMultiFormView(MultiFormDefinition<T> definition, LocaleContext localeContext, Datasource<T> datasource) {
        super(definition, localeContext, datasource);
        this.definition = definition;
    }

    @Override
    protected Component wrapChildForm(EditorView<T> childForm) {
        Component entry = super.wrapChildForm(childForm);

        Label caption = new Label();
        caption.addStyleName("numbered-multi-entry-caption");

        VerticalLayout container = new VerticalLayout(caption, entry);
        container.setMargin(false);
        container.setSpacing(false);
        container.setWidth("100%");
        container.addStyleName(ENTRY_STYLE);
        return container;
    }

    @Override
    public void layout() {
        super.layout();
        renumber();
    }

    @Override
    protected void onMove(AbstractOrderedLayout layout, boolean up) {
        super.onMove(layout, up);
        renumber();
    }

    @Override
    protected void onDelete(EditorView<T> view) {
        super.onDelete(view);
        renumber();
    }

    private void renumber() {
        AbstractOrderedLayout root = getRootLayout();
        if (root == null) {
            return;
        }

        String base = baseLabel();
        int index = 0;
        for (int i = 0; i < root.getComponentCount(); i++) {
            Component component = root.getComponent(i);
            if (!(component instanceof VerticalLayout) || !component.getStyleName().contains(ENTRY_STYLE)) {
                continue;
            }
            index++;
            VerticalLayout entry = (VerticalLayout) component;
            if (entry.getComponentCount() > 0 && entry.getComponent(0) instanceof Label) {
                ((Label) entry.getComponent(0)).setValue(base + " " + index);
            }
        }
    }

    private String baseLabel() {
        if (definition instanceof MultiFieldDefinition) {
            EditorPropertyDefinition field = ((MultiFieldDefinition<T>) definition).getField();
            if (field != null && StringUtils.isNotBlank(field.getLabel())) {
                return field.getLabel();
            }
        }
        return DEFAULT_LABEL;
    }
}
