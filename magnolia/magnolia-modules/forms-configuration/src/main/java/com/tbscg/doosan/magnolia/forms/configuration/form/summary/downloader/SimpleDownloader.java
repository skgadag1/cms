package com.tbscg.doosan.magnolia.forms.configuration.form.summary.downloader;

import com.vaadin.server.StreamResource;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class SimpleDownloader extends StreamResource {
    private final String data;
    public SimpleDownloader(String data, String filename) {
        super(new SimpleStreamSource(new StringBuilder(data)), filename);
        this.data = data;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDownloader that = (SimpleDownloader) o;
        return Objects.equals(data, that.data) && Objects.equals(getFilename(), that.getFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data);
    }

    private static class SimpleStreamSource implements StreamResource.StreamSource {
        StringBuilder data;

        public SimpleStreamSource(StringBuilder data) {
            this.data = data;
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(data.toString().getBytes());
        }
    }

}
