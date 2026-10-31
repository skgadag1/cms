<template>
  <div class="mb-3" data-test-id="product-selector">
    <label v-if="label" data-test-id="product-selector-label" class="w-100" :class="{'text-danger': required && !(markedItems && markedItems.length) && isTouched}">
      {{ label }}<span v-if="required"> *</span>
    </label>
    <b-badge v-for="(markedItem, i) in markedItems"
             :key="markedItem.identifier + i"
             class="mr-1 mb-2 cursor-pointer"
             data-test-id="product-selector-marked-item"
             pill
             variant="dark"
             @click.prevent="removeFromList(markedItem)"
    >
      <span class="d-flex align-items-center">
        {{ markedItem.name }}
        <b-icon icon="remove" class="close-icon ml-1" />
      </span>
    </b-badge>
    <div v-click-outside="clickOutside" data-test-id="product-selector-input" class="selector-wrapper bg-transparent p-0 border border-1 rounded w-100 position-relative transition-base" :class="[{'border-color-black':!isHidden}, {'border-color-danger': required && !(markedItems && markedItems.length) && isTouched}]" @click.prevent="showDropdown">
      <b-form-input v-model="text" class="border-0 py-4 pl-3 pr-6" :placeholder="placeholder" @blur.prevent="isTouched = true" @input="search" />
      <span :class="{'active': !isHidden}" class="sort-icon position-absolute">
        <b-icon icon="sort-down" class="text-black transition-base" :class="{'text-danger': required && !(markedItems && markedItems.length) && isTouched}" />
      </span>
      <div v-if="!isHidden" data-test-id="product-selector-dropdown" class="dropdown-container text-left py-2 bg-white">
        <ProductList name="category" :label="categoriesLabel" :search-array="productCategoriesSearch" :add-to-list="addToList" :no-results-message="noResultsMessage" />
        <ProductList name="subcategory" :label="subcategoriesLabel" :search-array="productSubcategoriesSearch" :add-to-list="addToList" :no-results-message="noResultsMessage" />
        <ProductList name="productGroup" :label="productGroupsLabel" :search-array="productGroupsSearch" :add-to-list="addToList" :no-results-message="noResultsMessage" />
        <ProductList name="product" :label="productsLabel" :search-array="productsSearch" :add-to-list="addToList" :no-results-message="noResultsMessage" />
      </div>
    </div>
    <p v-if="validationMessage && required && !(markedItems && markedItems.length) && isTouched" class="small text-danger mt-1">{{ validationMessage }}</p>
    <div class="d-flex justify-content-end align-items-center">
      <p v-if="markedItems && markedItems.length" data-test-id="product-selector-clear-btn" class="my-2 font-weight-bold text-primary cursor-pointer text-right" @click="clearAll">{{ clearAllLabel }}</p>
    </div>
  </div>
</template>

<script>
import {ref, useContext, useFetch} from '@nuxtjs/composition-api'
import config from '~/constants/props/productSelector.config'

const {
  noResultsMessage,
  label,
  categoriesLabel,
  subcategoriesLabel,
  productGroupsLabel,
  productsLabel,
  placeholder,
  clearAllLabel,
  validationMessage,
  name,
  required,
  onChange,
  useMapping,
  bold
} = config

