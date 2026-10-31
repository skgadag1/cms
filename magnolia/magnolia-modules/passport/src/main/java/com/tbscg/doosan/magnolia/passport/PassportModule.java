package com.tbscg.doosan.magnolia.passport;

import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiOptions;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class PassportModule implements ModuleLifecycle {

    private String apiEndpoint;
    private boolean apiCacheEnabled;
    private long apiCacheTTLSeconds;
    private long apiCacheMaxSize;
    private String tokenEndpoint;
    private String tokenUser;
    private String tokenPassword;
    private long documentWorkspaceId;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("START passport module");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("STOP passport module");
    }

    public ApiOptions getApiOptions() {
        return new ApiOptions()
                .withBaseUrl(getApiEndpoint())
                .withCacheEnabled(isApiCacheEnabled())
                .withCacheMaxSize(getApiCacheMaxSize())
                .withCacheTtl(getApiCacheTTLSeconds())
                .withDocumentWorkspaceId(getDocumentWorkspaceId());
    }
}
