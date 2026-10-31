package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.tbscg.doosan.magnolia.product.catalog.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.model.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
public class DefaultGpimClientTest {

    private ClientAndServer mockServer;
    private DefaultGpimClient gpimClient;

    private String getGpimBaseEndpoint(int port) {
        return "http://localhost:" + port;
    }

    @Before
    public void setUp() throws Exception {
        mockServer = ClientAndServer.startClientAndServer();

        final GpimConfig gpimConfig = new GpimConfig();
        gpimConfig.setEndpoint(getGpimBaseEndpoint(mockServer.getLocalPort()));

        gpimClient = new DefaultGpimClient(gpimConfig);
    }

    @After
    public void tearDown() throws Exception {
        mockServer.stop();
    }

    @Test
    public void getCategoryOK() throws Exception {
        // given
        String theCategoryName = "theCategoryName";
        setupCategoryMockResponse(
                theCategoryName,
                TestUtils.loadResourceFromPath(
                        "com/tbscg/doosan/magnolia/product/catalog/extimport/gpim/theCategoryNameResponse.json"));

        // when
        GpimCategory category = gpimClient.getCategory(theCategoryName, "en_US");

        // then
        assertThat(category, notNullValue());
        GpimCategory expectedCategory = GpimCategory.builder()
                .name("The category name")
                .code(theCategoryName)
                .products(Collections.emptyList())
                .subCategories(asList(
                        GpimCategory.builder().code(theCategoryName + "_subcategory1").name("Subcategory 1").build(),
                        GpimCategory.builder().code(theCategoryName + "_subcategory2").name("Subcategory 2").build()))
                .build();
        assertTrue("Categories does not match!", EqualsBuilder.reflectionEquals(expectedCategory,category));

    }

    private String buildEnhancedCategoryPath(String category) {
        return "/rest/enhancedCategory/catalog/Products/version/Online/category/" + category + "/details/P1/levels/0";
    }

    private void setupCategoryMockResponse(String category, String body) {
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(buildEnhancedCategoryPath(category))
                        .withQueryStringParameter("locale", "en_US")
                        .withQueryStringParameters(
                                Parameter.param("locale", "en_US"),
                                Parameter.param("uomSystem", "us")
                        )
        ).respond(
                response()
                        .withBody(JsonBody.json(body, MediaType.JSON_UTF_8))
        );

    }
}
