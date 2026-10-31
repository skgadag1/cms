package com.tbscg.doosan.magnolia.product.catalog.util;

import static com.tbscg.doosan.magnolia.dxp.core.util.JcrConstants.DXP_PREFIX;

public final class ProductCatalogConstants {
    public static final String PRODUCTS_WORKSPACE = "products";
    public static final String ATTACHMENTS_WORKSPACE = "attachments";

    private ProductCatalogConstants() {
    }

    public static final class Properties {
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String LIFECYCLE = "lifecycle";

        private Properties() {
        }
    }

    public static final class Lifecycle {
        public static final String CURRENT = "Current";
        public static final String HISTORICAL = "Historical";

        private Lifecycle() {
        }
    }

    public static final class PageProperties {
        public static final String PRODUCT = "model";
        public static final String PRODUCT_CATEGORY = "productCategory";
        public static final String ATTACHMENT = "attachmentModel";
        public static final String CURRENT_MODEL = "currentModel";

        private PageProperties() {
        }
    }

    public static final class RelatedPage {
        public static final String NODE_NAME = "pageRelated";
        public static final String PATH = "path";
        public static final String URL = "url";

        private RelatedPage() {
        }
    }

    public static final class NodeTypes {
        private NodeTypes() {
        }

        public static final class Product {
            public static final String NAME = DXP_PREFIX + "product";

            private Product() {
            }

            public static final class Properties {
                public static final String FEATURES = "features";

                private Properties() {
                }
            }
        }

        public static final class ProductGroup {
            public static final String NAME = DXP_PREFIX + "productGroup";

            private ProductGroup() {
            }
        }

        public static final class Category {
            public static final String NAME = DXP_PREFIX + "category";

            private Category() {
            }
        }

        public static final class SubCategory {
            public static final String NAME = DXP_PREFIX + "subcategory";

            private SubCategory() {
            }
        }

        public static final class Attachment {
            public static final String NAME = DXP_PREFIX + "attachment";

            private Attachment() {
            }

            public static final class Properties {
                public static final String PRODUCTS = "products";
                public static final String PRODUCT = "product";
                public static final String RELATED_ATTACHMENTS = "relatedAttachments";
                public static final String ATTACHMENT = "attachment";

                private Properties() {
                }
            }
        }

        public static final class AttachmentGroup {
            public static final String NAME = DXP_PREFIX + "attachmentGroup";

            private AttachmentGroup() {
            }
        }

        public static final class AttachmentCategory {
            public static final String NAME = DXP_PREFIX + "attachmentCategory";

            private AttachmentCategory() {
            }
        }
    }
}