export default {
  props: {
    noResultsMessage,
    label,
    categoriesLabel,
    subcategoriesLabel,
    productGroupsLabel,
    productsLabel,
    placeholder,
    clearAllLabel,
    validationMessage,
    name,
    required,
    onChange,
    useMapping,
    bold
  },
  setup({name, onChange, required, useMapping}) {
    const text = ref('')
    const isHidden = ref(true)
    const isTouched = ref(false)
    const products = ref([])
    const productCategories = ref([])
    const productSubcategories = ref([])
    const productGroups = ref([])
    const productsSearch = ref([])
    const productCategoriesSearch = ref([])
    const productSubcategoriesSearch = ref([])
    const productGroupsSearch = ref([])
    const markedItems = ref([])
    const {$services} = useContext()

    const showDropdown = () => {
      isHidden.value = false
    }

    const clickOutside = () => {
      isHidden.value = true
    }

    const clearAll = () => {
      markedItems.value = []
      onChange(markedItems.value, name, markedItems.value.length > 0 && required)
      text.value = ''
      productCategoriesSearch.value = productCategories.value
      productSubcategoriesSearch.value = productSubcategories.value
      productGroupsSearch.value = productGroups.value
      productsSearch.value = products.value
    }

    const removeFromSearchArray = (searchArray, item) => {
      searchArray.value = searchArray.value.filter(it => it?.identifier !== item?.identifier)
    }

    const addToSearchArray = (searchArray, item) => {
      searchArray.value.push(item)
      searchArray.value.sort((a, b) => a.name.localeCompare(b.name))
    }

    const searchThroughArray = (searchArray, array, phrase) => {
      searchArray.value = array.value.filter(it => it.name.toLowerCase().includes(phrase))
    }

    const initSearchArray = (searchArray, array) => {
      array.value = array.value.sort((a, b) => a.name.localeCompare(b.name)).filter(a => a.identifier && a.name)
      searchArray.value = array.value
    }

    const addToList = (item, type = 'product') => {
      markedItems.value.push({...item, type})
      onChange(markedItems.value, name, true)
      if (type === 'category') {
        removeFromSearchArray(productCategoriesSearch, item)
      } else if (type === 'subcategory') {
        removeFromSearchArray(productSubcategoriesSearch, item)
      } else if (type === 'productGroup') {
        removeFromSearchArray(productGroupsSearch, item)
      } else if (type === 'product') {
        removeFromSearchArray(productsSearch, item)
      }
    }

    const removeFromList = (item) => {
      markedItems.value = markedItems.value.filter(i => i.identifier !== item.identifier)
      onChange(markedItems.value, name, markedItems.value.length > 0 && required)
      if (item.type === 'category') {
        addToSearchArray(productCategoriesSearch, item)
      } else if (item.type === 'subcategory') {
        addToSearchArray(productSubcategoriesSearch, item)
      } else if (item.type === 'productGroup') {
        addToSearchArray(productGroupsSearch, item)
      } else if (item.type === 'product') {
        addToSearchArray(productsSearch, item)
      }
    }

    const search = (phrase) => {
      searchThroughArray(productCategoriesSearch, productCategories, phrase)
      searchThroughArray(productSubcategoriesSearch, productSubcategories, phrase)
      searchThroughArray(productGroupsSearch, productGroups, phrase)
      searchThroughArray(productsSearch, products, phrase)
    }

    if (process.client) {
      useFetch(async () => {
        try {
          const {data: {selectorResult: {categories}}} = await $services.content.getProductSelector(useMapping)
          categories.forEach((category) => {
            if (category?.products) {
              products.value = [...products.value, ...category.products]
            } else if (category?.subcategories) {
              category.subcategories.forEach((subcategory) => {
                if (!subcategory.products) {
                  subcategory?.productGroups.forEach((productGroup) => {
                    if (productGroup.products) {
                      products.value = [...products.value, ...productGroup.products]
                    }
                    productGroups.value.push(productGroup)
                  })
                } else {
                  products.value = [...products.value, ...subcategory.products]
                }
                productSubcategories.value.push({identifier: subcategory.identifier, name: subcategory.name})
              })
            } else if (category?.productGroups) {
              category.productGroups.forEach((productGroup) => {
                if (productGroup.products) {
                  products.value = [...products.value, ...productGroup.products]
                }
                productGroups.value.push(productGroup)
              })
            }
            productCategories.value.push({identifier: category.identifier, name: category.name})
          })

          initSearchArray(productCategoriesSearch, productCategories)
          initSearchArray(productSubcategoriesSearch, productSubcategories)
          initSearchArray(productGroupsSearch, productGroups)
          initSearchArray(productsSearch, products)
        } catch (error) {
        // eslint-disable-next-line
        console.error('[ProductSelector] Cannot fetch products from product selector', error)
        }
      })
    }

    return {
      text,
      isHidden,
      isTouched,
      showDropdown,
      clickOutside,
      productCategoriesSearch,
      productSubcategoriesSearch,
      productGroupsSearch,
      productsSearch,
      markedItems,
      addToList,
      removeFromList,
      search,
      clearAll
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.selector-wrapper {
  &.border-color-danger {
    input::placeholder {
      color: $danger;
    }
  }
   &:hover {
    box-shadow: $btn-box-shadow;
  }
}

.dropdown-container {
  max-height: 12rem;
  overflow-y: auto;
}

.sort-icon {
  right: 1rem;
  top: 1.25rem;
  font-size: .625rem;

  &.active {
    i {
      transform: rotateX(180deg);
    }
  }
}
</style>
