<template>
  <b-overlay :show="$fetchState.pending">
    <CompareModelPicker ref="mpicker"
                        :options="categoryModels"
                        :labels="labels"
                        :disabled="showCompareTable"
                        @activate="showCompareTable = false"
                        @remove="showCompareTable = false"
                        @compare="compareModelsHandler"
    />

    <CompareTableWrapper v-if="showCompareTable"
                         data-test-id="product-compare-table"
                         class="my-4"
                         no-bottom-margin
                         :labels="labels"
                         :models="modelsToCompare"
                         :uom="uom"
                         :config="productCompareConfig"
    />
  </b-overlay>
</template>

<script>
import {ref, useFetch, useContext, onDeactivated} from '@nuxtjs/composition-api'
import {PRODUCT_SPECS_REMOVED_SECTIONS, PRODUCT_SPECS_REMOVED_PROPERTIES, MODEL_TYPE, MODEL_LIFECYCLE} from '~/constants'
import useModels from '~/helpers/composables/useModels'

export default {
  inject: ['model'],
  provide: ['model'],
  props: {
    isVisible: {
      type: Boolean,
      default: false
    },
    uom: {
      type: Object
    },
    historical: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      default: () => { return {} }
    }
  },
  setup(props) {
    const {historical} = props
    const {model, models} = useModels(MODEL_TYPE.PRODUCT, historical)
    const mpicker = ref(null)
    const currentModels = models
    const currentModelsId = currentModels.map(x => x['@id'])
    const categoryModels = ref([])
    const modelsToCompare = ref([])
    const showCompareTable = ref(false)
    const {$services, $ga4} = useContext()
    const productCompareConfig = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      alwaysShowHeader: true,
      noBottomMargin: true
    }

    const getCurrentType = () => historical ? MODEL_LIFECYCLE.HISTORICAL : MODEL_LIFECYCLE.CURRENT

    const getCurrentModels = (data) => {
      if (!modelsToCompare.value.length) {
        const allModels = []
        Object.keys(data).map(x => data[x])?.forEach((y) => {
          if (y['@nodeType'] === 'dxp:productGroup') {
            allModels.push(...y['@nodes'].map(z => y[z]))
          } else {
            allModels.push(y)
          }
        })
        const currentType = getCurrentType()
        return allModels?.filter(z => z['@nodeType'] === 'dxp:product' && z.lifecycle?.toLowerCase() === currentType).filter(y => !currentModelsId.includes(y['@id']))
      }
      return modelsToCompare.value
    }

    onDeactivated(() => {
      mpicker.value?.clearAll()
      showCompareTable.value = false
    })

    useFetch(async () => {
      let _modelPath = model['@path'].replace(`/${model['@name']}`, '') // to be replaced if selected model is inside productGroup

      if (model['@nodeType'] === `dxp:${MODEL_TYPE.PRODUCT}`) {
        try {
          let parentPath = model['@path'].split('/').slice(0, -1).join('/')
          const data = await $services.bobcatapi.getProduct(null, parentPath)
          const parentName = data['@name']
          parentPath = data['@path'].replace(`/${parentName}`, '')
          _modelPath = data?.['@nodeType'] === 'dxp:productGroup' ? parentPath || _modelPath : _modelPath
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error(`[ProductCompare] Unable to fetch parent for model ${model['@path']}, ${e}`)
        }
      }

      if (!categoryModels.value.length) {
        try {
          const {data} = await $services.bobcatapi.getCategoryModels(_modelPath)
          categoryModels.value = getCurrentModels(data)
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error(`[ProductCompare] Unable to fetch category models for model ${model['@path']}, ${e}`)
        }
      }
    })

    const compareModelsHandler = (models) => {
      const currentType = getCurrentType()
      const m = []
      models.forEach((model) => {
        if (model['@nodeType'] === 'dxp:productGroup') {
          m.push(...model['@nodes'].map((name) => {
            return model[name]['@nodeType'] === 'dxp:product' ? model[name] : null
          })
            .filter(model => model?.lifecycle?.toLowerCase() === currentType)
            .filter(x => x))
        } else { m.push(model) }
      })

      modelsToCompare.value = m
      showCompareTable.value = true

      if (modelsToCompare.value?.length) {
        $ga4.events.product_compare_cta(modelsToCompare.value)
      }
    }

    return {
      mpicker,
      categoryModels,
      compareModelsHandler,
      modelsToCompare,
      showCompareTable,
      productCompareConfig
    }
  }
}
</script>
