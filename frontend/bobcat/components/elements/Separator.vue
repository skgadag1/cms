<template>
  <aside :class="[size, selectedVariant.background]" />
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/separatorVariants'

export default {
  props: {
    size: {
      type: String,
      default: 'py-5'
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props) {
    const selectedVariant = computed(() => VARIANTS_CLASSES[props.variant] || VARIANTS_CLASSES[WHITE_VARIANT])

    return {
      selectedVariant
    }
  }
}
</script>
