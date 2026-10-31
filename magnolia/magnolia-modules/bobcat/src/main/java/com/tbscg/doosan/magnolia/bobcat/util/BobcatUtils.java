package com.tbscg.doosan.magnolia.bobcat.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BobcatUtils {

    private BobcatUtils() {
    }

    /**
     * Create non parallel stream based on iterable object.
     *
     * @param iterable
     * @param <T>      type of item
     * @return {@link Stream}
     */
    public static <T> Stream<T> toStream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
