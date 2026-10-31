/**
 * This file Copyright (c) 2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.services;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import lombok.extern.slf4j.Slf4j;

/**
 * TBD.
 */
@Slf4j
public class VersionCleaner implements ModuleLifecycle {

    String versionWorkspace;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("*************************************************");
        log.debug("***  starting Version Cleaner module!!!       ***");
        log.debug("*************************************************");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("*************************************************");
        log.debug("***  stopping Version Cleaner module!!!       ***");
        log.debug("*************************************************");
    }

    public String getVersionWorkspace() {
        return versionWorkspace;
    }

    public void setVersionWorkspace(String versionWorkspace) {
        this.versionWorkspace = (!"".equals(versionWorkspace)) ? versionWorkspace : null;
    }

}
