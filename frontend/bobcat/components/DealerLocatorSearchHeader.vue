<template>
  <LazyHydrate never>
    <section class="container" data-test-id="dealer-locator-search-header">
      <div class="row">
        <div class="col-12">
          <component :is="htag" v-if="title" :class="htag" class="mb-2">{{ title }}</component>
          <div v-if="paragraph"
               ref="wrapper"
               v-html="paragraph"
          />
        </div>
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
    paragraph: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    title: {
      type: String
    }
  },
  setup(props) {
    const wrapper = ref(null)
    const {updateMediaElements} = useMedia()
    useMounted(props.nodeId)

    watch(wrapper, (newVal) => {
      if (process.client && newVal) {
        updateMediaElements(newVal, 'video')
        updateMediaElements(newVal, 'iframe')
      }
    })

    return {wrapper}
  }
}
</script>

<style lang="scss">
section[data-test-id='dealer-locator-search-header'] {
  iframe {
    width: 100%;
  }
}
</style>
