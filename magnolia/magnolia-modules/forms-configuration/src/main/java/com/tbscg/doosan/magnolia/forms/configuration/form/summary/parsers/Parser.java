package com.tbscg.doosan.magnolia.forms.configuration.form.summary.parsers;

import com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader.FormRequest;

import java.util.List;

public interface Parser<I, R> {
    R parse(List<I> items, FormRequest formRequest);
}
