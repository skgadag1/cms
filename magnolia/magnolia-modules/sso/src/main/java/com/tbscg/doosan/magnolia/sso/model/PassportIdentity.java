package com.tbscg.doosan.magnolia.sso.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassportIdentity implements Serializable {
    private String serialNumber;
    private List<String> webSecurityRoles;
}
