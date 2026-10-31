package com.tbscg.doosan.magnolia.bobcat.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiKey {
    @JsonProperty("ApiKey")
    String apiKey;
}
