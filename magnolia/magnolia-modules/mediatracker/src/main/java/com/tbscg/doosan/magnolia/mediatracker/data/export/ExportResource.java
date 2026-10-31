package com.tbscg.doosan.magnolia.mediatracker.data.export;

import com.vaadin.server.StreamResource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class ExportResource<T> extends StreamResource {

    public ExportResource(String filename, Supplier<List<T>> data, ExportGenerator<T> generator) {
        super(new ExportStreamSource<>(data, generator), filename);
    }

    private static class ExportStreamSource<T> implements StreamResource.StreamSource {

        private final Supplier<List<T>> dataProvider;
        private final ExportGenerator<T> generator;

        public ExportStreamSource(Supplier<List<T>> data, ExportGenerator<T> generator) {
            this.dataProvider = data;
            this.generator = generator;

        }

        @Override
        public InputStream getStream() {
            PipedOutputStream out = new PipedOutputStream();
            try {
                PipedInputStream in = new PipedInputStream(out);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    Stream<T> items = dataProvider.get().stream();
                    generator.generate(items, out);

                    try {
                        out.close();
                    } catch (IOException e) {
                        log.error("error closing the output stream", e);
                    }
                });
                return in;
            } catch (IOException e) {
                throw new RuntimeException("error during data streaming", e);
            }
        }
    }
}
