package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.UrlFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDataSearchResult;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;

import java.util.List;

public interface VanityUrlService extends DataProvider<VanityUrl, UrlFilters> {

    VanityUrl getUrl(String code);

    void createNewOrReplace(VanityUrl url);

    void deleteUrl(String code);

    List<VanityUrl> getUrlsByDomainCode(String domainCode);

    boolean isUrlPathNotTaken(VanityUrl url);

    VanityDataSearchResult<VanityUrl> fetchWithPagination(Query<VanityUrl, UrlFilters> query);
}
