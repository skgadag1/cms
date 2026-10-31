<template>
  <section data-test-id="features" :class="selectedColorVariant.background">
    <div class="container p-0 p-sm-3">
      <div class="d-flex flex-column w-100 justify-content-center align-items-center">
        <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
          <div class="col-12 px-0">
            <div class="w-100 px-3 px-sm-0">
              <div v-if="evolutionTitle" class="mb-3 mt-3 w-100" :class="selectedColorVariant.text" v-html="oEvolutionTitle" />
              <div v-else-if="title" class="mb-3 mt-3 w-100">
                <component :is="htag" :class="htag" data-test-id="features-title">{{ oTitle }}</component>
              </div>
            </div>
            <div v-if="categories && categories.length" class="col-12 px-0 mx-auto d-flex flex-column align-items-center justify-content-center w-100">
              <div class="d-flex justify-content-end w-100">
                <button
                  role="button"
                  class="d-flex justify-content-center align-items-center mb-3 px-3 bg-transparent border-0 font-weight-bold"
                  :class="selectedColorVariant.expandAllText"
                  data-test-id="features-expand-button"
                  title="Toogle accordion"
                  :aria-expanded="isExpandedAll"
                  @click.prevent="(e) => {ga4click(e); expandAllToggle()}"
                >
                  {{ isExpandedAll ? (labels.collapseAll || collapseLabel) : (labels.expandAll || expandLabel) }}  <span class="d-flex align-items-center justify-conent-center m-0 ml-2 transition-base" style="font-size: 6px" :style="isExpandedAll ? 'transform: rotate(180deg)' : null"><b-icon icon="sort-down" /></span>
                </button>
              </div>
              <div v-for="(category, idx) in categories" :key="`feature-category__${category['@id']}__${idx}`" class="p-0 transition-base mb-2 w-100" data-test-id="features-categories">
                <Accordion
                  :id="category['@id']"
                  :ref="(el) => category.element = el"
                  :toggle-cb="toggleAccordion"
                  :is-expanded="category.isExpanded"
                  :color="variant"
                  is-primary-accordion
                  :title="category.title"
                  :counter="category.features.length"
                  :htag="primaryAccordionHtag"
                  :animation-enabled="animationEnabled === 'true'"
                  @expanded="checkIsExpandedAll"
                  @click="ga4click"
                >
                  <div v-if="category.features && category.features.length" class="d-flex flex-column">
                    <Feature v-for="(feature, idf) in category.features"
                             :key="`feature-__${idf}`"
                             :ref="(el) => feature.element = el"
                             :feature="feature"
                             :color="variant"
                             :secondary-accordion-htag="secondaryAccordionHtag"
                             :animation-enabled="animationEnabled === 'true'"
                             @expanded="checkIsExpandedAll"
                             @toggle="toggleAccordion"
                    />
                  </div>
                </Accordion>
              </div>
            </div>
            <WarningAlert v-else-if="model && model.id && inEditorEdit" data-test-id="features-alert">No features configured for {{ mTitle }}.</WarningAlert>
            <WarningAlert v-else-if="inEditorEdit" data-test-id="features-alert">No model defined for page.</WarningAlert>
          </div>
        </LazyHydrate>
      </div>
    </div>
  </section>
</template>

