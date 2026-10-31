package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
@Builder(setterPrefix = "with")
public class CSVSections {
    private List<String> headers;
    private List<List<String>> rows;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CSVSections that = (CSVSections) o;
        return Objects.equals(headers, that.headers) && Objects.equals(rows, that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, rows);
    }
}
