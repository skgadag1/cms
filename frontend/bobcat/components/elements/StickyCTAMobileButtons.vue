<template>
  <div class="d-flex d-md-none flex-column w-100" data-test-id="sticky-cta-mobile-buttons">
    <a v-if="buttonActions && buttonActions.length"
       class="font-weight-bold d-md-none d-flex w-100 py-3 justify-content-center"
       href="#"
       @click.prevent="$emit('handle-button-click', buttonActions[0])"
    >{{ buttonActions[0].label }}</a>
    <button
      v-if="primaryButton"
      class="d-md-none d-flex w-100 justify-content-center btn btn-primary d-flex align-items-center font-weight-bold"
      @click.prevent="$emit('handle-button-click', primaryButton)"
    >
      {{ primaryButton.label }}
    </button>
  </div>
</template>

<script>
export default {
  props: {
    buttonActions: {
      type: Array,
      required: true
    },
    primaryButton: {
      type: Object,
      required: false
    }
  }
}
</script>
