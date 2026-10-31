import {onBeforeMount, onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'

export default function useSearchModel(props) {
  const {$services, $regions} = useContext()
  const region = $regions.current.name
  const lang = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE
  const searchByModel = props?.group?.searchByModel?.field === 'yes' ? props.group.searchByModel : null
  const modelFieldLabel = searchByModel?.modelFieldLabel || null
  const productTypeLabel = searchByModel?.productTypeFieldLabel || null
  const defaultProductType = searchByModel?.defaultProductType
    ? {
      type: searchByModel.defaultProductType?.title || searchByModel.defaultProductType['@name'],
      level: searchByModel.defaultProductType['@nodeType'].split(':')[1].toUpperCase()
    }
    : null
  const selectedType = ref({})
  const selectedModels = ref([])
  const productTypes = ref([])
  const productModels = ref([])
  const reset = ref(false)
  const multiSelectRef = ref(null)
  const clearFacetsOnSearch = ref(false)
  const isFetchingProducts = ref(true)
  let attachmentFacetCategory

  if (props.group?.field === 'attachments') {
    attachmentFacetCategory = props.facet?.['@path']
  }

  const labels = ref({})
  useFetch(async () => {
    labels.value = await $services.bobcatapi.getConcepts(['/common/labels/listing', '/%region%/common/labels/listing', '/common/labels/functional'])
  })

  const fetchModelsForProduct = async (productType) => {
    const models = await $services.dealerinventory.getModels(productType?.productCatalogName || productType.value, productType.level, false, {
      withoutEmptyModels: true,
      categoryName: productType?.name,
      region,
      lang,
      attachmentFacetCategory
    })
    productModels.value = models?.map(model => ({
      value: model.id,
      label: model.name,
      selected: 'false',
      disabled: 'false'
    }))?.sort((a, b) => (a.label).localeCompare(b.label))

    return productModels.value
  }

  const handleTypeChange = async (value) => {
    reset.value = true
    setTimeout(() => {
      reset.value = false
    }, 50)

    if (value?.value !== selectedType.value?.value) {
      selectedType.value = value
      await fetchModelsForProduct(value)
      selectedModels.value = []
    }
  }

  const handleModelChange = (value) => {
    if (value) {
      selectedModels.value = value
      multiSelectRef.value?.clickOutside()
    }
  }

  const removeType = async () => {
    reset.value = true
    selectedType.value = {}
    selectedModels.value = []
    setTimeout(() => {
      reset.value = false
    }, 50)
  }

  const removeModel = async (models, chip) => {
    productModels.value = productModels.value?.map((model) => {
      if (model.value === chip.value) {
        model.selected = 'false'
      } else {
        model.selected = models?.find(m => m.value === model.value)?.selected || 'false'
      }
      return model
    })
    selectedModels.value = models?.filter(m => m.selected === true || m.selected === 'true') || []
  }

  const {fetch: fetchProductTypes} = useFetch(async () => {
    isFetchingProducts.value = true

    const types = await $services.dealerinventory.getProductTypes(false, {
      withoutEmptyModels: true,
      region,
      lang,
      attachmentFacetCategory
    })
    productTypes.value = types?.map(type => ({
      ...type,
      value: type.name,
      label: type.name
    }))?.sort((a, b) => a?.sortRanking - b?.sortRanking)?.reverse()

    isFetchingProducts.value = false
  })

  onBeforeMount(async () => {
    if (defaultProductType) {
      reset.value = true

      const models = await $services.dealerinventory.getModels(defaultProductType.type, defaultProductType.level, false, {
        categoryName: defaultProductType.name,
        withoutEmptyModels: true,
        region,
        lang,
        attachmentFacetCategory
      })
      productModels.value = models?.map(model => ({
        value: model.id,
        label: model.name,
        selected: 'false',
        disabled: 'false'
      }))?.sort((a, b) => (a.label).localeCompare(b.label))

      setTimeout(() => {
        reset.value = false
      }, 50)

      return
    }

    if (!isFetchingProducts.value && !productTypes.value.length) {
      await fetchProductTypes()
    }
  })

  onServerPrefetch(fetchProductTypes)

  return {
    searchByModel,
    reset,
    selectedType,
    selectedModels,
    productTypes,
    productModels,
    multiSelectRef,
    labels,
    clearFacetsOnSearch,
    modelFieldLabel,
    productTypeLabel,
    defaultProductType,
    handleTypeChange,
    handleModelChange,
    removeType,
    removeModel,
    fetchModelsForProduct,
    fetchProductTypes
  }
}
