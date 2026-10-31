<template>
  <section class="container py-4" data-test-id="custom-iframe">
    <LazyHydrate never>
      <b-embed type="iframe"
               :aspect="ratio"
               :src="src"
               :title="title"
               :class="{ 'resize' : shouldAdaptHeight }"
               :height="shouldAdaptHeight ? `${height}` : ''"
               :style="shouldAdaptHeight ? `height: ${height}px` : ''"
               allowfullscreen
               allowtransparency
               :allow="allow !== '' && allow"
      />
    </LazyHydrate>
  </section>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    src: {
      type: String,
      required: true
    },
    ratio: {
      type: String,
      default: '1by1'
    },
    height: {
      type: String
    },
    allow: {
      type: String
    }
  },
  setup(props) {
    const shouldAdaptHeight = ref(props.ratio === 'custom')
    useMounted(props.nodeId)

    return {
      shouldAdaptHeight
    }
  }
}
</script>

<style lang="scss" scoped>
.embed-responsive {
  &-custom {
    iframe {
      position: relative;

      &.resize {
        resize: both;
      }
    }
  }
}
</style>
