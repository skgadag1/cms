<template>
  <b-button
    v-if="variant !== BUTTON_NAMES.NONE"
    v-bind="$attrs"
    :class="buttonClasses"
    :data-test-color="color"
    class="dynamic-button"
    @click="event => $emit('click', event)"
  >
    <b-icon v-if="icon" :icon="icon" />
    <slot />
  </b-button>
</template>

<script>
import {computed, defineComponent} from '@nuxtjs/composition-api'
import {BUTTON_NAMES, BUTTON_STYLES} from '~/constants/buttons'

export default defineComponent({
  inheritAttrs: false,
  props: {
    variant: {
      type: String,
      default: BUTTON_NAMES.HEXAGONAL,
      validator: value => Object.values(BUTTON_NAMES).includes(value)
    },
    icon: {
      type: String
    },
    color: {
      type: String
    }
  },
  setup(props) {
    const buttonClasses = computed(() => {
      if (props.variant === BUTTON_NAMES.NONE) {
        return []
      }
      const variantStyles = BUTTON_STYLES[props.variant] || BUTTON_STYLES[BUTTON_NAMES.HEXAGONAL]
      const colorStyles = variantStyles.colors[props.color] || variantStyles.default
      return [...variantStyles.base, ...colorStyles]
    })

    return {BUTTON_NAMES, buttonClasses}
  }
})
</script>

<style lang="scss">
@import '~/assets/sass/_colors';

.btn-linear {
  &.text-white {
    &:hover {
      color: $red !important
    }
  }
}
</style>
