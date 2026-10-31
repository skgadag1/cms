<template>
  <form class="search col-12 ngb-form mb-2" data-test-id="search-model">
    <div class="search-title py-2">
      <span class="text-left font-weight-bold">{{ labels.searchByModelTitleLabel || 'Search Attachments by Model' }}</span>
    </div>
    <div class="mt-3 mt-md-0 w-md-auto mx-0 mx-md-2 mx-lg-0 py-3">
      <Dropdown
        v-if="!defaultProductType"
        :reset="reset"
        :label="productTypeLabel || labels.productType || 'Product Type'"
        name="product_type"
        :init-value="selectedType"
        :placeholder="labels.productTypePlaceholder || 'Select'"
        :options="productTypes"
        required
        required-sign
        :on-change="handleTypeChange"
        get-whole-value
        disable-min-width
        related-dropdowns
      />
      <MultiSelect
        ref="multiSelectRef"
        :reset="reset"
        related-dropdowns
        default-id="value"
        :label="modelFieldLabel || labels.model || 'Model'"
        :clear-label="labels.clearAll || 'Clear all'"
        :selected-label="labels.selectedOptions || 'Selected options'"
        name="product_model"
        :init-values="selectedModels"
        :placeholder="labels.modelFieldPlaceholder || 'Select'"
        :options="productModels"
        :on-change="() => {}"
        :disabled-dropdown="!defaultProductType ? Object.keys(selectedType).length === 0 : false"
        get-whole-value
        disable-min-width
        @changed="handleModelChange"
      />
      <b-button
        variant="primary"
        type="button"
        class="btn submit-button col-12"
        :disabled="defaultProductType ? !selectedModels.length : (!selectedType.productCatalogName)"
        @click.prevent="search"
      >
        <span class="d-flex align-items-center justify-content-center">
          <span>{{ labels.searchLabel || 'Search' }}</span>
        </span>
      </b-button>
    </div>
  </form>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'

export default {
  props: {
    reset: Boolean,
    productTypeLabel: String,
    modelFieldLabel: String,
    selectedType: Object,
    selectedModels: Array,
    productTypes: Array,
    productModels: Array,
    defaultProductType: Object,
    labels: Object
  },
  emits: ['handleTypeChange', 'handleModelChange', 'search'],
  setup(props, {emit}) {
    const multiSelectRef = ref(null)

    const handleTypeChange = (value) => {
      emit('handleTypeChange', value)
    }

    const handleModelChange = (value) => {
      emit('handleModelChange', value)
    }

    const search = () => {
      emit('search')
    }

    return {
      multiSelectRef,
      handleTypeChange,
      handleModelChange,
      search
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/pagination';
@import '~/assets/sass/variables';

.search {
  border: 1px solid $tertiary !important;
  box-shadow: none !important;
  font-family: inherit;
  font-size: 1rem;
  background-color: white;

  &-title {
    border-bottom: 1px solid $tertiary !important;
  }
}
</style>
