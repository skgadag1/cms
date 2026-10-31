<template>
  <div v-if="field" data-test-id="product-groups" class="w-100 mb-3">
    <label v-if="label" :class="{'font-weight-bold': bold}">
      {{ label }}<span v-if="required"> *</span>
    </label>

    <div v-if="!isLoading" class="pt-2 border border-gray-light bg-white">
      <div class="pb-2 border-bottom border-gray-light px-3">
        <div
          class="transition-base product-interest-input d-flex align-items-center bg-gray-lighter border border-darker px-2 rounded position-relative"
          :class="{ 'search-focus': isInputFocused }"
        >
          <b-icon icon="search" :class="{ 'text-gray-light': triggerLimitMessage }" />
          <b-form-input
            v-model="searchText"
            :disabled="triggerLimitMessage"
            :placeholder="placeholder"
            class="bg-transparent border-0"
            :class="{ 'text-gray-medium': triggerLimitMessage }"
            @update="handleSearch"
            @blur="handleBlur"
            @focus="isInputFocused = true"
          />
          <button
            v-if="isInputFocused"
            class="zindex-1 bg-transparent d-flex justify-content-center align-items-center border-0"
            :disabled="triggerLimitMessage"
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

      <div v-if="productGroups.length" class="product-interest-results bg-white">
        <ul class="list-unstyled px-0 d-flex justify-content-center flex-column">
          <li
            v-for="(group, index) in productGroups"
            :key="index"
            class="px-3"
            :class="[
              index + 1 !== productGroups.length && group.products.length
                ? 'border-gray-light py-2 border-bottom'
                : 'pt-2'
            ]"
          >
            <p v-if="group.products.length" class="h6 mb-2" :class="{ 'text-gray': triggerLimitMessage }">
              {{ group.label }}
            </p>
            <ul class="list-unstyled">
              <li v-for="(product, i) in group.products" :key="i" class="mb-2">
                <b-form-checkbox
                  v-model="product.checked"
                  :name="product.label"
                  :disabled="triggerLimitMessage"
                  :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                  @change="handleProductSelect(group, product)"
                >
                  <span v-html="highlightText(product.label, searchText)" />
                </b-form-checkbox>
              </li>
            </ul>
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
import {computed, ref} from '@nuxtjs/composition-api'
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
      label = field?.label || '',
      name = field?.name || '',
      fieldValue = field?.fieldValue || '',
      noResultsMessage = field?.noResultsMessage || '',
      placeholder = field?.placeholder || '',
      limitMessage = field?.limitMessage || '',
      limit = field?.limit || 5
    } = field

    const searchText = ref('')
    const selectedProducts = ref([])
    const isInputFocused = ref(false)
    const productGroups = ref([])
    const productGroupsCopy = ref([])
    const productLimit = ref(Number(limit))
    const triggerLimitMessage = ref(false)
    const isLoading = ref(false)

    const limitMessageWithDynamicValue = computed(() =>
      limitMessage.replace('%LIMIT%', productLimit.value)
    )

    const processGroups = data =>
      data?.map(group => ({
        ...group,
        products: group.products.map(product => ({
          ...product,
          checked: false
        }))
      }))

    const getSelectedItems = () =>
      productGroupsCopy.value.flatMap(group => group.products.filter(product => product.checked))

    const handleLimit = () => {
      const selectedItems = getSelectedItems()
      triggerLimitMessage.value = selectedItems.length >= productLimit.value
    }

    const handleProductChange = () => {
      const selectedItems = getSelectedItems()
      const selectedProductMapping = selectedItems.map(item => item.value).join(';')
      const isValid = required.value ? selectedItems.length > 0 : true
      onChange(selectedProductMapping, name, isValid, fieldValue)
    }

    const syncCheckedState = (group, product) => {
      const groupInCopy = productGroupsCopy.value.find(g => g['@name'] === group['@name'])
      if (groupInCopy) {
        const productInCopy = groupInCopy.products.find(p => p['@id'] === product['@id'])
        if (productInCopy) {
          productInCopy.checked = product.checked
        }
      }
    }

    const handleProductSelect = (group, product) => {
      syncCheckedState(group, product)
      handleLimit()

      if (product.checked && !selectedProducts.value.includes(product.label)) {
        selectedProducts.value.push(product.label)
      } else if (!product.checked) {
        selectedProducts.value = selectedProducts.value.filter(p => p !== product.label)
      }

      handleProductChange()
    }

    const removeProductFromSelection = (productName) => {
      selectedProducts.value = selectedProducts.value.filter(p => p !== productName)

      productGroups.value.forEach((group) => {
        group.products.forEach((product) => {
          if (product.label === productName) {
            product.checked = false
          }
        })
      })

      productGroupsCopy.value.forEach((group) => {
        group.products.forEach((product) => {
          if (product.label === productName) {
            product.checked = false
          }
        })
      })

      handleLimit()
      handleProductChange()
    }

    const handleSearch = (text) => {
      if (text) {
        productGroups.value = productGroupsCopy.value
          .map((group) => {
            const filteredProducts = group.products.filter(product =>
              product.label.toLowerCase().includes(text.toLowerCase())
            )
            return filteredProducts.length ? {...group, products: filteredProducts} : null
          })
          .filter(Boolean)
      } else {
        productGroups.value = JSON.parse(JSON.stringify(productGroupsCopy.value))
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

    const applyDefaultSelection = () => {
      const initialSelectedLabels = []
      let checkedCount = 0
      productGroups.value.forEach((group) => {
        group.products.forEach((product) => {
          if (product.selected === 'true' && checkedCount < productLimit.value) {
            product.checked = true
            initialSelectedLabels.push(product.label)
            checkedCount++
          }
        })
      })

      selectedProducts.value = initialSelectedLabels
      productGroupsCopy.value = JSON.parse(JSON.stringify(productGroups.value))

      handleLimit()
      if (selectedProducts.value.length > 0) {
        handleProductChange()
      }
    }

    const rawGroups = field?.groups?.['@nodes']?.map(g => field?.groups[g])?.map(g => ({...g, products: g.products?.['@nodes'].map(p => g.products[p]) || []})) || []
    productGroups.value = processGroups(rawGroups)
    productGroupsCopy.value = JSON.parse(JSON.stringify(productGroups.value))

    applyDefaultSelection()

    return {
      label,
      bold,
      required,
      placeholder,
      noResultsMessage,
      isLoading,
      searchText,
      isInputFocused,
      productGroups,
      selectedProducts,
      handleProductSelect,
      removeProductFromSelection,
      handleSearch,
      highlightText,
      productLimit,
      triggerLimitMessage,
      limitMessageWithDynamicValue,
      clearSearch,
      handleBlur
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

div[data-test-id="product-groups"] {
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
