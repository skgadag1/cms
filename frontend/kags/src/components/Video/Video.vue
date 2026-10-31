<template>
  <b-col cols="12" :md="componentConfig.halfWidth === 'true' ? '6' : '12'" class="mb-5">
    <b-container class="box h-100">
      <b-row align-content="stretch">
        <div class="box w-100">
          <div class="py-3 text-center">
            <b-link @click.prevent="$refs.videoModal.show()">
              <b-img :src="translatedConfig('videoImg')" class="mb-4" fluid />
            </b-link>
            <h2>{{ translate('title') }}</h2>
            <p>{{ translate('subtitle') }}</p>
            <b-button v-if="componentConfig.buttonLabel"
                      variant="primary"
                      size="lg"
                      class="font-condensed text-large"
                      @click.prevent="$refs.videoModal.show()"
            >
              <b-icon icon="play" class="me-2" />
              {{ translate('buttonLabel') }}
            </b-button>
          </div>
        </div>
      </b-row>

      <b-modal id="video-modal"
               ref="videoModal"
               size="xl"
               teleport-disabled
               centered
               no-footer
               modal-class="modal-transparent"
               :lazy="false"
               @shown="playVideo"
               @hide="pauseVideo"
      >
        <div ref="videoObject" class="ratio ratio-16x9">
          <video controls>
            <source :src="translatedConfig('videoUrl')">
            {{ translate('videoError') }}
          </video>
        </div>
      </b-modal>
    </b-container>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
export default {
  setup() { return useLabels() },
  data() {
    return {
      componentConfig: this.config
    }
  },
  methods: {
    pauseVideo() {
      const vid = this.$refs['videoObject'].querySelector('video')
      if (vid) vid.pause()
    },
    playVideo() {
      const vid = this.$refs['videoObject'].querySelector('video')
      if (vid) vid.play()
    }
  }
}
</script>

<style lang="scss" scoped>
#video-modal {
  video {
    outline: none;
  }
}
</style>
