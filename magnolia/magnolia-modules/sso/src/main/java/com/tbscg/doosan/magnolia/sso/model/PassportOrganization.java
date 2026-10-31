package com.tbscg.doosan.magnolia.sso.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassportOrganization implements Serializable {
    private String organizationId;
    private OrgDivision division;
}

