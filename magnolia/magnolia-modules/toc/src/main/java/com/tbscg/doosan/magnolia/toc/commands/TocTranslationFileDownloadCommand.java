/**
 * This file Copyright (c) 2013-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 * <p>
 * <p>
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 * <p>
 * Any modifications to this file must keep this entire header
 * intact.
 */
package com.tbscg.doosan.magnolia.toc.commands;

import com.tbscg.doosan.magnolia.toc.io.ContentTransporter;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.Context;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.TranslationExportOperationDefinition;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.commands.TranslationFileDownloadCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.Session;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Locale;

/**
 * CUSTOMIZATION - Using custom {@link ContentTransporter}.
 *
 * Command for importing i18n-content. Previously it was possible to configure a custom {@link ContentTransporter} for
 * each command. This behavior was changed with version 2.2: If a custom form path (e.g. when used in a content app) is
 * needed, one needs to configure it here on command level. When executing the command, the form path will be set on the
 * content transporter.
 */
@Slf4j
public class TocTranslationFileDownloadCommand extends TranslationFileDownloadCommand {

    private final ContentTransporter contentTransporter;
    private final Provider<TranslationModule> translationModuleProvider;
    private final SiteManager siteManager;

    @Inject
    public TocTranslationFileDownloadCommand(ComponentProvider componentProvider,
                                             Provider<TranslationModule> translationModuleProvider,
                                             SiteManager siteManager) {
        super(componentProvider, translationModuleProvider);
        this.translationModuleProvider = translationModuleProvider;
        this.contentTransporter = componentProvider.newInstance(ContentTransporter.class);
        this.siteManager = siteManager;
    }


    @Override
    public boolean execute(Context context) throws Exception {
        final String path = getPath();
        final String workspace = getWorkspace();
        final String exporterName = getExporterName();
        final OutputStream outputStream = getOutputStream();
        final String formPath = getFormPath();

        log.debug("Starting export of website path {}.", path);

        final TranslationExportOperationDefinition translationExportOperationDefinition = translationModuleProvider.get().getExporters().get(exporterName);

        if (StringUtils.isNotBlank(formPath)) {
            contentTransporter.setFormPath(formPath);
        }

        Session session = context.getJCRSession(workspace);
        final Node node = session.getNode(path);
        final Site siteToExport = siteManager.getAssignedSite(node);
        final I18nContentSupport i18nContentSupport = siteToExport.getI18n();

        final Collection<Locale> allLocales = i18nContentSupport.getLocales();
        final Locale defaultLocale = i18nContentSupport.getDefaultLocale();
        final Locale[] supportedLocales = allLocales.toArray(new Locale[allLocales.size()]);

        contentTransporter.exportContent(translationExportOperationDefinition, node, defaultLocale, supportedLocales, outputStream);

        return true;
    }
}
