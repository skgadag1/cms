package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.tbscg.doosan.magnolia.product.catalog.extimport.PimModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.mockserver.model.XmlBody;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
public class DefaultPimppClientTest {

    private static final String CURRENT_STATUS = "current";
    private static final String NON_CURRENT_STATUS = "non-current";

    private ClientAndServer mockServer;
    private DefaultPimppClient pimppClient;

    @Before
    public void setUp() throws Exception {
        mockServer = ClientAndServer.startClientAndServer();

        final PimppConfig pimppConfig = new PimppConfig();
        pimppConfig.setEndpoint(getPimppHost());

        pimppClient = new DefaultPimppClient(pimppConfig);
    }

    @After
    public void tearDown() throws Exception {
        mockServer.stop();
    }

    @Test
    public void getCatalogShouldHttpGET() throws Exception {
        // given
        givenGetBackendProductCatalogueResponse(
                "<catalogue name=\"Web\" id=\"web\" xmlns=\"http://www.diicorp.com/2009/interwoven-hybris\"/>");

        // when
        PimppCatalog pimppCatalog = pimppClient.getCatalog();

        // then
        assertThat(pimppCatalog, notNullValue());
        assertThat(pimppCatalog.getCategories(), nullValue());
    }

    @Test
    public void getCatalogShouldParseCategory() throws Exception {
        // given
        final String category1 = "Excavators";
        final String category2 = "Loaders";
        final String status1 = "status1";
        final String status2 = "status2";

        givenGetBackendProductCatalogueResponse(
                "<catalogue name=\"Web\" id=\"web\" xmlns=\"http://www.diicorp.com/2009/interwoven-hybris\">\n" +
                        "    <category status=\"" + status1 + "\" name=\"" + category1 + "\" id=\"39977247518230480\"/>\n" +
                        "    <category status=\"" + status2 + "\" name=\"" + category2 + "\" id=\"39977247518279264\"/>\n" +
                        "</catalogue>\n");

        // when
        PimppCatalog pimppCatalog = pimppClient.getCatalog();

        // then
        assertThat(pimppCatalog, notNullValue());
        assertThat(pimppCatalog.getCategories(), equalTo(asList(
                PimppCategory.builder().status(status1).name(category1).build(),
                PimppCategory.builder().status(status2).name(category2).build()
        )));
    }

    @Test
    public void getCatalogShouldParseSubCategory() throws Exception {
        // given
        final String categoryName = "Loaders";
        final String subCategory1 = "Skid steer loaders";
        final String subCategory2 = "Compact  tracked loaders";

        //language=XML
        givenGetBackendProductCatalogueResponse(
                "<catalogue name=\"Web\" id=\"web\" xmlns=\"http://www.diicorp.com/2009/interwoven-hybris\">\n" +
                        "    <category status=\"" + CURRENT_STATUS + "\" name=\"" + categoryName + "\" id=\"39977247518279264\">\n" +
                        "        <category status=\"" + CURRENT_STATUS + "\" name=\"" + subCategory1 + "\" id=\"5067120331570630064\"/>\n" +
                        "        <category status=\"" + CURRENT_STATUS + "\" name=\"" + subCategory2 + "\" id=\"5067120331570606160\"/>\n" +
                        "    </category>\n" +
                        "</catalogue>");

        // when
        PimppCatalog pimppCatalog = pimppClient.getCatalog();

        // then
        assertThat(pimppCatalog, notNullValue());
        assertThat(pimppCatalog.getCategories(), equalTo(singletonList(
                defaultCategory().toBuilder()
                        .name(categoryName)
                        .subCategories(asList(
                                defaultCategory().toBuilder().name(subCategory1).build(),
                                defaultCategory().toBuilder().name(subCategory2).build()
                        ))
                        .build()
        )));
    }

    @Test
    public void getCatalogShouldParseModels() throws PimppClientException {
        // given
        final String categoryName = "Loaders";
        final String subCategoryName = "Skid steer loaders";

        final String pmi1 = "B4UC";
        final String pmi2 = "A3W7";
        final String pmi3 = "B4KH";

        final String name1 = "S70";
        final String name2 = "S100";

        givenGetBackendProductCatalogueResponse(
                "<catalogue name=\"Web\" id=\"web\" xmlns=\"http://www.diicorp.com/2009/interwoven-hybris\">\n" +
                        "    <category status=\"" + CURRENT_STATUS + "\" name=\"" + categoryName + "\" id=\"39977247518279264\">\n" +
                        "        <category status=\"" + CURRENT_STATUS + "\" name=\"" + subCategoryName + "\" id=\"5067120331570630064\">\n" +
                        "            <model default-variant=\"293955847236224\" type=\"machine\" status=\"" + CURRENT_STATUS + "\" name=\"" + name1 + "\"\n" +
                        "                   id=\"5067401806547382608\">\n" +
                        "                <variant status=\"" + CURRENT_STATUS + "\" name=\"S70 (" + pmi1 + " 11001)\" id=\"293955847236224\"/>\n" +
                        "                <variant status=\"" + NON_CURRENT_STATUS + "\" name=\"S70 (" + pmi2 + " 11001)\" id=\"289275802154096\"/>\n" +
                        "            </model>\n" +
                        "            <model default-variant=\"293369588819952\" type=\"machine\" status=\"" + CURRENT_STATUS + "\" name=\"" + name2 + "\"\n" +
                        "                   id=\"5067401806547346640\">\n" +
                        "                <variant status=\"" + CURRENT_STATUS + "\" name=\"S100 (" + pmi3 + " 11001)\" id=\"293369588819952\"/>\n" +
                        "            </model>\n" +
                        "        </category>\n" +
                        "    </category>\n" +
                        "</catalogue>");

        // when
        final PimppCatalog pimppCatalog = pimppClient.getCatalog();

        // then
        assertThat(pimppCatalog, notNullValue());

        final List<PimppCategory> categories = pimppCatalog.getCategories();
        assertThat(categories, hasSize(1));

        final PimppCategory expectedCategory = categories.get(0);
        assertThat(expectedCategory.getSubCategories(), hasSize(1));

        final List<PimppModel> products = expectedCategory.getSubCategories().get(0).getModels();
        assertThat(products, hasSize(2));
    }

    private void givenGetBackendProductCatalogueResponse(String xmlBody) {
        mockServer.when(request()
                .withMethod("GET")
                .withPath("/iw/services/gateway/getBackendProductCatalogue")
                .withQueryStringParameter("catalogue", "web")
        ).respond(
                response()
                        .withBody(XmlBody.xml(xmlBody, MediaType.XML_UTF_8))
        );
    }

    private String getPimppHost() {
        return "http://localhost:" + mockServer.getLocalPort();
    }

    private static PimppCategory defaultCategory() {
        return PimppCategory.builder().status(CURRENT_STATUS).build();
    }
}
