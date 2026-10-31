package com.tbscg.doosan.magnolia.pmd.app.pmiBrowser;

import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.PmiBrowserTree;
import com.vaadin.ui.Component;

import javax.inject.Inject;

public class PmiBrowserSubAppViewImpl implements PmiBrowserSubAppView {

    private final PmiBrowserTree tree;
    private Listener listener;

    @Inject
    public PmiBrowserSubAppViewImpl(ApiService apiService) {
        this.tree = new PmiBrowserTree(apiService);
        this.tree.setSizeFull();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return this.tree;
    }
}
