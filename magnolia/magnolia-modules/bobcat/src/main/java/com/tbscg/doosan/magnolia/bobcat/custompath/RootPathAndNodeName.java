package com.tbscg.doosan.magnolia.bobcat.custompath;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class RootPathAndNodeName {
    private String rootPath;
    private String nodeName;
}
