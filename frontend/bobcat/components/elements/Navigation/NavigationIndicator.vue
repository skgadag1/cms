<template>
  <b-badge v-if="text"
           class="navigation-indicator text-wrap mx-2 font-weight-bold text-uppercase bg-white text-primary rounded border border-color-red"
           v-on="$listeners"
  >
    {{ text }}
  </b-badge>
</template>

<script>
export default {
  props: {
    text: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="scss" scoped>
.navigation-indicator {
  vertical-align: middle;
  padding: 0.125rem 0.375rem;
  font-size: 0.875rem !important;
}
</style>
