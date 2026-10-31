package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PassportDocumentWithDetails {
    private long id;
    private String subject;
    private long startDateTime;
    private long endDateTime;
    private List<PropertySimple> brands;
    private List<PropertySimple> regions;
    private List<PassportDocumentTranslation> contents;
    private List<PassportFileTranslation> files;
    private List<PropertySimple> contractTypes;
    private List<PropertySimple> contractCodes;
    private List<PropertyAccountType> accountTypes;
    private List<PropertySimple> webSecurityRoles;
    private List<PropertyCodeName> countryCodes;
    private List<PropertySimple> applications;

    public PassportDocumentWithDetails(@JsonProperty("id") long id,
                                       @JsonProperty("subject") String subject,
                                       @JsonProperty("start_date_time") long startDateTime,
                                       @JsonProperty("end_date_time") long endDateTime,
                                       @JsonProperty("brands") List<PropertySimple> brands,
                                       @JsonProperty("regions") List<PropertySimple> regions,
                                       @JsonProperty("contract_types") List<PropertySimple> contractTypes,
                                       @JsonProperty("contract_codes") List<PropertySimple> contractCodes,
                                       @JsonProperty("account_types") List<PropertyAccountType> accountTypes,
                                       @JsonProperty("web_security_roles") List<PropertySimple> webSecurityRoles,
                                       @JsonProperty("country_codes") List<PropertyCodeName> countryCodes,
                                       @JsonProperty("contents") List<PassportDocumentTranslation> contents,
                                       @JsonProperty("files") List<PassportFileTranslation> files,
                                       @JsonProperty("application_bases") List<PropertySimple> applications
                            ) {
        this.id = id;
        this.subject = subject;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.brands = brands;
        this.regions = regions;
        this.contractCodes = contractCodes;
        this.contractTypes = contractTypes;
        this.accountTypes = accountTypes;
        this.webSecurityRoles = webSecurityRoles;
        this.countryCodes = countryCodes;
        this.contents = contents;
        this.files = files;
        this.applications = applications;
    }
}
