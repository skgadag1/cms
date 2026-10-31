<template>
  <div v-if="faultCode" data-test-id="diagnostic-model-information-code">
    <p class="small font-weight-bold mb-1 text-uppercase">{{ codeLabel }}</p>
    <p class="text-primary font-weight-bold h4">{{ faultCode }}</p>
  </div>
</template>

<script>
export default {
  props: {
    faultCode: String,
    codeLabel: String
  }
}
</script>
