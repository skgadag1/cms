package com.tbscg.doosan.magnolia.mediatracker.data.export;

import java.io.OutputStream;
import java.util.stream.Stream;

@FunctionalInterface
public interface ExportGenerator<T> {
    void generate(Stream<T> items, OutputStream exportStream);
}
