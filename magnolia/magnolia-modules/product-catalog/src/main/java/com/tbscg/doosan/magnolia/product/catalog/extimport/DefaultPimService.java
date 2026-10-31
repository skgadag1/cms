package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimCatalog;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimModelService;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppModelService;
import info.magnolia.init.MagnoliaConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Default implementation for hierarchy retrieval service.
 * Will use GPIM to fetch NA hierarchy and PIM++ for EMEA.
 */
@Slf4j
public class DefaultPimService implements PimService {

    private final GpimModelService gpimService;
    private final PimppModelService pimppService;
    private final MagnoliaConfigurationProperties magnoliaProperties;

    /**
     * Creates new {@link DefaultPimService}
     *
     * @param gpimService        GPIM implementation of {@link PimModelService} instance.
     * @param pimppService       PIM++ implementation of {@link PimModelService} instance.
     * @param magnoliaProperties
     */
    @Inject
    public DefaultPimService(
            final GpimModelService gpimService,
            final PimppModelService pimppService,
            final MagnoliaConfigurationProperties magnoliaProperties) {
        this.gpimService = gpimService;
        this.pimppService = pimppService;
        this.magnoliaProperties = magnoliaProperties;
    }

    private boolean isHistoricalImportEnabled() {
        return "true".equalsIgnoreCase(magnoliaProperties.getProperty("magnolia.bobcat.historical.products.enable"));
    }

    /**
     * Gets product catalog for Region. Since GPIM yelds only sub-categories, it's response will be wrapped with artificial 'Imported' category.
     *
     * @param locale GPIM locale from REGION app.
     * @return {@link GpimCatalog} instance
     * @throws PimServiceException
     */
    @Override
    public PimCatalog getGPIMProductCatalog(String locale) throws PimServiceException {
        return gpimService.getCatalog(PimFilter.builder()
                        .modelType(PimModelType.PRODUCT)
                        .includeHistorical(isHistoricalImportEnabled())
                        .build(),
                locale);
    }

    /**
     * Gets product catalog for region. Since GPIM yelds only sub-categories, it's response will be wrapped with artificial 'Imported' category.
     *
     * @param locale GPIM locale from REGION app.
     * @return {@link GpimCatalog} instance
     * @throws PimServiceException
     */
    @Override
    public PimCatalog getGPIMAttachmentCatalog(String locale) throws PimServiceException {
        return gpimService.getCatalog(PimFilter.builder()
                        .modelType(PimModelType.ATTACHMENT)
                        .includeHistorical(isHistoricalImportEnabled())
                        .build(),
                locale);
    }

    /**
     * Gets product catalog for EMEA from PIM++.
     *
     * @return {@link com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppCatalog} instance
     * @throws PimServiceException
     */
    @Override
    public PimCatalog getEMEAProductCatalog() throws PimServiceException {
        return pimppService.getCatalog(PimFilter.builder()
                        .modelType(PimModelType.PRODUCT)
                        .includeHistorical(isHistoricalImportEnabled())
                        .build(),
                "");
    }

    /**
     * Gets attachments catalog for EMEA from PIM++.
     *
     * @return {@link com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppCatalog} instance
     * @throws PimServiceException
     */
    @Override
    public PimCatalog getEMEAAttachmentCatalog() throws PimServiceException {
        return pimppService.getCatalog(PimFilter.builder()
                        .modelType(PimModelType.ATTACHMENT)
                        .includeHistorical(isHistoricalImportEnabled())
                        .build(),
                "");
    }

    /**
     * Gets attachment to product relation for NA from GPIM.
     *
     * @return {@link GpimCatalog} instance
     * @throws PimServiceException
     */
    @Override
    public PimCatalog getRelationDetails() throws PimServiceException {
        return gpimService.getCatalog(PimFilter.builder()
                        .modelType(PimModelType.ATTACHMENT)
                        .includeHistorical(isHistoricalImportEnabled())
                        .build(),
                "en_US");
    }

}
