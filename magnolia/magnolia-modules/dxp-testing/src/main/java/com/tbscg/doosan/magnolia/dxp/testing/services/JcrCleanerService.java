package com.tbscg.doosan.magnolia.dxp.testing.services;

/**
 * JcrCleanerService service used to remove unnecessary JCR nodes.
 */
public interface JcrCleanerService {

    /**
     * Deletes all the JCR nodes that are created when selenide tests are executed.
     *
     */
    void cleanSelenideItems();

}
