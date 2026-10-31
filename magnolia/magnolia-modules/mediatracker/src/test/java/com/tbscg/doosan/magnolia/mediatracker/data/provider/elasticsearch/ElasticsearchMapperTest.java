package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import junitparams.JUnitParamsRunner;
import junitparams.NamedParameters;
import junitparams.Parameters;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JUnitParamsRunner.class)
public class ElasticsearchMapperTest {

    @SuppressWarnings("unused")
    @NamedParameters("nullOrEmptyJson")
    private static String[] nullOrEmptyJson() {
        return new String[]{
                null,
                "",
                "   \t ",
                "{}"
        };
    }

    @Test
    @Parameters(named = "nullOrEmptyJson")
    public void resultsToUrlsListShouldReturnEmptyList(final String nullOrEmptyJson) {
        // when
        final List<VanityUrl> vanityUrls = ElasticsearchMapper.resultsToUrlsList(nullOrEmptyJson);

        // then
        assertThat(vanityUrls, is(empty()));
    }

    @Test
    public void resultsToUrlsListShouldMapItems() throws Exception {
        // given
        final String json = IOUtils.toString(
                getResourceAsStream("ElasticsearchMapper.resultsToUrlList.queryResult.default.json"),
                StandardCharsets.UTF_8.name());

        // when
        final List<VanityUrl> vanityUrls = ElasticsearchMapper.resultsToUrlsList(json);

        // then
        assertThat(vanityUrls, hasSize(6));
        assertThat(vanityUrls.get(0).getCode(), equalTo("21ecb268-a08d-414b-a0cc-bd2d275cf9c2"));
        assertThat(vanityUrls.get(1).getCode(), equalTo("b3fff12a-3c62-418f-b4f9-79ea962d2fbf"));
        assertThat(vanityUrls.get(2).getCode(), equalTo("aaade7d9-48c2-4f77-987e-eb064f6f9e14"));
        assertThat(vanityUrls.get(3).getCode(), equalTo("325f7537-9443-4d45-98fd-ca30dad37d21"));
        assertThat(vanityUrls.get(4).getCode(), equalTo("aa188a28-0f8f-4dba-8542-a0d9833fb325"));
        assertThat(vanityUrls.get(5).getCode(), equalTo("0704e5c7-5f15-4c5b-a2a4-0ab68830f9cc"));
    }

    private InputStream getResourceAsStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Test
    @Parameters(named = "nullOrEmptyJson")
    public void resultsToDomainsListShouldReturnEmptyList(final String nullOrEmptyJson) {
        // when
        final List<VanityDomain> vanityDomains = ElasticsearchMapper.resultsToDomainsList(nullOrEmptyJson);

        // then
        assertThat(vanityDomains, is(empty()));
    }

    @Test
    public void createConditionsShouldReturnJsonObjectWithKeywordPath() throws IOException {
        createConditionsShouldReturnJsonObjectPath("ElasticsearchMapper.toJsonQueryString.queryResult.default.json", true);
    }

    @Test
    public void createConditionsShouldReturnJsonObjectWithoutKeywordPath() throws IOException {
        createConditionsShouldReturnJsonObjectPath("ElasticsearchMapper.toJsonQueryString.queryResult.without.json", false);
    }

    public void  createConditionsShouldReturnJsonObjectPath(String resource, boolean isStrict) throws IOException {
        // given
        ElasticsearchQueryParams q = ElasticsearchQueryParams.fromUrlPath("/test", "1234", "test", isStrict);
        String expectedResult = IOUtils
                .toString(
                        getResourceAsStream(resource),
                        StandardCharsets.UTF_8.name())
                .replaceAll("\\s+", "");
        // when
        final String result = ElasticsearchMapper.toJsonQueryString(q);

        // then
        assertEquals(result, expectedResult);
    }

}
