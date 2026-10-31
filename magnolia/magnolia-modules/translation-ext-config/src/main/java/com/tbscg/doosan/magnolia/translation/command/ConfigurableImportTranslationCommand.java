package com.tbscg.doosan.magnolia.translation.command;

import com.tbscg.doosan.magnolia.translation.importer.ExcludedFieldOverwriteImportSupport;
import info.magnolia.translation.ext.core.command.ImportTranslationCommand;
import info.magnolia.translation.ext.core.exception.CommandException;
import info.magnolia.translation.ext.core.helper.XliffTransporter;
import info.magnolia.translation.ext.core.model.TranslationSubmission;
import info.magnolia.translation.ext.core.persistance.SubmissionStore;
import info.magnolia.translation.ext.core.translator.AsynchronousTranslator;
import info.magnolia.translation.ext.core.translator.Translator;
import info.magnolia.translation.ext.core.translator.TranslatorProviderRegistry;
import info.magnolia.ui.framework.message.MessagesManager;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

/**
 * CTSX import command that also normalizes excluded fields when overwrite is enabled.
 */
public class ConfigurableImportTranslationCommand extends ImportTranslationCommand {

    private final XliffTransporter xliffTransporter;
    private final SubmissionStore submissionStore;
    private final TranslatorProviderRegistry translatorProviderRegistry;
    private final ExcludedFieldOverwriteImportSupport excludedFieldOverwriteImportSupport;

    @Inject
    public ConfigurableImportTranslationCommand(XliffTransporter xliffTransporter,
                                                SubmissionStore submissionStore,
                                                MessagesManager messagesManager,
                                                TranslatorProviderRegistry translatorProviderRegistry,
                                                ExcludedFieldOverwriteImportSupport excludedFieldOverwriteImportSupport) {
        super(xliffTransporter, submissionStore, messagesManager, translatorProviderRegistry);
        this.xliffTransporter = xliffTransporter;
        this.submissionStore = submissionStore;
        this.translatorProviderRegistry = translatorProviderRegistry;
        this.excludedFieldOverwriteImportSupport = excludedFieldOverwriteImportSupport;
    }

    @Override
    public void doImport(TranslationSubmission submission) throws CommandException {
        try {
            Document translatedDoc = submission.getTranslatedDocument();
            if (translatedDoc == null) {
                translatedDoc = this.submissionStore.getById(submission.getId()).getTranslatedDocument();
            }

            this.excludedFieldOverwriteImportSupport.importTranslatedContents(
                    this.submissionStore,
                    this.xliffTransporter,
                    submission,
                    translatedDoc
            );

            Translator translator = this.translatorProviderRegistry.getTranslatorById(submission.getConfigId());
            if (translator instanceof AsynchronousTranslator) {
                ((AsynchronousTranslator) translator).sendDownloadConfirmation(submission.getId());
            }
        } catch (RepositoryException e) {
            throw new CommandException("A JCR exception occurred while importing the translation", e);
        }
    }
}
