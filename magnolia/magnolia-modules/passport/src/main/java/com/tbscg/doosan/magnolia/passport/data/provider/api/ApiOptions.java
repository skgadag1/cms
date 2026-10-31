package com.tbscg.doosan.magnolia.passport.data.provider.api;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
public class ApiOptions {

    private String baseUrl;
    private boolean cacheEnabled;
    private long cacheTtl;
    private long cacheMaxSize;
    private long documentWorkspaceId;
}
