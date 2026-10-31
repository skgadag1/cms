package com.tbscg.doosan.magnolia.concepts.util;

/**
 * This class manages publicly available constants for concepts module.
 *
 * @author Alejandro Puig Escobar
 */
public class ConceptsConstants {

    public static final String CONCEPTS_APP_NAME = "concepts";
    public static final String CONCEPTS_WORKSPACE = "concepts";

    public static final class NodeTypes {

        public static final class Concept {

            public static final String NAME = info.magnolia.jcr.util.NodeTypes.Content.NAME;

            public static final class Properties {
                // choices only exist in legacy concepts
                public static final String CHOICE_VALUE = "value";
                public static final String RICH_TEXT_CHOICE = "RichText";
                public static final String PLAIN_TEXT_CHOICE = "PlainText";
                public static final String RICH_TEXT_VALUE = CHOICE_VALUE + RICH_TEXT_CHOICE;
                public static final String PLAIN_TEXT_VALUE = CHOICE_VALUE + PLAIN_TEXT_CHOICE;
                public static final String LOCALE_SEPARATOR = "_";

            }
        }
    }
}
