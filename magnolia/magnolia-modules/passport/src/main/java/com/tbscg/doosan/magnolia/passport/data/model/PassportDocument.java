package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@EqualsAndHashCode
public class PassportDocument {

    private long id;
    private String title;
    private String description;
    private String subject;
    private long startDateTime;
    private long endDateTime;
    private Set<Long> brandIds;
    private Set<Long> regionIds;
    private Map<String, PassportDocumentTranslation> contents;
    private Map<String, PassportFileTranslation> files;
    private Set<Long> contractTypeIds;
    private Set<Long> contractCodeIds;
    private Set<String> accountTypes;
    private Set<String> subTypes;
    private Set<String> webSecurityRoles;
    private Set<Long> applicationBaseIds;
    private Set<String> countryCodes;

    public PassportDocument(@JsonProperty("id") long id,
                            @JsonProperty("subject") String subject,
                            @JsonProperty("title") String title,
                            @JsonProperty("description") String description,
                            @JsonProperty("start_date_time") long startDateTime,
                            @JsonProperty("end_date_time") long endDateTime,
                            @JsonProperty("brand_ids") Set<Long> brandIds,
                            @JsonProperty("region_ids") Set<Long> regionIds,
                            @JsonProperty("contract_type_ids") Set<Long> contractTypeIds,
                            @JsonProperty("contract_code_ids") Set<Long> contractCodeIds,
                            @JsonProperty("account_types") Set<String> accountTypes,
                            @JsonProperty("sub_types") Set<String> subTypes,
                            @JsonProperty("web_security_roles") Set<String> webSecurityRoles,
                            @JsonProperty("country_codes") Set<String> countryCodes,
                            @JsonProperty("contents") Map<String, PassportDocumentTranslation> contents,
                            @JsonProperty("files") Map<String, PassportFileTranslation> files,
                            @JsonProperty("application_base_ids") Set<Long> applicationBaseIds
    ) {
        this.id = id;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.brandIds = brandIds;
        this.regionIds = regionIds;
        this.contractCodeIds = contractCodeIds;
        this.contractTypeIds = contractTypeIds;
        this.accountTypes = accountTypes;
        this.subTypes = subTypes;
        this.webSecurityRoles = webSecurityRoles;
        this.countryCodes = countryCodes;
        this.contents = contents;
        this.files = files;
        this.applicationBaseIds = applicationBaseIds;
    }

    public static PassportDocument of(PassportDocumentWithDetails document) {
        PassportDocument newDocument = new PassportDocument();
        newDocument.setId(document.getId());
        newDocument.setSubject(document.getSubject());
        newDocument.setStartDateTime(document.getStartDateTime());
        newDocument.setEndDateTime(document.getEndDateTime());

        return newDocument;
    }
}
