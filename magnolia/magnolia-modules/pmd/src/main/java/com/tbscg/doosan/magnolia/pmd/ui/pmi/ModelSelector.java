package com.tbscg.doosan.magnolia.pmd.ui.pmi;

import com.tbscg.doosan.magnolia.pmd.data.model.Model;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.data.provider.CallbackDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.ComboBox;

import java.util.stream.Stream;

public class ModelSelector extends ComboBox<Model> {

    private final ApiService apiService;

    public ModelSelector(ApiService apiService) {
        super();
        this.apiService = apiService;
        setWidth(200, Unit.PIXELS);
        setItemCaptionGenerator(Model::getCode);

        CallbackDataProvider.FetchCallback<Model, String> fetchCallback = query -> {
            String filter = query.getFilter().orElse("");
            if (filter.length() > 1) {
                return this.apiService.getModels(filter).stream()
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

    public ModelSelector(ApiService apiService, String caption) {
        this(apiService);
        setCaption(caption);
    }

}
