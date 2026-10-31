package com.tbscg.doosan.magnolia.dxp.core.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Use with caution! This is an InputStream wrapper which prevents the undelying stream from being closed.
 * The wrapped stream must be managed and closed properly externally. Any code accessing this wrapper won't
 * be able to close the stream.
 */
public class UnclosableInputStream extends InputStream {

    private InputStream delegate;

    public UnclosableInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
        //do nothing, prevent stream closing
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
}
