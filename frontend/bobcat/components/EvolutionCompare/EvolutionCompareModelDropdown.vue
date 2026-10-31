<template>
  <div class="position-relative w-100 evolution-compare-model-dropdown-root"
       :class="{'evolution-compare-model-dropdown-root--open': menuOpen}"
       data-test-id="evolution-compare-model-dropdown-root"
  >
    <b-dropdown ref="dropdown"
                variant="transparent"
                :menu-class="menuClass"
                :toggle-class="toggleClass"
                class="evolution-compare-model-dropdown w-100"
                data-test-id="evolution-compare-model-dropdown"
                boundary="viewport"
                no-flip
                no-caret
                lazy
                block
                :disabled="!models.length || disabled"
                @shown="onShown"
                @hidden="onHidden"
    >
      <template #button-content>
        <span class="evolution-dd-toggle-label text-truncate flex-grow-1 min-w-0 pr-2"
              :class="value ? 'font-weight-bold text-body' : 'text-muted'"
        >{{ toggleLabel }}</span>
        <b-icon icon="chevron-alternative"
                class="text-dark flex-shrink-0 evolution-dd-chevron transition-base h4 mb-0"
                aria-hidden="true"
        />
      </template>

      <div class="evolution-dd-menu-inner d-flex flex-column flex-grow-1 w-100 overflow-hidden min-h-0">
        <div class="mx-2 my-2 flex-shrink-0">
          <label class="sr-only" :for="searchFieldId">{{ placeholder }}</label>
          <div class="evolution-dd-search-shell d-flex align-items-center bg-white border border-color-gray-dark-medium rounded-2 px-2 flex-shrink-0">
            <b-icon icon="search" class="text-muted mr-2 flex-shrink-0" />
            <b-form-input :id="searchFieldId"
                          ref="searchInput"
                          v-model="searchText"
                          type="search"
                          :placeholder="placeholder"
                          autocomplete="off"
                          class="border-0 shadow-none p-0 h-100 bg-transparent text-body flex-grow-1 evolution-dd-search-input"
                          data-test-id="evolution-compare-model-dropdown__search"
                          @keyup.esc.native="hide"
            />
            <button v-show="searchText.length"
                    type="button"
                    class="bg-transparent d-flex justify-content-center align-items-center border-0 flex-shrink-0"
                    :aria-label="clearSearchLabel"
                    :aria-hidden="!searchText.length"
                    tabindex="-1"
                    data-test-id="evolution-compare-model-dropdown__search-clear"
                    @mousedown.stop.prevent
                    @click.stop.prevent="clearSearch"
            >
              <b-icon icon="remove" class="ml-2" />
            </button>
          </div>
        </div>

        <ul class="evolution-dd-options-list list-unstyled mb-0 flex-grow-1 bg-white overflow-auto overflow-x-hidden pb-1 min-h-0"
            role="listbox"
        >
          <template v-for="(row, rowIdx) in listRows">
            <li v-if="row.kind === 'header'"
                :key="`hdr-${rowIdx}-${row.label}`"
                class="evolution-dd-group-header px-3 pt-2 pb-1 mb-0 text-uppercase font-weight-bold text-body"
                role="presentation"
            >
              {{ row.label }}
            </li>
            <li v-else
                :key="row.model['@id'] || row.model.id || `row-${rowIdx}`"
                role="option"
                :aria-selected="isSelected(row.model)"
            >
              <button type="button"
                      class="border-0 bg-transparent transition-base w-100 text-left d-flex align-items-center rounded-0 shadow-none text-body py-2 px-2 mb-0 text-decoration-none"
                      :class="{'font-weight-bold': isSelected(row.model)}"
                      data-test-id="evolution-compare-model-option"
                      @click.prevent="onPick(row.model)"
              >
                <span class="evolution-dd-option-check d-flex align-items-center justify-content-start flex-shrink-0">
                  <span v-if="isSelected(row.model)"
                        class="evolution-dd-checkmark font-weight-bold text-dark d-block"
                        aria-hidden="true"
                  ><b-icon icon="success-check h4 mb-0 mr-1" /></span>
                </span>
                <span class="evolution-dd-option-title text-truncate flex-grow-1">{{ titleOf(row.model) }}</span>
              </button>
            </li>
          </template>
          <li v-if="!listRows.length"
              class="evolution-dd-empty px-3 py-3 text-muted text-center"
          >
            {{ noResultsLabel }}
          </li>
        </ul>
      </div>
    </b-dropdown>
  </div>
