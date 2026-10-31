<template>
  <LazyHydrate when-visible>
    <section data-test-id="model-lookup" class="py-4 bg-gray-lighter">
      <div class="container">
        <div class="row d-flex justify-content-center">
          <b-col cols="12" lg="6">
            <b-card class="model-lookup-card rounded-4 overflow-hidden"
                    data-test-id="model-lookup-card"
                    :header="title"
                    :header-class="`card-title bg-dark text-white ${htag}`"
                    header-tag="header"
                    no-body
            >
              <b-card-body class="text-center p-3">
                <b-form-select v-if="categories && categories.length"
                               v-model="selectedCategory"
                               :options="categories"
                               size="md"
                               class="model-lookup-select cursor-pointer"
                               :class="{'text-gray-600':selectedCategory === -1}"
                               data-test-id="model-lookup__category-select"
                               @change="resetModel"
                >
                  <template #first>
                    <b-form-select-option :value="-1" disabled>{{ labels.equipmentCategory || 'Equipment Category' }}</b-form-select-option>
                  </template>
                </b-form-select>
                <b-overlay :show="loadingModels" class="mt-3">
                  <b-form-select v-if="selectedCategory !== -1"
                                 v-model="selectedModel"
                                 :options="models"
                                 :disabled="!models.length"
                                 class="model-lookup-select cursor-pointer"
                                 :class="{'text-gray-600':selectedModel === -1}"
                                 data-test-id="model-lookup__model-select"
                  >
                    <template #first>
                      <b-form-select-option :value="-1" disabled>{{ labels.modelNumber || 'Model Number' }}</b-form-select-option>
                    </template>
                  </b-form-select>
                </b-overlay>
                <b-overlay :show="loadingModelUrl" :class="{'mt-3': selectedModel !== -1}">
                  <b-button v-if="selectedModel !== -1"
                            variant="primary"
                            class="mx-0 px-4"
                            :class="{'cursor-pointer': currentUrl}"
                            :href="currentUrl"
                            :disabled="!currentUrl"
                            data-test-id="model-lookup__button"
                  >
                    {{ labels.viewModelDetails || 'View Model Details' }}
                  </b-button>
                </b-overlay>
              </b-card-body>
            </b-card>
          </b-col>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {ref, useContext, useFetch, watch, onMounted} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: String,
    htag: {
      type: String,
      default: 'h4'
    },
    lifecycle: {
      type: Object,
      default: () => {
        return {
          field: 'historical'
        }
      }
    },
    productCategory: {
      type: String,
      required: true
    },
    productSubcategory: String
  },
  setup(props) {
    const {productCategory, productSubcategory} = props
    const {$services} = useContext()
    const loadingModels = ref(false)
    const loadingModelUrl = ref(false)
    const category = ref(null)
    const selectedCategory = ref(-1)
    const selectedModel = ref(-1)
    const lifecycle = props.lifecycle.field
    const currentUrl = ref('')
    const categories = ref([])
    const models = ref([])
    useMounted(props.nodeId)

    const resetModel = () => {
      selectedModel.value = -1
    }

    const processCategoriesTree = (cats, root = false) => {
      if (root) {
        const models = processModelsTree(cats['@nodes'].map(key => cats[key]), lifecycle)
        if (models.length) {
          const filteredOutNodes = cats['@nodes'].filter(key => cats[key]['@nodeType'] && ['dxp:product', 'dxp:productGroup'].includes(cats[key]['@nodeType']))
          cats['dxp-fe-uncategorized'] = {
            ...cats,
            '@nodes': filteredOutNodes,
            '@nodeType': 'dxp:subcategory',
            title: `${cats.title || cats['@name']}`
          }

          if (!cats['@nodes'].includes('dxp-fe-uncategorized')) { cats['@nodes'].push('dxp-fe-uncategorized') }
        }
      }

      return cats['@nodes']?.map((key) => {
        if (!['dxp:category', 'dxp:subcategory'].includes(cats[key]['@nodeType'])) { return null }
        const models = processModelsTree(cats[key]['@nodes'].map(k => cats[key][k]), lifecycle)

        if (cats[key]['@nodeType'] === 'dxp:category') {
          const subcats = processCategoriesTree(cats[key])
          // handling for categories having direct children products
          if (models.length) {
            subcats.push({
              html: cats[key].title || cats[key]['@name'],
              value: {
                ...cats,
                models
              }
            })
          }
          if (subcats.length) {
            return {
              label: cats[key].title || cats[key]['@name'],
              html: cats[key].title || cats[key]['@name'],
              options: subcats,
              disabled: true
            }
          }
        }

        return {
          ...cats[key],
          html: cats[key]?.title || cats[key]['@name'],
          value: cats[key]['@id']
        }
      }).filter(y => y).sort((a, b) => a.html > b.html ? 1 : -1) || []
    }

    const processModelsTree = (categories, lifecycle, recursiveCall = false) => {
      return categories?.map((category) => {
        if (!['dxp:product', 'dxp:productGroup'].includes(category['@nodeType'])) {
          return null
        } else if (category['@nodeType'] === 'dxp:productGroup') {
          const subcats = !recursiveCall ? processModelsTree(category['@nodes'].map(key => category[key]), lifecycle, true) : []
          if (subcats.length) {
            return {
              label: category.title || category['@name'],
              html: category.title || category['@name'],
              options: subcats,
              disabled: true
            }
          } else if (category?.lifecycle?.toLowerCase() === lifecycle) {
            return {
              html: category.title || category['@name'],
              value: category
            }
          }
        }

        return (category?.lifecycle?.toLowerCase() === lifecycle)
          ? {
            html: category.title || category['@name'],
            value: category
          }
          : null
      }).filter(x => x) || []
    }

    const getModelsForCategory = async (_cat) => {
      const {data} = await $services.bobcatapi.getCategoryModels(_cat['@path'])

      const categoryModels = data?.map((model) => { return {...model, html: model?.title || model['@name'], value: model['@id']} })
      return processModelsTree(categoryModels, lifecycle)
    }

    watch(selectedCategory, async (_catId) => {
      const currentCategory = categories.value.filter(c => c.value === _catId)?.[0] || categories.value.map(cat => cat?.options?.filter(c => c.value === _catId)?.[0])?.filter(x => x)?.[0]
      if (!currentCategory) { return }
      loadingModels.value = true
      models.value = await getModelsForCategory(currentCategory)
      if (!models.value?.length) { currentCategory.disabled = true }
      loadingModels.value = false
    })

    watch(selectedModel, async (_currentModel) => {
      if (!_currentModel) { return }
      loadingModelUrl.value = true
      currentUrl.value = _currentModel?.id ? await $services.content.getModelPage(_currentModel.id, lifecycle) : ''
      loadingModelUrl.value = false
    })

    const labels = ref({})

    useFetch(async () => {
      category.value = (await $services.bobcatapi.getCategories([productCategory]))?.[0]
      categories.value = processCategoriesTree(category.value, true)

      labels.value = await $services.bobcatapi.getConcepts()
    })

    if (process.client && productSubcategory) {
      onMounted(() => {
        setTimeout(() => { selectedCategory.value = productSubcategory }, 150)
      })
    }

    return {
      loadingModelUrl,
      loadingModels,
      selectedCategory,
      selectedModel,
      categories,
      category,
      models,
      resetModel,
      labels,
      currentUrl
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/bootstrap_forms';
@import '~/assets/sass/colors';

.model-lookup-select {
  option {
    color: $black;

    &:disabled {
      color:$gray-600;
    }
  }

  &:focus {
    border-color: initial;
    box-shadow: none;
  }
}
</style>
