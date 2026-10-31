package com.tbscg.doosan.magnolia.mediatracker.ui;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

public class CaptionedLabel extends Label {

    public CaptionedLabel(String caption) {
        setCaption(caption);
    }

    public CaptionedLabel(String caption, Object value) {
        super(getStringValue(value));
        setCaption(caption);
    }

    private static String getStringValue(Object o) {
        return o != null ? String.valueOf(o) : "";
    }

    public CaptionedLabel(String caption, String text, ContentMode contentMode) {
        super(text, contentMode);
        setCaption(caption);
    }
}
