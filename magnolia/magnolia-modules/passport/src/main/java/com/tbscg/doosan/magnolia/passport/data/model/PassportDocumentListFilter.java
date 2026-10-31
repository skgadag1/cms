package com.tbscg.doosan.magnolia.passport.data.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PassportDocumentListFilter {
    private String brandIds;
    private String regionIds;
    private String subject;
    private String title;
    private String description;
    private String applicationBaseIds;
}
