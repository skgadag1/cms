<template>
  <b-form-group data-test-id="product-interest">
    <label v-if="label" :class="{ 'font-weight-bold': bold === 'true' }">
      {{ label }}<span v-if="required === 'true'"> *</span>
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
      <div v-if="selectedSubcategories.length" class="py-2 bg-white border-bottom border-gray-light">
        <ul class="categories list-unstyled px-3 mb-0 d-flex align-items-center flex-wrap">
          <li v-for="(subcategoryName, index) in selectedSubcategories" :key="index" class="d-flex align-items-center">
            <b-badge
              pill
              variant="dark"
              class="mr-2 cursor-pointer"
              @click.prevent="removeSubcategoryFromSelection(subcategoryName)"
            >
              <span class="d-flex align-items-center">
                {{ subcategoryName }}
                <b-icon icon="remove" class="ml-2" />
              </span>
            </b-badge>
          </li>
        </ul>
      </div>

      <div v-if="codes.length" class="product-interest-results bg-white">
        <ul class="list-unstyled px-0 d-flex justify-content-center flex-column">
          <li
            v-for="(category, index) in codes"
            :key="index"
            class="px-3"
            :class="[
              index + 1 !== codes.length && category.contractCodes.length
                ? 'border-gray-light py-2 border-bottom'
                : 'pt-2'
            ]"
          >
            <p v-if="category.contractCodes.length" class="h6 mb-2" :class="{ 'text-gray': triggerLimitMessage }">
              {{ category.title }}
            </p>
            <ul class="list-unstyled">
              <li v-for="(subcategory, i) in category.contractCodes" :key="i" class="mb-2">
                <b-form-checkbox
                  v-model="subcategory.checked"
                  :name="subcategory.fullName"
                  :disabled="triggerLimitMessage"
                  :class="{ 'product-interest-checkbox-disabled': triggerLimitMessage }"
                  @change="handleSubcategorySelect(category, subcategory)"
                >
                  <span v-html="highlightText(subcategory.fullName, searchText)" />
                </b-form-checkbox>
              </li>
            </ul>
          </li>
        </ul>
      </div>
      <div v-else class="px-3">
        <WarningAlert type="warning">
          <div class="d-flex align-items-center">
            <b-icon icon="warn" class="mr-2" />
            <p class="mb-0">{{ noResultsMessage }}</p>
          </div>
        </WarningAlert>
      </div>
    </div>
    <div v-else-if="isLoading" class="text-center py-6">
      <b-spinner variant="dark" />
    </div>
  </b-form-group>
</template>

