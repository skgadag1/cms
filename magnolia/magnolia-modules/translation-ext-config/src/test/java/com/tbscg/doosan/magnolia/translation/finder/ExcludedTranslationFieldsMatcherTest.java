package com.tbscg.doosan.magnolia.translation.finder;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExcludedTranslationFieldsMatcherTest {

    @Test
    public void shouldMatchSimpleI18nFieldByLeafName() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("title,description");

        assertTrue(ExcludedTranslationFieldsMatcher.matches(excludedFields, "title.i18n."));
    }

    @Test
    public void shouldMatchNestedFieldByExactNormalizedPath() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("seo/title");

        assertTrue(ExcludedTranslationFieldsMatcher.matches(excludedFields, "seo.i18n./title.i18n."));
    }

    @Test
    public void shouldMatchNestedFieldByLeafName() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("title");

        assertTrue(ExcludedTranslationFieldsMatcher.matches(excludedFields, "seo.i18n./title.i18n."));
    }

    @Test
    public void shouldMatchMultifieldPropertyAfterRemovingIndexPlaceholder() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("items/title");

        assertTrue(ExcludedTranslationFieldsMatcher.matches(excludedFields, "items.index./title.i18n."));
    }

    @Test
    public void shouldCollapseRepeatedSlashesDuringNormalization() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("seo///title");

        assertTrue(ExcludedTranslationFieldsMatcher.matches(excludedFields, "seo.i18n.///title.i18n."));
    }

    @Test
    public void shouldNotMatchDifferentField() {
        Set<String> excludedFields = ExcludedTranslationFieldsMatcher.parseCsv("title,description");

        assertFalse(ExcludedTranslationFieldsMatcher.matches(excludedFields, "summary.i18n."));
    }
}
