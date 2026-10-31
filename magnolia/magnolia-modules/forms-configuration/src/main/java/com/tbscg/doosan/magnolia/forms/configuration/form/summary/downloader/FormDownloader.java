package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;

public interface FormDownloader<DataType> {
    void initDownload(LoaderInput<DataType> data);
}
