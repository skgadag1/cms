<template>
  <b-badge v-b-tooltip="{ placement: 'top', title: tooltip, titleClass: 'tooltip-light' }"
           class="counter-rounded rounded-circle d-inline-flex align-items-center justify-content-center align-self-center font-base ms-1"
           :variant="variant"
  >
    <slot />
  </b-badge>
</template>

<script>
export default {
  props: {
    tooltip: {
      type: String,
      required: false
    },
    variant: {
      type: String,
      required: false,
      default: 'danger'
    }
  }
}
</script>

<style lang="scss">
@import "../../scss/colors";

.counter-rounded {
  width: 1.5rem;
  height: 1.5rem;
  font-weight: 700;
  font-size: .75rem;
  box-shadow: 1px 2px 1px 0px $black;
}
</style>
