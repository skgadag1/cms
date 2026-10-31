package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Prefix;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.data.provider.CallbackDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.ComboBox;

import java.util.stream.Stream;

public class PrefixSelector extends ComboBox<Prefix> {

    private final ApiService apiService;

    public PrefixSelector(ApiService apiService) {
        super();
        this.apiService = apiService;
        setWidth(150, Unit.PIXELS);
        setItemCaptionGenerator(Prefix::getPrefix);

        CallbackDataProvider.FetchCallback<Prefix, String> fetchCallback = query -> {
            String filter = query.getFilter().orElse("");
            if (filter.length() > 1) {
                return this.apiService.getPrefixes(filter).stream()
                        .limit(query.getLimit())
                        .skip(query.getOffset());
            }
            return Stream.empty();
        };
        setDataProvider(DataProvider.fromFilteringCallbacks(
                fetchCallback,
                query -> (int) fetchCallback.fetch(query).count()
        ));
    }

    public PrefixSelector(ApiService apiService, String caption) {
        this(apiService);
        setCaption(caption);
    }

}
