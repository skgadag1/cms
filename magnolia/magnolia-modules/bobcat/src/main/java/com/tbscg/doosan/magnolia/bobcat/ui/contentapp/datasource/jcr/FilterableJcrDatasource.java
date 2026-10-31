package com.tbscg.doosan.magnolia.bobcat.ui.contentapp.datasource.jcr;

import info.magnolia.context.Context;
import info.magnolia.ui.datasource.jcr.JcrDatasource;

import javax.inject.Inject;
import javax.inject.Provider;

public class FilterableJcrDatasource extends JcrDatasource {

    @Inject
    public FilterableJcrDatasource(Provider<Context> contextProvider, FilterableJcrDatasourceDefinition datasourceDefinition) {
        super(contextProvider, datasourceDefinition);
    }
}
