package com.tbscg.doosan.magnolia.mediatracker.ui;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

public class ButtonBar extends HorizontalLayout {

    public ButtonBar(Component... buttons) {
        super();
        setWidth(100, Sizeable.Unit.PERCENTAGE);
        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        addComponents(buttons);
        addComponentsAndExpand(new Panel());
    }
}
