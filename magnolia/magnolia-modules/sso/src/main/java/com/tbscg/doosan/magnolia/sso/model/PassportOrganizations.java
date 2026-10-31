package com.tbscg.doosan.magnolia.sso.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassportOrganizations {
    private List<PassportOrganization> organizations;
}
