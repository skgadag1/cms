package com.tbscg.doosan.magnolia.product.catalog.extimport;

public interface PimModelService {
    PimCatalog getCatalog(PimFilter filter, String locale) throws PimServiceException;
}
