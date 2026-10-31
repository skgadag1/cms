package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Slf4j
public class PassportDocumentSavePayload {
    @JsonIgnore
    private long documentId;
    private List<PassportDocumentTranslation> contents;
    private String subject;
    @JsonProperty("start_date_time")
    private long startDateTime;
    @JsonProperty("end_date_time")
    private long endDateTime;
    @JsonProperty("brand_ids")
    private List<Long> brandIds;
    @JsonProperty("region_ids")
    private List<Long> regionIds;
    @JsonProperty("contract_type_ids")
    private List<Long> contractTypeIds;
    @JsonProperty("contract_code_ids")
    private List<Long> contractCodeIds;
    @JsonProperty("employee_number")
    private String employeeNumber;
    @JsonProperty("account_types")
    private List<String> accountTypes;
    @JsonProperty("web_security_roles")
    private List<String> webSecurityRoles;
    @JsonProperty("country_codes")
    private List<String> countryCodes;
    @JsonProperty("application_base_ids")
    private List<Long> applicationBaseIds;

    public PassportDocumentSavePayload(PassportDocumentWithDetails document) {
        this.documentId = document.getId();
        this.subject = document.getSubject();
        this.contents = document.getContents();
        this.startDateTime = document.getStartDateTime();
        this.endDateTime = document.getEndDateTime();
        this.brandIds = document.getBrands().stream().map(PropertySimple::getId).distinct().collect(Collectors.toList());
        this.regionIds = document.getRegions().stream().map(PropertySimple::getId).distinct().collect(Collectors.toList());
        this.contractCodeIds = document.getContractCodes().stream().map(PropertySimple::getId).collect(Collectors.toList());
        this.contractTypeIds = document.getContractTypes().stream().map(PropertySimple::getId).collect(Collectors.toList());
        this.accountTypes = document.getAccountTypes().stream().map(PropertyAccountType::getName).collect(Collectors.toList());
        this.webSecurityRoles = document.getWebSecurityRoles().stream().map(PropertySimple::getName).collect(Collectors.toList());
        this.countryCodes = document.getCountryCodes().stream().map(PropertyCodeName::getCode).collect(Collectors.toList());
        this.applicationBaseIds = document.getApplications().stream().map(PropertySimple::getId).collect(Collectors.toList());
    }
}
