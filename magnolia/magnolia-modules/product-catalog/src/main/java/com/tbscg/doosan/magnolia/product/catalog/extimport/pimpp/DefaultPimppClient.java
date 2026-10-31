package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

/**
 * Default implementation for PimppClient for PIM++ integration.
 */
@Slf4j
public class DefaultPimppClient implements PimppClient {

    private static final String GET_BACKEND_CATALOGUE_PATH = "/iw/services/gateway/getBackendProductCatalogue?catalogue=web";
    private final PimppConfig pimppConfig;

    public DefaultPimppClient(final PimppConfig pimppConfig) {
        this.pimppConfig = pimppConfig;
    }

    /**
     * @return copy of current configuration object.
     */
    public PimppConfig getPimppConfig() {
        return pimppConfig.copy();
    }

    @Override
    public PimppCatalog getCatalog() throws PimppClientException {
        final Request request = Request.Get(pimppConfig.getEndpoint() + GET_BACKEND_CATALOGUE_PATH);
        try {
            return new XmlMapper().readValue(
                    request.execute()
                            .returnContent()
                            .asString(),
                    PimppCatalog.class);
        } catch (final IOException e) {
            throw new PimppClientException("Failed to get PIM++ catalog.", e);
        }
    }
}
