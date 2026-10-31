<template>
  <b-form-group data-test-id="drop-down-advanced-wrapper" class="mb-md-3 mb-sm-0">
    <label v-if="label" :class="[{'font-weight-bold': bold}]">
      {{ label }}<span v-if="required">*</span>
    </label>
    <div v-click-outside="clickOutside" :class="`position-relative ${!disableMinWidth ? 'drop-down-wrapper--fixed-min-width' : ''}`">
      <button :disabled="disabledDropdown"
              :class="[{'active': !isHidden}, `${disabledDropdown ? 'bg-gray' : 'bg-white'}`]"
              class="form-control px-3 py-3 bg-white border d-flex justify-content-start align-items-center custom-button"
              @focus="onFocus"
              @click.prevent="isHidden = !isHidden"
      >
        <div v-if="(selected && selected.length) || (label && placeholder)"
             class="position-relative h6 py-1 m-0 pr-2 w-100 text-left font-weight-light"
        >
          <span :class="[{'px-5' : icon}, {'text-black-600': !selected.length}, {'text-gray-400': disabledDropdown}]">
            {{
              selected.length ? (isMultiSelect === 'true' ? `${selected.length} ${selectedLabel}` : selected[0].label) : placeholder || label
            }}
          </span>
          <span v-if="icon"
                class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3"
          >
            <b-icon :icon="icon" class="text-black" />
          </span>
        </div>
        <span class="small">
          <b-icon icon="sort-down" class="mt-1 text-black" />
        </span>
      </button>
      <div v-if="!isHidden" :class="{'active': !isHidden}" class="bg-white border options z-index-4 facet-popup-modal">
        <!-- <p class="mb-1 font-weight-bold text-primary cursor-pointer w-100" @click="clearAll">{{ clearLabel }}</p> -->
        <div v-if="searchEnabled == 'true' && searchPlaceholder" class="px-2 py-2 border-bottom">
          <b-input-group class="d-none d-flex z-index-1 form-control-wrapper align-items-center justify-content-between border border-color-gray-200 rounded-2 transition-base bg-white w-100 height-4 flex-nowrap">
            <p class="h5 pl-2 mb-0 tertiary-color line-height-sm"><b-icon icon="search" /></p>
            <b-form-input
              ref="searchKeywordInput"
              v-model="searchKeyword"
              autocomplete="off"
              name="keyword-search"
              :placeholder="searchPlaceholder"
              class="border-0 text-dark form-control search-box-input pl-2"
              @keyup="textChange"
            />
            <span v-if="searchKeyword"
                  class="h6 mr-2 mb-0 line-height-sm close-icon"
                  @click.stop="clearSearchBox"
            />
          </b-input-group>
        </div>
        <div class="pt-3 pb-3 values-container scroll-shadows overflow-auto">
          <div v-if="multiSelectOptions.length && !noFacetSearchData" class="col-md-12 col-sm-12 px-0 d-flex flex-column rg-1">
            <div
              v-for="(c, i) in multiSelectOptions"
              :key="`drop-down-category__${c.category}_${i}`"
              class="category-options col-md-auto"
            >
              <h5 v-if="c.category && c.values.length > 0" class="mb-0 px-3">{{ c.category }}</h5>
              <!-- Single-value facet code -->
              <div v-if="c.values.length > 0 && isMultiSelect !== 'true'" class="d-flex flex-column pt-2 pb-0">
                <div
                  v-for="(o, oi) in c.values"
                  :key="`drop-down-option__${o.value}_${i}_${oi}`"
                  :disabled="o.disabled === 'true'"
                  class="user-select-none drop-down-option py-1"
                  :class="[`${o.selected ? 'drop-down-option-selected' : ''}`]"
                  @click="handleChange(o)"
                >
                  <b-icon v-if="o.selected" icon="success" class="position-absolute text-black mt-1 ml-4" />
                  <span class="drop-down-option-label pl-7" v-html="hightlightText(o.label)" />
                </div>
              </div>
              <!-- Multi-value facet code -->
              <div v-if="c.values.length > 0 && isMultiSelect === 'true'" class="d-flex flex-column py-0">
                <div
                  v-for="(o, oi) in c.values"
                  :key="`drop-down-option__${o.value}_${i}_${oi}`"
                  :disabled="o.disabled === 'true'"
                  class="user-select-none drop-down-option py-1 pl-3"
                  :class="[`${o.selected ? 'drop-down-option-selected' : ''}`]"
                >
                  <b-form-checkbox
                    v-model="o.selected"
                    class="w-100 multi-select-option-checkbox pl-4"
                    :disabled="o.disabled === 'true'"
                    @change="handleChange(o)"
                  >
                    <span class="multi-select-option-checkbox-label">{{ o.label }}</span>
                  </b-form-checkbox>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="(!multiSelectOptions.length || noFacetSearchData) && searchKeyword && noResultsLabel" class="col-md-12 col-sm-12">
            <div v-if="noResultsLabel" class="no-results-container"><b-icon icon="info" /><span class="font-weight-bold">{{ noResultsLabel }}</span></div>
          </div>
        </div>
      </div>
    </div>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {label, name, options, icon, clearLabel, selectedLabel, onChange, required, validationMessage, placeholder, bold, reset, disabledDropdown, getWholeValue, defaultId, relatedDropdowns, disableMinWidth, initValues, filterValues, searchEnabled, searchPlaceholder, noResultsLabel, noFacetSearchData, isMultiSelect} = config

