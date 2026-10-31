<template>
  <div class="custom-switch-dropdown text-nowrap">
    <div v-if="label"
         class="pr-2"
         :class="fuseLabelAndOptionsForMobile && `d-none d-md-inline-block`"
         :title="generateConceptHint('units')"
    >
      {{ label }}
    </div>
    <span ref="longestLabelRef" class="invisible position-absolute z-index-n">{{ longestLabel }}</span>
    <b-dropdown v-bind="config || {}"
                variant="transparent"
                :disabled="disabled"
                toggle-class="d-flex align-items-center px-3 border border-color-gray-600 bg-white"
    >
      <template #button-content>
        <b-icon v-if="icon" :icon="icon" class="d-md-none h4 mb-0 mr-1" />
        <span ref="currentLabelRef" class="m-0 pr-2 py-1 w-100 text-left">
          <span v-if="label && fuseLabelAndOptionsForMobile" class="d-md-none">{{ label }}: </span>{{ selectedLabel }}
        </span>
      </template>
      <b-dropdown-item v-for="o in options"
                       :key="`switch-dropdown-option__${o.value}`"
                       @click="selected = o"
      >
        <span v-if="label && fuseLabelAndOptionsForMobile" class="d-md-none">{{ label }}: </span>{{ o.label }}
      </b-dropdown-item>
    </b-dropdown>
  </div>
</template>

<script>
import {computed, ref, watch, onMounted} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  props: {
    value: Object,
    config: Object,
    label: String,
    icon: String,
    options: {
      type: Array,
      required: true
    },
    disabled: {
      type: Boolean,
      default: false
    },
    fuseLabelAndOptionsForMobile: {
      type: Boolean,
      default: false
    },
    selectedOption: {
      type: Object
    }
  },
  setup(props, {emit}) {
    const {options, selectedOption} = props
    const fallbackOption = options.find(o => o.selected || o.selected === 'true') || options[0] || {}
    const selected = ref(selectedOption ? options.find(o => o?.value === selectedOption.value) || fallbackOption : fallbackOption)
    const longestLabelRef = ref(null)
    const currentLabelRef = ref(null)
    const {generateConceptHint} = useConcepts()
    const selectedLabel = computed(() => selected.value?.label || props.label || '')

    const longestLabel = ref(options?.[0]?.label || '')
    options.forEach((o) => {
      if (o.label?.length > longestLabel.value.length) { longestLabel.value = o.label }
    })

    if (process.client) {
      onMounted(() => {
        if (currentLabelRef.value) { currentLabelRef.value.style.minWidth = `${longestLabelRef.value?.offsetWidth + 8}px` }
      })
    }

    watch(() => props.value, (s) => {
      if (s?.value !== selected.value?.value) {
        selected.value = options.find(x => x.value === s?.value) || fallbackOption
      }
    })

    watch(() => props.selectedOption, (s) => {
      if (s && s.value !== selected.value?.value) {
        selected.value = props.options.find(x => x.value === s.value) || s
      }
    }, {immediate: true})

    watch(selected, (s) => { if (s?.value) { emit('changed', s) } })

    return {
      selected,
      selectedLabel,
      longestLabel,
      longestLabelRef,
      currentLabelRef,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss" >
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.custom-switch-dropdown {
  .dropdown {
    @include media-breakpoint-down(sm) {
      width: 100%;
    }
  }

  .dropdown-menu {
    min-width: 100%;
    max-height: 21rem;
    overflow-y: auto;
  }

  .dropdown-toggle {
    min-width: $dropdown-min-width !important;
    height: 2.5rem;

    &:not([disabled]) {
      color: $black !important;
    }

    &:focus {
      box-shadow: none !important;
    }
  }
}
</style>
