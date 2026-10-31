<template>
  <div>
    <aside data-test-id="model-selector-facets" class="d-none d-lg-flex flex-column model-selector-facets">
      <div v-for="facet in facets" :key="facet.id" class="model-selector-facet-group" :data-test-id="`model-selector-facet-${facet.filterField}`">
        <p class="model-selector-question mb-3">
          {{ facet.heading }}
        </p>
        <div class="d-flex flex-column model-selector-options" role="radiogroup" :aria-label="facet.heading">
          <div v-for="option in facet.values"
               :key="option.id"
               class="model-selector-option d-flex align-items-center"
               :class="{'model-selector-option--selected': isSelected(facet, option), 'model-selector-option--disabled': !option.count}"
               role="radio"
               :aria-checked="isSelected(facet, option)"
               :aria-disabled="!option.count"
               tabindex="0"
               :data-test-id="`model-selector-option-${facet.filterField}-${option.id}`"
               @click="toggleOption(facet, option)"
               @keydown.enter.space.prevent="toggleOption(facet, option)"
          >
            <img v-if="option.icon" :src="resolveIcon(option.icon)" :alt="option.name" class="mr-2 flex-shrink-0 model-selector-option-icon">
            <span v-else class="mr-2 model-selector-option-icon-slot" aria-hidden="true" />
            <span class="text-left">
              <span class="d-block model-selector-option-name">{{ option.name }}</span>
              <small v-if="option.description" class="d-block model-selector-option-description">{{ option.description }}</small>
            </span>
          </div>
        </div>
      </div>
    </aside>

    <div v-if="stickyVisible" class="d-lg-none model-selector-sticky" :style="{top: stickyTop}" data-test-id="model-selector-sticky">
      <button type="button"
              class="d-flex align-items-center w-100 model-selector-sticky-bar"
              :aria-expanded="stickyOpen"
              @click="stickyOpen = !stickyOpen"
      >
        <span class="d-flex align-items-center flex-grow-1 text-left model-selector-sticky-selections">
          <span v-if="!stickySelections.length" class="model-selector-sticky-label">
            {{ labels.filters || 'Filters' }}
          </span>
          <span v-for="selection in stickySelections"
                :key="selection.key"
                class="d-flex align-items-center model-selector-sticky-chip"
          >
            <img v-if="selection.icon"
                 :src="resolveIcon(selection.icon)"
                 :alt="selection.name"
                 class="flex-shrink-0 model-selector-sticky-icon"
            >
            <span class="model-selector-sticky-label">{{ selection.name }}</span>
          </span>
        </span>
        <span class="flex-shrink-0 model-selector-sticky-chevron"
              :class="{'model-selector-sticky-chevron--up': stickyOpen}"
              aria-hidden="true"
        />
      </button>

      <div v-if="stickyOpen" class="model-selector-sticky-panel">
        <div v-for="facet in facets" :key="`sticky-${facet.id}`" class="model-selector-facet-group">
          <p class="model-selector-question mb-3">
            {{ facet.heading }}
          </p>
          <div class="d-flex flex-column model-selector-options" role="radiogroup" :aria-label="facet.heading">
            <div v-for="option in facet.values"
                 :key="option.id"
                 class="model-selector-option d-flex align-items-center"
                 :class="{'model-selector-option--selected': isSelected(facet, option), 'model-selector-option--disabled': !option.count}"
                 role="radio"
                 :aria-checked="isSelected(facet, option)"
                 :aria-disabled="!option.count"
                 tabindex="0"
                 @click="toggleOption(facet, option)"
                 @keydown.enter.space.prevent="toggleOption(facet, option)"
            >
              <img v-if="option.icon" :src="resolveIcon(option.icon)" :alt="option.name" class="mr-2 flex-shrink-0 model-selector-option-icon">
              <span v-else class="mr-2 model-selector-option-icon-slot" aria-hidden="true" />
              <span class="text-left">
                <span class="d-block model-selector-option-name">{{ option.name }}</span>
                <small v-if="option.description" class="d-block model-selector-option-description">{{ option.description }}</small>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="stickyOpen" class="d-lg-none model-selector-sticky-overlay" @click="stickyOpen = false" />

    <div ref="mobileFacetsRef" class="d-lg-none model-selector-facets-mobile" data-test-id="model-selector-facets-mobile">
      <div v-for="facet in facets" :key="facet.id" class="model-selector-facet-group" :data-test-id="`model-selector-facet-mobile-${facet.filterField}`">
        <p class="model-selector-question mb-3">
          {{ facet.heading }}
        </p>
        <b-dropdown block
                    no-flip
                    variant="transparent"
                    menu-class="w-100 py-0 model-selector-dropdown-menu"
                    toggle-class="model-selector-dropdown-toggle d-flex align-items-center justify-content-between text-left"
        >
          <template #button-content>
            <span v-if="selectedOptionFor(facet)" class="d-flex align-items-center">
              <img v-if="selectedOptionFor(facet).icon"
                   :src="resolveIcon(selectedOptionFor(facet).icon)"
                   :alt="selectedOptionFor(facet).name"
                   class="mr-2 flex-shrink-0 model-selector-option-icon"
              >
              <span class="model-selector-option-name">{{ selectedOptionFor(facet).name }}</span>
            </span>
            <span v-else class="model-selector-dropdown-placeholder">
              {{ labels.selectOne || 'Select one...' }}
            </span>
          </template>

          <b-dropdown-item-button v-for="option in facet.values"
                                  :key="option.id"
                                  :disabled="!option.count"
                                  :active="isSelected(facet, option)"
                                  button-class="d-flex align-items-center model-selector-dropdown-item"
                                  :data-test-id="`model-selector-option-mobile-${facet.filterField}-${option.id}`"
                                  @click="toggleOption(facet, option)"
          >
            <img v-if="option.icon" :src="resolveIcon(option.icon)" :alt="option.name" class="mr-2 flex-shrink-0 model-selector-option-icon">
            <span v-else class="mr-2 model-selector-option-icon-slot" aria-hidden="true" />
            <span class="text-left">
              <span class="d-block model-selector-option-name">{{ option.name }}</span>
              <small v-if="option.description" class="d-block model-selector-option-description">{{ option.description }}</small>
            </span>
          </b-dropdown-item-button>
        </b-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import {computed, onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    },
    facetsToDisplay: {
      type: Array,
      default: () => []
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    includeFacetLabel: Boolean
  },
  setup(props, {emit}) {
    const {$utils} = useContext()

    const mobileFacetsRef = ref(null)
    const stickyVisible = ref(false)
    const stickyOpen = ref(false)
    const stickyTop = ref('0px')

    const updateSticky = () => {
      const el = mobileFacetsRef.value
      if (!el) {
        return
      }
      const rect = el.getBoundingClientRect()
      const visible = getComputedStyle(el).display !== 'none' && rect.bottom < 0
      if (!visible) {
        stickyOpen.value = false
      }
      stickyVisible.value = visible

      const bottomOf = (selector) => {
        const node = document.querySelector(selector)
        if (!node) {
          return 0
        }
        const position = getComputedStyle(node).position
        if (position !== 'fixed' && position !== 'sticky') {
          return 0
        }
        const bounds = node.getBoundingClientRect()
        return bounds.top < 200 ? bounds.bottom : 0
      }
      stickyTop.value = `${Math.max(
        bottomOf('header[data-test-id="main-header"]'),
        bottomOf('[data-test-id="anchor-alternative-nav-wrapper"]')
      )}px`
    }

    onMounted(() => {
      updateSticky()
      window.addEventListener('scroll', updateSticky, {passive: true})
      window.addEventListener('resize', updateSticky)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('scroll', updateSticky)
      window.removeEventListener('resize', updateSticky)
    })

    const facets = computed(() => props.facetsToDisplay?.filter(facet => facet.values?.length) || [])

    const resolveIcon = icon => $utils.getCloudinaryAsset(icon, true)

    const facetFilteredName = (facet) => {
      const facetLabelPart = (props.includeFacetLabel && facet.label) ? `~${facet.label}` : ''
      return `${facet.filterField}${facetLabelPart}:`
    }

    const selectedValuesFor = (facet) => {
      const filteredName = facetFilteredName(facet)
      return props.value
        .filter(x => x.startsWith(`${facet.filterField}:`) || x.startsWith(`${facet.filterField}~`))
        .filter(x => x.startsWith(filteredName))
        .flatMap(x => x.replace(filteredName, '').split(';'))
    }

    const isSelected = (facet, option) => selectedValuesFor(facet).includes(String(option.id))

    const selectedOptionFor = facet => facet.values?.find(option => isSelected(facet, option)) || null

    const stickySelections = computed(() => facets.value
      .map(facet => ({facet, option: selectedOptionFor(facet)}))
      .filter(({option}) => option)
      .map(({facet, option}) => ({key: `${facet.id}-${option.id}`, name: option.name, icon: option.icon})))

    const toggleOption = (facet, option) => {
      if (!option.count) {
        return
      }

      const filteredName = facetFilteredName(facet)
      const alreadySelected = selectedValuesFor(facet).includes(String(option.id))
      const newValuesForFacet = alreadySelected ? [] : [filteredName + option.id]

      const otherFacetsSelections = props.value.filter(x => !x.startsWith(filteredName))
      emit('input', [...otherFacetsSelections, ...newValuesForFacet])
    }

    return {
      facets,
      isSelected,
      selectedOptionFor,
      toggleOption,
      resolveIcon,
      mobileFacetsRef,
      stickyVisible,
      stickyOpen,
      stickySelections,
      stickyTop
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';

.model-selector-facets {
  width: 100%;
}

.model-selector-facet-group {
  margin-bottom: 2rem;
  color: $black;

  &:last-child {
    margin-bottom: 0;
  }
}

.model-selector-sticky {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 1030;
  background-color: $white;
  box-shadow: 0 2px 4px rgba($black, 0.1);
}

.model-selector-sticky-bar {
  padding: 0.75rem 0.5rem;
  border: 0;
  background-color: transparent;
}

.model-selector-sticky-selections {
  gap: 1rem;
  min-width: 0;
  overflow: hidden;
}

.model-selector-sticky-chip {
  gap: 0.25rem;
  min-width: 0;
}

.model-selector-sticky-icon {
  width: 1.5rem;
  height: 1.5rem;
  object-fit: contain;
}

.model-selector-sticky-label {
  font-size: 1rem;
  line-height: 1.5rem;
  white-space: nowrap;
}

.model-selector-sticky-chevron {
  width: 1.5rem;
  height: 1.5rem;
  margin-left: 2rem;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0.5rem;
    height: 0.5rem;
    border-right: 2px solid $black;
    border-bottom: 2px solid $black;
    transform: translate(-50%, -70%) rotate(45deg);
  }

  &--up::after {
    transform: translate(-50%, -30%) rotate(-135deg);
  }
}

.model-selector-sticky-panel {
  max-height: 70vh;
  overflow-y: auto;
  padding: 1rem;
  border-top: 1px solid $tertiary;
}

.model-selector-sticky-overlay {
  position: fixed;
  inset: 0;
  z-index: 1029;
  background-color: rgba($black, 0.5);
}

.model-selector-facets-mobile {
  padding-bottom: 2rem;

  ::v-deep .model-selector-dropdown-toggle {
    width: 100%;
    min-height: 4.5rem;
    padding: 0.75rem 1rem;
    border: 1px solid $tertiary;
    background-color: $white;
    white-space: normal;
  }

  ::v-deep .model-selector-dropdown-menu {
    max-height: 70vh !important;
    overflow-y: auto;
    border: 1px solid $tertiary;
    border-radius: 0;
    margin-top: -1px;
  }

  ::v-deep .model-selector-dropdown-item {
    min-height: 4.5rem;
    padding: 0.75rem 1rem;
    white-space: normal;

    &.active,
    &:active {
      background-color: $gray-100;
      color: inherit;
    }
  }

  .model-selector-dropdown-placeholder {
    font-size: 1.125rem;
    line-height: 1.5rem;
    color: $gray-600;
  }
}

.model-selector-question {
  font-family: $headings-font-family;
  font-size: 1.125rem;
  font-weight: 700;
  line-height: 1.5rem;
  margin-bottom: 0;
}

.model-selector-options {
  gap: 1rem;
}

.model-selector-option-icon {
  width: 3rem;
  height: 3rem;
  object-fit: contain;
}

.model-selector-option-icon-slot {
  width: 3rem;
  flex-shrink: 0;
}

.model-selector-option-name {
  font-family: $headings-font-family;
  font-size: 1.125rem;
  line-height: 1.5rem;
}

.model-selector-option-description {
  font-size: 1rem;
  line-height: 1.5rem;
}

.model-selector-option {
  min-height: 4rem;
  border: 1px solid $tertiary;
  background-color: $white;
  cursor: pointer;
  padding: 0.5rem;

  &--selected {
    border-left: 4px solid $primary;
    padding-left: 5px;
    background-color: $gray-100;
  }

  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
