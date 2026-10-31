package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;


import com.vaadin.server.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.StringJoiner;

@Slf4j
public class CSVDownloader implements FormDownloader<CSVSections> {
    private static final String DELIMITER = ",";

    @Override
    public void initDownload(LoaderInput<CSVSections> data) {
        String result = parseDataToCSVFormat(data.getData());
        download(result, data.getFileName());
    }

    private void download(String result, String filename) {
        SimpleDownloader simpleDownloader = new SimpleDownloader(result, filename);
        Page.getCurrent().open(simpleDownloader, "_parent", true);
    }

    private String parseDataToCSVFormat(CSVSections data) {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.join(DELIMITER, data.getHeaders()));
        data.getRows().stream()
                .map(cells -> String.join(DELIMITER, cells))
                .forEach(joiner::add);
        return joiner.toString();
    }


}