export default {
  props: {
    label,
    name,
    options,
    icon,
    clearLabel,
    selectedLabel,
    onChange,
    required,
    validationMessage,
    placeholder,
    bold,
    reset,
    disabledDropdown,
    getWholeValue,
    defaultId,
    relatedDropdowns,
    disableMinWidth,
    initValues,
    filterValues,
    searchEnabled,
    searchPlaceholder,
    noResultsLabel,
    noFacetSearchData,
    isMultiSelect
  },
  setup(props, {emit}) {
    const {name, onChange, required, getWholeValue, defaultId, relatedDropdowns, filterValues, isMultiSelect} = props
    const multiSelectOptions = ref(props.options || [])
    const selected = ref(props.initValues || [])

    const isValidated = ref(true)
    const isHidden = ref(true)
    const touched = ref(false)
    const isClickAction = ref(false)
    const tmpSelected = ref(selected.value?.map(m => m[defaultId]))

    const searchKeywordInput = ref(null)
    const searchKeyword = ref('')
    const textChange = (force) => {
      filterValues(searchKeyword.value, force)
    }

    if (onChange) {
      const isValid = required ? selected.value.length > 0 : true
      onChange(getWholeValue ? selected.value : selected.value.map(el => el.value), name, isValid)
    }

    const handleChange = (option) => {
      isClickAction.value = true
      if (isMultiSelect === 'true') {
        if (option.selected) {
          selected.value = selected.value.concat(option)
        } else {
          selected.value = selected.value.filter(o => o.value !== option.value)
        }
      } else {
        option.selected = !option.selected
        selected.value.forEach((option) => {
          option.selected = false
        })
        if (option.selected) {
          selected.value = [option]
        } else {
          selected.value = []
        }
      }

      isValidated.value = selected.value.length > 0
      if (searchKeyword.value) {
        textChange(true)
      }
    }

    watch(selected, (newVal, oldVal) => {
      if (!props.reset) {
        if (onChange) {
          const selectedValues = []
          multiSelectOptions.value.forEach((o) => {
            o.values.forEach((v) => {
              if (v.selected === true) {
                selectedValues.push(v)
              }
            })
          })

          if (newVal.length !== selectedValues.length && selectedValues.length > 0) {
            selected.value = selectedValues
            newVal.value = selectedValues
          }
          if (selectedValues.length === newVal.length && !isHidden.value && isClickAction.value) {
            emitChanged()
          }
          if (!oldVal?.length && !newVal?.length) { return onChange(getWholeValue ? newVal : newVal.map(el => el.value), name, true) }
          const isValid = required ? newVal.length > 0 || selectedValues.length > 0 : true
          isValidated.value = isValid
          onChange(getWholeValue ? newVal : newVal.map(el => el.value), name, isValid)
          if (isMultiSelect !== 'true' && selectedValues.length > 0 && isClickAction.value) {
            clearSearchBox()
            isHidden.value = true
          }
          isClickAction.value = false
        }
        touched.value = true
      }
    })

    const clickOutside = () => {
      if (!isHidden.value) {
        isHidden.value = true
        clearSearchBox()
      }
    }

    const clearSearchBox = () => {
      if (searchKeyword.value) {
        searchKeyword.value = ''
        textChange()
      }
    }

    const clearAll = () => {
      selected.value = []
      multiSelectOptions.value = multiSelectOptions.value.map((opt) => {
        const values = opt.values.map(item => ({...item, selected: false}))
        return {...opt, values}
      })
    }

    watch(() => props.reset, (newVal) => {
      if (newVal) {
        clearAll()
        touched.value = false
      }
    })

    if (relatedDropdowns) {
      watch(() => props.options, (newVal) => {
        multiSelectOptions.value = newVal
        selected.value = newVal?.filter(o => (o?.selected === 'true' || o?.selected === true) && o?.disabled === 'false') || []
      })
    }

    const onFocus = () => {
      tmpSelected.value = selected.value?.map(m => m[defaultId])
    }

    const emitChanged = () => {
      emit('changed', selected.value)
    }

    const escapeRegExp = (val) => {
      return val.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') // $& means the whole matched string
    }

    const hightlightText = (x) => {
      if (searchKeyword.value) {
        const escaped = escapeRegExp(searchKeyword.value)
        const reg = new RegExp(escaped, 'gi')
        return x.replace(reg, function (match) {
          return `<span style="background: #FFC800;">${match}</span>`
        })
      }
      return x
    }

    return {
      selected,
      touched,
      handleChange,
      isHidden,
      clickOutside,
      clearAll,
      multiSelectOptions,
      isValidated,
      onFocus,
      searchKeywordInput,
      searchKeyword,
      textChange,
      clearSearchBox,
      hightlightText
    }
  }
}
</script>

  <style lang="scss" scoped>
  @import '~bootstrap/scss/functions';
  @import '~/assets/sass/variables';
  @import '~/assets/sass/utils';
  @import '~bootstrap/scss/mixins';

  .drop-down-wrapper--fixed-min-width {
    min-width: 20.875rem;
  }

  i {
    transition: all 0.2s ease;
  }

  .tertiary-color {
    color: $tertiary;
  }

  .line-height-sm {
    line-height: 1.125rem;
  }

  .close-icon {
    width: 1.125rem;
    height: 1.125rem;
    cursor: pointer;
    background-position: center;
    background-image: url("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0zIDEyQzMgNy4wMjgyMyA3LjAyODIzIDMgMTIgM0MxNi45NzE4IDMgMjEgNy4wMjgyMyAyMSAxMkMyMSAxNi45NzE4IDE2Ljk3MTggMjEgMTIgMjFDNy4wMjgyMyAyMSAzIDE2Ljk3MTggMyAxMlpNMTYuNDEyOSAxNC45Nzk0QzE2LjU4MzUgMTQuODA4OSAxNi41ODM1IDE0LjUzMzEgMTYuNDEyOSAxNC4zNjI1TDE0LjAzMjMgMTJMMTYuNDE2NSA5LjY0MTEzQzE2LjU4NzEgOS40NzA1NiAxNi41ODcxIDkuMTk0NzYgMTYuNDE2NSA5LjAyNDE5TDE0Ljk3OTQgNy41ODcxQzE0LjgwODkgNy40MTY1MyAxNC41MzMxIDcuNDE2NTMgMTQuMzYyNSA3LjU4NzFMMTIgOS45Njc3NEw5LjY0MTEzIDcuNTgzNDdDOS40NzA1NyA3LjQxMjkgOS4xOTQ3NiA3LjQxMjkgOS4wMjQyIDcuNTgzNDdMNy41ODcxIDkuMDIwNTZDNy40MTY1MyA5LjE5MTEzIDcuNDE2NTMgOS40NjY5NCA3LjU4NzEgOS42Mzc1TDkuOTY3NzQgMTJMNy41ODcxIDE0LjM1ODlDNy40MTY1MyAxNC41Mjk0IDcuNDE2NTMgMTQuODA1MiA3LjU4NzEgMTQuOTc1OEw5LjAyMDU3IDE2LjQxMjlDOS4xOTExMyAxNi41ODM1IDkuNDY2OTQgMTYuNTgzNSA5LjYzNzUgMTYuNDEyOUwxMiAxNC4wMzIzTDE0LjM1ODkgMTYuNDEyOUMxNC41Mjk0IDE2LjU4MzUgMTQuODA1MiAxNi41ODM1IDE0Ljk3NTggMTYuNDEyOUwxNi40MTI5IDE0Ljk3OTRaIiBmaWxsPSJibGFjayIvPgo8L3N2Zz4K");
  }

  .active {
    border-color: $black !important;
    // max-height: auto;

    i.bobcat-icon-sort-down {
      transform: rotateX(180deg);
    }
  }

  .facet-popup-modal {
    width: 100%;
    max-height: calc(65vh - 90px);
    overflow-y: auto;
    right: unset;
    left: 0;
  }

  .rg-1 {
    row-gap: 1rem;
  }

  .scroll-shadows {
    max-height: calc(65vh - 140px);
    max-width: 100%;
    overflow: auto;
    -webkit-overflow-scrolling: touch;
    overflow-scrolling: touch;

    background: /* Shadow Cover TOP */
      linear-gradient(
        white 100%,
        rgba(255, 255, 255, 0.5)
      ) center top,

      /* Shadow Cover BOTTOM */
      linear-gradient(
        rgba(255, 255, 255, 0.5),
        white 100%
      ) center bottom,

      /* Shadow TOP */
      linear-gradient(rgba(0, 0, 0, 0.3) 0%, rgba(255, 255, 255, 0) 100%) center top,

      /* Shadow BOTTOM */
      linear-gradient(rgba(255, 255, 255, 0) 0%, rgba(0, 0, 0, 0.5) 100%) center bottom;

    background-repeat: no-repeat;
    background-size: 100% 4.5rem, 100% 4.5rem, 100% 2rem, 100% 2rem;
    background-attachment: local, local, scroll, scroll;
  }

  .flex-gap-2 {
    gap: 1rem
  }

  .flex-gap-2 {
    gap: 1rem
  }

  .values-container {
    display: flex;
    flex-wrap: wrap;
  }

  .custom-button {
    height: 3rem !important;
  }

  .pl-7 {
    padding-left: 3.25rem;
  }

  .category-options {
    flex: 1 1 50%;
    box-sizing: border-box;
    padding: 0;
  }

  @include media-breakpoint-down(sm) {
    .category-options {
        flex: 1 1 100%; /* Stacks columns on smaller screens */
    }

    .facet-popup-modal {
      width: auto;
    }
  }

  .drop-down-option {
    &:hover {
      background-color: $tertiary;
      cursor: pointer;
    }
  }

  button {
    &:focus {
      box-shadow: none;
    }

    .input-icon {
      left: 0;
    }

    &.active:hover {
      border-color: $black !important;
    }

    .small {
      font-size: 0.62rem;
    }

    &:hover {
      box-shadow: $btn-box-shadow;
      .border {
        transition: $input-transition;
      }
    }
  }

  .options {
    overflow: hidden;
    @include absolute-top-right;
    position: absolute !important;
    transform: translate3d(0px, 50px, 0px);
    margin-top: -2px;
    border: none !important;
    box-shadow: 0 0.5rem 1rem 0 #00000033;
    border-bottom-left-radius: 2px;
    border-bottom-right-radius: 2px;
  }

  .drop-down-option-label {
    font-size: 1rem;
    line-height: 1.5rem !important;
    display: block;
    width: auto;
  }

  .no-results-container {
      margin: 0 auto 1rem;
      padding: 1rem;
      max-width: fit-content;
      font-family: $font-family-sans-serif;
      border: 2px solid $gray-200;
      border-radius: 0.5rem;
      background-color: $gray-100;
      font-size: 1.125rem;
      display: flex;
      column-gap: 1rem;
      align-items: center;
      line-height: 1.5rem;
  }

  </style>
