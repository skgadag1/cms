<template>
  <LazyHydrate never>
    <section class="container">
      <div class="row justify-content-center" data-test-id="article-text-section">
        <div ref="contentWrapper"
             class="justify-content-center flex-column align-items-center col-12 col-sm-8 col-lg-7"
             data-test-id="article-text-section-wrapper"
             v-html="text"
        />
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'
import useMedia from '~/helpers/composables/useMedia'

export default {
  props: {
    nodeId: {
      type: String
    },
    text: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const contentWrapper = ref(null)
    const {updateMediaElements} = useMedia()
    useMounted(props.nodeId)

    watch(contentWrapper, (newVal) => {
      if (process.client && newVal) {
        updateMediaElements(newVal, 'video')
        updateMediaElements(newVal, 'iframe')
      }
    })

    return {contentWrapper}
  }
}
</script>

<style lang="scss">
div[data-test-id='article-text-section'] {
  iframe {
    width: 100%;
  }
}
</style>
