package com.tbscg.doosan.magnolia.dxp.workflow.util;

import info.magnolia.context.Context;

import java.util.Map;
import java.util.Optional;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.PARAMETER_MGNL_DATA;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;

public final class PublicationScopes {

    private PublicationScopes() {
    }

    public static PublicationScope all() {
        return PublicationScope.ALL;
    }

    public static PublicationScope fromContext(final Context context) {
        if (context == null) {
            return all();
        }
        return Optional.ofNullable(context.get(LIFECYCLE))
                .map(PublicationScopes::fromLifecycleValue)
                .orElseGet(() -> context.get(PARAMETER_MGNL_DATA) instanceof Map<?, ?> params
                        ? fromParams(params)
                        : all());
    }

    public static PublicationScope fromParams(final Map<?, ?> params) {
        return params == null ? all() : fromLifecycleValue(params.get(LIFECYCLE));
    }

    public static PublicationScope forProductLifecycle(final String lifecycle) {
        return fromLifecycleValue(lifecycle);
    }

    private static PublicationScope fromLifecycleValue(final Object lifecycleValue) {
        if (!(lifecycleValue instanceof String lifecycle) || WorkflowUtil.isUnsupportedProductLifecycle(lifecycle)) {
            return all();
        }
        return new ProductLifecyclePublicationScope(lifecycle);
    }
}
