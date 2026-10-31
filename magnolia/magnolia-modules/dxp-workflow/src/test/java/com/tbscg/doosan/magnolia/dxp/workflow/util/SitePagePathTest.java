package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SitePagePathTest {

    @Test
    public void shouldBuildPathForLanguage_returnPath() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region/path/to/a/page");
        assertEquals("/region/en/path/to/a/page", sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("path/to/a/page", sitePagePath.getPath());
        assertEquals("/site/region/path/to/a/page", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenUpperCaseAndSlash() {
        final SitePagePath sitePagePath = new SitePagePath("/site/REGION/path/To/A/paGe/");
        assertEquals("/region/en/path/to/a/page", sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("path/to/a/page", sitePagePath.getPath());
        assertEquals("/site/region/path/to/a/page", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenSiteWithoutSlash() {
        final SitePagePath sitePagePath = new SitePagePath("site/region/path/to/a/page");
        assertEquals("/region/en/path/to/a/page", sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("path/to/a/page", sitePagePath.getPath());
        assertEquals("/site/region/path/to/a/page", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenLanguageEmpty() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region/path/to/a/page");
        assertEquals("/region/path/to/a/page", sitePagePath.buildPathForLanguage(EMPTY));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("path/to/a/page", sitePagePath.getPath());
        assertEquals("/site/region/path/to/a/page", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenLanguageNull() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region/path/to/a/page");
        assertEquals("/region/path/to/a/page", sitePagePath.buildPathForLanguage(null));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("path/to/a/page", sitePagePath.getPath());
        assertEquals("/site/region/path/to/a/page", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenPathPartEmpty() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region");
        assertEquals("/region/pl", sitePagePath.buildPathForLanguage("pl"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site/region/", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenPathPartEmptyWithSlashes() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region/////");
        assertEquals("/region/pl", sitePagePath.buildPathForLanguage("pl"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site/region/", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenPathPartEmptyAndLanguageEmpty() {
        final SitePagePath sitePagePath = new SitePagePath("/site/region//");
        assertEquals("/region", sitePagePath.buildPathForLanguage(EMPTY));
        assertEquals("site", sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site/region/", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenRegionPartEmpty() {
        final SitePagePath sitePagePath = new SitePagePath("/site");
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site//", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenRegionPartEmptyWithSlashes() {
        final SitePagePath sitePagePath = new SitePagePath("/site//");
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site//", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenRegionPartEmptyAndLanguageEmpty() {
        final SitePagePath sitePagePath = new SitePagePath("/site");
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage(EMPTY));
        assertEquals("site", sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site//", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenRegionAndPathPartEmptyWithSlashes() {
        final SitePagePath sitePagePath = new SitePagePath("/site//////////");
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals("site", sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("/site//", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenEmptySite() {
        final SitePagePath sitePagePath = new SitePagePath("/");
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals(EMPTY, sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("///", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenEmptySiteButHasRegionAndPath() {
        final SitePagePath sitePagePath = new SitePagePath("//region/and/some/path");
        assertEquals("/region/en/and/some/path", sitePagePath.buildPathForLanguage("en"));
        assertEquals(EMPTY, sitePagePath.getSite());
        assertEquals("region", sitePagePath.getRegion());
        assertEquals("and/some/path", sitePagePath.getPath());
        assertEquals("//region/and/some/path", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenNoPath() {
        final SitePagePath sitePagePath = new SitePagePath(EMPTY);
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals(EMPTY, sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("///", sitePagePath.toString());
    }

    @Test
    public void shouldBuildPathForLanguage_returnPath_whenNullPath() {
        final SitePagePath sitePagePath = new SitePagePath(null);
        assertEquals(EMPTY, sitePagePath.buildPathForLanguage("en"));
        assertEquals(EMPTY, sitePagePath.getSite());
        assertEquals(EMPTY, sitePagePath.getRegion());
        assertEquals(EMPTY, sitePagePath.getPath());
        assertEquals("///", sitePagePath.toString());
    }

}
