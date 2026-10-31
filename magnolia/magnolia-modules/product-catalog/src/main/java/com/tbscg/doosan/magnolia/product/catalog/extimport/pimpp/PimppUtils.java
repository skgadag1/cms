package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to support PIM++ implementations.
 */
public final class PimppUtils {

    private static final Pattern PIMPP_MODEL_VARIANT_PMI_PATTERN = Pattern.compile("^.+\\((\\S+)\\s.+\\)$");

    private PimppUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class.");
    }

    /**
     * <p>Parse PMI from format found in PIM++ API response:
     * <code>'NAME (PMI CODE)'</code>
     * </p>
     *
     * <p>Example: <code>parsePMI("S100 (B4KH 11001)")</code> will return <code>"B4KH"</code></p>
     *
     * @param modelVariantName model name with encoded PMI
     * @param defaultValue value to return when model name doesn't match the expected pattern
     * @return extracted PMI or <code>defaultValue</code> when input doesn't match pattern.
     */
    public static String parsePMI(final CharSequence modelVariantName, final String defaultValue) {
        if (modelVariantName == null) {
            return defaultValue;
        }
        final Matcher matcher = PIMPP_MODEL_VARIANT_PMI_PATTERN.matcher(modelVariantName);
        return matcher.matches() ? matcher.group(1) : defaultValue;
    }
}