</template>

<script>
import {computed, getCurrentInstance, nextTick, ref, watch} from '@nuxtjs/composition-api'

const norm = s => String(s || '').trim().toLowerCase()

const groupLabelForModel = (model) => {
  const raw = (model?.parent?.parentTitle || model?.parent?.parentName || '').trim()
  return raw || null
}

const MENU_CLASS = [
  'w-100', 'p-0', 'm-0', 'border', 'border-color-gray-dark-medium', 'border-top-0',
  'bg-white', 'shadow-none', 'overflow-hidden', 'evolution-dd-menu'
].join(' ')

const TOGGLE_CLASS = [
  'd-flex', 'align-items-center', 'justify-content-between', 'w-100', 'pl-3', 'py-2', 'pr-2',
  'bg-white', 'text-left', 'border', 'border-color-gray-dark-medium', 'shadow-none', 'evolution-dd-toggle'
].join(' ')

export default {
  props: {
    models: {
      type: Array,
      default: () => []
    },
    value: {
      type: Object,
      default: null
    },
    excludeIds: {
      type: Array,
      default: () => []
    },
    placeholder: {
      type: String,
      default: 'Search...'
    },
    chooseLabel: {
      type: String,
      default: 'Select model'
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['input'],
  setup(props, {emit}) {
    const vm = getCurrentInstance()
    const searchFieldId = `evolution-compare-model-dropdown-search-${vm?.proxy?._uid ?? ''}`

    const dropdown = ref(null)
    const searchInput = ref(null)
    const searchText = ref('')
    const menuOpen = ref(false)

    const menuClass = MENU_CLASS
    const toggleClass = TOGGLE_CLASS

    watch(() => props.value, () => {
      searchText.value = ''
    })

    const excluded = computed(() => new Set((props.excludeIds || []).map(String).filter(Boolean)))

    const titleOf = model => model?.title || model?.['@name'] || ''

    const modelRowKey = model => String(model?.id || model?.['@id'] || '').trim()

    const toggleLabel = computed(() => titleOf(props.value) || props.chooseLabel)

    const noResultsLabel = computed(() =>
      props.labels?.noMatchingModels || props.labels?.noMatchingResults || 'No models match your search'
    )

    const clearSearchLabel = computed(() =>
      props.labels?.clearSearch || props.labels?.clear || 'Clear search'
    )

    const filteredModels = computed(() => {
      const normalizedSearchQuery = norm(searchText.value)
      return (props.models || []).filter((model) => {
        const rowKey = modelRowKey(model)
        if (!rowKey || excluded.value.has(rowKey)) {
          return false
        }
        if (!normalizedSearchQuery) {
          return true
        }
        const titleMatches = norm(titleOf(model)).includes(normalizedSearchQuery)
        const sectionMatches = norm(groupLabelForModel(model) || '').includes(normalizedSearchQuery)
        return titleMatches || sectionMatches
      })
    })

    const listRows = computed(() => {
      const models = filteredModels.value
      const normalizedSearchQuery = norm(searchText.value)
      const hasActiveSearch = Boolean(normalizedSearchQuery)

      if (hasActiveSearch) {
        const sorted = [...models].sort((modelA, modelB) =>
          titleOf(modelA).localeCompare(titleOf(modelB))
        )
        return sorted.map(model => ({kind: 'model', model}))
      }

      const groups = new Map()
      for (const model of models) {
        const label = groupLabelForModel(model)
        const key = label || '__ungrouped__'
        if (!groups.has(key)) {
          groups.set(key, {label, models: []})
        }
        groups.get(key).models.push(model)
      }

      const keys = [...groups.keys()].sort((a, b) => {
        if (a === '__ungrouped__') {
          return 1
        }
        if (b === '__ungrouped__') {
          return -1
        }
        return String(a).localeCompare(String(b))
      })

      const rows = []
      for (const key of keys) {
        const {label, models: groupModels} = groups.get(key)
        if (label) {
          rows.push({kind: 'header', label: String(label).toUpperCase()})
        }
        groupModels.sort((modelA, modelB) => titleOf(modelA).localeCompare(titleOf(modelB)))
        for (const model of groupModels) {
          rows.push({kind: 'model', model})
        }
      }
      return rows
    })

    const isSelected = (model) => {
      if (!props.value || !model) {
        return false
      }
      const selectedKey = modelRowKey(model)
      const valueKey = modelRowKey(props.value)
      return Boolean(selectedKey && valueKey && selectedKey === valueKey)
    }

    const hide = () => {
      dropdown.value?.hide?.(true)
    }

    const onShown = () => {
      menuOpen.value = true
      nextTick(() => {
        searchInput.value?.focus?.()
      })
    }

    const onHidden = () => {
      menuOpen.value = false
      searchText.value = ''
    }

    const onPick = (model) => {
      emit('input', model)
      hide()
    }

    const clearSearch = () => {
      searchText.value = ''
      nextTick(() => {
        const inputEl = searchInput.value?.$el || searchInput.value
        inputEl?.focus?.()
      })
    }

    return {
      dropdown,
      searchInput,
      searchText,
      menuOpen,
      searchFieldId,
      menuClass,
      toggleClass,
      listRows,
      toggleLabel,
      noResultsLabel,
      clearSearchLabel,
      titleOf,
      isSelected,
      onPick,
      onShown,
      onHidden,
      hide,
      clearSearch
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/colors';

.evolution-compare-model-dropdown {
  ::v-deep .evolution-dd-toggle.dropdown-toggle {
    border-radius: 2px;
  }

  .evolution-dd-options-list {
    border-top: 1px solid $gray-600;
  }

  &.show ::v-deep .evolution-dd-toggle.dropdown-toggle {
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }

  ::v-deep .evolution-dd-menu.dropdown-menu {
    margin-top: -1px;
    max-height: 20rem;
    border-radius: 0 0 2px 2px;
    box-shadow: none !important;
    top: 100% !important;
    bottom: auto !important;
    transform: none !important;

    &[x-placement^="top"],
    &[data-popper-placement^="top"] {
      top: 100% !important;
      bottom: auto !important;
      transform: none !important;
    }
  }

  &.show ::v-deep .evolution-dd-chevron {
    transform: rotate(180deg);
  }

  ::v-deep .dropdown-toggle:focus,
  ::v-deep .dropdown-toggle:active,
  ::v-deep .evolution-dd-search-input:focus {
    box-shadow: none;
    outline: none;
  }

  ::v-deep .evolution-dd-search-input {
    font-size: 1rem;
    line-height: 1.25;
  }

  button[data-test-id="evolution-compare-model-option"]:hover {
   background-color: $gray-100 !important;
  }
}

.evolution-compare-model-dropdown-root {
  z-index: 1;
}

.evolution-dd-toggle-label {
  font-size: 0.9375rem;
  line-height: 1.25;
}

.evolution-dd-menu-inner {
  max-height: 20rem;
}

.evolution-dd-search-shell {
  height: 2.5rem;
  box-sizing: border-box;
}

.evolution-dd-group-header {
  font-size: 1rem;
  line-height: 1.25;
  letter-spacing: 0.02em;
}

.evolution-dd-option-check {
  width: 1.5rem;
  min-width: 1.5rem;
}

.evolution-dd-checkmark {
  font-size: 1rem;
  line-height: 1;
}

.evolution-dd-option-title,
.evolution-dd-empty {
  font-size: 1rem;
  line-height: 1.25;
}
</style>
