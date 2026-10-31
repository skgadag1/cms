package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.pdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppCatalog;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;

/**
 * PDD implementation for PimppClient for PIM++ integration.
 */
@Slf4j
public class PddPimppClient implements PimppClient {

    private final static int TIMEOUT = 200_000;
    private static final String GET_BACKEND_CATALOGUE_PATH = "/catalogs/emea";

    private final PddPimppConfig pimppConfig;
    private final ObjectMapper objectMapper;
    private String token;
    private long tokenExpiration;

    public PddPimppClient(final PddPimppConfig pimppConfig) {
        this.pimppConfig = pimppConfig;
        objectMapper = new ObjectMapper();
    }

    /**
     * @return copy of current configuration object.
     */
    public PddPimppConfig getPimppConfig() {
        return pimppConfig.copy();
    }

    @Override
    public PimppCatalog getCatalog() throws PimppClientException {
        try {
            final String token = getToken();

            log.debug("START: getting catalog from PIM++");
            final Request request = Request.Get(pimppConfig.getEndpoint() + GET_BACKEND_CATALOGUE_PATH)
                    .addHeader("Authorization", "Bearer " + token)
                    .connectTimeout(TIMEOUT)
                    .socketTimeout(TIMEOUT);

            log.debug("Executing request = {}", request);

            final Response response = request.execute();
            log.debug("DONE: got response for catalog");

            return objectMapper.readValue(response.returnContent().asStream(), PimppCatalog.class);
        } catch (final IOException | URISyntaxException e) {
            throw new PimppClientException("Failed to get PIM++ catalog.", e);
        }
    }

    private synchronized String getToken() throws IOException, URISyntaxException {
        if (!isTokenValid()) {
            refreshToken();
        }
        return this.token;
    }

    private boolean isTokenValid() {
        return this.token != null && System.currentTimeMillis() < this.tokenExpiration;
    }

    private void refreshToken() throws IOException, URISyntaxException {
        final String auth = pimppConfig.getTokenUser() + ":" + pimppConfig.getTokenPassword();
        String encodedAuth = new String(Base64.getEncoder().encode(auth.getBytes()));

        final URIBuilder tokenUri = new URIBuilder(pimppConfig.getTokenEndpoint());
        tokenUri.addParameter("grant_type", "client_credentials");
        tokenUri.addParameter("scope", "oci");

        final Request request = Request.Post(tokenUri.build().toString())
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + encodedAuth);

        final Response response = request.execute();

        final PddPimppToken tokenResponse = objectMapper.readValue(response.returnContent().asStream(), PddPimppToken.class);
        if (tokenResponse != null) {
            if (tokenResponse.getAccessToken() != null && tokenResponse.getExpiresIn() != 0) {
                long timestamp = System.currentTimeMillis();
                this.token = tokenResponse.getAccessToken();
                this.tokenExpiration = timestamp + (tokenResponse.getExpiresIn() * 1000);
                return;
            }
        }
        throw new RuntimeException("Invalid token response");
    }
}
