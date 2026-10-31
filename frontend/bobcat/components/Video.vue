<template>
  <section class="my-3" data-test-id="video" :class="{'bg-gray-lighter' : grayBgOpt}">
    <LazyHydrate when-visible>
      <div class="container">
        <div class="row justify-content-center">
          <div data-test-id="video-container"
               class="position-relative w-100 bg-asset transition-base col-12"
               :class="{'col-sm-8 col-lg-6' : !fullWidthOpt}"
          >
            <div v-if="assetItem && assetItem.video" class="mw-100">
              <div class="asset-wrapper position-relative">
                <video v-if="assetItem.video"
                       playsinline
                       :data-src="assetItem.video"
                       controls
                       class="position-absolute w-100 h-100"
                       :poster="assetItem.poster"
                       data-test-id="video-player"
                >
                  <source :src="assetItem.video" type="video/mp4">
                  Your browser does not support HTML5 video.
                </video>
              </div>
            </div>
            <div v-else-if="assetItem && assetItem.youtube" class="mw-100">
              <div class="asset-wrapper position-relative">
                <div class="position-absolute w-100 h-100">
                  <YoutubeIframe
                    v-if="assetItem.youtube"
                    data-test-id="video-youtube"
                    :src="assetItem.youtube"
                    full-height
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import useSchema from '~/helpers/composables/useSchema'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    asset: {
      type: Object,
      required: true
    },
    fullWidth: {
      type: String
    },
    grayBg: {
      type: String
    }
  },
  setup(props) {
    const {$utils, $regions, $config} = useContext()
    const {asset, fullWidth, grayBg} = props
    const assetItem = ref({})
    const assetMetadata = ref({})
    const fullWidthOpt = fullWidth === 'true'
    const grayBgOpt = grayBg === 'true'
    const videoSchema = ref(null)
    useMounted(props.nodeId)

    if (asset?.video) {
      Object.assign(assetItem.value, {
        video: $utils.getCloudinaryAsset(asset.video, false),
        poster: $utils.getCloudinaryAsset(asset.poster) || $utils.getCloudinaryAsset(asset.video, false, {effect: 'preview', resource_type: 'video'})
      })
      assetMetadata.value = $utils.getCloudinaryAssetMetadata(asset.video)
    } else if (asset?.youtube) {
      let id = asset.youtube.split(asset.youtube.includes('/embed/') ? '/embed/' : 'watch?v=').pop()
      if (id.includes('&t=')) { id = id.replace('&t=', '&start=') }
      if (id.includes('%LANG%')) { id = id.replaceAll('%LANG%', $regions.current.language.name) }

      Object.assign(assetItem.value, {
        youtube: `https://www.youtube.com/embed/${id}`
      })
    }

    const {createVideoObject} = useSchema({}, $utils, $regions, $config)

    videoSchema.value = createVideoObject({...assetItem.value, ...assetMetadata.value})

    return {
      assetItem,
      assetMetadata,
      fullWidthOpt,
      grayBgOpt,
      videoSchema
    }
  },
  jsonld() {
    return this.videoSchema
  }
}
</script>

<style lang="scss" scoped>
.bg-asset {
  video {
    object-fit: cover;
  }

  .asset-wrapper {
    padding-bottom: 66.6%;
  }
}

section[data-test-id='video'] {
  ::v-deep iframe {
    width: 100%;
  }
}
</style>
