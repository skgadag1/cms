package com.tbscg.doosan.magnolia.pmd.ui;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import lombok.Getter;

@Getter
public class Header extends HorizontalLayout {

    private final Label titleLabel = new Label();

    public Header() {
        this.titleLabel.addStyleName("heading-2");
        VerticalLayout left = new VerticalLayout();
        left.addComponent(titleLabel);
        addComponent(left);
        setWidthFull();
    }

    public Header(String title) {
        this();
        setValue(title);
    }

    public void setValue(String value) {
        this.titleLabel.setValue(value);
    }
}
