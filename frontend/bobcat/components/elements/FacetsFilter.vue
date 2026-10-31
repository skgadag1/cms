<template>
  <FacetsWrapper ref="facetsWrapper" :filter-count="selected.length" :labels="labels" @hidden="discardChanges">
    <template #facets>
      <slot name="search-facets" />

      <FacetsChips class="d-block d-md-none my-2"
                   :chips="localChips"
                   @removeFilteringSelection="handleChipRemove"
      />
      <b-button v-if="value.length || typesExists || models.length"
                variant="outline-primary"
                class="mb-2 text-center sticky-top-button"
                block
                :title="generateConceptHint('clearAll')"
                @click="clearAndHide"
      >
        {{ labels.clearAll || 'Clear All' }}
      </b-button>

      <slot name="search" />

      <template v-if="!emptyFilters">
        <template v-for="(facet, i) in facets">
          <div v-if="!facet.hideFacet"
               :key="facet.id"
               class="facets mb-2"
               :data-test-id="`facet-${facet.facetType}-${facet.filterField}`"
          >
            <b-button
              v-b-toggle="`facet-collapse-${i}-${facet.filterField ? facet.filterField.replace(/\s/g, '') : facet.id}`"
              variant="outline"
              class="facets-toggle d-flex justify-content-between p-2"
              block
            >
              <span class="text-left font-weight-bold py-1 pl-1 pl-lg-2">{{ facet.label }}</span> <span
                class="dropdown-toggle py-1 pr-1 pr-lg-2"
              />
            </b-button>
            <b-collapse
              :id="`facet-collapse-${i}-${facet.filterField ? facet.filterField.replace(/\s/g, '') : facet.id}`"
              visible
              class="facets-content mt-0"
            >
              <b-card class="p-3" no-body>
                <a v-if="selectedIncludes(facet)"
                   :title="generateConceptHint(facet.clearLabelKey)"
                   class="font-weight-bold pb-2 cursor-pointer"
                   @click.prevent="handleClear(facet.id, facet.filterField)"
                >
                  {{ labels[facet.clearLabelKey] || facet.clearLabelFallback }}
                </a>

                <CheckboxGroupFacet v-if="[FACET_TYPES.DEFAULT, FACET_TYPES.STATIC, FACET_TYPES.TAG].includes(facet.facetType)"
                                    :facet="facet"
                                    :value="pendingSelected"
                                    :expandable="expandable"
                                    :show-empty-facets="showEmptyFacets"
                                    :include-facet-label="includeFacetLabel"
                                    @input="changeValue"
                />
                <NumericFacet v-else-if="facet.facetType === FACET_TYPES.NUMERIC"
                              :facet="facet"
                              :value="pendingSelected"
                              :labels="labels"
                              :include-facet-label="includeFacetLabel"
                              @input="changeValue"
                />
              </b-card>
            </b-collapse>
          </div>
        </template>
      </template>
      <div v-if="showApplyButton && hasChanges" class="position-relative pb-5">
        <b-button
          variant="primary"
          class="mt-3 mb-2 text-center sticky-bottom-button d-md-block"
          block
          :title="generateConceptHint('apply')"
          @click="applyChanges"
        >
          {{ labels.apply || 'Apply' }}
        </b-button>
      </div>
    </template>
  </FacetsWrapper>
</template>

<script>
import {computed, ref, watch, onMounted, onUnmounted} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'
import FACET_TYPES from '@/constants/facets'

