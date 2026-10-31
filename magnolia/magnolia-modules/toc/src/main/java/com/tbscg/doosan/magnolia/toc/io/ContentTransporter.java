/**
 * This file Copyright (c) 2017-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.toc.io;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.TranslationExportOperationDefinition;
import info.magnolia.translation.TranslationImportOperationDefinition;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.finder.AdaptivePropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertiesToTranslateFinder;
import info.magnolia.translation.io.TranslationItem;
import info.magnolia.translation.io.reader.TranslationItemReader;
import info.magnolia.translation.io.writer.TranslationItemWriter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * CUSTOMIZATION - Uses Custom {@link ExportVisitor} and {@link TranslationItemImporter}.
 *
 * Content transporter that imports or exports translated contents to/from a repository.
 */
public class ContentTransporter {

    private final SiteManager siteManager;
    private final ComponentProvider componentProvider;
    private final Provider<TranslationModule> translationModuleProvider;

    @Setter
    private String formPath;

    @Inject
    public ContentTransporter(SiteManager siteManager, ComponentProvider componentProvider, Provider<TranslationModule> translationModuleProvider) {
        this.siteManager = siteManager;
        this.componentProvider = componentProvider;
        this.translationModuleProvider = translationModuleProvider;
    }

    /**
     * @deprecated since 2.5.1. Use {@link #exportContent(TranslationExportOperationDefinition translationExportOperationDefinition, Node node, Locale sourceLocale, Locale[] targetLocales, OutputStream outputStream)} instead.
     */
    @Deprecated
    public void exportContent(TranslationExportOperationDefinition translationExportOperationDefinition,
                              Session session,
                              String basePath,
                              OutputStream outputStream) throws IOException, RepositoryException {

        final Node node = session.getNode(basePath);
        final Site siteToExport = siteManager.getAssignedSite(node);
        final I18nContentSupport i18nContentSupport = siteToExport.getI18n();
        final Collection<Locale> allLocales = i18nContentSupport.getLocales();
        final Locale fallbackLocale = i18nContentSupport.getFallbackLocale();

        this.exportContent(translationExportOperationDefinition, node, fallbackLocale, allLocales.toArray(new Locale[0]), outputStream);
    }

    /**
     * Exports content at the specified base path from given {@link Node node} to the specified output stream.
     */
    public void exportContent(TranslationExportOperationDefinition translationExportOperationDefinition,
                              Node node,
                              Locale sourceLocale,
                              Locale[] targetLocales,
                              OutputStream outputStream) throws IOException, RepositoryException {

        final Site siteToExport = siteManager.getAssignedSite(node);
        final I18nContentSupport i18nContentSupport = siteToExport.getI18n();

        final List<Locale> exportLocales = Arrays.asList(targetLocales);
        // in case target locales contains source locale, remove it
        final Locale[] targetLocalesExcludingSource = exportLocales
                .stream()
                .filter(l -> !l.equals(sourceLocale))
                .toArray(Locale[]::new);

        // Export the content properties
        PropertiesToTranslateFinder propertiesToTranslateFinder = translationModuleProvider.get().getPropertiesToTranslateFinder();
        if (propertiesToTranslateFinder instanceof AdaptivePropertiesToTranslateFinder) {
            propertiesToTranslateFinder = formPath == null ?
                    componentProvider.newInstance(propertiesToTranslateFinder.getClass()) :
                    componentProvider.newInstance(propertiesToTranslateFinder.getClass(), formPath);
        }

        try (final TranslationItemWriter translationItemWriter = componentProvider.newInstance(
                translationExportOperationDefinition.getWriterImplementationClass(),
                i18nContentSupport, outputStream, targetLocalesExcludingSource, sourceLocale)) {

            final ExportVisitor exportVisitor = new ExportVisitor(sourceLocale, i18nContentSupport.getFallbackLocale(), targetLocalesExcludingSource, translationItemWriter, propertiesToTranslateFinder);

            translationItemWriter.writeHeaderRow(sourceLocale);

            NodeUtil.visit(node, exportVisitor);
        }
    }

    /**
     * Imports content from the specified input stream into given {@link Session session}.
     */
    public void importContent(TranslationImportOperationDefinition translationImportOperationDefinition,
                              Session session,
                              InputStream inputStream,
                              boolean overwriteExistingValues,
                              boolean writeEmptyValues) throws IOException, RepositoryException {

        try (final TranslationItemReader translationItemReader = componentProvider.newInstance(
                translationImportOperationDefinition.getReaderImplementationClass(),
                inputStream, StandardCharsets.UTF_8.name())) {

            TranslationItem[] translationItems = translationItemReader.read();

            // Import the updates
            TranslationItemImporter importer = new TranslationItemImporter(session, overwriteExistingValues, writeEmptyValues);

            for (TranslationItem translationItem : translationItems) {
                importer.applyUpdate(translationItem);
            }

            session.save();
        }
    }
}
