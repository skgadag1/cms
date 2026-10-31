<template>
  <aside
    v-if="!isHidden"
    class="overlay p-0 p-sm-3 d-flex justify-content-center align-items-center"
    @click.self.prevent="hideModal"
  >
    <div
      data-test-id="media-preview-container"
      class="position-relative mh-100 d-flex flex-column w-100 bg-black"
    >
      <YoutubeIframe
        v-if="assetItem.youtube"
        :height="youtubeHeight"
        :src="assetItem.video"
      />
      <GTMVideo
        v-else-if="assetItem.video"
        playsinline
        controls
        autoplay
        :poster="assetItem.poster"
        :src="assetItem.video"
        data-test-id="media-preview-video"
      />
      <ImageSet
        v-else-if="assetItem.img"
        :src="assetItem.img"
        :alt="assetItem.imageAlt"
        :title="assetItem.imageTitle"
        :img-width="assetItem.imgWidth"
        :img-height="assetItem.imgHeight"
        aspect-ratio="auto"
        data-test-id="media-preview-img"
      />
      <span
        role="button"
        data-test-id="media-preview-close-btn"
        class="close-btn position-absolute d-flex justify-content-center align-items-center bg-white keyboard-focus"
        aria-expanded="true"
        tabindex="0"
        @click.prevent="hideModal"
        @keydown.enter.prevent="hideModal"
        @keydown.space.prevent="hideModal"
      ><b-icon icon="close" /></span>
      <div v-if="title || paragraph"
           ref="descriptionWrapper"
           class="text-white py-0 py-sm-4 d-flex"
      >
        <div class="media-text-container d-flex flex-column px-4 py-4 py-sm-0">
          <h2>{{ title }}</h2>
          <div data-test-id="card-paragraph" v-html="paragraph" />
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
import {onBeforeUnmount, onMounted, onUpdated, ref, nextTick} from '@nuxtjs/composition-api'

export default {
  props: {
    assetItem: {
      type: Object,
      required: true
    },
    title: {
      type: String
    },
    paragraph: {
      type: String
    }
  },
  setup(props) {
    const isHidden = ref(true)
    const youtubeHeight = ref(0)
    const descriptionWrapper = ref(null)

    if (props?.assetItem?.youtube) {
      const setYoutubeHeight = () => {
        if (!isHidden.value) {
          const descriptionHeight = descriptionWrapper.value?.clientHeight || 0
          youtubeHeight.value = window.innerWidth > 576
            ? window.innerHeight - descriptionHeight - 32
            : window.innerHeight / 1.5 - descriptionHeight
        }
      }

      onMounted(() => {
        nextTick(function () {
          setTimeout(() => {
            window.addEventListener('resize', setYoutubeHeight, {passive: true})
          }, 0)
        })
      })

      onUpdated(() => {
        setYoutubeHeight()
      })

      onBeforeUnmount(() =>
        window.removeEventListener('resize', setYoutubeHeight)
      )
    }

    const showModal = () => {
      isHidden.value = false
    }

    const hideModal = () => {
      isHidden.value = true
    }

    return {
      isHidden,
      showModal,
      hideModal,
      youtubeHeight,
      descriptionWrapper
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/buttons';

.overlay {
  z-index: $zindex-dropdown + 50;
}

.close-btn {
  top: 0.5rem;
  right: 0.5rem;
  width: 1.5rem;
  height: 1.5rem;
  opacity: $btn-expand-opacity;
}

video,
img {
  object-fit: contain;
  background: $black;
  max-width: 100%;
  max-height: 100%;
  overflow: auto;

  @include media-breakpoint-down(sm) {
    height: 16rem !important;
  }
}

.media-text-container {
  border-left: 0.5rem solid $primary;
  max-width: 45rem;
  margin-left: 9rem;

  @include media-breakpoint-down(lg) {
    max-width: 30rem;
  }

  @include media-breakpoint-down(sm) {
    margin-left: 0;

    h2 {
      font-size: $h4-font-size;
    }
  }
}

.keyboard-focus:focus {
  outline: 2px solid $blue !important;
}

</style>
