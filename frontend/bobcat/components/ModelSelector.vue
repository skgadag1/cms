<template>
  <b-overlay :show="loading" blur="0">
    <section class="py-4 bg-gray-lighter" data-test-id="model-selector">
      <div class="container">
        <div v-if="error" class="row">
          <div class="col-12">
            <WarningAlert>{{ error }}</WarningAlert>
          </div>
        </div>

        <div v-if="translatedChips.length" class="row mt-2">
          <div class="col-12">
            <FacetsChips :chips="translatedChips" @removeFilteringSelection="removeFilteringSelection" />
            <a class="mb-0 font-weight-bold cursor-pointer" @click.stop="clearFilters">
              {{ labels.resetFilters || 'Clear All' }}
            </a>
          </div>
        </div>

        <div class="row mt-3 model-selector-body">
          <div class="col-12 col-lg-3 model-selector-side">
            <div v-if="headerHtml" class="model-selector-header" v-html="headerHtml" />
            <ModelSelectorFacets v-if="initialized"
                                 v-model="filteringSelected"
                                 :facets-to-display="facetsToDisplay"
                                 :labels="labels"
                                 :include-facet-label="true"
            />
            <ListingSkeleton v-else :cols="1" :items="3" img-placement="none" />
          </div>
          <div class="col-12 col-lg-9 model-selector-results-panel"
               :class="{'model-selector-results-panel--collapsed': !expanded}"
          >
            <ListingSkeleton v-if="!initialized" :cols="3" :items="6" />
            <div v-else class="row mx-n2" data-test-id="model-selector__results">
              <div v-for="(model, i) in results"
                   :key="model['@id'] || i"
                   class="col-12 col-md-6 col-xl-4 px-2 mb-3"
                   :data-test-id="`model-selector-result-item__${i}`"
              >
                <EvolutionModelListingItem
                  :item="model"
                  :labels="labels"
                  :show-chips="false"
                  :show-compare="showCompareLinkOpt"
                  :compare-mode="'link'"
                  :compare-href="compareHrefFor(model)"
                  :compare-target="comparePageTarget"
                  :compare-label="compareLabel"
                  :show-indicator="true"
                  :show-model-details-button="showModelDetailsButtonOpt"
                  :model-details-label="modelDetailsLabel"
                  :model-details-variant="modelDetailsVariant"
                  :show-build-price-button="showBuildAndQuoteButtonOpt"
                  :bq-variant="bqVariant"
                  :show-suggested-price="true"
                  :divider-above-properties="true"
                />
              </div>
            </div>

            <div v-if="results.length > COLLAPSED_RESULTS" class="model-selector-show-more">
              <DynamicButton :variant="BUTTON_NAMES.HEXAGONAL"
                             color="white-red"
                             data-test-id="model-selector-show-more"
                             @click="expanded = !expanded"
              >
                {{ expanded ? (labels.showLess || 'SHOW LESS') : (labels.showMore || 'SHOW MORE') }}
              </DynamicButton>
            </div>
          </div>
        </div>
      </div>
    </section>
  </b-overlay>
</template>

