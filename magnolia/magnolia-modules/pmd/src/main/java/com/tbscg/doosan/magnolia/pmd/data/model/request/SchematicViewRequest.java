package com.tbscg.doosan.magnolia.pmd.data.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SchematicViewRequest extends PmdRequest {

    private long id;

    public SchematicViewRequest(long id) {
        this.id = id;
    }
}