export default {
  props: {
    value: Array,
    facetsToDisplay: Array,
    labels: Object,
    chips: Array,
    expandable: Boolean,
    emptyFilters: Boolean,
    includeFacetLabel: Boolean,
    showApplyButton: {
      type: Boolean,
      default: false
    },
    showEmptyFacets: Boolean,
    models: {
      type: Array,
      default: () => []
    },
    type: {
      type: Object,
      default: () => {}
    },
    primaryFacetsToHide: {
      type: Array,
      default: () => []
    }
  },
  setup(props, {emit}) {
    const selected = ref([...(props.value || [])])
    const pendingSelected = ref([...(props.value || [])])
    const hasChanges = ref(false)
    const facetsWrapper = ref(null)
    const {generateConceptHint} = useConcepts()
    const localChips = ref([...(props.chips || [])])
    const isDesktop = ref(false)

    watch(() => props.value, (s) => {
      selected.value = s
      pendingSelected.value = [...s]
      hasChanges.value = false
    })

    watch(() => props.chips, (s) => {
      localChips.value = [...s]
    })

    const checkDesktop = () => {
      isDesktop.value = window.innerWidth >= 768
    }

    onMounted(() => {
      checkDesktop()
      window.addEventListener('resize', checkDesktop)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', checkDesktop)
    })

    const handleClear = (facetId, facetFilterField) => {
      if (isDesktop.value) {
        emit('clearFilteringCategory', facetId)
      } else {
        clearFilteringCategory(facetFilterField)
      }
    }

    const changeValue = (facetId, val) => {
      let _selected = pendingSelected.value.filter(x => !x.startsWith(`${facetId}:`) && !x.startsWith(`${facetId}~`))
      _selected = _selected.concat(val)

      if (!props.showApplyButton || window.matchMedia('(min-width: 768px)').matches) {
        selected.value = _selected
        emit('input', _selected)
        emit('applyFilters')
      } else {
        pendingSelected.value = _selected
        hasChanges.value = true
      }
    }

    const applyChanges = () => {
      selected.value = [...pendingSelected.value]
      emit('input', pendingSelected.value)
      emit('applyFilters')
      hasChanges.value = false
      facetsWrapper.value?.hideSideBar()
    }

    const discardChanges = () => {
      pendingSelected.value = [...(props.value || [])]
      localChips.value = [...(props.chips || [])]
      hasChanges.value = false
    }

    const clearAndHide = () => {
      emit('clearFilteringSelection', null)
      facetsWrapper.value?.hideSideBar()
    }

    const handleChipRemove = (chip) => {
      const newSelection = pendingSelected.value.filter(x => x !== chip)
      localChips.value = localChips.value.filter(x => x.value !== chip)
      pendingSelected.value = newSelection
      hasChanges.value = true
    }

    const clearFilteringCategory = (facetFilterField) => {
      pendingSelected.value = pendingSelected.value.filter(x => !x.startsWith(`${facetFilterField}:`) && !x.startsWith(`${facetFilterField}~`))
      localChips.value = localChips.value.filter(x => !x.value.startsWith(`${facetFilterField}:`) && !x.value.startsWith(`${facetFilterField}~`))
      hasChanges.value = true
    }

    const typesExists = computed(() => {
      return props.type ? Object.keys(props.type).length : false
    })

    const selectedIncludes = (category) => {
      for (const item of selected.value) {
        const selectedFacetKey = item.split(':').shift().split('~').shift()
        const selectedFacetValues = item.split(':').pop().split(';')
        if (category.filterField === selectedFacetKey &&
          (category.values?.some(v => selectedFacetValues.includes(String(v.id)) || selectedFacetValues.includes(String(v.name))) ||
          category.facetType === FACET_TYPES.NUMERIC)
        ) {
          return true
        }
      }
      return false
    }

    const facets = computed(() => props.facetsToDisplay?.map((x) => {
      const facet = {
        ...x,
        clearLabelKey: x.facetType === FACET_TYPES.NUMERIC ? 'reset' : 'clearAll',
        clearLabelFallback: x.facetType === FACET_TYPES.NUMERIC ? 'Reset' : 'Clear All',
        ...(x.filterField === 'pageType.keyword' && {
          values: x.values?.map((val) => {
            return {
              ...val,
              name: props.labels?.[val.id] || val.id
            }
          })
        })
      }

      facet.hideFacet = !!props.primaryFacetsToHide.find(f => x?.filterField === f?.id || x?.label === f?.label)

      if (facet.values && !x.switchOffSortAlphanumeric) {
        if (/^\$?[0-9]+\D+/.test('' + facet.values?.[0]?.name)) { // begins with numeric values, i.e. $10,000, 14 in, 50 mph
          facet.values?.sort((a, b) => {
            const vala = ('' + a.name).match(/^\$?([0-9,.]+).+/)?.[1].replace(/[.,]/g, '') || ('' + a.name)
            const valb = ('' + b.name).match(/^\$?([0-9,.]+).+/)?.[1].replace(/[.,]/g, '') || ('' + b.name)
            return parseInt(vala) - parseInt(valb)
          })
        } else { // string value
          facet.values?.sort((a, b) => ('' + a.name).localeCompare('' + b.name))
        }
      }
      return facet
    }).filter(x => (x.values ? (props.showEmptyFacets || x.values.some(val => val.count > 0)) : true) && !(x.facetType === FACET_TYPES.NUMERIC && x?.max <= x?.min)))

    return {
      selected,
      selectedIncludes,
      FACET_TYPES,
      typesExists,
      changeValue,
      generateConceptHint,
      facets,
      hasChanges,
      applyChanges,
      facetsWrapper,
      discardChanges,
      clearAndHide,
      handleChipRemove,
      localChips,
      pendingSelected,
      clearFilteringCategory,
      handleClear
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/bootstrap_forms';
@import '~/assets/sass/colors';
@import '~/assets/sass/custom';

.facets {
  .facets-toggle {
    border: 1px solid $tertiary !important;
    box-shadow: none !important;
    font-family: inherit;
    font-size: 1rem;
    background-color: white;

    .not-collapsed .dropdown-toggle {
      transform: rotateX(180deg);
    }

    .dropdown-toggle {
      color: black;

    }
  }

  .facets-content {
    .card {
      border-top: none;
      box-shadow: none !important;
    }
  }
}
.sticky-top-button {
  position: sticky;
  top: 0;
  z-index: 10;
  background: white;
}

.sticky-bottom-button {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: 1;
  margin: 0;
  border-radius: 0;
  width: 90% !important;
  left: 15px;
  box-shadow: $sticky-btn-box-shadow;

  @media (max-width: 767px) {
    bottom: 0;
    border-radius: 0;
    margin-left: 1rem;
    left: 0;
    right: 0;
  }
}
</style>
