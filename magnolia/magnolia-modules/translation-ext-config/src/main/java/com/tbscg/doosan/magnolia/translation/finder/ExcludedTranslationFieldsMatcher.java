package com.tbscg.doosan.magnolia.translation.finder;

import info.magnolia.translation.finder.PropertyToTranslate;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses the configured CSV exclusion list and matches it against Magnolia translation property names.
 */
@NoArgsConstructor
public final class ExcludedTranslationFieldsMatcher {
    public static Set<String> parseCsv(String excludedFieldsCsv) {
        if (excludedFieldsCsv == null || excludedFieldsCsv.trim().isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(excludedFieldsCsv.split(","))
                .map(ExcludedTranslationFieldsMatcher::normalizePropertyName)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean matches(Set<String> excludedFields, String propertyName) {
        if (excludedFields == null || excludedFields.isEmpty()) {
            return false;
        }

        String normalizedPropertyName = normalizePropertyName(propertyName);
        if (normalizedPropertyName.isEmpty()) {
            return false;
        }

        if (excludedFields.contains(normalizedPropertyName)) {
            return true;
        }

        return excludedFields.contains(extractLeafFieldName(normalizedPropertyName));
    }

    static String normalizePropertyName(String propertyName) {
        if (propertyName == null) {
            return "";
        }

        String normalized = propertyName.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace(PropertyToTranslate.I18N_PLACEHOLDER, "");
        normalized = normalized.replace(PropertyToTranslate.INDEX_PLACEHOLDER, "");

        normalized = normalized.replaceAll("/{2,}", "/");

        return trimSlashes(normalized);
    }

    private static String extractLeafFieldName(String propertyName) {
        int lastSlashIndex = propertyName.lastIndexOf(PropertyToTranslate.PATH_SEPARATOR);
        if (lastSlashIndex < 0) {
            return propertyName;
        }
        return propertyName.substring(lastSlashIndex + 1);
    }

    private static String trimSlashes(String value) {
        int start = 0;
        int end = value.length();

        while (start < end && value.charAt(start) == PropertyToTranslate.PATH_SEPARATOR.charAt(0)) {
            start++;
        }
        while (end > start && value.charAt(end - 1) == PropertyToTranslate.PATH_SEPARATOR.charAt(0)) {
            end--;
        }

        return value.substring(start, end);
    }
}
