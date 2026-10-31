<template>
  <div class="specification-row-attachment no-gutters d-flex align-items-stretch">
    <div class="col">
      <div class="py-1 font-weight-bold print-border-bottom product-specification d-flex align-items-center h-100 text-break">
        {{ name }}
      </div>
    </div>
    <div class="col">
      <div class="py-1 print-border-bottom product-specification d-flex align-items-center justify-content-end h-100 text-break">
        <span v-if="amountInGroup">{{ amountInGroup }} {{ labels.options || 'options' }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import {
  useContext,
  onServerPrefetch,
  ref,
  useFetch
} from '@nuxtjs/composition-api'
export default {
  props: {
    name: {
      type: String,
      required: true
    },
    amountInGroup: {
      type: [String, Number],
      default: ''
    }
  },
  setup() {
    const {$services} = useContext()
    const labels = ref({})

    const {fetch} = useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/specifications'])
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    return {
      labels
    }
  }
}
</script>
