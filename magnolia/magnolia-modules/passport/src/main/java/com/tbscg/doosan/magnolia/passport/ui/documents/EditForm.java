package com.tbscg.doosan.magnolia.passport.ui.documents;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.ui.Component;

public abstract class EditForm<BEAN> {

    private final Binder<BEAN> binder = new Binder<>();
    private final BEAN data;

    public EditForm(BEAN data) {
        this.data = data;
    }

    public BEAN getData() throws ValidationException {
        this.binder.writeBean(this.data);
        return data;
    }

    public abstract Component asComponent();

    protected abstract void bind(Binder<BEAN> binder);

    public void bind() {
        bind(this.binder);
        this.binder.readBean(this.data);
    }
}
