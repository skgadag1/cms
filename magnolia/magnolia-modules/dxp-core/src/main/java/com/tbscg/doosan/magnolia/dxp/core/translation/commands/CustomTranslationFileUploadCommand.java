package com.tbscg.doosan.magnolia.dxp.core.translation.commands;

import com.tbscg.doosan.magnolia.dxp.core.util.UnclosableInputStream;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.TranslationImportOperationDefinition;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.commands.TranslationFileUploadCommand;
import info.magnolia.translation.io.ContentTransporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Command for importing i18n-content. Previously it was possible to configure a custom {@link ContentTransporter} for
 * each command. This behavior was changed with version 2.2: If a custom form path (e.g. when used in a content app) is
 * needed, one needs to configure it here on command level. When executing the command, the form path will be set on the
 * content transporter.
 */
@Slf4j
public class CustomTranslationFileUploadCommand extends TranslationFileUploadCommand {

    private final ContentTransporter contentTransporter;
    private final Provider<TranslationModule> translationModuleProvider;

    @Inject
    public CustomTranslationFileUploadCommand(ComponentProvider componentProvider, Provider<TranslationModule> contentTranslationSupportModuleProvider) {
        super(componentProvider, contentTranslationSupportModuleProvider);
        this.translationModuleProvider = contentTranslationSupportModuleProvider;
        this.contentTransporter = componentProvider.newInstance(ContentTransporter.class);
    }

    @Override
    public boolean execute(Context context) throws Exception {
        final InputStream inputStream = getFileInputStream();

        Objects.requireNonNull(inputStream, "Cannot import file because input stream is null.");

        final String workspace = (String) context.get(Context.ATTRIBUTE_REPOSITORY);
        final Session session = context.getJCRSession(workspace);

        final String uploadFileName = getFile();

        if (uploadFileName.endsWith(".zip")) {
            ZipEntry entry;
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                List<String> imported = new ArrayList<>();
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    final String filename = entry.getName();

                    log.debug("Starting import from ZIP file {} with importEmptyValues = {} and overwriteExistingValues = {}.", filename, getImportEmptyValues(), getOverwriteExistingValues());

                    final TranslationImportOperationDefinition importOperationDefinition = getTranslationImportOperationDefinition(filename);
                    if (importOperationDefinition != null) {
                        importFile(importOperationDefinition, session, new UnclosableInputStream(zipInputStream));
                        imported.add(entry.getName());
                    }
                }
                if (imported.isEmpty()) {
                    throw new IllegalArgumentException(String.format("No supported imports found in zip file [%s]", uploadFileName));
                } else {
                    log.info(String.format("Translations imported from [%s]: [%s]", uploadFileName, Arrays.toString(imported.toArray())));
                }
            }
        } else {
            log.debug("Starting import from file {} with importEmptyValues = {} and overwriteExistingValues = {}.", uploadFileName, getImportEmptyValues(), getOverwriteExistingValues());

            final TranslationImportOperationDefinition importOperationDefinition = getTranslationImportOperationDefinition(uploadFileName);
            if (importOperationDefinition != null) {
                importFile(importOperationDefinition, session, inputStream);
            } else {
                throw new IllegalArgumentException(String.format("Unknown file format. Import operation for file [%s] not found.", uploadFileName));
            }
        }

        return true;
    }

    private void importFile(TranslationImportOperationDefinition importOperationDefinition, final Session session, final InputStream inputStream) throws Exception {
        contentTransporter.importContent(importOperationDefinition, session, inputStream, getOverwriteExistingValues(), getImportEmptyValues());
    }

    private TranslationImportOperationDefinition getTranslationImportOperationDefinition(String fileName) {
        final Map<String, TranslationImportOperationDefinition> importers = translationModuleProvider.get().getImporters();
        for (TranslationImportOperationDefinition importOperationDefinition : importers.values()) {
            if (StringUtils.endsWith(fileName, importOperationDefinition.getExtension())) {
                return importOperationDefinition;
            }
        }
        return null;
    }
}
