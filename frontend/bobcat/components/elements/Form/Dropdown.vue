<template>
  <b-form-group data-test-id="dropdown" :class="touched ? (isValidated ? 'is-valid' : 'is-invalid') : ''">
    <label v-if="label"
           :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]"
    >
      <LazyHydrate never><span>{{ label }}</span></LazyHydrate>
      <LazyHydrate never><span v-if="required" v-html="requiredSign" /></LazyHydrate>
    </label>

    <div class="dropdown-wrapper">
      <b-dropdown
        ref="dropdown"
        block
        :disabled="disabledDropdown || !(options && options.length) || (options.length === 1 && selected && selected.value === options[0].value)"
        menu-class="w-100"
        no-flip
        :toggle-class="`d-flex align-items-center py-3 px-3 dropdown-md ${!disableMinWidth && 'dropdown-md--fixed-min-width'} ${disabledDropdown ? 'bg-gray' : 'bg-white'}`"
        @toggle="toggleDropdown"
        @hidden="removeKeyDown"
        @shown="addOverlayIfNeeded"
      >
        <template #button-content>
          <div v-if="selected && selected.label" class="position-relative h6 text-capitalize py-1 m-0 w-100 text-left font-weight-light">
            <span :class="[{'px-5' : icon}, {'text-gray-600': !selected.value}]" class="font-weight-normal overflow-hidden w-100 d-flex label-wrapper">
              {{ selected.label }}
            </span>
            <span v-if="icon" class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3">
              <b-icon :icon="icon" class="text-black" />
            </span>
          </div>
        </template>
        <span v-if="!filteredOptions || !filteredOptions.length" class="text-gray-600 px-3">{{ noResultsMsg }}</span>
        <template v-for="(o, i) in filteredOptions">
          <b-dropdown-item
            v-if="!(selected && selected.value === o.value)"
            :key="`custom-dropdown-option__${o.value || o['@name'] || o.label}__${i}`"
            :disabled="o.disabled === 'true'"
            @click="selected = o"
          >
            <LazyHydrate never><span>{{ o.label }}</span></LazyHydrate>
          </b-dropdown-item>
        </template>
      </b-dropdown>
    </div>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      <LazyHydrate never><span>{{ validationMessage }}</span></LazyHydrate>
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch, onMounted} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {label, name, icon, options, onChange, required, validationMessage, placeholder, relatedDropdowns, disabledDropdown, bold, noResultsMsg, reset, getWholeValue, initValue, requiredSign, disableMinWidth, forceValidation} = config

