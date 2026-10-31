<template>
  <div>
    <aside data-test-id="facets" class="d-none d-lg-block" :class="{'floating-facets': floatingFilters, 'show': showFilters}">
      <div class="filters-container" :class="{'col-12 col-lg-3' :floatingFilters}">
        <div v-if="floatingFilters" class="border-bottom bg-gray-lighter px-3 py-2">
          <span class="font-weight-bold">Filters</span>
          <button type="button" aria-label="Close" class="close" @click="$emit('closeFilters')">
            <b-icon icon="close" class="small font-weight-bold text-black" />
          </button>
        </div>
        <div :class="{'p-3' :floatingFilters}">
          <slot name="search-facets" />

          <FacetsChips class="d-block d-md-none my-2"
                       :chips="localChips"
                       @removeFilteringSelection="handleChipRemove"
          />
          <b-button v-if="value.length || typesExists || models.length"
                    variant="outline-primary"
                    class="d-md-none mb-3 text-center sticky-top-button"
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
                   class="facets mb-3"
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

                    <CheckboxGroupFacet
                      v-if="[FACET_TYPES.DEFAULT, FACET_TYPES.STATIC, FACET_TYPES.TAG].includes(facet.facetType)"
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
          <div v-if="showApplyButton" class="position-relative pb-5">
            <DynamicButton
              :variant="BUTTON_NAMES.HEXAGONAL"
              :disabled="!hasChanges"
              class="mb-3 text-center w-100"
              :title="generateConceptHint('apply')"
              @click="applyChanges"
            >
              {{ labels.apply || 'Apply' }}
            </DynamicButton>
          </div>
        </div>
      </div>
    </aside>
    <aside data-test-id="facets-mobile" class="mt-3 facets-mobile d-lg-none">
      <BSidebar :id="sidebarId"
                v-model="isSideBarVisible"
                title="Filters"
                header-class="font-weight-bold border-bottom bg-gray-lighter"
                class="over-pac-container"
                shadow
                backdrop
                bg-variant="white"
                @hidden="$emit('hidden', $event)"
      >
        <div class="px-3 py-2 pb-5">
          <slot name="search-facets" />

          <FacetsChips class="d-block d-md-none my-2"
                       :chips="localChips"
                       @removeFilteringSelection="handleChipRemove"
          />
          <b-button v-if="value.length || typesExists || models.length"
                    variant="outline-primary"
                    class="mb-3 text-center sticky-top-button"
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
                   class="facets mb-3"
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

                    <CheckboxGroupFacet
                      v-if="[FACET_TYPES.DEFAULT, FACET_TYPES.STATIC, FACET_TYPES.TAG].includes(facet.facetType)"
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
          <div v-if="showApplyButton" class="position-relative pb-5">
            <DynamicButton
              :variant="BUTTON_NAMES.HEXAGONAL"
              :disabled="!hasChanges"
              class="mt-3 mb-3 text-center sticky-bottom-button d-md-block opacity-5"
              :title="generateConceptHint('apply')"
              @click="applyChanges"
            >
              {{ labels.apply || 'Apply' }}
            </DynamicButton>
          </div>
        </div>
      </BSidebar>
    </aside>
  </div>
</template>

<script>
import {computed, onMounted, onUnmounted, ref, watch} from '@nuxtjs/composition-api'
import {BSidebar} from 'bootstrap-vue'
import DynamicButton from '../../DynamicButton.vue'
import {BUTTON_NAMES} from '../../../constants/buttons'
import useConcepts from '~/helpers/composables/useConcepts'
import FACET_TYPES from '@/constants/facets'

export default {
  components: {DynamicButton, BSidebar},
  props: {
    value: Array,
    facetsToDisplay: Array,
    labels: Object,
    chips: Array,
    expandable: Boolean,
    emptyFilters: Boolean,
    showApplyButton: {
      type: Boolean,
      default: false
    },
    sidebarId: String,
    showEmptyFacets: Boolean,
    includeFacetLabel: Boolean,
    models: {
      type: Array,
      default: () => []
    },
    type: {
      type: Object,
      default: () => {
      }
    },
    primaryFacetsToHide: {
      type: Array,
      default: () => []
    },
    floatingFilters: {
      type: Boolean,
      default: false
    },
    showFilters: {
      type: Boolean,
      default: true
    },
    currentChipsColor: {
      type: String,
      default: 'black'
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
    const isSideBarVisible = ref(false)

    const hideSideBar = () => {
      isSideBarVisible.value = false
    }

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

      if (!props.showApplyButton) {
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
      handleClear,
      isSideBarVisible,
      hideSideBar,
      BUTTON_NAMES
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
  position: fixed!important;
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

.floating-facets {
  transition: all 0.3s ease;
  background-color: rgba(0,0,0,0);
  opacity: 0;
  pointer-events: none;
  overflow: hidden;

  .filters-container {
    transition: $transition-base;
    transform: translateX(-100%);
    height: 100%;
    overflow-y: auto;
    background-color: $white;
    padding: 0;
    border: $border-width solid $gray-200;
  }

  &.show {
    pointer-events: auto;
    background-color: rgba(0,0,0,0.6);
    opacity: 1;

    .filters-container {
      transform: translateX(0);
    }
  }
}

.facets-mobile {
  .b-sidebar, .b-sidebar-backdrop {
    top: initial !important;
    padding-bottom: initial !important;

    .b-sidebar-header {
      font-size: 1.125rem;

      strong {
        font-weight: normal;
      }
    }
  }

  .over-pac-container {
    z-index: 1051;
  }
}
</style>
