<template>
  <LazyHydrate never>
    <section v-if="disclaimerContent" class="container py-5 section-text">
      <div :class="{'h4 m-0' : disclaimerConcept?.value === 'PlainText'}" v-html="disclaimerContent" />
    </section>
  </LazyHydrate>
</template>

<script>
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    disclaimerConcept: {
      type: Object
    }
  },
  setup(props) {
    useMounted(props.nodeId)
    const disclaimerContent = props.disclaimerConcept?.['value' + props.disclaimerConcept?.value]

    return {disclaimerContent}
  }
}
</script>
