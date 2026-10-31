package com.tbscg.doosan.magnolia.translation.importer;

import info.magnolia.translation.ext.core.model.I18nItem;
import info.magnolia.translation.ext.core.model.TranslationSubmission;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves which content nodes/config paths must be re-scanned to apply excluded-field overwrite rules.
 */
@NoArgsConstructor
final class ExcludedFieldTraversalSpecResolver {

    static List<ExcludedFieldTraversalSpec> resolve(TranslationSubmission submission) {
        Set<ExcludedFieldTraversalSpec> specs = new LinkedHashSet<>();

        List<I18nItem> i18nFields = submission.getI18nFields();
        if (i18nFields != null) {
            boolean hasBlankConfigPath = i18nFields.stream()
                    .anyMatch(item -> StringUtils.isBlank(item.getConfigPath()));

            if (hasBlankConfigPath) {
                specs.add(new ExcludedFieldTraversalSpec(submission.getContentId(), null, true));
            }

            i18nFields.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getConfigPath()))
                    .forEach(item -> specs.add(new ExcludedFieldTraversalSpec(
                            extractNodeIdentifier(item, submission.getContentId()),
                            item.getConfigPath(),
                            false
                    )));
        }

        if (specs.isEmpty()) {
            specs.add(new ExcludedFieldTraversalSpec(submission.getContentId(), null, true));
        }

        return new ArrayList<>(specs);
    }

    private static String extractNodeIdentifier(I18nItem item, String fallbackIdentifier) {
        if (StringUtils.isBlank(item.getKey())) {
            return fallbackIdentifier;
        }

        String[] keyParts = item.getKey().split(":", 2);
        return keyParts.length > 0 && StringUtils.isNotBlank(keyParts[0]) ? keyParts[0] : fallbackIdentifier;
    }

    record ExcludedFieldTraversalSpec(String nodeIdentifier, String configPath, boolean recursive){}
}
