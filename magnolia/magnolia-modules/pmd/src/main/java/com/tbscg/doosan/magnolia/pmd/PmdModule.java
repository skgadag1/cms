package com.tbscg.doosan.magnolia.pmd;


import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiOptions;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class PmdModule implements ModuleLifecycle {

    private String apiEndpoint;
    private boolean apiCacheEnabled;
    private long apiCacheTTLSeconds;
    private long apiCacheMaxSize;

    private String tokenEndpoint;
    private String tokenUser;
    private String tokenPassword;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("START pmd module");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("STOP pmd module");
    }

    public ApiOptions getApiOptions() {
        return new ApiOptions()
                .withBaseUrl(getApiEndpoint())
                .withCacheEnabled(isApiCacheEnabled())
                .withCacheMaxSize(getApiCacheMaxSize())
                .withCacheTtl(getApiCacheTTLSeconds());
    }
}
