
package com.tbscg.doosan.magnolia.pmd.data.model;

import lombok.*;

@Getter
@Setter
@With
@NoArgsConstructor
@AllArgsConstructor
public class SchematicListFilter {

    private String pmiType;
    private Long pmiId;
    private String serial;
}
