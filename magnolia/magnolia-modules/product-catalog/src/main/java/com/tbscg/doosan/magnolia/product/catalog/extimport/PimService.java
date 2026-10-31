package com.tbscg.doosan.magnolia.product.catalog.extimport;

/**
 * Interface for service providing hierarchy from external PIM services.
 */
public interface PimService {

    /**
     * @return Root category containing products sub-categories for GPIM locale.
     * @param locale  GPIM locale from REGION app.
     * @throws PimServiceException
     */
    PimCatalog getGPIMProductCatalog(String locale) throws PimServiceException;

    /**
     * @return Root category containing products sub-categories for EMEA.
     * @throws PimServiceException
     */
    PimCatalog getEMEAProductCatalog() throws PimServiceException;

    /**
     * @return Root category containing attachments sub-categories for GPIM locale.
     * @param locale  GPIM locale from REGION app.
     * @throws PimServiceException
     */
    PimCatalog getGPIMAttachmentCatalog(String locale) throws PimServiceException;

    /**
     * @return Root category containing attachments sub-categories for EMEA.
     * @throws PimServiceException
     */
    PimCatalog getEMEAAttachmentCatalog() throws PimServiceException;

    PimCatalog getRelationDetails() throws PimServiceException;
}