<script>
import {computed, onMounted, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import {DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE} from '~/constants/staticRoutes'
import {USER_COVEO_METADATA} from '~/constants'

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
    const {
      label = '',
      name = '',
      bold = 'false',
      noResultsMessage = '',
      placeholder = '',
      limitMessage = '',
      limit = 3,
      required = 'false'
    } = field

    const searchText = ref('')
    const selectedSubcategories = ref([])
    const isInputFocused = ref(false)
    const codes = ref([])
    const codesCopy = ref([])
    const productLimit = ref(Number(limit))
    const triggerLimitMessage = ref(false)
    const isLoading = ref(true)

    const limitMessageWithDynamicValue = computed(() =>
      limitMessage.replace('%LIMIT%', productLimit.value)
    )

    const {$services} = useContext()
    const router = useRouter()

    useFetch(async () => {
      const urlParam = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
      if (Number.isInteger(parseInt(urlParam))) {
        const data = await $services.dealerlocator.getDealerContractCodes(urlParam)
        codes.value = processCodes(data)
        codesCopy.value = JSON.parse(JSON.stringify(codes.value))

        applyDefaultSelectionFromLocalStorage()
        isLoading.value = false
      }
    })

    const processCodes = data =>
      data?.map(category => ({
        ...category,
        contractCodes: category.contractCodes.map(subcategory => ({
          ...subcategory,
          checked: false
        }))
      }))

    const handleSearch = (text) => {
      if (text) {
        codes.value = codesCopy.value
          .map((category) => {
            const filteredContractCodes = category.contractCodes.filter(subcategory =>
              subcategory.fullName.toLowerCase().includes(text.toLowerCase())
            )
            return filteredContractCodes.length ? {...category, contractCodes: filteredContractCodes} : null
          })
          .filter(Boolean)
      } else {
        codes.value = codesCopy.value.map(category => ({
          ...category,
          contractCodes: category.contractCodes.map(subcategory => ({
            ...subcategory,
            checked: subcategory.checked
          }))
        }))
        handleLimit()
      }
    }

    const getSelectedItems = () =>
      codes.value.flatMap(category => category.contractCodes.filter(subcategory => subcategory.checked))

    const handleLimit = () => {
      const selectedItems = codesCopy.value.flatMap(category =>
        category.contractCodes.filter(subcategory => subcategory.checked)
      )
      triggerLimitMessage.value = selectedItems.length >= productLimit.value
    }

    const handleSubcategoryChange = () => {
      const selectedSubcategoriesList = getSelectedItems().map(item => item.englishName || item.fullName).join(';')
      const isValid = required === 'true' ? selectedSubcategoriesList.length > 0 : true
      onChange(selectedSubcategoriesList, name, isValid)
    }

    const syncCheckedState = (category, subcategory) => {
      const categoryInCopy = codesCopy.value.find(c => c.title === category.title)
      const subcategoryInCopy = categoryInCopy?.contractCodes?.find(sub => sub.fullName === subcategory.fullName)
      if (subcategoryInCopy) {
        subcategoryInCopy.checked = subcategory.checked
      }
    }

    const handleSubcategorySelect = (category, subcategory) => {
      syncCheckedState(category, subcategory)
      handleLimit()

      if (subcategory.checked && !selectedSubcategories.value.includes(subcategory.fullName)) {
        selectedSubcategories.value.push(subcategory.fullName)
      } else if (!subcategory.checked) {
        selectedSubcategories.value = selectedSubcategories.value.filter(c => c !== subcategory.fullName)
      }

      handleSubcategoryChange()
    }

    const removeSubcategoryFromSelection = (subcategoryName) => {
      selectedSubcategories.value = selectedSubcategories.value.filter(c => c !== subcategoryName)

      codes.value = codes.value.map(category => ({
        ...category,
        contractCodes: category.contractCodes.map(subcategory =>
          subcategory.fullName === subcategoryName ? {...subcategory, checked: false} : subcategory
        )
      }))

      codesCopy.value = codesCopy.value.map(category => ({
        ...category,
        contractCodes: category.contractCodes.map(subcategory =>
          subcategory.fullName === subcategoryName ? {...subcategory, checked: false} : subcategory
        )
      }))

      handleLimit()
      handleSubcategoryChange()
    }

    const highlightText = (text, search) => {
      if (!search) {
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

    const applyDefaultSelectionFromLocalStorage = () => {
      if (!process.client) {
        return
      }
      const storedData = localStorage.getItem(USER_COVEO_METADATA)
      if (storedData) {
        const metadata = JSON.parse(storedData)
        if (metadata && Array.isArray(metadata.products)) {
          const storedProducts = metadata.products.map(p => p.toLowerCase())

          let checkedCount = 0

          codes.value = codes.value.map(category => ({
            ...category,
            contractCodes: category.contractCodes.map((subcategory) => {
              const shouldCheck =
                storedProducts.includes(subcategory.fullName.toLowerCase()) && checkedCount < productLimit.value
              if (shouldCheck) {
                checkedCount++
              }

              return {
                ...subcategory,
                checked: shouldCheck
              }
            })
          }))

          selectedSubcategories.value = codes.value
            .flatMap(category => category.contractCodes)
            .filter(subcategory => subcategory.checked)
            .map(subcategory => subcategory.fullName)
          codesCopy.value = JSON.parse(JSON.stringify(codes.value))

          handleLimit()
          handleSubcategoryChange()
        }
      }
    }

    onMounted(applyDefaultSelectionFromLocalStorage)

    return {
      searchText,
      isInputFocused,
      codes,
      selectedSubcategories,
      handleSubcategorySelect,
      removeSubcategoryFromSelection,
      handleSearch,
      highlightText,
      productLimit,
      triggerLimitMessage,
      limitMessageWithDynamicValue,
      clearSearch,
      handleBlur,
      label,
      bold,
      noResultsMessage,
      required,
      placeholder,
      isLoading
    }
  }
}

</script>

<style lang="scss">
@import '~/assets/sass/colors';

fieldset[data-test-id="product-interest"] {
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
