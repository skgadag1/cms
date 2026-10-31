<template>
  <div
    class="d-flex align-items-center flex-lg-row flex-column w-100 mt-lg-3 mt-sm-0 mt-md-0 w-md-auto mx-0 mx-lg-0"
  >
    <div class="mx-0 w-100">
      <DropdownAdvanced
        ref="dropdownRef"
        :reset="reset"
        related-dropdowns
        :default-id="field"
        :label="label"
        :name="field"
        :init-values="selectedModels"
        :placeholder="placeholder"
        :options="visibleFacetValues"
        :on-change="() => {}"
        :disabled-dropdown="isDisabled"
        :required="isRequired === 'true'"
        :filter-values="filterValues"
        :search-enabled="searchEnabled"
        :selected-label="selectedLabel"
        :search-placeholder="searchPlaceholder"
        :no-results-label="noResultsLabel"
        :no-facet-search-data="noFacetSearchData"
        :is-multi-select="isMultiSelect"
        get-whole-value
        class="custom-facet"
        @changed="handleModelChange"
      />
    </div>
  </div>
</template>

<script>
import {computed, ref, useContext} from '@nuxtjs/composition-api'
import {buildFacet, loadFacetSetActions, loadGenericAnalyticsActions} from '@coveo/headless'
import {inEditorEdit} from '~/helpers/vue-editor'
import {BUSINESS_REGIONS} from '~/constants'

