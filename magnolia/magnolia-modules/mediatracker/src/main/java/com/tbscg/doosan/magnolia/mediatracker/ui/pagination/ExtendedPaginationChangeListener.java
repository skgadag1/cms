package com.tbscg.doosan.magnolia.mediatracker.ui.pagination;

import java.io.Serializable;

public interface ExtendedPaginationChangeListener extends Serializable {
    void changed(ExtendedPaginationResource resource);
}
