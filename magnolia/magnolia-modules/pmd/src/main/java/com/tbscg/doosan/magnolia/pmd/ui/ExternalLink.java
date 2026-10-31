package com.tbscg.doosan.magnolia.pmd.ui;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;

public class ExternalLink extends Link {

    public ExternalLink(String caption, String url) {
        super(caption, new ExternalResource(url));
        setTargetName("_blank");
    }

    public ExternalLink(String url) {
        this(url, url);
    }
}
