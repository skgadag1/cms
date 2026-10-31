<template>
  <div v-if="field" data-test-id="product-interest-selector" class="w-100 mb-3">
    <label v-if="label" :class="{'font-weight-bold': bold}">
      {{ label }}<span v-if="required"> *</span>
    </label>

    <div v-if="!isLoading && selectedProducts" class="pt-2 border border-gray-light bg-white">
      <div class="pb-2 border-bottom border-gray-light px-3">
        <div
          class="transition-base product-interest-input d-flex align-items-center bg-gray-lighter border border-darker px-2 rounded position-relative"
          :class="{ 'search-focus': isInputFocused }"
        >
          <b-icon icon="search" :class="{ 'text-gray-light': triggerLimitMessage }" />
          <b-form-input
            v-model="searchText"
            :disabled="triggerLimitMessage && selectedProducts.length >= productLimit"
            :placeholder="placeholder"
            class="bg-transparent border-0"
            :class="{ 'text-gray-medium': triggerLimitMessage && selectedProducts.length >= productLimit }"
            @update="handleSearch"
            @blur="handleBlur"
            @focus="isInputFocused = true"
          />
          <button
            v-if="isInputFocused"
            class="zindex-1 bg-transparent d-flex justify-content-center align-items-center border-0"
            :disabled="triggerLimitMessage && selectedProducts.length >= productLimit"
            @click.prevent="clearSearch"
          >
            <b-icon icon="remove" />
          </button>
        </div>
      </div>
      <div v-if="triggerLimitMessage" class="text-primary py-2 border-bottom border-gray-light">
        <div class="px-3 d-flex align-items-center">
          <b-icon icon="error" class="mr-2" />
          {{ limitMessageWithDynamicValue }}
        </div>
      </div>
      <div v-if="selectedProducts.length" class="py-2 bg-white border-bottom border-gray-light">
        <ul class="categories list-unstyled px-3 mb-0 d-flex align-items-center flex-wrap">
          <li v-for="(productName, index) in selectedProducts" :key="index" class="d-flex align-items-center">
            <b-badge
              pill
              variant="dark"
              class="mr-2 cursor-pointer"
              @click.prevent="removeProductFromSelection(productName)"
            >
              <span class="d-flex align-items-center">
                {{ productName }}
                <b-icon icon="remove" class="ml-2" />
              </span>
            </b-badge>
          </li>
        </ul>
      </div>

      <div v-if="productCategories.length" class="product-interest-results bg-white">
        <ul class="list-unstyled px-0 d-flex justify-content-center flex-column">
          <li
            v-for="(category, index) in productCategories"
            :key="index"
            class="px-3"
            :class="getCategoryClasses(category, index)"
          >
            <div v-if="hasVisibleProducts(category)">
              <p class="h6 mb-2" :class="{ 'text-gray': triggerLimitMessage && selectedProducts.length >= productLimit }">
                {{ category.name }}
              </p>

              <ul v-if="category.products && category.products.length" class="list-unstyled">
                <li v-for="(product, i) in category.products" :key="i" class="mb-2">
                  <b-form-checkbox
                    v-model="product.checked"
                    :name="product.name"
                    :disabled="triggerLimitMessage"
                    :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                    @change="handleProductSelect(product, 'product')"
                  >
                    <span v-html="highlightText(product.name, searchText)" />
                  </b-form-checkbox>
                </li>
              </ul>

              <div v-if="category.subcategories && category.subcategories.length">
                <div v-for="(subcategory, subIndex) in category.subcategories" :key="subIndex" class="ml-3 mb-3">
                  <p class="h6 mb-2" :class="{ 'text-gray': triggerLimitMessage && selectedProducts.length >= productLimit }">
                    {{ subcategory.name }}
                  </p>

                  <ul v-if="subcategory.products && subcategory.products.length" class="list-unstyled">
                    <li v-for="(product, i) in subcategory.products" :key="i" class="mb-2">
                      <b-form-checkbox
                        v-model="product.checked"
                        :name="product.name"
                        :disabled="triggerLimitMessage"
                        :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                        @change="handleProductSelect(product, 'product')"
                      >
                        <span v-html="highlightText(product.name, searchText)" />
                      </b-form-checkbox>
                    </li>
                  </ul>

                  <div v-if="subcategory.productGroups && subcategory.productGroups.length">
                    <div v-for="(productGroup, groupIndex) in subcategory.productGroups" :key="groupIndex" class="ml-3 mb-2">
                      <p class="h6 mb-1" :class="{ 'text-gray': triggerLimitMessage && selectedProducts.length >= productLimit }">
                        {{ productGroup.name }}
                      </p>
                      <ul v-if="productGroup.products && productGroup.products.length" class="list-unstyled">
                        <li v-for="(product, i) in productGroup.products" :key="i" class="mb-2">
                          <b-form-checkbox
                            v-model="product.checked"
                            :name="product.name"
                            :disabled="triggerLimitMessage"
                            :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                            @change="handleProductSelect(product, 'product')"
                          >
                            <span v-html="highlightText(product.name, searchText)" />
                          </b-form-checkbox>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="category.productGroups && category.productGroups.length">
                <div v-for="(productGroup, groupIndex) in category.productGroups" :key="groupIndex" class="ml-3 mb-2">
                  <p class="small mb-1 text-muted" :class="{ 'text-gray': triggerLimitMessage && selectedProducts.length >= productLimit }">
                    {{ productGroup.name }}
                  </p>
                  <ul v-if="productGroup.products && productGroup.products.length" class="list-unstyled">
                    <li v-for="(product, i) in productGroup.products" :key="i" class="mb-2">
                      <b-form-checkbox
                        v-model="product.checked"
                        :name="product.name"
                        :disabled="triggerLimitMessage"
                        :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                        @change="handleProductSelect(product, 'product')"
                      >
                        <span v-html="highlightText(product.name, searchText)" />
                      </b-form-checkbox>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <div v-else class="px-3">
        <div role="alert" class="alert alert-warning">
          <div class="d-flex align-items-center">
            <b-icon icon="exclamation-triangle" class="mr-2" />
            <p class="mb-0">{{ noResultsMessage }}</p>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="isLoading" class="text-center py-6">
      <b-spinner variant="dark" />
    </div>
  </div>
