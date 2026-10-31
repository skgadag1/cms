package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import javax.inject.Provider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@RunWith(JUnitParamsRunner.class)
public class SimpleBuildAndQuoteClientTest {

    /**
     * If set to true, will output mock server logs after each test.
     */
    private static final boolean OUTPUT_MOCK_SERVER_LOGS = false;

    private static final String INVALID_BUILD_AND_QUOTE_URI = "foobar";
    private static final String CATEGORIES_ENDPOINT = "/categories";

    private static ClientAndServer mockServer;
    private static String buildAndQuoteHost;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {
        mockServer = ClientAndServer.startClientAndServer();
        buildAndQuoteHost = "http://localhost:" + mockServer.getLocalPort();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        mockServer.stop();
    }

    @Before
    public void setUp() throws Exception {
        mockServer.reset();
    }

    @After
    public void tearDown() throws Exception {
        if (OUTPUT_MOCK_SERVER_LOGS) {
            System.err.println("Mock server logs:");
            System.err.println(mockServer.retrieveLogMessages(request()));
        }
    }

    @Test
    public void ctxShouldCreateSimpleBuildAndQuoteService() {
        // given
        // when
        new SimpleBuildAndQuoteClient(mockedProductCatalogProvider("https://some.host.com"));

        // then
        // NOOP
    }

    @Test
    public void ctxShouldFailOnInvalidURL() {
        // given
        // expect
        expectedException.expect(BuildAndQuoteClientException.class);
        expectedException.expectMessage("Invalid B&Q url");
        expectedException.expectCause(instanceOf(MalformedURLException.class));

        // when
        new SimpleBuildAndQuoteClient(mockedProductCatalogProvider(INVALID_BUILD_AND_QUOTE_URI));
    }

    @Test
    public void shouldHandleEmptyResponse() {
        // given
        final SimpleBuildAndQuoteClient buildAndQuoteService = defaultBuildAndQuoteService();

        //language=JSON
        mockGetCategoriesWith("[]");

        // when
        final List<BuildAndQuoteCategory> actual = buildAndQuoteService.getCategoryList();

        // then
        assertThat(actual, emptyCollectionOf(BuildAndQuoteCategory.class));
    }

    @Test
    public void shouldHandleResponse() {
        // given
        final SimpleBuildAndQuoteClient buildAndQuoteService = defaultBuildAndQuoteService();

        final String categoryID1 = "104";
        final String categoryName1 = "Skid-Steer Loaders";
        final String categoryID2 = "103";
        final String categoryName2 = "Excavators";

        //language=JSON
        mockGetCategoriesWith("[\n" +
                "  {\n" +
                "    \"prodCategoryId\": " + categoryID1 + ",\n" +
                "    \"prodCategoryName\": \"" + categoryName1 + "\",\n" +
                "    \"prodCategoryIsPriced\": true\n" +
                "  },\n" +
                "  {\n" +
                "    \"prodCategoryId\": " + categoryID2 + ",\n" +
                "    \"prodCategoryName\": \"" + categoryName2 + "\",\n" +
                "    \"prodCategoryIsPriced\": true\n" +
                "  }\n" +
                "]\n");

        // when
        final List<BuildAndQuoteCategory> actual = buildAndQuoteService.getCategoryList();

        // then
        assertThat(actual, equalTo(Arrays.asList(
                mkCategory(categoryID1, categoryName1),
                mkCategory(categoryID2, categoryName2)
        )));
    }

    @Test
    @Parameters(
            {
                    "404",
                    "500"
            })
    public void shouldHandleErrorResponse(final int statusCode) {
        // given
        final SimpleBuildAndQuoteClient buildAndQuoteService = defaultBuildAndQuoteService();

        mockServer.when(request()
                .withMethod("GET")
                .withPath(CATEGORIES_ENDPOINT)
        ).respond(response().withStatusCode(statusCode));

        // expect
        expectedException.expect(BuildAndQuoteClientException.class);
        expectedException.expectMessage("Failed to get category list from B&Q");
        expectedException.expectCause(instanceOf(IOException.class));

        // when
        buildAndQuoteService.getCategoryList();
    }

    private static ProductCatalogModule mockProductCatalogWithBAndQHost(String host) {
        final BuildAndQuoteConfig buildAndQuoteConfig = new BuildAndQuoteConfig();
        buildAndQuoteConfig.setHost(host);

        final ProductCatalogModule productCatalogModule = mock(ProductCatalogModule.class);
        doReturn(buildAndQuoteConfig).when(productCatalogModule).getBuildAndQuoteConfig();

        return productCatalogModule;
    }

    private static BuildAndQuoteCategory mkCategory(String categoryID1, String categoryName1) {
        final BuildAndQuoteCategory category = new BuildAndQuoteCategory();
        category.setProdCategoryId(categoryID1);
        category.setProdCategoryName(categoryName1);

        return category;
    }

    private static void mockGetCategoriesWith(final String responseJson) {
        mockServer.when(request()
                .withMethod("GET")
                .withPath(CATEGORIES_ENDPOINT)
        ).respond(response().withBody(
                json(responseJson, MediaType.JSON_UTF_8)
        ));
    }

    private static SimpleBuildAndQuoteClient defaultBuildAndQuoteService() {
        return new SimpleBuildAndQuoteClient(mockedProductCatalogProvider(buildAndQuoteHost));
    }

    private static Provider<ProductCatalogModule> mockedProductCatalogProvider(String buildAndQuoteHost) {
        return () -> mockProductCatalogWithBAndQHost(buildAndQuoteHost);
    }
}
