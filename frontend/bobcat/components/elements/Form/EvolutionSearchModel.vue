<template>
  <div class="search ngb-form facets  mb-3"
       data-test-id="search-model"
  >
    <b-button
      v-b-toggle="`search-by-model`"
      variant="outline"
      class="facets-toggle d-flex justify-content-between p-2"
      block
    >
      <span class="text-left font-weight-bold py-1 pl-1 pl-lg-2">{{ labels.searchByEquipmentTitleLabel || 'Search by Equipment' }}</span> <span
        class="dropdown-toggle py-1 pr-1 pr-lg-2"
      />
    </b-button>
    <b-collapse
      id="search-by-model"
      visible
      class="facets-content mt-0"
    >
      <b-card class="p-3" no-body>
        <form class="" data-test-id="search-model">
          <Dropdown
            v-if="!defaultProductType"
            :reset="reset"
            :label="productTypeLabel || labels.productType || 'Product Type'"
            name="product_type"
            class="label-no-margin"
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
            class="label-no-margin"
            default-id="value"
            :label="modelFieldLabel || labels.model || 'Model'"
            :clear-label="labels.clearAll || 'Clear all'"
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
          <DynamicButton
            :variant="BUTTON_NAMES.HEXAGONAL"
            class="btn submit-button col-12"
            :disabled="defaultProductType ? !selectedModels.length : (!selectedType.productCatalogName)"
            @click.prevent="search"
          >
            <span class="d-flex align-items-center justify-content-center">
              <span>{{ labels.searchLabel || 'Search' }}</span>
            </span>
          </DynamicButton>
        </form>
      </b-card>
    </b-collapse>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import DynamicButton from '../../DynamicButton.vue'
import {BUTTON_NAMES} from '../../../constants/buttons'

export default {
  components: {DynamicButton},
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
      search,
      BUTTON_NAMES
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/pagination';
@import '~/assets/sass/variables';

.search {
  :deep(.label-no-margin label) {
    margin-bottom: 0;
  }
}
</style>
