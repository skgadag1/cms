<template>
  <div class="custom-switch-dropdown d-flex align-items-center">
    <div v-if="label"
         class="pr-2"
         :class="fuseLabelAndOptionsForMobile && `d-none d-xl-block text-nowrap`"
         :title="generateConceptHint('units')"
    >
      {{ label }}
    </div>
    <span ref="longestLabelRef" class="invisible position-absolute z-index-n">{{ longestLabel }}</span>
    <b-dropdown variant="transparent"
                :disabled="disabled"
                :lazy="config && config.lazy"
                :right="config && config.right"
                toggle-class="d-flex align-items-center px-2 border border-color-gray-600"
                :class="backgroundClass"
    >
      <template #button-content>
        <b-icon v-if="icon" :icon="icon" class="h4 mb-0 mr-1" />
        <span ref="currentLabelRef" class="m-0 pr-2 py-1 w-100 text-left">
          <span v-if="label && fuseLabelAndOptionsForMobile" class="d-xl-none">{{ label }}: </span>{{ selected.label }}
        </span>
      </template>
      <b-dropdown-item v-for="o in options"
                       :key="`switch-dropdown-option__${o.value}`"
                       @click="selected = o"
      >
        <span v-if="label && fuseLabelAndOptionsForMobile" class="d-xl-none">{{ label }}: </span>{{ o.label }}
      </b-dropdown-item>
    </b-dropdown>
  </div>
</template>

<script>
import {onMounted, ref, watch} from '@nuxtjs/composition-api'
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
    },
    backgroundClass: {
      type: String,
      default: 'bg-white'
    }
  },
  setup(props, {emit}) {
    const {options, selectedOption} = props
    const selected = ref(selectedOption ? options.find(o => o?.value === selectedOption.value) : options.find(o => o.selected || o.selected === 'true') || options[0] || {})
    const longestLabelRef = ref(null)
    const currentLabelRef = ref(null)
    const {generateConceptHint} = useConcepts()

    const longestLabel = ref(options?.[0]?.label || '')
    options.forEach((o) => {
      if (o.label?.length > longestLabel.value.length) {
        longestLabel.value = o.label
      }
    })

    if (process.client) {
      onMounted(() => {
        if (currentLabelRef.value) {
          currentLabelRef.value.style.minWidth = `${longestLabelRef.value?.offsetWidth + 8}px`
        }
      })
    }

    watch(() => props.value, (s) => {
      if (s?.value !== selected.value) {
        selected.value = options.find(x => x.value === s.value) || options[0]
      }
    })

    watch(selected, (s) => {
      if (s?.value) {
        emit('changed', s)
      }
    })

    return {
      selected,
      longestLabel,
      longestLabelRef,
      currentLabelRef,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.custom-switch-dropdown {
  .dropdown {
    @include media-breakpoint-down(lg) {
      width: 100%;
    }
  }

  .dropdown-menu {
    min-width: 100%;
  }

  .dropdown-toggle {
    min-width: $dropdown-min-width !important;

    &:not([disabled]) {
      color: $black !important;
    }

    &:focus {
      box-shadow: none !important;
    }
  }
}
</style>
