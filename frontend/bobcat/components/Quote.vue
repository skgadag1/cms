<template>
  <LazyHydrate never>
    <blockquote class="container">
      <div class="row justify-content-center" data-test-id="article-quote">
        <div class="d-flex justify-content-center flex-column align-items-center col-12 col-sm-8 col-lg-7">
          <b-icon icon="start-quote" class="quotes" data-test-id="article-quote-start-quote" />
          <h4 data-test-id="article-quote-title" class="text-center mt-3">{{ quote }}</h4>
          <b-avatar v-if="avatarImage"
                    class="avatar bg-transparent"
                    data-test-id="article-quote-avatar"
                    variant="info"
                    :src="avatarImage"
                    :alt="avatar.imageAlt"
                    size="2.5rem"
          />
          <h5 data-test-id="article-quote-name" class="text-center m-0 mt-3">{{ authorName }}</h5>
          <p data-test-id="article-quote-job-position" class="text-center">{{ authorSubtitle }}</p>
          <b-icon icon="end-quote" class="quotes" data-test-id="article-quote-end-quote" />
        </div>
      </div>
    </blockquote>
  </LazyHydrate>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    quote: {
      type: String
    },
    authorName: {
      type: String
    },
    authorSubtitle: {
      type: String
    },
    avatar: {
      type: Object
    }
  },
  setup(props) {
    const {$utils} = useContext()
    const {avatar} = props
    const avatarImage = ref($utils.getCloudinaryAsset(avatar?.src, true, {crop: 'fill', width: 40, height: 40}))
    useMounted(props.nodeId)

    return {
      avatarImage
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.quotes {
  font-size: 2.65rem;
}

.avatar {
  border: 2px solid $primary;
}
</style>