export default {
  props: {
    label, name, icon, options, onChange, required, validationMessage, placeholder, relatedDropdowns, disabledDropdown, bold, noResultsMsg, reset, getWholeValue, initValue, requiredSign, disableMinWidth, forceValidation
  },
  setup(props, {emit}) {
    const text = ref('')
    const {label, name, onChange, required, placeholder, relatedDropdowns, getWholeValue} = props
    const selected = ref(props.options?.find(o => o?.selected === 'true' && o.disabled === 'false') || {value: null, label: placeholder || label})
    const isValidated = ref(true)
    const touched = ref(false)
    const dropdown = ref(null)
    const onceClicked = ref(false)
    const filteredOptions = ref(props.options)

    const checkIfScrollIsOnTheEnd = (e) => {
      if (e.offsetHeight + e.scrollTop >= e.scrollHeight) { e.classList.remove('dropdown-overlay') } else { e.classList.add('dropdown-overlay') }
    }

    const addOverlayIfNeeded = () => {
      const list = dropdown.value.$el?.querySelector('ul.dropdown-menu')
      if (filteredOptions.value.length > 7) { list.classList.add('dropdown-overlay') }
    }

    const filterByText = (t) => {
      const fOpts = props.options?.filter(o => o.label.toLowerCase().includes(t.toLowerCase()))
      filteredOptions.value = fOpts
    }

    const onKeyDown = (e) => {
      if (e.keyCode === 32) {
        e.preventDefault()
      }
      if ((e.keyCode > 64 && e.keyCode < 91) || (e.keyCode > 96 && e.keyCode < 123) || e.keyCode === 8 || e.keyCode === 32) {
        const currentValue = e.keyCode === 8 ? text.value.slice(0, -1) : text.value + e.key
        text.value = currentValue
        selected.value.label = currentValue || placeholder
        filterByText(currentValue)
      }
    }

    const toggleDropdown = () => {
      document.addEventListener('keydown', onKeyDown, false)
    }

    const removeKeyDown = () => {
      document.removeEventListener('keydown', onKeyDown)
    }

    if (onChange && selected?.value?.value) {
      const isValid = required ? selected.value.value : true
      onChange(getWholeValue ? selected.value : selected.value.value, name, isValid)
    }

    watch(selected, (newVal, oldVal) => {
      if (!props.reset) {
        if (newVal.value) { onceClicked.value = true }
        filteredOptions.value = props.options
        text.value = ''
        emit('changed', newVal)
        if (onChange) {
          if (!oldVal.value && !newVal.value) { return onChange(newVal.value, name, !required) }
          const isValid = required ? Boolean(newVal.value) : true
          isValidated.value = isValid
          onChange(getWholeValue ? newVal : newVal.value, name, isValid)
        }
        setTimeout(() => { onceClicked.value = false }, 100)
        touched.value = true
      }
    })

    if (relatedDropdowns) {
      watch(() => props.options, (newVal) => {
        filteredOptions.value = newVal
        selected.value = newVal?.find(o => o?.selected === 'true' && o?.disabled === 'false') || {value: null, label: placeholder || label}
      })
    }

    watch(() => props.reset, (newVal) => {
      if (newVal) {
        selected.value = props.options?.find(o => o.selected === 'true' && o.disabled === 'false') || {value: null, label: placeholder || label}
        text.value = ''
        touched.value = false
      }
    })

    watch(() => props.initValue, (v) => {
      if (v?.label) { selected.value = props.options?.find(o => o.label === v.label) }
    }, {immediate: true})

    watch(() => props.forceValidation, (newVal) => {
      if (newVal && !selected.value.value) {
        isValidated.value = false
      }
    })

    if (process.client) {
      onMounted(() => {
        const list = dropdown.value.$el?.querySelector('ul.dropdown-menu')
        list.addEventListener('scroll', () => checkIfScrollIsOnTheEnd(list))
        if (dropdown.value) {
          const toggle = dropdown.value.$refs.toggle
          toggle.addEventListener('focus', () => {
            if (!onceClicked.value) { dropdown.value.show() }
          })
        }
      })
    }

    return {
      selected,
      isValidated,
      touched,
      toggleDropdown,
      removeKeyDown,
      filteredOptions,
      text,
      dropdown,
      addOverlayIfNeeded
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

// hack because of - all inputs should have the same height
.dropdown-md {
  max-height: 50px;

  .label-wrapper {
    @include media-breakpoint-up(sm) {
      max-width: 100%;
    }
  }

  .input-icon {
    left: 0;
  }
}

.dropdown-md--fixed-min-width {
  min-width: 16rem;
}

.dropdown-overlay {
  box-shadow: inset 0 -3rem 10px -10px #d9d9d9;
}

// hack for showing scroll always on Mac Devices
ul.dropdown-menu {
  max-height: 21.2rem !important;
  &::-webkit-scrollbar {
    width: 7px;
    height: 7px;
    background: white;
  }

  &::-webkit-scrollbar-thumb {
    border-radius: 4px;
    background-color: rgba(0,0,0,.5);
    -webkit-box-shadow: 0 0 1px rgba(255,255,255,.5);
  }
}

//exception for hack above
.main-nav ul.dropdown-menu {
  max-height: none !important;
}
</style>
