package com.tbscg.doosan.magnolia.regions.compatibility.app.availability;

import com.tbscg.doosan.magnolia.dxp.core.compatibility.app.availability.ClipboardContentIsNodeType;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.ui.framework.ContentClipboard;

import javax.inject.Inject;

/**
 * Checks whether content clipboard contains copied items from <code>dxp:language</code> node type.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class ClipboardContentIsLanguage extends ClipboardContentIsNodeType {

    @Inject
    public ClipboardContentIsLanguage(ContentClipboard<?> contentClipboard) {
        super(contentClipboard);
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        return super.isClipboardContentOfType(RegionsConstants.NodeTypes.Language.NAME, RegionsConstants.REGIONS_WORKSPACE);
    }

}
