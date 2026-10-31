<template>
  <div class="overlay-wrapper position-relative">
    <slot />
    <div v-if="show"
         class="overlay-wrapper__backdrop position-absolute d-flex align-items-center justify-content-center"
         :class="[`bg-${variant}`, {rounded}]"
         :style="{opacity}"
    >
      <div class="spinner-border" :class="`text-${spinnerVariant}`" role="status">
        <span class="visually-hidden">{{ translate('loadingLabel', 'CommonFunctional') }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels';

// Was `extends: BOverlay` (bootstrap-vue) -- BOverlay's Vue-2-style render function crashes
// under @vue/compat ("h.apply is not a function"), and it renders on nearly every page's
// initial mount, so it broke the whole app. Every call site only ever passes `:show` and a
// prop API without depending on any bootstrap-vue component.
export default {
  conceptName: ['CommonFunctional'],
  setup() {
    return useLabels()
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    variant: {
      type: String,
      required: false,
      default: 'gray'
    },
    spinnerVariant: {
      type: String,
      required: false,
      default: 'primary'
    },
    rounded: {
      type: Boolean,
      required: false,
      default: true
    },
    opacity: {
      type: String,
      required: false,
      default: '.7'
    },
    noFade: {
      type: Boolean,
      required: false,
      default: true
    }
  }
}
</script>

<style lang="scss" scoped>
.overlay-wrapper__backdrop {
  inset: 0;
  z-index: 10;
}
</style>
