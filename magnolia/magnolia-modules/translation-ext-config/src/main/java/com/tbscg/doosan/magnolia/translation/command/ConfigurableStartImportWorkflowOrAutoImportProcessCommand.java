package com.tbscg.doosan.magnolia.translation.command;

import com.tbscg.doosan.magnolia.translation.importer.ExcludedFieldOverwriteImportSupport;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.SimpleContext;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.scheduler.SchedulerModule;
import info.magnolia.translation.ext.core.TranslationConstants;
import info.magnolia.translation.ext.core.command.StartImportWorkflowOrAutoImportProcessCommand;
import info.magnolia.translation.ext.core.definition.TranslationProviderDefinition;
import info.magnolia.translation.ext.core.definition.TranslationProviderDefinitionRegistry;
import info.magnolia.translation.ext.core.exception.CommandException;
import info.magnolia.translation.ext.core.helper.XliffTransporter;
import info.magnolia.translation.ext.core.model.TranslationSubmission;
import info.magnolia.translation.ext.core.persistance.SubmissionStore;
import info.magnolia.translation.ext.core.service.HistoryService;
import info.magnolia.ui.framework.message.MessagesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.util.Objects;

/**
 * CTSX polling command that keeps excluded fields aligned with the source value during auto-import.
 */
public class ConfigurableStartImportWorkflowOrAutoImportProcessCommand extends StartImportWorkflowOrAutoImportProcessCommand {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableStartImportWorkflowOrAutoImportProcessCommand.class);

    private static final String TRANSLATION_IMPORT_WORKFLOW_CATALOG = "workflowTranslation";
    private static final String TRANSLATION_IMPORT_WORKFLOW_COMMAND = "launchTranslationImportWorkflowCommand";
    private static final String COMMAND_NAME = "startImportWorkflowOrAutoImportProcessCommand";

    private final ExcludedFieldOverwriteImportSupport excludedFieldOverwriteImportSupport;

    @Inject
    public ConfigurableStartImportWorkflowOrAutoImportProcessCommand(HistoryService historyService,
                                                                     MailModule mailModule,
                                                                     SchedulerModule schedulerModule,
                                                                     MessagesManager messagesManager,
                                                                     SubmissionStore submissionStore,
                                                                     XliffTransporter xliffTransporter,
                                                                     TranslationProviderDefinitionRegistry translationProviderDefinitionRegistry,
                                                                     CommandsManager commandsManager,
                                                                     ExcludedFieldOverwriteImportSupport excludedFieldOverwriteImportSupport) {
        super(historyService, mailModule, schedulerModule, messagesManager, submissionStore, xliffTransporter,
                translationProviderDefinitionRegistry, commandsManager);
        this.excludedFieldOverwriteImportSupport = excludedFieldOverwriteImportSupport;
    }

    @Override
    protected boolean processSingleSubmission(TranslationSubmission submission) throws Exception {
        try {
            submission = this.submissionStore.getById(submission.getId());
            if (getProcessingStatus().equals(submission.getStatus())
                    && Boolean.FALSE.equals(submission.getImportProcessStarted())) {
                importTranslationResult(submission);
            }
        } catch (Exception e) {
            setImportProcessStarted(submission, false);
            throw new CommandException(e);
        }

        return true;
    }

    private void importTranslationResult(TranslationSubmission submission) throws Exception {
        log.info("importTranslationResult started: submissionId is [{}]", submission.getId());

        if (!Objects.requireNonNull(getTranslationById(submission.getConfigId())).isAutoTranslationAllowed()) {
            startImportWorkflow(submission);
        } else {
            autoImport(submission);
        }

        log.debug("Done importTranslationResult for [{}]", submission.getId());
    }

    private TranslationProviderDefinition getTranslationById(String providerId) {
        for (TranslationProviderDefinition def : this.translationProviderDefinitionRegistry.getAllDefinitions()) {
            if (providerId.equals(def.getConfigName())) {
                return def;
            }
        }
        return null;
    }

    private void autoImport(TranslationSubmission submission) throws RepositoryException {
        Document translatedDoc = submission.getTranslatedDocument();
        if (translatedDoc != null) {
            this.excludedFieldOverwriteImportSupport.importTranslatedContents(
                    this.submissionStore,
                    this.xliffTransporter,
                    submission,
                    translatedDoc
            );
        }
    }

    private void startImportWorkflow(TranslationSubmission submission) throws Exception {
        Command command = commandsManager.getCommand(TRANSLATION_IMPORT_WORKFLOW_CATALOG, TRANSLATION_IMPORT_WORKFLOW_COMMAND);
        if (command != null) {
            SimpleContext context = new SimpleContext();
            context.setAttribute(TranslationConstants.TRANSLATION_SUBMISSION_CONTEXT_KEY, submission, Context.SESSION_SCOPE);
            boolean workflowStarted = command.execute(context);
            if (!workflowStarted) {
                setImportProcessStarted(submission, true);
            }
        }
    }

    private void setImportProcessStarted(TranslationSubmission translationSubmission, boolean value) {
        try {
            translationSubmission = submissionStore.getById(translationSubmission.getId());
            translationSubmission.setImportProcessStarted(value);
            submissionStore.save(translationSubmission);
        } catch (RepositoryException e) {
            log.error("Failed to set import workflow check");
        }
    }

    @Override
    protected String getCommandName() {
        return COMMAND_NAME;
    }
}
