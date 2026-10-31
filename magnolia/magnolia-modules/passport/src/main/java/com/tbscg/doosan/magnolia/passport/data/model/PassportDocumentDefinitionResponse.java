package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassportDocumentDefinitionResponse {

    private List<PropertySimple> webSecurityRoles;
    private List<PropertySimple> contractTypes;
    private List<PropertySimple> contractCodes;
    private List<PropertyName> accountTypes;
    private List<PropertyName> organizationSubTypes;
    private List<PropertyCodeName> countryCodes;
}
