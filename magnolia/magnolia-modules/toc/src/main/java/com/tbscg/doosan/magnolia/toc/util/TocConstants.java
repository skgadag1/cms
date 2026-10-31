package com.tbscg.doosan.magnolia.toc.util;

import static com.tbscg.doosan.magnolia.dxp.core.util.JcrConstants.DXP_PREFIX;

/**
 * This class manages constants publicly available for FAQ module.
 *
 * @author Alejandro Puig Escobar
 */
public class TocConstants {

    public static final String TOC_WORKSPACE = "toc";

    public static final class NodeTypes {

        public static final class Toc {

            public static final String NAME = DXP_PREFIX + "toc";

        }

        public static final class Package {

            public static final String NAME = DXP_PREFIX + "tocPackage";

        }
    }
}
