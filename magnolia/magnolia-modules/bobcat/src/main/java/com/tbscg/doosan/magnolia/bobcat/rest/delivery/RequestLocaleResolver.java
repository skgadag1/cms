package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.Context;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class RequestLocaleResolver {

    private final I18nContentSupport i18nContentSupport;
    private final Provider<Context> contextProvider;

    @Inject
    public RequestLocaleResolver(final I18nContentSupport i18nContentSupport,
                                 final Provider<Context> contextProvider) {
        this.i18nContentSupport = i18nContentSupport;
        this.contextProvider = contextProvider;
    }

    public Locale resolve() {
        final Locale localeFromI18n = i18nContentSupport.getLocale();
        if (localeFromI18n != null) {
            return localeFromI18n;
        }
        return contextProvider.get().getLocale();
    }
}