<script>
import {inject, ref, useContext, useFetch, onServerPrefetch} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '../constants/variants/accordionGroupVariants'
import {COLORS} from '~/constants/accordion'
import useModels from '@/helpers/composables/useModels'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  inject: ['model'],
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    evolutionTitle: {
      type: String
    },
    expandLabel: {
      type: String,
      default: 'Expand all'
    },
    collapseLabel: {
      type: String,
      default: 'Collapse all'
    },
    htag: {
      type: String,
      default: 'h2'
    },
    primaryAccordionHtag: {
      type: String,
      default: 'h3'
    },
    secondaryAccordionHtag: {
      type: String,
      default: 'h4'
    },
    variant: {
      type: String,
      default: 'evolutionWhite'
    },
    animationEnabled: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {model} = useModels()
    const {$ga4, $services} = useContext()
    useMounted(props.nodeId)

    const isExpandedAll = ref(false)
    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])
    const labels = ref({})

    const {fetch: fetchLabels} = useFetch(async () => {
      if (!Object.keys(labels.value || {}).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      }
    })

    onServerPrefetch(async () => {
      await fetchLabels()
    })

    const categories = ref([])
    const accordionsExpanded = ref(0)
    const colors = ref(COLORS)
    const featuresCategories = inject('model')?.features
    const catOrGroup = v => (v === 'dxp:featureCategory' || v === 'dxp:featureGroup')
    const mTitle = model?.title || model?.id || model?.['@name']
    const oTitle = props.title?.replace('%ID%', model?.id)
      .replace('%NAME%', model?.['@name'])
      .replace('%TITLE%', mTitle)
    const oEvolutionTitle = props.evolutionTitle?.replace('%ID%', model?.id)
      .replace('%NAME%', model?.['@name'])
      .replace('%TITLE%', mTitle)

    const mapFeature = arr => ({
      ...arr,
      title: arr?.title || arr?.['@name']?.replace(/-/g, ' ')
    })

    const checkIfCategoryExists = path =>
      categories.value.find(category => category['@path'] === path)

    const pushCategory = (parent, child) => {
      if (checkIfCategoryExists(parent['@path'])) {
        const index = categories.value.findIndex(category => category['@path'] === parent['@path'])
        if (categories.value[index]?.features && child) {
          categories.value[index].features = [...categories.value[index].features, mapFeature(parent[child])]
        } else if (child) {
          categories.value[index].features = [mapFeature(parent[child])]
        }
      } else {
        categories.value.push({
          ...parent,
          title: parent?.title || parent?.['@name']?.replace(/-/g, ' '),
          features: child ? [mapFeature(parent[child])] : []
        })
      }
    }

    const checkIsExpandedAll = () => {
      let expandedAll = true
      let collapsedAll = true
      categories.value.forEach((accordion) => {
        if (!expandedAll && !collapsedAll) { return } // cancel check if both false
        expandedAll = expandedAll && accordion.element?.isVisible
        collapsedAll = collapsedAll && !accordion.element?.isVisible
        if (accordion.features?.length && accordion.element?.isVisible) {
          accordion?.features.forEach((child) => {
            expandedAll = expandedAll && child.element?.isVisible
            collapsedAll = collapsedAll && !child.element?.isVisible
          })
        }
      })
      if (expandedAll) { isExpandedAll.value = true }
      if (collapsedAll) { isExpandedAll.value = false }
    }

    const mapFeaturesCategories = (featuresCategory) => {
      if (featuresCategory['@nodeType'] === 'dxp:feature') {
        const parentPath = featuresCategory['@path'].replace(`/${featuresCategory['@name']}`, '')
        const parentCategory = checkIfCategoryExists(parentPath)
        if (!parentCategory) {
          categories.value.push({
            ...featuresCategory,
            '@path': parentPath,
            title: featuresCategory.featureCateogryTitle,
            features: [mapFeature(featuresCategory)]
          })
        } else {
          categories.value = categories.value.map((c) => {
            if (c['@path'] === parentPath) {
              return ({
                ...featuresCategory,
                '@path': parentPath,
                title: c?.featureCateogryTitle,
                features: [...(c?.features ? c?.features : []), mapFeature(featuresCategory)]
              })
            }
            return c
          })
        }
      } else if (featuresCategory['@nodes']?.length > 0) {
        featuresCategory['@nodes']?.forEach((fc) => {
          if (catOrGroup(featuresCategory[fc]['@nodeType'])) {
            mapFeaturesCategories(featuresCategory[fc])
          } else {
            pushCategory(featuresCategory, fc)
          }
        })
      } else { pushCategory(featuresCategory) }
    }

    featuresCategories?.forEach((featuresCategory) => {
      mapFeaturesCategories(featuresCategory)
    })

    const uniqueCategories = categories.value

    categories.value.forEach((category, i) => {
      const index = categories.value.findIndex(c => (c.title === category.title))
      const isDuplicated = index !== i
      if (isDuplicated && uniqueCategories[index]) {
        uniqueCategories[index].features = uniqueCategories[index].features?.length ? [...uniqueCategories[index].features, ...category.features] : category.features
        uniqueCategories.splice(i, 1)
      }
    })

    categories.value = uniqueCategories.filter(category => category?.features?.length > 0)

    const toggleAccordion = (isExpanded, id) => {
      categories.value.forEach(async (category) => {
        if (category['@id'] === id) {
          if (category.features.length) {
            if (isExpanded === false) { // close childs on closing
              await category.features.forEach((child) => {
                if (child.element) { child.element.toggleCallback(isExpanded) }
              })
            }
          }
          if (category.element) { category.element.setVisible(isExpanded) }
        }
      })
    }

    const expandAllToggle = () => {
      categories.value.forEach((category) => {
        if (category.element) { category.element?.setVisible(!isExpandedAll.value) }
      })
      categories.value.forEach((accordion) => {
        if (accordion.element) { accordion.element?.setVisible(!isExpandedAll.value) }
        if (accordion.features.length) {
          accordion.features.forEach((child) => {
            if (child.element) { child.element.toggleCallback(!isExpandedAll.value) }
          })
        }
      })
    }

    const ga4click = (event) => {
      $ga4.events.product_features_click(event, model)
    }

    return {
      mTitle,
      oTitle,
      oEvolutionTitle,
      categories,
      colors,
      isExpandedAll,
      checkIsExpandedAll,
      selectedColorVariant,
      accordionsExpanded,
      expandAllToggle,
      toggleAccordion,
      inEditorEdit,
      ga4click,
      labels
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

</style>
