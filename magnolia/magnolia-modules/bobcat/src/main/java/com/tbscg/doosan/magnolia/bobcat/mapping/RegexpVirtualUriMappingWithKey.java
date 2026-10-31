package com.tbscg.doosan.magnolia.bobcat.mapping;

import info.magnolia.virtualuri.mapping.RegexpVirtualUriMapping;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegexpVirtualUriMappingWithKey extends RegexpVirtualUriMapping {
    private String apiKeyType;
}
