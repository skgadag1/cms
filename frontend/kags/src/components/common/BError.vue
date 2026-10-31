<template>
  <b-alert show variant="danger" body-class="d-flex flex-grow-1">
    <b-icon icon="exclamation-triangle" class="flex-grow-0 pe-3" />
    <div class="font-condensed">
      <slot />
      <div v-if="code" class="mt-3">{{ translate('errorCode') }} {{ code }}</div>
    </div>
  </b-alert>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
export default {
  setup() { return useLabels() },
  props: {
    code: {
      type: Number,
      required: false
    }
  }
}
</script>
