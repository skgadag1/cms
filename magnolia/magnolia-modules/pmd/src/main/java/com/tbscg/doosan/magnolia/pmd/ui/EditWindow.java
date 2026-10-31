package com.tbscg.doosan.magnolia.pmd.ui;

import com.vaadin.data.ValidationException;
import com.vaadin.data.ValidationResult;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import info.magnolia.ui.vaadin.server.ErrorMessageUtil;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditWindow<T> extends Window {

    private final Consumer<T> saveHandler;
    private final EditForm<T> form;

    public EditWindow(String caption, EditForm<T> form, Consumer<T> saveHandler) {
        super(caption);
        this.saveHandler = saveHandler;
        this.form = form;
        this.form.bind();
        center();
        setModal(true);
        setWidth(800, Unit.PIXELS);
        setHeight(90, Unit.PERCENTAGE);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        Panel scrollPanel = new Panel(form.asComponent());
        scrollPanel.setSizeFull();
        layout.addComponentsAndExpand(scrollPanel);
        layout.addComponent(getButtonBar());
        setContent(layout);
    }

    private ButtonBar getButtonBar() {
        Button saveButton = new Button("Save", event -> {
            try {
                T data = this.form.getData();
                if (this.saveHandler != null) {
                    this.saveHandler.accept(data);
                }
                close();
            } catch (Exception e) {
                if (e instanceof ValidationException) {
                    ValidationException validationException = (ValidationException) e;
                    String desc = validationException.getValidationErrors().stream()
                            .map(ValidationResult::getErrorMessage)
                            .map(m -> "<li>" + m + "</li>")
                            .collect(Collectors.joining());
                    Notification notification = new Notification(e.getMessage(), "<ul>" + desc + "</ul>",
                            Notification.Type.WARNING_MESSAGE,true);
                    notification.setDelayMsec(5000);
                    notification.show(Page.getCurrent());
                } else {
                    Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        });
        Button cancelButton = new Button("Cancel", e -> close());
        return new ButtonBar(saveButton, cancelButton);
    }
}
