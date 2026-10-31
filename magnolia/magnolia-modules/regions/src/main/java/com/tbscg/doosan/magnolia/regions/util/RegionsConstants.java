package com.tbscg.doosan.magnolia.regions.util;

import static com.tbscg.doosan.magnolia.dxp.core.util.JcrConstants.DXP_PREFIX;

/**
 * Class that exposes constants for regions workspace.
 *
 * @author Alejandro Puig Escobar
 */
public class RegionsConstants {

    private RegionsConstants() {
    }

    public static final String REGIONS_WORKSPACE = "regions";
    public static final String REGION_SITE_NAME_SEPARATOR = "-";

    public static final class NodeTypes {

        private NodeTypes() {
        }

        public static final class Website {

            private Website() {
            }

            public static final String NAME = DXP_PREFIX + "website";

        }

        public static final class Region {

            private Region() {
            }

            public static final String NAME = DXP_PREFIX + "region";

        }

        public static final class Language {

            private Language() {
            }

            public static final String NAME = DXP_PREFIX + "language";

        }
    }

    public static class Properties {

        private Properties() {
        }

        public static final String TITLE = "title";

    }

}
