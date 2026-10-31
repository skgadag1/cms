package com.tbscg.doosan.magnolia.sso;

import com.tbscg.doosan.magnolia.sso.settings.Settings;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
public class SsoDoosanModule implements ModuleLifecycle {

    public static final String MODULE_NAME = "sso-doosan";

    private Map<String, Settings> authenticationServices = new HashMap<>();
    private String ngPassportUrl;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", SsoDoosanModule.MODULE_NAME);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("Module {} stopping.", SsoDoosanModule.MODULE_NAME);
    }

}
