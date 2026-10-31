package com.tbscg.doosan.magnolia.regions.commands;

import lombok.Data;

@Data
public class BackupRegionsApiCommandConfig {
    private String s3Bucket;
    private String apiUrl;
}
