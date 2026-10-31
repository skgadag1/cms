package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class LoaderInput<DataType> {
    private String fileName;
    private DataType data;
}
