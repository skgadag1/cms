package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import lombok.Data;

@Data
public class BuildAndQuoteCategory {

    private String prodCategoryId;
    private String prodCategoryName;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String prodCategoryId;
        private String prodCategoryName;

        private Builder() {
        }

        public Builder withProdCategoryId(final String prodCategoryId) {
            this.prodCategoryId = prodCategoryId;
            return this;
        }

        public Builder withProdCategoryName(final String prodCategoryName) {
            this.prodCategoryName = prodCategoryName;
            return this;
        }

        public BuildAndQuoteCategory build() {
            final BuildAndQuoteCategory buildAndQuoteCategory = new BuildAndQuoteCategory();
            buildAndQuoteCategory.setProdCategoryId(prodCategoryId);
            buildAndQuoteCategory.setProdCategoryName(prodCategoryName);
            return buildAndQuoteCategory;
        }
    }
}
