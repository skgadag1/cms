<template>
  <span data-test-id="divider" class="custom-divider w-100" :class="color" />
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      default: 'bg-gray-lighter'
    }
  }
}
</script>

<style lang="scss">
.custom-divider {
  height: 1px;
}
</style>
