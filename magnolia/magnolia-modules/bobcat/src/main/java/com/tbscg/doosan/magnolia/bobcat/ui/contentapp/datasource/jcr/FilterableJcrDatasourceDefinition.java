package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.datasource.jcr;

import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import com.vaadin.shared.data.sort.SortDirection;
import info.magnolia.ui.contentapp.preview.JcrPreviewDefinition;
import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@DatasourceType("filterableJcrDatasource")
public class FilterableJcrDatasourceDefinition extends JcrDatasourceDefinition {

    private String name = "jcr";
    private String rootPath = "/";
    private String workspace;
    private boolean includeProperties;
    private boolean includeSystemProperties;
    private boolean includeSystemNodes;
    private boolean prefilterQuery;
    private Set<String> allowedNodeTypes = new HashSet<>();
    private String describeByProperty;
    private JcrPreviewDefinition preview;
    private String nodeNameProperty;

    private Map<String, SortDirection> sortBy = new LinkedHashMap<>();
    private NodeFilter nodeFilter;

}
