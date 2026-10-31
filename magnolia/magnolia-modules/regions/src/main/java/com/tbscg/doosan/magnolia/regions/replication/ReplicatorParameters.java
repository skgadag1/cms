package com.tbscg.doosan.magnolia.regions.replication;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReplicatorParameters {
    String workspace;
    String site;
}
