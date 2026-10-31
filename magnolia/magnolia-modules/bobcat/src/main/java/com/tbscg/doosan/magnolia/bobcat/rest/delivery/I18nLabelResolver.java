package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Optional;

@Singleton
public class I18nLabelResolver {

    private final I18nContentSupport i18nContentSupport;

    @Inject
    public I18nLabelResolver(final I18nContentSupport i18nContentSupport) {
        this.i18nContentSupport = i18nContentSupport;
    }

    public LabelLookup forProperty(final String propertyName, final Locale locale) {
        return new LabelLookup(propertyName, locale);
    }

    public final class LabelLookup {
        private final String propertyName;
        private final Locale locale;

        private LabelLookup(final String propertyName, final Locale locale) {
            this.propertyName = propertyName;
            this.locale = locale;
        }

        public Optional<String> resolve(final Node node) {
            try {
                if (locale != null) {
                    final Property localizedProperty = i18nContentSupport.getProperty(node, propertyName, locale);
                    final String localizedTitle = localizedProperty != null ? localizedProperty.getString() : null;
                    if (!StringUtils.isBlank(localizedTitle)) {
                        return Optional.of(localizedTitle);
                    }
                }

                final String nonLocalizedTitle = PropertyUtil.getString(node, propertyName);
                if (!StringUtils.isBlank(nonLocalizedTitle)) {
                    return Optional.of(nonLocalizedTitle);
                }
            } catch (RepositoryException e) {
                return Optional.empty();
            }
            return Optional.empty();
        }
    }
}
