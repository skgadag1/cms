package com.tbscg.doosan.magnolia.translation.importer;

import info.magnolia.translation.ext.core.model.I18nItem;
import info.magnolia.translation.ext.core.model.TranslationSubmission;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExcludedFieldTraversalSpecResolverTest {

    @Test
    public void resolvesRecursiveRootTraversalForPageSubmissions() {
        TranslationSubmission submission = new TranslationSubmission();
        submission.setContentId("root-page-id");
        submission.setI18nFields(Arrays.asList(
                createItem("component-id-1:title", null),
                createItem("component-id-2:seo/title", "")
        ));

        List<ExcludedFieldTraversalSpecResolver.ExcludedFieldTraversalSpec> specs =
                ExcludedFieldTraversalSpecResolver.resolve(submission);

        assertEquals(1, specs.size());
        assertEquals("root-page-id", specs.get(0).nodeIdentifier());
        assertNull(specs.get(0).configPath());
        assertTrue(specs.get(0).recursive());
    }

    @Test
    public void resolvesDistinctNodeAndConfigPathPairsForAppSubmissions() {
        TranslationSubmission submission = new TranslationSubmission();
        submission.setContentId("root-app-id");
        submission.setI18nFields(Arrays.asList(
                createItem("node-a:title", "/modules/app-one/apps/app-one/subApps/detail/editor/form"),
                createItem("node-a:description", "/modules/app-one/apps/app-one/subApps/detail/editor/form"),
                createItem("node-b:title", "/modules/app-two/apps/app-two/subApps/detail/editor/form")
        ));

        List<ExcludedFieldTraversalSpecResolver.ExcludedFieldTraversalSpec> specs =
                ExcludedFieldTraversalSpecResolver.resolve(submission);

        assertEquals(2, specs.size());
        assertEquals("node-a", specs.get(0).nodeIdentifier());
        assertEquals("/modules/app-one/apps/app-one/subApps/detail/editor/form", specs.get(0).configPath());
        assertFalse(specs.get(0).recursive());
        assertEquals("node-b", specs.get(1).nodeIdentifier());
        assertEquals("/modules/app-two/apps/app-two/subApps/detail/editor/form", specs.get(1).configPath());
        assertFalse(specs.get(1).recursive());
    }

    @Test
    public void fallsBackToRecursiveRootWhenSubmissionHasNoI18nItems() {
        TranslationSubmission submission = new TranslationSubmission();
        submission.setContentId("root-page-id");

        List<ExcludedFieldTraversalSpecResolver.ExcludedFieldTraversalSpec> specs =
                ExcludedFieldTraversalSpecResolver.resolve(submission);

        assertEquals(1, specs.size());
        assertEquals("root-page-id", specs.get(0).nodeIdentifier());
        assertNull(specs.get(0).configPath());
        assertTrue(specs.get(0).recursive());
    }

    private I18nItem createItem(String key, String configPath) {
        I18nItem item = new I18nItem();
        item.setKey(key);
        item.setConfigPath(configPath);
        return item;
    }
}