<script>
import {computed, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '../constants/buttons'
import FACET_TYPES from '../constants/facets'
import EvolutionModelListingItem from './LoadersEvolution/Listings/EvolutionModelListingItem.vue'
import ModelSelectorFacets from './LoadersEvolution/Listings/ModelSelectorFacets.vue'
import FacetsChips from './elements/Facets/FacetsChips.vue'
import DynamicButton from './DynamicButton.vue'
import useFilteringElements from '~/helpers/composables/useFilteringElements'
import useQueryParams from '~/helpers/composables/useQueryParams'
import useDebounce from '~/helpers/composables/useDebounce'
import {buildModelCompareHref, mapModelSelectorCard} from '~/helpers/modelSelectorCard'

const MAX_RESULTS = 100
const COLLAPSED_RESULTS = 6
const TAG_FILTER_FIELD = 'tags.value'

export default {
  components: {EvolutionModelListingItem, ModelSelectorFacets, FacetsChips, DynamicButton},
  props: {
    heading: {
      type: String,
      default: ''
    },
    productCategory: {
      type: [Object, Array],
      default: null
    },
    facet: {
      type: [Object, String],
      default: null
    },
    facetConfiguration: {
      type: [Object, Array],
      default: null
    },
    showModelDetailsButton: {
      type: String,
      default: 'true'
    },
    modelDetailsLabel: {
      type: String
    },
    modelDetailsVariant: {
      type: Object
    },
    showBuildAndQuoteButton: {
      type: String,
      default: 'true'
    },
    bqVariant: {
      type: Object
    },
    showCompareLink: {
      type: String,
      default: 'true'
    },
    compareLabel: {
      type: String
    },
    comparePageLink: {
      type: Object
    }
  },
  setup(props) {
    const {$services, $utils, $regions} = useContext()
    const {queryParams, updateQueryParams} = useQueryParams()
    const {debounce} = useDebounce()

    const loading = ref(false)
    const initialized = ref(false)
    const error = ref(false)
    const total = ref(0)
    const results = ref([])
    const resolvedCategories = ref([])
    const expanded = ref(true)
    const labels = ref({})

    const facetCategory = props.facet?.['@path'] || false

    const productCategoryIds = (Array.isArray(props.productCategory) ? props.productCategory : [props.productCategory]).filter(Boolean)

    const facetConfigEntries = (props.facetConfiguration?.['@nodes'] || [])
      .map(n => props.facetConfiguration[n])
      .filter(entry => entry?.enabled !== 'false' && entry?.facet)

    const valueEntriesFor = entry => (entry.values?.['@nodes'] || []).map(n => entry.values[n])

    const showModelDetailsButtonOpt = props.showModelDetailsButton === 'true'
    const showBuildAndQuoteButtonOpt = props.showBuildAndQuoteButton === 'true'
    const showCompareLinkOpt = props.showCompareLink === 'true'
    const comparePage = $utils.getLinkField(props.comparePageLink, $regions.current.language.name)
    const comparePageUrl = comparePage?.url
    const comparePageTarget = comparePage?.target || '_self'

    const {
      filteringSelected,
      chips,
      initialFilterFacets,
      currentFilterFacets,
      removeFilteringSelection,
      clearFilteringSelection,
      getFilteringOrderValue
    } = useFilteringElements([], queryParams.value, 0, MAX_RESULTS, true)

    const sortValue = ref('name:asc')

    const translatedChips = computed(() => chips.value.map(chip => ({
      label: chip.facetLabel.replace(/\s/g, '&nbsp;') + ': ' + chip.facetValue.replace(/\s/g, '&nbsp;'),
      value: chip.value
    })))

    const facetsToDisplay = ref([])

    const headerHtml = computed(() => (typeof props.heading === 'string' ? props.heading : ''))

    const withAttachmentsCount = (model, attachmentCounts) => {
      const count = attachmentCounts.find(a => a?.id === model['@id'])?.compatibleAttachmentsCount
      return count
        ? [...(model.keyProperties || []), {name: labels.value.compatibleAttachments || 'Compatible Attachments', value: count, unit: ''}]
        : (model.keyProperties || [])
    }

    const transformResults = (data, attachmentCounts = []) => (data || [])
      .map(x => mapModelSelectorCard({...x, keyProperties: withAttachmentsCount(x, attachmentCounts)}))

    const updateFacetsToDisplay = () => {
      facetsToDisplay.value = facetConfigEntries.map((entry) => {
        const filterField = entry.facet?.id || entry.facet?.jcrName
        const facet = (initialFilterFacets.value || []).find(f =>
          f.filterField === filterField || (f.filterField === TAG_FILTER_FIELD && f.id === entry.facet?.label))
        if (!facet) {
          return null
        }
        facet.hideFacet = false
        facet.label = entry.facet?.label || facet.id || facet.name
        facet.heading = entry.questionText || facet.label

        const overridesByTagName = new Map(
          valueEntriesFor(entry)
            .filter(v => v.iconTag)
            .flatMap(v => [v.iconTag.label, v.iconTag['@name']].filter(Boolean).map(key => [key, v]))
        )
        facet.values = facet.values?.map((facetValue) => {
          const matchingCurrentFilter = currentFilterFacets.value.find(x => x.id === facet.id)
          facetValue.count = matchingCurrentFilter?.values?.find(y => y.id === facetValue.id)?.count || 0
          const override = overridesByTagName.get(facetValue.id) || overridesByTagName.get(facetValue.name)
          return {...facetValue, icon: override?.icon || null, description: override?.description || null}
        })

        const sortAlphanumericOff = `${entry.facet?.switchOffSortAlphanumeric}` === 'true'
        if (facet.values && !sortAlphanumericOff) {
          if (/^\$?[0-9]+\D+/.test('' + facet.values?.[0]?.name)) {
            facet.values.sort((a, b) => {
              const vala = ('' + a.name).match(/^\$?([0-9,.]+).+/)?.[1].replace(/[.,]/g, '') || ('' + a.name)
              const valb = ('' + b.name).match(/^\$?([0-9,.]+).+/)?.[1].replace(/[.,]/g, '') || ('' + b.name)
              return parseInt(vala) - parseInt(valb)
            })
          } else {
            facet.values.sort((a, b) => ('' + a.name).localeCompare('' + b.name))
          }
        }
        return facet
      }).filter(Boolean)
    }

    const buildDefaultFilteringSelected = () => {
      const seeded = []
      facetConfigEntries.forEach((entry) => {
        const filterField = entry.facet?.facetType === FACET_TYPES.TAG
          ? TAG_FILTER_FIELD
          : entry.facet?.id || entry.facet?.jcrName
        if (!filterField) {
          return
        }
        const facetLabelPart = entry.facet?.label ? `~${entry.facet.label}` : ''
        const defaultTagNames = valueEntriesFor(entry)
          .filter(v => v.defaultSelected === 'true' && v.iconTag)
          .map(v => v.iconTag.label || v.iconTag['@name'])
          .filter(Boolean)
        if (!defaultTagNames.length) {
          return
        }
        seeded.push(`${filterField}${facetLabelPart}:${defaultTagNames[0]}`)
      })
      return seeded
    }

    const FALLBACK_SORT = 'sortRating:desc'

    const fetchModelsFor = async (cat, perPage, sort = sortValue.value, filters = filteringSelected.value?.join('|') || false) => {
      try {
        const {data} = await $services.content.getListingResults('product', facetCategory, cat.category, cat.subcategory, {
          page: 0,
          perPage,
          sort,
          filters
        })
        return data
      } catch (e) {
        if (sort !== FALLBACK_SORT) {
          return fetchModelsFor(cat, perPage, FALLBACK_SORT, filters)
        }
        throw e
      }
    }

    const fetchAllForCategory = async (cat) => {
      const probe = await fetchModelsFor(cat, 1)
      return probe.total ? await fetchModelsFor(cat, Math.min(probe.total, MAX_RESULTS)) : probe
    }

    const mergeCategoryResults = (perCategoryData) => {
      const seen = new Set()
      const results = perCategoryData.flatMap(d => d.results || []).filter((model) => {
        const id = model['@id'] || model.id
        if (!id || seen.has(id)) {
          return false
        }
        seen.add(id)
        return true
      })

      const facetLists = perCategoryData.map(d => d.facets || [])
      const commonKeys = facetLists.reduce((acc, list) => {
        const keys = new Set(list.map(f => f.id))
        return acc === null ? keys : new Set([...acc].filter(k => keys.has(k)))
      }, null) || new Set()

      const facets = [...commonKeys].map((key) => {
        const perCategoryFacet = facetLists.map(list => list.find(f => f.id === key)).filter(Boolean)
        const valueIds = [...new Set(perCategoryFacet.flatMap(f => (f.values || []).map(v => v.id)))]
        const values = valueIds.map((id) => {
          const sample = perCategoryFacet.map(f => f.values?.find(v => v.id === id)).find(Boolean)
          const count = perCategoryFacet.reduce((sum, f) => sum + (f.values?.find(v => v.id === id)?.count || 0), 0)
          return {...sample, count}
        })
        return {...perCategoryFacet[0], values}
      })

      return {results, facets, total: results.length}
    }

    const fetchUnfilteredFacets = async () => {
      const probes = await Promise.all(resolvedCategories.value.map(cat => fetchModelsFor(cat, 1, sortValue.value, false)))
      return mergeCategoryResults(probes).facets
    }

    const fetchAllResults = async (isInitial = false) => {
      loading.value = !isInitial
      error.value = false

      try {
        const perCategoryData = await Promise.all(resolvedCategories.value.map(fetchAllForCategory))
        const merged = mergeCategoryResults(perCategoryData)

        currentFilterFacets.value = merged.facets
        if (!initialFilterFacets.value?.length) {
          initialFilterFacets.value = filteringSelected.value?.length
            ? await fetchUnfilteredFacets()
            : merged.facets
        }

        const attachmentCounts = await $services.content
          .getCompatibleAttachmentsCount(merged.results.map(m => m['@id']))
          .catch(() => [])

        total.value = merged.total
        results.value = transformResults(merged.results, attachmentCounts || [])
        updateFacetsToDisplay()
        initialized.value = true
      } catch (e) {
        error.value = '[ModelSelector] Unable to get results'
        // eslint-disable-next-line no-console
        console.error(error.value, e)
      }

      loading.value = false
    }

    const clearFilters = () => {
      clearFilteringSelection(null)
    }

    const compareHrefFor = (model) => {
      return buildModelCompareHref(comparePageUrl, model)
    }

    if (process.client) {
      watch(filteringSelected, (n, o) => {
        if (initialized.value && JSON.stringify(n) !== JSON.stringify(o)) {
          updateQueryParams({facets: n?.length && n})
          debounce(() => fetchAllResults())
        }
      })
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts([
        '/common/labels',
        '/common/labels/specifications',
        '/%region%/common/labels',
        '/%region%/common/labels/specifications'
      ])
      if (!facetCategory) {
        error.value = '[ModelSelector] Missing facet category'
        return
      }

      try {
        const facetDefinitions = await $services.bobcatapi.getFacets(facetCategory)
        const sortableFacet = facetDefinitions?.data?.find(x => x.sorting === 'true')
        if (sortableFacet) {
          sortValue.value = getFilteringOrderValue(sortableFacet)
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[ModelSelector] Unable to get facet configuration, falling back to default sort', e)
      }

      try {
        const categoryNodes = await $services.bobcatapi.getCategories(productCategoryIds)
        resolvedCategories.value = (categoryNodes || []).map((node) => {
          const categoryPath = node?.['@path']?.split('/') || []
          const isSubcategory = node?.['@nodeType'] === 'dxp:subcategory'
          return {
            category: categoryPath[categoryPath.length - (isSubcategory ? 2 : 1)] || '',
            subcategory: isSubcategory ? node.title : ''
          }
        }).filter(cat => cat.category)

        if (!resolvedCategories.value.length) {
          error.value = '[ModelSelector] Unable to resolve product category'
          return
        }
      } catch (e) {
        error.value = '[ModelSelector] Unable to resolve product category'
        // eslint-disable-next-line no-console
        console.error(error.value, e)
        return
      }

      const defaults = buildDefaultFilteringSelected()
      if (defaults.length && !filteringSelected.value.length) {
        filteringSelected.value = defaults
      }

      if (Array.isArray(queryParams.value?.facets)) {
        filteringSelected.value = queryParams.value.facets
      } else if (queryParams.value?.facets) {
        filteringSelected.value = queryParams.value.facets.split(',')
      }

      await fetchAllResults(true)
    })

    return {
      loading,
      initialized,
      error,
      expanded,
      COLLAPSED_RESULTS,
      BUTTON_NAMES,
      headerHtml,
      total,
      results,
      labels,
      filteringSelected,
      translatedChips,
      removeFilteringSelection,
      clearFilters,
      facetsToDisplay,
      showModelDetailsButtonOpt,
      showBuildAndQuoteButtonOpt,
      showCompareLinkOpt,
      comparePageTarget,
      compareHrefFor
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/colors';

.model-selector-header {
  margin-bottom: 2rem;

  h1, h2, h3, h4, h5, h6 {
    margin-bottom: 0;
  }
}

.model-selector-body {
  margin: 0;
}

.model-selector-side {
  align-self: flex-start;
  padding: 1.5rem 1rem 2rem;
  background-color: $white;

  @media (min-width: 992px) {
    padding: 1.5rem 2rem 2rem;
  }
}

.model-selector-results-panel {
  position: relative;
  padding: 1rem;

  @media (min-width: 992px) {
    padding: 2rem;
  }

  &--collapsed {
    overflow: hidden;
    max-height: 44rem;

    @media (min-width: 992px) {
      max-height: 56.25rem;
    }
  }
}

.model-selector-show-more {
  display: flex;
  justify-content: center;
  padding: 1rem 0;

  .model-selector-results-panel--collapsed & {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 2;
    background: linear-gradient(to bottom, rgba($gray-100, 0) 0%, $gray-100 86.9%);
  }
}
</style>
