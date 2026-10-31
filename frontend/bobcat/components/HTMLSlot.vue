<template>
  <LazyHydrate never>
    <section v-html="processedHtml" />
  </LazyHydrate>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: ['html', 'nodeId'],
  setup(props) {
    useMounted(props.nodeId)

    const processedHtml = computed(() => {
      if (!props.html) { return '' }

      // Add loading="lazy" to all iframe tags
      return props.html.replace(
        /<iframe/gi,
        '<iframe loading="lazy"'
      )
    })

    return {
      processedHtml
    }
  }

}
</script>
