package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {

    private long id;
    @JsonProperty("brand_id")
    private long brandId;
    @JsonProperty("region_id")
    private long regionId;
    @JsonProperty("workspace_id")
    private long workspaceId;
    @JsonProperty("application_base_id")
    private long applicationBaseId;
    @JsonProperty("application_base_name")
    private String applicationBaseName;
    @JsonProperty("web_security_roles")
    private List<Long> webSecurityRoles;
}
