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
public class BrandsResponse {
    private List<PropertySimple> brands;
}
