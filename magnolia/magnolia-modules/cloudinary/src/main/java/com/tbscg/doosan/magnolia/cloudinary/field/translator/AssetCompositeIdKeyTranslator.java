package com.tbscg.doosan.magnolia.cloudinary.field.translator;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.Item;
import info.magnolia.dam.api.ItemKey;

import javax.inject.Inject;

public class AssetCompositeIdKeyTranslator implements Converter<Item, String> {

    private final AssetProviderRegistry assetProviderRegistry;

    @Inject
    public AssetCompositeIdKeyTranslator(AssetProviderRegistry assetProviderRegistry) {
        this.assetProviderRegistry = assetProviderRegistry;
    }

    @Override
    public Result<String> convertToModel(Item value, ValueContext context) {
        return Result.of(() -> {
            if (value == null) {
                return null;
            }
            return value.getItemKey().asString();
        }, Throwable::getMessage);
    }

    @Override
    public Item convertToPresentation(String compositeKey, ValueContext context) {
        if (compositeKey == null) {
            return null;
        }

        final ItemKey itemKey = ItemKey.from(compositeKey);
        try {
            return assetProviderRegistry.getProviderFor(itemKey).getItem(itemKey);
        } catch (AssetProvider.AssetNotFoundException e) {
            return null;
        }
    }
}
