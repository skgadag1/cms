<template>
  <span data-test-id="evolution-divider" class="d-block evolution-divider w-100" :class="color" />
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      default: 'text-steel'
    }
  }
}
</script>

<style lang="scss">
.evolution-divider {
  height: 1px;
  background-image: linear-gradient(to right, currentColor 0%, transparent 90%);
  background-color: transparent;
}
</style>
