import {inject, computed, unref} from '@nuxtjs/composition-api'
import {MODEL_TYPE, MODEL_LIFECYCLE} from '~/constants'

export default function useModels(modelType = MODEL_TYPE.PRODUCT, historical = false) {
  const model = inject('model')
  const selectedVariantName = inject('selectedVariant')

  const modelData = unref(model)
  let models = [modelData]

  if (modelData?.['@nodeType']) {
    models = modelData['@nodeType'] === `dxp:${modelType?.toLowerCase()}Group`
      ? modelData['@nodes'].map((name) => {
        return modelData[name] ? (modelData[name]['@nodeType'] === `dxp:${modelType.toLowerCase()}` ? modelData[name] : null) : null
      }).filter(x => x)
      : [modelData]
  }

  const _currentType = historical ? MODEL_LIFECYCLE.HISTORICAL : MODEL_LIFECYCLE.CURRENT
  models = models.filter(m => m?.lifecycle?.toLowerCase() === _currentType)

  const modelVariant = computed(() => {
    const name = selectedVariantName?.value
    if (!name) { return null }
    const v = models.find(m => m?.['@name'] === name) || null
    return v
  })

  const handleVariantChange = (v) => {
    if (selectedVariantName) { selectedVariantName.value = v }
  }

  return {
    model,
    models,
    modelVariant,
    selectedVariantName,
    handleVariantChange
  }
}