export default {
  props: {
    engine: {
      type: Object
    },
    field: {
      type: String
    },
    isDisabled: {
      type: Boolean,
      default: true
    },
    facetChanged: {
      type: Function
    },
    label: {
      type: String
    },
    placeholder: {
      type: String
    },
    searchEnabled: {
      type: String
    },
    searchPlaceholder: {
      type: String
    },
    selectedLabel: {
      type: String,
      default: 'Selected options'
    },
    noResultsLabel: {
      type: String
    },
    defaultValues: {
      type: String
    },
    isRequired: {
      type: String
    },
    isMultiSelect: {
      type: String
    },
    othersCategoryLabel: {
      type: String
    },
    isCategorized: {
      type: Boolean
    },
    facetName: {
      type: String
    },
    allowedValues: {
      type: Array,
      default: () => []
    }
  },
  setup(props, {expose, emit}) {
    const dropdownRef = ref(null)
    const reset = ref(false)
    const selectedType = ref({})
    const selectedModels = ref([])
    const facetValues = ref([])
    const state = ref(null)
    const {
      engine,
      field,
      facetChanged,
      defaultValues,
      othersCategoryLabel,
      isCategorized,
      facetName
    } = props
    const {$ga4, $regions} = useContext()
    const gtmEvent = 'filter_interaction'
    const eventType = 'dealer_locator_event'
    const regionInfo = {
      name: $regions?.current?.name
    }
    const isEMEA = BUSINESS_REGIONS.emea.includes($regions.current.name)

    let facetDefaultValues = []
    try {
      facetDefaultValues = JSON.parse(defaultValues)
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error)
    }

    const {toggleSelectFacetValue} = loadFacetSetActions(engine)
    const noFacetSearchData = ref(false)
    let currentSelection = []

    const numberOfValues = 100

    const logGtmEvents = (eventPayload) => {
      $ga4.events.coveo_analytics_events(eventPayload)
    }

    const controller = buildFacet(engine, {
      options: {
        field,
        facetId: field,
        numberOfValues,
        resultsMustMatch: 'atLeastOneValue',
        facetSearch: {
          numberOfValues
        }
      }
    })

    const isCurrentlySelected = (value) => {
      const inCurrentSelection = currentSelection.find((o) => {
        return o.value === value && !!o.selected
      })
      return !!inCurrentSelection
    }

    const sortedValues = (values, category) => {
      if (category === othersCategoryLabel) {
        return values
      }
      const obj = facetDefaultValues.find(o => o.category === category)
      if (obj?.values.length > 0 && values.length > 0) {
        return values.sort((a, b) => {
          return obj.values.indexOf(a.value) - obj.values.indexOf(b.value)
        })
      }
      return []
    }

    const sortedCategories = facetDefaultValues
      .map(item => item.category)
      .concat(othersCategoryLabel)
      .filter(item => !!item)
    let facetSearchInProgress = false

    controller.subscribe(() => {
      let isFacetSearch = false
      state.value = controller.state

      if (controller.state.facetSearch.isLoading) {
        facetSearchInProgress = true
      } else if (
        facetSearchInProgress === true &&
        !controller.state.facetSearch.isLoading
      ) {
        facetSearchInProgress = false
        isFacetSearch = true
      }

      if (isFacetSearch) {
        const facetSearchValues = controller.state.facetSearch.values
        const isFacetSearchWithQuery = !!controller.state.facetSearch.query

        let values = facetSearchValues.map(facet => ({
          value: facet.value || facet.rawValue,
          label: facet.value || facet.displayValue,
          selected: isCurrentlySelected(facet.value || facet.rawValue),
          disabled: 'false'
        }))

        if (currentSelection.length > 0) {
          const query = controller.state.facetSearch.query
          currentSelection.forEach((cs) => {
            const isAvailable = values.find(o => o.value === cs.value)
            if (!isAvailable) {
              if (
                !query ||
                (query && cs.label.toLowerCase().includes(query.toLowerCase()))
              ) {
                values = values.concat(cs)
              }
            }
          })
        }

        if (
          !isFacetSearchWithQuery &&
          controller.state.values.length >= facetSearchValues.length
        ) {
          currentSelection = values.filter(o => !!o.selected)
          facetChanged(field, currentSelection, 'subscribe')
        }

        const valuesMapperObj = {}
        noFacetSearchData.value = isFacetSearchWithQuery && values.length === 0

        if (values.length > 0) {
          values.forEach((facetVal) => {
            const categoryObj = facetDefaultValues.find((obj) => {
              return obj.values.includes(facetVal.value)
            })

            const category = categoryObj?.category || othersCategoryLabel
            const mapperCategory = valuesMapperObj[category]
            valuesMapperObj[category] = !mapperCategory
              ? [facetVal]
              : [...valuesMapperObj[category], facetVal]
          })

          const valuesMapper = Object.keys(valuesMapperObj)
            .map(key => ({
              category: isCategorized ? key : '',
              values: isCategorized
                ? sortedValues(valuesMapperObj[key], key)
                : valuesMapperObj[key]
            }))
            .sort(
              (a, b) =>
                sortedCategories.indexOf(a.category) -
                sortedCategories.indexOf(b.category)
            )
            .filter(obj => obj.values?.length > 0)

          if (facetName !== 'product') {
            const orderMap = new Map(
              facetDefaultValues?.[0]?.values?.map((val, index) => [val, index])
            )

            let mappedValues = valuesMapper?.[0]?.values || []

            if (isEMEA && facetName === 'services') {
              // Hide word 'Used' in Services for EMEA only - AV-1478
              const emeaUsedNames = [
                'Verwendet',
                'Occasion',
                'Usado',
                'Seconda mano',
                'Gebruikt',
                'Бывший в употреблении',
                'Used'
              ]
              mappedValues = values.filter(
                item => !emeaUsedNames.includes(item.value)
              )
            }

            mappedValues.sort((a, b) => {
              const aIndex = orderMap.has(a.value)
                ? orderMap.get(a.value)
                : Infinity
              const bIndex = orderMap.has(b.value)
                ? orderMap.get(b.value)
                : Infinity
              return aIndex - bIndex
            })

            valuesMapper[0].values = mappedValues
          }

          // Handle product facet filtering for EMEA regions - AV-1627
          if (isEMEA && facetName === 'product') {
            const currentRegion = $regions.current?.name
            const currentLanguage = $regions?.current?.language?.name

            const normalizeString = str => str.toLowerCase().replace(/-/g, '')

            const emeaHiddenProducts = {
              eu: {
                en: [
                  'backhoe loaders',
                  'toolcat utility work machine',
                  'utility vehicle'
                ],
                fr: [
                  'chargeusespelleteuses',
                  'véhicule utilitaire toolcat',
                  'véhicules utilitaires'
                ],
                de: [
                  'baggerlader',
                  'toolcatarbeitsmaschine',
                  'nutztransporter'
                ],
                es: [
                  'retroexcavadora',
                  'máquina de trabajo utilitaria',
                  'camiones multiusos'
                ],
                it: ['terne', 'veicolo da lavoro toolcat', 'camion da lavoro'],
                nl: [
                  'graaflaadmachine',
                  'toolcatwerkmachine',
                  'multifunctionele werkvoertuigen'
                ]
              },
              mea: {
                en: [
                  'toolcat utility work machine',
                  'utility vehicle',
                  'mowers',
                  'compact wheel loaders',
                  'compact tractors'
                ],
                fr: [
                  'véhicule utilitaire toolcat',
                  'véhicules utilitaires',
                  'tondeuses',
                  'chargeuse articulée',
                  'tracteurs compacts'
                ]
              },
              cis: {
                en: [
                  'toolcat utility work machine',
                  'utility vehicle',
                  'mowers',
                  'compact wheel loaders',
                  'compact tractors',
                  'small articulated loaders'
                ],
                ru: [
                  'универсальная машина toolcat',
                  'грузовикиутилиты',
                  'косилки',
                  'компактные колесные погрузчики',
                  'компактные тракторы',
                  'малые артикулированные погрузчики'
                ]
              }
            }

            const hiddenProducts =
              emeaHiddenProducts[currentRegion]?.[currentLanguage] || []

            if (hiddenProducts.length > 0) {
              valuesMapper.forEach((category) => {
                category.values = category.values.filter((item) => {
                  const normalizedItemValue = normalizeString(item.value)
                  return !hiddenProducts.some((hiddenProduct) => {
                    const normalizedHidden = normalizeString(hiddenProduct)
                    return normalizedHidden === normalizedItemValue
                  })
                })
              })
            }
          }

          facetValues.value = valuesMapper
        } else {
          facetValues.value = []
        }
      }
    })

    // Restricts the facet to a fixed set of values while a dependent facet requires it - AV-3012
    const visibleFacetValues = computed(() => {
      if (!props.allowedValues.length) {
        return facetValues.value
      }
      return facetValues.value
        .map(category => ({...category, values: category.values.filter(o => props.allowedValues.includes(o.value))}))
        .filter(category => category.values.length > 0)
    })

    const filterValues = (keyword, force) => {
      if (controller.state.facetSearch.query !== keyword || force) {
        controller.facetSearch.updateText(keyword || '')
        controller.facetSearch.search()
      }
    }

    controller.facetSearch.search()

    const updateFacet = (value, state, updateCurrentSelection) => {
      engine.dispatch(
        toggleSelectFacetValue({
          facetId: field,
          selection: {
            state,
            value
          }
        })
      )

      facetValues.value = facetValues.value.map((o) => {
        o.values = o.values.map((m) => {
          if (m.value === value) {
            m.selected = state === 'selected'
          }
          return m
        })
        return o
      })

      if (updateCurrentSelection) {
        currentSelection = currentSelection.filter((o) => {
          return o.value !== value
        })
      }
    }

    const setEngineAdditionalContext = (additionalContext) => {
      emit('change-engine-context', additionalContext)
    }

    const logAnalytics = (facetSelection) => {
      const {logCustomEvent} = loadGenericAnalyticsActions(engine)

      // Making sure log values in an array for the services facet to not override previous values
      const additionalContext = {
        [facetName]:
          facetName === 'services'
            ? [
              ...(engine?.state?.context?.contextValues?.services || []),
              facetSelection?.toLowerCase()
            ]
            : facetSelection?.toLowerCase()
      }

      // Set engine context so that selected facet value can be logged in event
      setEngineAdditionalContext(additionalContext)
      const coveoEventPayload = {
        evt: facetName + 'Select', // Concatenate label with 'Select' to form event type
        type: eventType
      }
      // Log event to Coveo
      engine.dispatch(logCustomEvent(coveoEventPayload))

      const gtmEventPayload = {
        cause: gtmEvent,
        segment_options: engine?.state?.context?.contextValues?.industry,
        product_options: engine?.state?.context?.contextValues?.product,
        services_options: engine?.state?.context?.contextValues?.services
      }

      // Add language preference parameter but only to Industry Select event
      if (facetName === 'industry') {
        gtmEventPayload.language_preference =
          $regions.current?.locale?.toLowerCase()
      }

      // Log event to GTM
      logGtmEvents(gtmEventPayload)
    }

    const handleModelChange = (v) => {
      if (v) {
        v.forEach((o) => {
          const exist = currentSelection.find(m => o.value === m.value)

          if (!exist) {
            updateFacet(o.value, 'selected')
            // Log Analytics for facet selection but only for non-NA regions
            if (regionInfo.name !== 'na') {
              logAnalytics(o.value)
            }
          }
        })

        currentSelection.forEach((o) => {
          const exist = v.find(m => o.value === m.value)
          if (!exist) {
            updateFacet(o.value, 'idle')
          }
        })

        currentSelection = v
        facetChanged(field, v)
      }
    }

    expose({
      handleModelChange,
      updateFacet
    })

    return {
      state,
      inEditorEdit,
      selectedModels,
      reset,
      selectedType,
      facetValues,
      visibleFacetValues,
      handleModelChange,
      dropdownRef,
      filterValues,
      noFacetSearchData
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-facet ::v-deep label {
  font-size: 1rem;
}
</style>
