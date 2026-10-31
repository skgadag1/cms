package com.tbscg.doosan.magnolia.bobcat.commands;

import lombok.Data;

@Data
public class BackupCommandConfig {
    private String s3Bucket;
    private String groupNamePath;
    private String roleArnPath;
}
