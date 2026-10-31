import {computed, ref, watch} from '@nuxtjs/composition-api'
import FACET_TYPES from '@/constants/facets'

const getFilteringOrderValue = (x) => {
  let appendix = 'asc'
  // NGB-2544: in future we can extend this but for now only rating needs special handling
  if (x?.sortDesc === 'true' || ['sortrating', 'publisheddate', 'postdate'].includes(x?.id?.toLowerCase())) {
    appendix = 'desc'
  }

  return `${x.id}:${appendix}`
}

const PER_PAGE_BASE = 12

export default function useFilteringElements(mgnlCommonData = {}, queryParams = {}, campaignsAmount = 0, resultsPerPage = 12, includeFacetLabels) {
  const filteringSelected = ref(queryParams?.facets?.split(',')?.length > 0 ? queryParams?.facets?.split(',') : (queryParams?.length ? queryParams : []))
  const initialFilterFacets = ref([])
  const currentFilterFacets = ref([])

  const currency = ref({
    selected: 'us',
    us: {
      value: 'us',
      label: 'USD'
    },
    cad: {
      value: 'cad',
      label: 'CAD'
    }
  })

  const perPageInitial = parseInt(resultsPerPage)
  const perPage = ref(queryParams?.perPage || perPageInitial)

  const perPageObject = computed({ // used for current perPage selection in dropdown
    get: () => {
      return {
        label: perPage.value,
        value: perPage.value
      }
    },
    set: () => {}
  })

  const perPageOptions = computed({
    get: () => {
      const result = []
      for (let i = 1; i <= 4; i++) {
        result.push({label: i * PER_PAGE_BASE, value: i * PER_PAGE_BASE, ...(i * PER_PAGE_BASE === parseInt(perPageObject.value?.value) && {selected: true})})
      }
      return result
    },
    set: () => {}
  })

  const chips = computed({
    get: () => {
      return filteringSelected.value?.flatMap((x) => {
        if (!initialFilterFacets.value?.length) {
          return false
        }

        const selectedFacet = x.split(':').shift().split('~')
        const selectedFacetKey = selectedFacet[0]
        const facetLabelFromSelection = selectedFacet[1]
        return x.split(':').pop().split(';').map((facetValue) => {
          const originalFacetValue = facetValue
          let filterFacet = initialFilterFacets?.value?.find(f => f.filterField === selectedFacetKey && f.values?.some(v => v.id === facetValue || v.name === facetValue))
          if (!filterFacet) { filterFacet = initialFilterFacets?.value?.find(f => f.filterField === selectedFacetKey) } // just in case value not found, pick first by name

          const facetLabel = filterFacet ? filterFacet.label || filterFacet.name : (facetLabelFromSelection || selectedFacetKey)
          if (filterFacet?.facetType === FACET_TYPES.NUMERIC) {
            const values = facetValue.split('!')
            facetValue = [values[0], filterFacet.unit, '-', values[1], filterFacet.unit].join(' ')
          } else if (filterFacet?.facetType === FACET_TYPES.STATIC) {
            const value = filterFacet.values.find(v => v.id === facetValue)
            facetValue = value.name
          } else {
            facetValue.replace('-', ' ')
          }

          const facetLabelPart = (includeFacetLabels && facetLabel) ? `~${facetLabel}` : ''
          return {
            label: facetLabel + ': ' + facetValue,
            facetLabel,
            facetValue,
            value: `${selectedFacetKey}${facetLabelPart}:${originalFacetValue}`
          }
        })
      }).filter(x => x) || []
    },
    set: () => {}
  })

  const changePerPage = (s) => {
    if (s?.value !== perPage.value) {
      perPage.value = s.value
    }
  }

  const clearFacetValues = (valuesToRemove) => {
    let filteredFacets = filteringSelected.value

    valuesToRemove.forEach((item) => {
      const removedFacetKey = item.split(':').shift()
      const removedValue = item.split(':').pop()

      filteredFacets = filteredFacets.reduce((p, c) => {
        if (!c.startsWith(`${removedFacetKey}:`)) {
          p.push(c)
        } else {
          const filtered = c.replace(`${removedFacetKey}:`, '').split(';').filter(v => v !== removedValue)
          if (filtered.length > 0) {
            p.push(`${removedFacetKey}:${filtered.join(';')}`)
          }
        }

        return p
      }, [])
    })

    filteringSelected.value = filteredFacets
  }

  const removeFilteringSelection = item => clearFacetValues([item])

  const clearFilteringSelection = (category = null) => {
    if (!category) { filteringSelected.value = []; return }
    const filterFacet = initialFilterFacets.value.find(f => f.id === category)
    const facetLabelPart = (includeFacetLabels && filterFacet?.name) ? `~${filterFacet.name}` : ''
    const filterFacetName = `${filterFacet.filterField}${facetLabelPart}:`
    if (filterFacet.facetType === FACET_TYPES.NUMERIC) {
      filteringSelected.value = filterFacet ? filteringSelected.value.filter(x => !x.startsWith(filterFacetName)) : []
    } else {
      const toClear = filterFacet?.values?.map(i => filterFacetName + (i.id ?? i.name)) || []
      clearFacetValues(toClear)
    }
  }

  watch(() => mgnlCommonData.value, (v) => {
    if (v && v.length !== initialFilterFacets.value) { initialFilterFacets.value = v }
  })

  const filtersEmpty = computed({
    get: () => !initialFilterFacets.value?.length,
    set: () => {}
  })

  return {
    chips,
    filteringSelected,
    initialFilterFacets,
    currentFilterFacets,
    removeFilteringSelection,
    clearFilteringSelection,
    perPage,
    perPageInitial,
    perPageOptions,
    perPageObject,
    changePerPage,
    currency,
    filtersEmpty,
    getFilteringOrderValue
  }
}
