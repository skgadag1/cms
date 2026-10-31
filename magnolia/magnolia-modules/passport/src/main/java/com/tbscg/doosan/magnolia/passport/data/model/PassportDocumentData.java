package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class PassportDocumentData {
    @JsonIgnore
    private final Map<String, String> title = new HashMap<>();
    @JsonIgnore
    private final Map<String, String> description = new HashMap<>();
    private String subject;
    @JsonProperty("start_date_time")
    private long startDateTime;
    @JsonProperty("end_date_time")
    private long endDateTime;
    @JsonProperty("brand_ids")
    private Set<Long> brandIds = new HashSet<>();
    @JsonProperty("region_ids")
    private Set<Long> regionIds = new HashSet<>();
    @JsonProperty("contract_type_ids")
    private Set<Long> contractTypeIds = new HashSet<>();
    @JsonProperty("contract_code_ids")
    private Set<Long> contractCodeIds = new HashSet<>();
    @JsonProperty("account_types")
    private Set<String> accountTypes = new HashSet<>();
    @JsonProperty("sub_types")
    private Set<String> subTypes = new HashSet<>();
    @JsonProperty("web_security_roles")
    private Set<String> webSecurityRoles = new HashSet<>();
    @JsonProperty("country_codes")
    private Set<String> countryCodes = new HashSet<>();
    @JsonProperty("employee_number")
    private String employeeNumber;
    @JsonProperty("application_base_ids")
    private Set<Long> applicationBaseIds = new HashSet<>();

    public PassportDocumentData(PassportDocument document) {
        this.subject = document.getSubject();
        this.startDateTime = document.getStartDateTime();
        this.endDateTime = document.getEndDateTime();
        this.brandIds = document.getBrandIds();
        this.regionIds = document.getRegionIds();
        this.contractCodeIds = document.getContractCodeIds();
        this.contractTypeIds = document.getContractTypeIds();
        this.accountTypes = document.getAccountTypes();
        this.subTypes = document.getSubTypes();
        this.webSecurityRoles = document.getWebSecurityRoles();
        this.applicationBaseIds = document.getApplicationBaseIds();
        this.countryCodes = document.getCountryCodes();

        document.getContents().forEach((key, value) -> {
            this.title.put(key, value.getTitle());
            this.description.put(key, value.getDescription());
        });
    }

    @JsonProperty("contents")
    public List<PassportDocumentTranslation> getContents() {
        return this.title.entrySet()
                .stream()
                .map(l -> new PassportDocumentTranslation(l.getKey(), l.getValue(), this.description.get(l.getKey())))
                .collect(Collectors.toList());
    }
}