</template>

<script>
import {computed, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useParser from '~/helpers/composables/useParser'

export default {
  props: {
    onChange: {
      type: Function
    },
    field: {
      type: Object
    }
  },
  setup(props) {
    const {field = {}, onChange} = props
    const {parseBoolean} = useParser()

    const required = computed(() => {
      return parseBoolean(field?.required)
    })

    const bold = computed(() => parseBoolean(field?.bold))

    const {
      id = '',
      label = '',
      name = '',
      placeholder = '',
      limitMessage = '',
      limit = 5,
      noResultsMessage = '',
      useMapping = false,
      excludedCategories = [],
      excludedSubcategories = []
    } = field

    const searchText = ref('')
    const selectedProducts = ref([])
    const isInputFocused = ref(false)
    const productCategories = ref([])
    const productCategoriesCopy = ref([])
    const productLimit = ref(Number(limit))
    const triggerLimitMessage = ref(false)
    const isLoading = ref(true)

    const productMap = ref(new Map())

    const {$services} = useContext()

    const limitMessageWithDynamicValue = computed(() =>
      limitMessage.replace('%LIMIT%', productLimit.value)
    )

    const processCategories = (categories) => {
      const parseExcludedItems = (excludedItems, propertyName) => {
        if (!excludedItems || !excludedItems['@nodes']) { return [] }

        const titles = []
        excludedItems['@nodes'].forEach((nodeName) => {
          const nodeData = excludedItems[nodeName]
          if (nodeData && nodeData[propertyName] && nodeData[propertyName].title) {
            titles.push(nodeData[propertyName].title)
          }
        })
        return titles
      }

      const excludedCategoryNames = parseExcludedItems(excludedCategories, 'excludedCategory')
      const excludedSubcategoryNames = parseExcludedItems(excludedSubcategories, 'excludedSubcategory')

      const processProducts = (products) => {
        if (!products) { return [] }
        return products.map((product) => {
          const cached = {
            ...product,
            checked: false
          }
          if (!productMap.value.has(product.identifier || product.name)) {
            productMap.value.set(product.identifier || product.name, cached)
          }
          return cached
        })
      }

      const processProductGroups = (productGroups) => {
        if (!productGroups) { return [] }
        return productGroups.map(group => ({
          ...group,
          products: processProducts(group.products)
        }))
      }

      const processSubcategories = (subcategories) => {
        if (!subcategories) { return [] }
        return subcategories
          .filter(sub => !excludedSubcategoryNames.includes(sub.name))
          .map(subcategory => ({
            ...subcategory,
            products: processProducts(subcategory.products),
            productGroups: processProductGroups(subcategory.productGroups)
          }))
      }

      const processCategory = category => ({
        ...category,
        products: processProducts(category.products),
        subcategories: processSubcategories(category.subcategories),
        productGroups: processProductGroups(category.productGroups)
      })

      return categories
        .filter(category => !excludedCategoryNames.includes(category.name))
        .map(processCategory)
    }

    const hasVisibleProducts = (category) => {
      if (category.products && category.products.length) { return true }
      if (category.subcategories && category.subcategories.some(sub =>
        (sub.products && sub.products.length) ||
        (sub.productGroups && sub.productGroups.some(group => group.products && group.products.length))
      )) { return true }
      if (category.productGroups && category.productGroups.some(group => group.products && group.products.length)) { return true }
      return false
    }

    const getAllProducts = () => Array.from(productMap.value.values())

    const getSelectedProducts = () => {
      return getAllProducts().filter(product => product.checked)
    }

    const handleLimit = () => {
      const selectedItems = getSelectedProducts()
      triggerLimitMessage.value = selectedItems.length >= productLimit.value
    }

    const handleProductChange = () => {
      const selectedItems = getSelectedProducts()
      const selectedProductMapping = selectedItems.map(item => item.name).join(';')
      const isValid = required.value ? selectedItems.length > 0 : true
      onChange(selectedProductMapping, name, isValid, id)
    }

    const handleProductSelect = (product) => {
      if (product.checked && getSelectedProducts().length > productLimit.value) {
        product.checked = false
        return
      }

      const cached = productMap.value.get(product.identifier || product.name)
      if (cached) {
        cached.checked = product.checked
      }

      handleLimit()
      updateSelectedProducts()
      handleProductChange()
    }

    const updateSelectedProducts = () => {
      selectedProducts.value = getSelectedProducts().map(p => p.name)
    }

    const removeProductFromSelection = (productName) => {
      const toUncheck = getAllProducts().find(p => p.name === productName)
      if (toUncheck) {
        toUncheck.checked = false
        const cached = productMap.value.get(toUncheck.identifier || toUncheck.name)
        if (cached) { cached.checked = false }
      }

      updateSelectedProducts()
      handleLimit()
      handleProductChange()
    }

    const filterCategories = (categories, searchText) => {
      if (!searchText) { return categories }

      const text = searchText.toLowerCase()

      const filterProductsDeep = products =>
        (products || []).filter(p => p.name.toLowerCase().includes(text))

      return categories.map((category) => {
        const filteredSubcategories = (category.subcategories || []).map(sub => ({
          ...sub,
          products: filterProductsDeep(sub.products),
          productGroups: (sub.productGroups || []).map(group => ({
            ...group,
            products: filterProductsDeep(group.products)
          })).filter(group => group.products.length > 0)
        })).filter(sub => sub.products.length > 0 || sub.productGroups.length > 0)

        const filteredGroups = (category.productGroups || []).map(group => ({
          ...group,
          products: filterProductsDeep(group.products)
        })).filter(group => group.products.length > 0)

        return {
          ...category,
          products: filterProductsDeep(category.products),
          subcategories: filteredSubcategories,
          productGroups: filteredGroups
        }
      }).filter(hasVisibleProducts)
    }

    const syncProductSelection = (categories) => {
      return categories.map((category) => {
        const newCategory = {...category}
        if (newCategory.products) {
          newCategory.products = newCategory.products.map(p => ({...p, checked: productMap.value.get(p.identifier || p.name)?.checked || false}))
        }
        if (newCategory.subcategories) {
          newCategory.subcategories = syncProductSelection(newCategory.subcategories)
        }
        if (newCategory.productGroups) {
          newCategory.productGroups = newCategory.productGroups.map((group) => {
            const newGroup = {...group}
            if (newGroup.products) {
              newGroup.products = newGroup.products.map(p => ({...p, checked: productMap.value.get(p.identifier || p.name)?.checked || false}))
            }
            return newGroup
          })
        }
        return newCategory
      })
    }

    const handleSearch = (text) => {
      const categoriesWithState = syncProductSelection(JSON.parse(JSON.stringify(productCategoriesCopy.value)))
      if (text) {
        productCategories.value = filterCategories(categoriesWithState, text)
      } else {
        productCategories.value = categoriesWithState
        handleLimit()
      }
    }

    const highlightText = (text, search) => {
      if (!search || !text) {
        return text
      }
      const regex = new RegExp(`(${search})`, 'gi')
      return text.replace(regex, '<span class="highlighted-text">$1</span>')
    }

    const clearSearch = () => {
      searchText.value = ''
      handleSearch('')
      isInputFocused.value = false
    }

    const handleBlur = (event) => {
      const clickedElement = event.relatedTarget
      if (!clickedElement || !clickedElement.closest('.product-interest-input')) {
        isInputFocused.value = false
      }
    }

    const getCategoryClasses = (category, index) => {
      return index + 1 !== productCategories.value.length && hasVisibleProducts(category)
        ? 'border-gray-light py-2 border-bottom'
        : 'pt-2'
    }

    if (process.client) {
      useFetch(async () => {
        try {
          const {data: {selectorResult: {categories}}} = await $services.content.getProductSelector(useMapping)
          const processedCategories = processCategories(categories)
          productCategories.value = processedCategories
          productCategoriesCopy.value = JSON.parse(JSON.stringify(processedCategories))
          isLoading.value = false
        } catch (error) {
          isLoading.value = false
        }
      })
    }

    return {
      label,
      bold,
      required,
      placeholder,
      noResultsMessage,
      isLoading,
      searchText,
      isInputFocused,
      productCategories,
      selectedProducts,
      handleProductSelect,
      removeProductFromSelection,
      handleSearch,
      highlightText,
      productLimit,
      triggerLimitMessage,
      limitMessageWithDynamicValue,
      clearSearch,
      handleBlur,
      hasVisibleProducts,
      getCategoryClasses
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';

div[data-test-id="product-interest-selector"] {
  .highlighted-text {
    background: $red-highlight !important
  }

  .categories {
    row-gap: 0.5rem
  }

  .product-interest-input {
    height: 2.5rem;

    &.search-focus {
      border-color: $red-focus-border !important;
      border-width: 2px !important;
    }
  }

  .product-interest-results {
    max-height: 25rem;
    overflow-y: auto;
  }

  .product-interest-checkbox-disabled {
    .custom-control-label {
      cursor: auto !important;
    }
  }
}
</style>
