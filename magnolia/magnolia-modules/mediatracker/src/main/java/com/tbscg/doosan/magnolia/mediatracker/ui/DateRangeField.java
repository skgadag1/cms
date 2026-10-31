package com.tbscg.doosan.magnolia.mediatracker.ui;

import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;

public class DateRangeField extends HorizontalLayout {

    private final DateField from = new DateField();
    private final DateField to = new DateField();

    public DateRangeField(String caption) {
        this.from.setDateFormat(DateFormatUtil.DATE_FORMAT);
        this.to.setDateFormat(DateFormatUtil.DATE_FORMAT);
        setCaption(caption);
        setSpacing(true);
        addComponent(new CaptionedField("From", this.from));
        addComponent(new CaptionedField("To", this.to));
    }

    public DateField getFrom() {
        return from;
    }

    public DateField getTo() {
        return to;
    }
}
