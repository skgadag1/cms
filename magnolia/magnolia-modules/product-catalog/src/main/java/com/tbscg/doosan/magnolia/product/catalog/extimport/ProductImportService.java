package com.tbscg.doosan.magnolia.product.catalog.extimport;

import org.apache.commons.lang3.function.FailableCallable;

/**
 * Import service that will fill JCR with data from PimCategory provided by the supplier.
 */
public interface ProductImportService {

    /**
     * Imports data by calling supplier & transforming response into JCR nodes that are put on a given path.
     *
     * @param catalogSupplier      data supplier
     * @param relationsSupplier     data supplier
     * @param importedCategoryPath destination path
     * @param workspace            workspace
     * @throws Exception
     */
    void importExternalCatalog(
            FailableCallable<PimCatalog, Exception> catalogSupplier,
            FailableCallable<PimCatalog, Exception> relationsSupplier,
            String importedCategoryPath,
            String workspace
    ) throws Exception;
}
