package com.tbscg.doosan.magnolia.faq.util;

import static com.tbscg.doosan.magnolia.dxp.core.util.JcrConstants.DXP_PREFIX;

/**
 * This class manages constants publicly available for FAQ module.
 *
 * @author Alejandro Puig Escobar
 */
public class FaqConstants {

    public static final String FAQ_WORKSPACE = "faq";

    public static final class NodeTypes {

        public static final class Faq {

            public static final String NAME = DXP_PREFIX + "faq";

        }

        public static final class Section {

            public static final String NAME = DXP_PREFIX + "faqSection";

        }
    }
}
