package com.tbscg.doosan.magnolia.dxp.testing.commands;

import com.tbscg.doosan.magnolia.dxp.testing.services.JcrCleanerService;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * JCR Cleaner Command (job) that gets launched by the scheduler module.
 *
 * @author Giovanni José López Reza
 */
@Slf4j
public class CleanTestItemsCommand extends MgnlCommand {

    @Inject
    private JcrCleanerService jcrCleanerService;

    @Override
    public boolean execute(Context context) {
        boolean executeSuccess = true;
        log.info("Clean Test Items command start.");

        if (jcrCleanerService != null) {
            jcrCleanerService.cleanSelenideItems();
        }
        else {
            executeSuccess = false;
            log.error("JCR Cleaner Service is not available.");
        }

        log.info("Clean Test Items command end.");
        return executeSuccess;
    }
}
