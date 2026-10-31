package com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources;

import java.util.stream.Stream;

public interface TableConnector<T> {
    Stream<T> getRowsByKey(String key);
}
