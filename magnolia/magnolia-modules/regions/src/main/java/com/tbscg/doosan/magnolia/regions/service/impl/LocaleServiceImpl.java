package com.tbscg.doosan.magnolia.regions.service.impl;

import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalesWrapper;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Singleton
public class LocaleServiceImpl implements LocaleService {

    private final SiteManager siteManager;

    @Inject
    public LocaleServiceImpl(SiteManager siteManager) {
        this.siteManager = siteManager;
    }

    public LocalesWrapper resolveLocales(Node fromNode) {
        Site site = siteManager.getAssignedSite(fromNode);
        if (site.getI18n() != null) {
            I18nContentSupport i18n = site.getI18n();
            Locale defaultLocale = i18n.getDefaultLocale();
            return new LocalesWrapper(new ArrayList<>(i18n.getLocales()), defaultLocale);
        } else {
            Locale englishLocale = new Locale(Locale.ENGLISH.getLanguage());
            List<Locale> locales = new ArrayList<>();
            locales.add(englishLocale);
            return new LocalesWrapper(locales, englishLocale);
        }
    }

    @Override
    public boolean isLocalized(Node fromNode) {
        Site site = siteManager.getAssignedSite(fromNode);
        //we could alternatively check if there is more than 1 locale
        return site != null && site.getI18n() != null;
    }
}
