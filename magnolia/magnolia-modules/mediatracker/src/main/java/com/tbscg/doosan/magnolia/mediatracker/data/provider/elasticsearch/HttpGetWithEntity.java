package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;
import java.net.URL;

public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "GET";

    public HttpGetWithEntity(final String url) throws Exception {
        URL urlObj = new URL( url );
        URI uri = urlObj.toURI();
        setURI(uri);
    }
    
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
