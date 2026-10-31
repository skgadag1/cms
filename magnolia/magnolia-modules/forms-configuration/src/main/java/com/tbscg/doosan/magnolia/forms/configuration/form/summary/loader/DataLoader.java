package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import java.util.List;

public interface DataLoader<T> {
    List<T> getData(FormRequest request);
}
