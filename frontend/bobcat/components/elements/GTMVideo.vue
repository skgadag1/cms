<template>
  <video
    v-if="src"
    ref="videoPlayer"
    :playsinline="playsinline"
    :data-src="src"
    :controls="controls"
    :autoplay="autoplay"
    :loop="loop"
    :poster="poster"
    :data-test-id="dataTestId"
    :class="stylesClass"
    onloadstart="this.volume=0.7"
  >
    <source :src="src" type="video/mp4">
    Your browser does not support HTML5 video.
  </video>
</template>

<script>
import {onMounted, ref, useContext} from '@nuxtjs/composition-api'
import useSchema from '~/helpers/composables/useSchema'
import useConcepts from '~/helpers/composables/useDataLayer'

export default {
  props: {
    playsinline: {
      type: Boolean,
      default: true
    },
    controls: {
      type: Boolean,
      default: true
    },
    autoplay: {
      type: Boolean,
      default: false
    },
    loop: {
      type: Boolean,
      default: false
    },
    poster: {
      type: String
    },
    src: {
      type: String,
      required: true
    },
    metadata: {
      type: Object
    },
    stylesClass: {
      type: String
    },
    dataTestId: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const {$utils, $regions, $config} = useContext()
    const videoPlayer = ref(null)
    const progress = ref(0)
    const playCounter = ref(0)
    const pauseCounter = ref(0)
    const VIDEO_PLAYER = 'gtm.custom_video_player'
    const {setDataLayer} = useConcepts()
    const {poster, src, metadata} = props
    const videoSchema = ref(null)
    const {createVideoObject} = useSchema({}, $utils, $regions, $config)

    videoSchema.value = createVideoObject({video: src, poster, metadata})

    if (process.client) {
      onMounted(() => {
        if (videoPlayer.value) {
          if (window?.dataLayer) {
            window.dataLayer.push(VIDEO_PLAYER)
          }

          videoPlayer.value.addEventListener('play', () => {
            playCounter.value = playCounter.value + 1
            setDataLayer(VIDEO_PLAYER, {
              video_action: {playCounter: playCounter.value}
            })
            setDataLayer(VIDEO_PLAYER, {video_action: {isPlaying: true}})
          })

          videoPlayer.value.addEventListener('pause', () => {
            pauseCounter.value = pauseCounter.value + 1
            setDataLayer(VIDEO_PLAYER, {
              video_action: {pauseCounter: pauseCounter.value}
            })
            setDataLayer(VIDEO_PLAYER, {video_action: {isPlaying: false}})
          })

          videoPlayer.value.addEventListener('timeupdate', () => {
            const p = Math.round(
              (videoPlayer.value.currentTime / videoPlayer.value.duration) * 100
            )
            if (p > 0 && p <= 25 && progress.value !== 25) {
              progress.value = 25
              setDataLayer(VIDEO_PLAYER, {
                video_action: {progress: `${progress.value}%`}
              })
            } else if (p > 25 && p <= 50 && progress.value !== 50) {
              progress.value = 50
              setDataLayer(VIDEO_PLAYER, {
                video_action: {progress: `${progress.value}%`}
              })
            } else if (p > 50 && p <= 75 && progress.value !== 75) {
              progress.value = 75
              setDataLayer(VIDEO_PLAYER, {
                video_action: {progress: `${progress.value}%`}
              })
            } else if (p > 75 && p <= 100 && progress.value !== 100) {
              progress.value = 100
              setDataLayer(VIDEO_PLAYER, {
                video_action: {progress: `${progress.value}%`}
              })
            }
          })
        }
      })
    }

    return {
      videoPlayer,
      videoSchema
    }
  },
  jsonld() {
    return this.videoSchema
  }
}
</script>
