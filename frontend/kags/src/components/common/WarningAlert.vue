<template>
  <div class="alert alert-warning-outline border-warning my-3">
    <h3 class="text-uppercase font-condensed d-flex align-items-center">
      <b-img :src="assetServer + '/assets/img/icons/alert.svg'" height="28" class="me-2" /><slot name="title">{{ translate("warning", 'WarningAlert') }}</slot>
    </h3>
    <slot name="body" />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
export default {
  conceptName: ['WarningAlert'],
  setup() { return useLabels() },
}
</script>

<style lang="scss">
.alert-warning-outline {
  line-height: 1.2rem;
}
</style>
