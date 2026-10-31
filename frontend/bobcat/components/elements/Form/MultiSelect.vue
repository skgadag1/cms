<template>
  <b-form-group data-test-id="multi-select-wrapper">
    <label v-if="label" :class="[touched ? (isValidated ? 'is-valid' : 'is-invalid') : '', {'font-weight-bold': bold}]">
      {{ label }}<span v-if="required"> *</span>
    </label>
    <div v-click-outside.mousedown="clickOutside" :class="`position-relative ${!disableMinWidth ? 'multi-select-wrapper--fixed-min-width' : ''}`">
      <button :disabled="disabledDropdown"
              :class="[{'active': !isHidden}, touched ? (isValidated ? 'is-valid' : 'is-invalid') : '', `${disabledDropdown ? 'bg-gray' : 'bg-white'}`]"
              class="form-control px-3 py-4 bg-white border d-flex justify-content-start align-items-center"
              @focus="onFocus"
              @click.prevent="isHidden = false"
      >
        <div v-if="selected && selected.length || label" class="position-relative h6 text-capitalize py-1 m-0 pr-2 w-100 text-left font-weight-light">
          <span class="font-weight-normal" :class="[{'px-5' : icon}, {'text-gray-600': !selected.length}, {'text-gray-400': disabledDropdown}]">
            {{ selected.length ? `${selected.length} ${selectedLabel}` : placeholder || label }}
          </span>
          <span v-if="icon" class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3">
            <b-icon :icon="icon" class="text-black" />
          </span>
        </div>
        <span class="small">
          <b-icon icon="sort-down" class="mt-1 text-black" />
        </span>
      </button>
      <div v-if="!isHidden" :class="{'active overflow-auto': !isHidden}" class="px-3 pt-1 pb-3 bg-white border options z-index-4 w-100">
        <p class="mb-1 font-weight-bold text-primary cursor-pointer" @click="clearAll">{{ clearLabel }}</p>
        <div
          v-for="(o, i) in multiSelectOptions"
          :key="`multi-select-option__${o.value}_${i}`"
          :disabled="o.disabled === 'true'"
          class="user-select-none"
        >
          <b-form-checkbox
            v-model="o.selected"
            class="w-100"
            :disabled="o.disabled === 'true'"
            @change="handleChange(o)"
          >
            {{ o.label }}
          </b-form-checkbox>
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

const {label, name, options, icon, clearLabel, selectedLabel, onChange, required, validationMessage, placeholder, bold, reset, disabledDropdown, getWholeValue, defaultId, relatedDropdowns, disableMinWidth, initValues} = config

export default {
  props: {
    label, name, options, icon, clearLabel, selectedLabel, onChange, required, validationMessage, placeholder, bold, reset, disabledDropdown, getWholeValue, defaultId, relatedDropdowns, disableMinWidth, initValues
  },
  setup(props, {emit}) {
    const {name, onChange, required, getWholeValue, defaultId, relatedDropdowns} = props
    const multiSelectOptions = ref(props.options || [])
    const selected = ref(multiSelectOptions.value.filter(o => o.selected === 'true' && o.disabled === 'false') || [])
    const isValidated = ref(true)
    const isHidden = ref(true)
    const touched = ref(false)
    const tmpSelected = ref(selected.value?.map(m => m[defaultId]))

    if (onChange) {
      const isValid = required ? selected.value.length > 0 : true
      onChange(getWholeValue ? selected.value : selected.value.map(el => el.value), name, isValid)
    }

    const handleChange = (option) => {
      if (option.selected === true || option.selected === 'true') {
        selected.value = [...(selected.value), option]
      } else {
        selected.value = selected.value.filter(opt => option[defaultId] !== opt[defaultId])
      }
    }

    watch(selected, (newVal, oldVal) => {
      if (!props.reset) {
        if (onChange) {
          if (!oldVal?.length && !newVal?.length) { return onChange(getWholeValue ? newVal : newVal.map(el => el.value), name, true) }
          const isValid = required ? newVal.length > 0 : true
          isValidated.value = isValid
          onChange(getWholeValue ? newVal : newVal.map(el => el.value), name, isValid)
        }
        touched.value = true
      }
    })

    const clickOutside = () => {
      if (!isHidden.value) {
        isHidden.value = true
        emitChanged()
      }
    }

    const clearAll = () => {
      selected.value = []
      multiSelectOptions.value = multiSelectOptions.value.map(opt => ({...opt, selected: 'false'}))
    }

    watch(() => props.reset, (newVal) => {
      if (newVal) {
        clearAll()
        touched.value = false
      }
    })

    watch(() => props.initValues, (values) => {
      if (values?.length > 0 && multiSelectOptions?.value?.length > 0) {
        selected.value = multiSelectOptions.value.reduce((p, o) => {
          if (values.some(v => v.label === o.label)) {
            o.selected = 'true'
            p.push(o)
          }
          return p
        }, [])
      }
    }, {immediate: true})

    if (relatedDropdowns) {
      watch(() => props.options, (newVal) => {
        multiSelectOptions.value = newVal
        selected.value = newVal?.filter(o => (o?.selected === 'true' || o?.selected === true) && o?.disabled === 'false') || []
      })
    }

    const onFocus = () => {
      isHidden.value = false
      tmpSelected.value = selected.value?.map(m => m[defaultId])
    }

    const emitChanged = () => {
      let hasChanged = false
      if (tmpSelected.value?.length === selected.value?.length) {
        selected.value.forEach((model) => { hasChanged = hasChanged || !tmpSelected.value.includes(model[defaultId]) })
      } else {
        hasChanged = true
      }
      if (hasChanged) { emit('changed', selected.value) }
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
      onFocus
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';

.multi-select-wrapper--fixed-min-width {
  min-width: 16rem;
}

i {
  transition: all 0.2s ease;
}

.active {
  border-color: $black !important;
  max-height: 10rem;

  i.bobcat-icon-sort-down {
    transform: rotateX(180deg);
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
  @include absolute-top-right;
  position: absolute !important;
  transform: translate3d(0px, 50px, 0px);
  margin-top: -2px;
  border-top: none !important;
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
}
</style>
