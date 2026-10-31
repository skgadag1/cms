package com.tbscg.doosan.magnolia.pmd.ui;

import com.tbscg.doosan.magnolia.pmd.data.model.DocumentFileData;
import com.tbscg.doosan.magnolia.pmd.data.model.Locale;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.HttpService;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Upload.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class LocalFileUpload
        extends Window
        implements Receiver, SucceededListener, StartedListener, FinishedListener, FailedListener {

    public static interface UploadUrlProvider {
        String getUploadUrl(DocumentFileData data);
    }

    private final UploadUrlProvider uploadUrlProvider;
    private final Runnable saveHandler;
    private final ComboBox<Locale> language = new ComboBox<>("Language");
    private final Upload upload = new Upload();
    private final ProgressBar progressBar = new ProgressBar();
    private final Button submitButton = new Button("Submit", VaadinIcons.UPLOAD);
    private String inputFileName;
    private File tempFile;

    public LocalFileUpload(UploadUrlProvider uploadUrlProvider, List<Locale> locales, Runnable saveHandler) {
        this.uploadUrlProvider = uploadUrlProvider;
        this.saveHandler = saveHandler;

        this.language.setEmptySelectionAllowed(false);
        this.language.setItems(locales);
        this.language.setSelectedItem(locales.stream().filter(Locale::isFallback).findFirst().orElse(null));
        this.language.setItemCaptionGenerator(Locale::getName);
        this.language.addValueChangeListener(e -> validate());

        this.upload.setCaption("Document file");
        this.upload.setImmediateMode(false);
        this.upload.setButtonCaption(null);
        this.upload.setReceiver(this);
        this.upload.addStartedListener(this);
        this.upload.addSucceededListener(this);
        this.upload.addFailedListener(this);
        this.upload.addFinishedListener(this);
        this.upload.addChangeListener(e -> {
            this.inputFileName = e.getFilename();
            validate();
        });

        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisible(false);

        this.submitButton.setEnabled(false);
        this.submitButton.addClickListener(e -> {
            this.submitButton.setEnabled(false);
            this.upload.submitUpload();
        });

        center();
        setModal(true);
        setWidth(600, Unit.PIXELS);
        setHeight(300, Unit.PIXELS);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponentsAndExpand(new FormLayout(this.language, this.upload), this.progressBar);
        layout.addComponent(this.submitButton);
        setContent(layout);
    }

    private void validate() {
        boolean langSelected = this.language.getSelectedItem().isPresent();
        boolean fileSelected = StringUtils.isNotEmpty(this.inputFileName);
        this.submitButton.setEnabled(fileSelected && langSelected);
    }

    public void refresh() {
        this.inputFileName = null;
        validate();
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        try {
            this.tempFile = File.createTempFile(filename + "_", null);
            return new FileOutputStream(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadSucceeded(SucceededEvent event) {
        if (event.getLength() > 0) {
            DocumentFileData data = new DocumentFileData();
            data.setFileName(event.getFilename());
            data.setContentType(event.getMIMEType());
            data.setLocale(this.language.getSelectedItem().map(Locale::getCode).orElse("en-US"));

            String uploadUrl = this.uploadUrlProvider.getUploadUrl(data);

            HttpService s3 = new HttpService(uploadUrl);
            s3.putFile("", this.tempFile, data.getContentType());

            close();
            if (this.saveHandler != null) {
                this.saveHandler.run();
            }
        } else {
            Notification.show("ERROR", "File is empty", Type.ERROR_MESSAGE);
            refresh();
        }
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        Notification.show("ERROR", event.getReason().toString(), Type.ERROR_MESSAGE);
    }

    @Override
    public void uploadFinished(FinishedEvent event) {
        if (this.tempFile != null && this.tempFile.exists()) {
            this.tempFile.delete();
        }
        this.progressBar.setVisible(false);
        this.submitButton.setEnabled(true);
    }

    @Override
    public void uploadStarted(StartedEvent event) {
        this.progressBar.setVisible(true);
    }
}
