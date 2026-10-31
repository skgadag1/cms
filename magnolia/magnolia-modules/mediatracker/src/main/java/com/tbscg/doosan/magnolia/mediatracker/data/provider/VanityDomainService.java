package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.DomainFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDataSearchResult;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;

import java.util.List;

public interface VanityDomainService extends DataProvider<VanityDomain, DomainFilters> {

    VanityDomain getDomain(String code);

    List<VanityDomain> getDomains(Division division);

    void saveDomain(VanityDomain domain);

    void deleteDomain(String code);

    boolean isDomainNameNotTaken(VanityDomain domain);

    VanityDataSearchResult<VanityDomain> fetchWithPagination(Query<VanityDomain, DomainFilters> query);

}
