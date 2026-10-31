package com.tbscg.doosan.magnolia.translation.importer;

import com.tbscg.doosan.magnolia.translation.ExcludedTranslationFieldsConfig;
import com.tbscg.doosan.magnolia.translation.finder.ExcludedTranslationFieldsMatcher;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.ext.core.TranslationConstants;
import info.magnolia.translation.ext.core.TranslationStatus;
import info.magnolia.translation.ext.core.finder.AdaptivePropertiesToTranslateFinderExt;
import info.magnolia.translation.ext.core.helper.XliffTransporter;
import info.magnolia.translation.ext.core.io.I18nItemTranslationItemWriter;
import info.magnolia.translation.ext.core.model.I18nItem;
import info.magnolia.translation.ext.core.model.TranslationSubmission;
import info.magnolia.translation.ext.core.persistance.SubmissionStore;
import info.magnolia.translation.ext.core.predicate.ExcludeNodeTypePredicate;
import info.magnolia.translation.ext.core.util.TranslationUtils;
import info.magnolia.translation.finder.PropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.translation.io.ExportVisitor;
import info.magnolia.translation.io.TranslationItem;
import info.magnolia.translation.io.TranslationItemImporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Imports translated content and, when overwrite is enabled, mirrors the source value into excluded target fields.
 */
@Slf4j
public class ExcludedFieldOverwriteImportSupport {
    private final ComponentProvider componentProvider;
    private final SiteManager siteManager;

    @Inject
    public ExcludedFieldOverwriteImportSupport(ComponentProvider componentProvider, SiteManager siteManager) {
        this.componentProvider = componentProvider;
        this.siteManager = siteManager;
    }

    public void importTranslatedContents(SubmissionStore submissionStore,
                                         XliffTransporter xliffTransporter,
                                         TranslationSubmission submission,
                                         Document translatedDoc) throws RepositoryException {
        if (!TranslationStatus.TRANSLATED.toString().equals(submission.getStatus())) {
            return;
        }

        BatchImportOptions options = resolveBatchImportOptions(submission.getBatchId());
        Session contentSession = MgnlContext.getJCRSession(submission.getContentWorkspace());

        xliffTransporter.importXliff(translatedDoc, contentSession, options.overwriteExistingNodes, options.writeEmptyNodes, false);

        if (options.overwriteExistingNodes) {
            overwriteExcludedFields(contentSession, submission, options.writeEmptyNodes);
        }

        contentSession.save();
        TranslationUtils.saveMoveAndUpdateStatus(
                submissionStore,
                submission,
                TranslationStatus.IMPORTED.toString(),
                TranslationConstants.IMPORTED_QUEUE_PATH
        );
    }

    private void overwriteExcludedFields(Session contentSession,
                                         TranslationSubmission submission,
                                         boolean writeEmptyNodes) throws RepositoryException {
        Set<String> excludedFields = ExcludedTranslationFieldsConfig.getExcludedFields();
        if (excludedFields.isEmpty()) {
            return;
        }

        Locale sourceLocale = TranslationUtils.toLocale(submission.getSourceLanguage());
        Locale targetLocale = TranslationUtils.toLocale(submission.getTargetLanguage());
        if (sourceLocale.equals(targetLocale)) {
            return;
        }

        Map<String, I18nItem> excludedItemsByKey = new LinkedHashMap<>();
        for (ExcludedFieldTraversalSpecResolver.ExcludedFieldTraversalSpec spec : ExcludedFieldTraversalSpecResolver.resolve(submission)) {
            if (StringUtils.isBlank(spec.nodeIdentifier())) {
                continue;
            }

            Node node = contentSession.getNodeByIdentifier(spec.nodeIdentifier());
            collectExcludedItems(node, spec, sourceLocale, targetLocale, excludedFields)
                    .forEach(item -> excludedItemsByKey.putIfAbsent(item.getKey(), item));
        }

        if (excludedItemsByKey.isEmpty()) {
            return;
        }

        TranslationItemImporter importer = new TranslationItemImporter(contentSession, true, writeEmptyNodes);
        for (I18nItem item : excludedItemsByKey.values()) {
            TranslationItem translationItem = new TranslationItem.Builder(
                    item.getKey(),
                    item.getMasterValue(),
                    sourceLocale,
                    item.getContext()
            ).withLocalizedValues(Collections.singletonMap(targetLocale, item.getMasterValue()))
                    .build();
            importer.applyUpdate(translationItem);
        }

        log.debug("Overwrote {} excluded translation field(s) for submission [{}].",
                excludedItemsByKey.size(), submission.getId());
    }

