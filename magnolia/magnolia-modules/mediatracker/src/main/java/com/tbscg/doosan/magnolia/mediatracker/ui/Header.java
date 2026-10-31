package com.tbscg.doosan.magnolia.mediatracker.ui;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;

@Getter
public class Header extends HorizontalLayout {

    private final ComboBox<Division> divisionSelect = new ComboBox<>();

    public Header(String title, Division[] divisions) {
        VerticalLayout left = new VerticalLayout();

        VerticalLayout right = new VerticalLayout();
        right.setWidth(100, Sizeable.Unit.PERCENTAGE);
        right.setDefaultComponentAlignment(Alignment.TOP_RIGHT);

        Label titleLabel = new Label(title);
        titleLabel.addStyleName("heading-2");
        left.addComponent(titleLabel);

        divisionSelect.setItems(divisions);
        divisionSelect.setEmptySelectionAllowed(false);
        right.addComponent(new CaptionedField("Division", divisionSelect));

        addComponent(left);
        addComponent(right);
        setWidth(100, Sizeable.Unit.PERCENTAGE);
    }

    public ComboBox<Division> getDivisionSelect() {
        return divisionSelect;
    }
}
