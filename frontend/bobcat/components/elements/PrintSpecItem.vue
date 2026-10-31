<template>
  <div class="specification-row no-gutters d-flex align-items-stretch">
    <div class="col">
      <div class="py-1 print-border-bottom product-specification d-flex align-items-center h-100 text-break">{{ label }}</div>
    </div>
    <div class="col">
      <div class="print-border-bottom font-weight-bold product-specification d-flex align-items-center h-100 text-break">
        <template v-if="typeof model === 'object'">
          <div v-if="Array.isArray(model.value)" class="row no-gutters w-100">
            <div v-for="(x, k) in model.value" :key="`test__${k}`" class="col-12 col-md" :class="{'pr-1' : k !== model.value.length - 1}">
              <span v-html="x.value" /> <span v-if="unit || x.unit">{{ unit || x.unit }}</span>
            </div>
          </div>
          <span v-else v-html="model.value" />&nbsp;<span v-if="model.unit || unit">{{ model.unit || unit }}</span>
        </template>
        <div v-else-if="model">
          <span v-html="model" /><span v-if="unit">&nbsp;{{ unit }}</span>
        </div>
      </div>
    </div>
    <div v-if="isFeatureSection" class="print-check-col d-flex align-items-center justify-content-center print-border-bottom">
      <slot name="suffix" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    model: {
      type: [Object, String],
      required: true
    },
    label: {
      type: String,
      required: true
    },
    unit: {
      type: String,
      required: true
    },
    isFeatureSection: {
      type: Boolean,
      default: false
    }
  }
}
</script>
