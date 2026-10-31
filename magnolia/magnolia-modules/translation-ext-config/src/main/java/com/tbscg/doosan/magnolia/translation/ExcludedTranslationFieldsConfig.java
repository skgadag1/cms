package com.tbscg.doosan.magnolia.translation;

import com.tbscg.doosan.magnolia.translation.finder.ExcludedTranslationFieldsMatcher;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.Set;

/**
 * Reads the CTSX field-exclusion configuration from Magnolia config.
 */
@NoArgsConstructor
public final class ExcludedTranslationFieldsConfig {

    private static final Logger log = LoggerFactory.getLogger(ExcludedTranslationFieldsConfig.class);

    public static final String CTSX_CORE_CONFIG_PATH = "/modules/content-translation-support-ext-core/config";
    public static final String EXCLUDED_FIELDS_PROPERTY = "excludedFields";

    public static Set<String> getExcludedFields() {
        try {
            Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
            if (!configSession.nodeExists(CTSX_CORE_CONFIG_PATH)) {
                return Collections.emptySet();
            }

            Node configNode = configSession.getNode(CTSX_CORE_CONFIG_PATH);
            return ExcludedTranslationFieldsMatcher.parseCsv(
                    PropertyUtil.getString(configNode, EXCLUDED_FIELDS_PROPERTY, "")
            );
        } catch (RepositoryException e) {
            log.warn("Failed to read translation exclusions from '{}'.", CTSX_CORE_CONFIG_PATH, e);
            return Collections.emptySet();
        }
    }
}