    private List<I18nItem> collectExcludedItems(Node node,
                                                ExcludedFieldTraversalSpecResolver.ExcludedFieldTraversalSpec spec,
                                                Locale sourceLocale,
                                                Locale targetLocale,
                                                Set<String> excludedFields) throws RepositoryException {
        PropertiesToTranslateFinder finder = resolveFinder(spec.configPath());
        PropertiesToTranslateFinder excludedOnlyFinder = new ExcludedOnlyPropertiesToTranslateFinder(finder, excludedFields);

        Site site = siteManager.getAssignedSite(node);
        I18nContentSupport i18nContentSupport = site.getI18n();
        I18nItemTranslationItemWriter writer = new I18nItemTranslationItemWriter(
                null,
                node.getSession().getWorkspace().getName(),
                spec.configPath(),
                node.getPath()
        );
        ExportVisitor visitor = new ExportVisitor(
                sourceLocale,
                i18nContentSupport.getDefaultLocale(),
                new Locale[]{targetLocale},
                writer,
                excludedOnlyFinder
        );

        if (spec.recursive()) {
            NodeUtil.visit(node, visitor, new ExcludeNodeTypePredicate(node.getPrimaryNodeType().getName()));
        } else {
            visitor.visit(node);
        }

        return writer.getItems();
    }

    private PropertiesToTranslateFinder resolveFinder(String configPath) {
        return StringUtils.isBlank(configPath)
                ? componentProvider.newInstance(AdaptivePropertiesToTranslateFinderExt.class)
                : componentProvider.newInstance(AdaptivePropertiesToTranslateFinderExt.class, configPath);
    }

    private BatchImportOptions resolveBatchImportOptions(String batchId) {
        boolean overwriteExistingNodes = true;
        boolean writeEmptyNodes = true;

        try {
            Session session = MgnlContext.getJCRSession(TranslationConstants.Workspace.TRANSLATION_BATCHES);
            Node batchNode = session.getNodeByIdentifier(batchId);
            overwriteExistingNodes = PropertyUtil.getBoolean(batchNode, TranslationConstants.OVERWRITE_EXISTING_NODES, true);
            writeEmptyNodes = PropertyUtil.getBoolean(batchNode, TranslationConstants.WRITE_EMPTY_NODES, true);
        } catch (RepositoryException e) {
            log.error("Error retrieving batch [{}]. Falling back to overwriteExistingNodes=true and writeEmptyNodes=true.", batchId, e);
        }

        return new BatchImportOptions(overwriteExistingNodes, writeEmptyNodes);
    }

    private record BatchImportOptions(boolean overwriteExistingNodes, boolean writeEmptyNodes) {

    }

    private record ExcludedOnlyPropertiesToTranslateFinder(PropertiesToTranslateFinder delegate,
                                                           Set<String> excludedFields)
            implements PropertiesToTranslateFinder {

        @Override
            public List<PropertyToTranslate> getPropertiesToTranslate(Node node) {
                return delegate.getPropertiesToTranslate(node).stream()
                        .filter(property -> ExcludedTranslationFieldsMatcher.matches(
                                excludedFields, property.getPropertyName()))
                        .collect(Collectors.toList());
            }
        }
}
