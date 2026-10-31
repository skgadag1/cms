package com.tbscg.doosan.magnolia.cloudinary.contentapp;

import com.vaadin.data.provider.*;
import com.vaadin.ui.ItemCaptionGenerator;
import info.magnolia.dam.app.data.AssetListDataProvider;
import info.magnolia.ui.contentapp.CaptionFilteringDataProviderWrapper;
import info.magnolia.ui.contentapp.FilteringMode;

import java.util.stream.Stream;

/**
 * Decorator class for {@link CaptionFilteringDataProviderWrapper}, which prevents
 * unnecessary API requests causing poor performance of dialog form creation.
 * {@link CaptionFilteringDataProviderWrapper} is normally used by v6 of
 * Magnolia UI code (wrapper of DataProvider in field factory) when setting up
 * the dialog form data by fetching the data from DataProvider of each field in the form.
 */
public class CustomCaptionFilteringDataProviderWrapper<T, F> extends DataProviderWrapper<T, String, F> {

    private final CaptionFilteringDataProviderWrapper<T, F> delegate;

    public static <T, F> CustomCaptionFilteringDataProviderWrapper<T, F> wrap(DataProvider<T, F> src) {
        return new CustomCaptionFilteringDataProviderWrapper<T, F>(src);
    }

    private CustomCaptionFilteringDataProviderWrapper(DataProvider<T, F> dataProvider) {
        super(dataProvider);
        this.delegate = CaptionFilteringDataProviderWrapper.wrap(dataProvider);
    }

    public CustomCaptionFilteringDataProviderWrapper<T, F> withItemCaptionGenerator(ItemCaptionGenerator<T> icg) {
        this.delegate.withItemCaptionGenerator(icg);
        return this;
    }

    public CustomCaptionFilteringDataProviderWrapper<T, F> withFilteringMode(FilteringMode fm) {
        this.delegate.withFilteringMode(fm);
        return this;
    }

    public Stream<T> fetch(Query<T, String> t) {
        //FIXME: when migrating Cloudinary integration to v6 of Magnolia UI
        if (this.dataProvider instanceof AssetListDataProvider) {
            return Stream.empty();
        }
        return this.delegate.fetch(t);
    }

    public int size(Query<T, String> t) {
        //FIXME: when migrating Cloudinary integration to v6 of Magnolia UI
        if (this.dataProvider instanceof AssetListDataProvider) {
            return 0;
        }
        return this.delegate.size(t);
    }

    protected F getFilter(Query<T, String> query) {
        return null;
    }
}
