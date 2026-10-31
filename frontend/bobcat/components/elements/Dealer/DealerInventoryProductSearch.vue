<template>
  <div class="dealer-inventory-product-search d-flex flex-column flex-lg-row justify-content-center align-items-center py-5" data-test-id="dl-inv-product-search">
    <div class="dealer-inventory-product-search__text mb-4 mb-lg-0" :class="textClass">
      <h2 class="dealer-inventory-product-search__headline mb-2" data-test-id="dl-inv-product-search-headline">{{ headline }}</h2>
      <div v-if="description" class="dealer-inventory-product-search__description" data-test-id="dl-inv-product-search-description" v-html="description" />
    </div>

    <div class="dealer-inventory-product-search__card bg-white" data-test-id="dl-inv-product-search-card">
      <DropdownAdvanced :label="productLabel"
                        :placeholder="placeholder"
                        :options="dropdownOptions"
                        :filter-values="filterValues"
                        :search-placeholder="searchPlaceholder"
                        :no-results-label="noResultsLabel"
                        :no-facet-search-data="!dropdownOptions.length"
                        :on-change="() => {}"
                        search-enabled="true"
                        name="dealerInventoryProduct"
                        default-id="value"
                        class="mb-0"
                        data-test-id="dl-inv-product-search-dropdown"
                        related-dropdowns
                        disable-min-width
                        get-whole-value
                        @changed="changeProduct"
      />
      <b-button variant="primary"
                class="w-100 mt-3"
                :disabled="!selectedProduct"
                data-test-id="dl-inv-product-search-button"
                @click="search"
      >
        {{ searchLabel }}
      </b-button>
    </div>
  </div>
</template>

<script>
import {computed, ref} from '@nuxtjs/composition-api'
import DropdownAdvanced from '~/components/elements/Form/DropdownAdvanced.vue'

const parseCategories = (json) => {
  try {
    const categories = JSON.parse(json || '[]')
    return Array.isArray(categories) ? categories : []
  } catch {
    return []
  }
}

export default {
  components: {
    DropdownAdvanced
  },
  props: {
    options: {
      type: Array,
      default: () => []
    },
    productDefaultValues: {
      type: String
    },
    othersCategoryLabel: {
      type: String
    },
    headline: {
      type: String
    },
    description: {
      type: String
    },
    productLabel: {
      type: String
    },
    placeholder: {
      type: String
    },
    searchLabel: {
      type: String
    },
    searchPlaceholder: {
      type: String
    },
    noResultsLabel: {
      type: String
    },
    textClass: {
      type: String
    }
  },
  setup(props, {emit}) {
    const selectedProduct = ref(null)
    const keyword = ref('')
    const categories = computed(() => parseCategories(props.productDefaultValues))

    const getCategory = name => categories.value.find(({values}) => values?.includes(name))
    const getRank = (list, item) => (list.includes(item) ? list.indexOf(item) : list.length)

    // groups the dealer's product types by the authored categories, same JSON as the dealer locator product facet
    const groupedOptions = computed(() => {
      const groups = new Map()
      props.options.forEach(({irwName, name}) => {
        const category = getCategory(name)?.category || props.othersCategoryLabel
        groups.set(category, [...(groups.get(category) || []), {label: name, value: irwName}])
      })
      const categoryNames = categories.value.map(({category}) => category)
      return [...groups.entries()]
        .sort(([a], [b]) => getRank(categoryNames, a) - getRank(categoryNames, b))
        .map(([category, values]) => {
          const order = getCategory(values[0].label)?.values || []
          return {
            category: categories.value.length ? category : '',
            values: [...values].sort((a, b) => getRank(order, a.label) - getRank(order, b.label))
          }
        })
    })

    const dropdownOptions = computed(() => {
      const query = keyword.value.trim().toLowerCase()
      return groupedOptions.value
        .map(group => ({
          ...group,
          values: group.values
            .filter(({label}) => !query || label.toLowerCase().includes(query))
            .map(option => ({...option, selected: option.value === selectedProduct.value?.value}))
        }))
        .filter(group => group.values.length)
    })

    const filterValues = (value) => {
      keyword.value = value || ''
    }

    const changeProduct = ([product] = []) => {
      selectedProduct.value = product || null
    }

    const search = () => {
      if (selectedProduct.value) {
        emit('search', selectedProduct.value.value)
      }
    }

    return {
      selectedProduct,
      dropdownOptions,
      filterValues,
      changeProduct,
      search
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.dealer-inventory-product-search {
  @include media-breakpoint-up(lg) {
    min-height: 33.4375rem;
  }

  &__text {
    width: 100%;

    @include media-breakpoint-up(lg) {
      flex: 0 0 26.9375rem;
      max-width: 26.9375rem;
      margin-right: 1rem;
    }
  }

  &__headline {
    @include media-breakpoint-up(lg) {
      font-size: 3rem;
      line-height: 4.09375rem;
    }
  }

  &__description,
  &__description p {
    font-size: 1.125rem;
    line-height: 1.5rem;
  }

  &__card {
    box-shadow: 0 .125rem .25rem rgba($black, .1);
    width: 100%;
    padding: 2rem;

    @include media-breakpoint-up(lg) {
      flex: 0 0 22.5rem;
      max-width: 22.5rem;
    }

    .category-options {
      flex-basis: 100%;
    }

    .btn:disabled {
      opacity: 1;
    }
  }
}
</style>
