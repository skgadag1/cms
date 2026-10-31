<template>
  <div data-test-id="diagnostic-model-information-description" class="pb-3">
    <p class="small font-weight-bold mb-1 text-uppercase">{{ descriptionLabel }}</p>
    <p v-if="description" class="m-0">{{ description }}</p>
    <b-skeleton v-if="loading" class="mt-2" />
  </div>
</template>

<script>
export default {
  props: {
    descriptionLabel: String,
    description: String,
    loading: Boolean
  }
}
</script>
