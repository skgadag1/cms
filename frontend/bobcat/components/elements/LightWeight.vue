<template>
  <article class="mb-5" data-test-id="light-weight">
    <div class="position-relative">
      <div data-test-id="light-weight-container"
           class="position-relative w-100 mb-1 bg-asset transition-base"
           :class="{'multi-columns' : !isSingleColumn}"
      >
        <div class="mw-100">
          <div data-test-id="asset-wrapper"
               :data-src="assetItem ? (assetItem.video || assetItem.youtube || assetItem.img) : ''"
               class="asset-wrapper position-relative bg-white"
          >
            <button v-if="assetItem.video || assetItem.youtube"
                    class="d-flex justify-content-center align-items-center position-absolute border-0 text-white h1 bg-transparent video-btn z-index-1"
            >
              <b-icon icon="play-circle-regular" @click.native="(e) => {ga4click(e); showModal()}" />
            </button>
            <ImageSet class="position-absolute w-100 h-100"
                      :src="assetItem.poster"
                      :alt="assetItem.alt"
                      :title="assetItem.title"
                      :img-width="assetItem.imgWidth"
                      :img-height="assetItem.imgHeight"
                      :is-img="assetItem.isImg"
                      :data-test-id="`light-weight-${assetItem.img ? 'img' : assetItem.youtube ? 'youtube' : 'video'}`"
                      :cloudinary-options="assetItem.imageTransformation"
                      @click.native="(e) => {ga4click(e); showModal()}"
            />
          </div>
        </div>
      </div>
      <span role="button"
            aria-label="Expand"
            data-test-id="light-weight-btn-expand"
            class="btn-expand position-absolute d-flex justify-content-center align-items-center bg-white keyboard-focus"
            aria-expanded="false"
            tabindex="0"
            @click="(e) => {ga4click(e); showModal()}"
            @keydown.enter.prevent="(e) => { ga4click(e); showModal() }"
            @keydown.space.prevent="(e) => { ga4click(e); showModal() }"
      ><b-icon
        icon="expand"
      /></span>
    </div>
    <component :is="htag" v-if="title" class="mt-3 mb-1" :class="{'text-white' : isDarkMode}">{{ title }}</component>
    <div ref="cardParagraph" :class="{'text-white' : isDarkMode}" data-test-id="card-paragraph" v-html="paragraph" />
    <MediaPreview v-if="assetItem.img || assetItem.video"
                  ref="mediaPreview"
                  :title="title"
                  :paragraph="paragraph"
                  :asset-item="assetItem"
    />
  </article>
</template>

<script>
import {onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useSchema from '~/helpers/composables/useSchema'

function getLinkToVideo(id, params = {}) {
  const url = new URL(`https://www.youtube.com/embed/${id}`)

  for (const param of Object.entries(params)) {
    url.searchParams.append(...param)
  }

  return url.toString()
}

export default {
  props: {
    title: {
      type: String
    },
    asset: {
      type: Object
    },
    paragraph: {
      type: String
    },
    isSingleColumn: {
      type: Boolean
    },
    isDarkMode: {
      type: Boolean
    },
    htag: {
      type: String,
      default: 'h4'
    }
  },
  setup(props) {
    const {$utils, $cloudinary, $regions, $ga4, $config} = useContext()
    const {asset, title} = props
    const mediaPreview = ref(null)
    const video = ref(null)
    const assetItem = {}
    const cardParagraph = ref(null)

    const assetsSchema = ref([])
    const {createVideoObject, createImageObject} = useSchema({}, $utils, $regions, $config)

    if (asset?.field === 'img' && asset?.img?.src) {
      const {metadata} = $utils.getCloudinaryAssetMetadata(asset.img.src)
      Object.assign(assetItem, {
        poster: asset.img.src,
        img: asset.img.src,
        title: asset.img.imageTitle || metadata?.context?.caption || metadata?.title,
        alt: asset.img.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
        imgWidth: asset.img.imageWidth || null,
        imgHeight: asset.img.imageHeight || null,
        asset_id: metadata?.asset_id
      })

      assetsSchema.value.push(createImageObject(assetItem))
    } else if (asset?.field === 'youtube' && asset?.youtube) {
      const id = asset.youtube.split(asset.youtube.includes('/embed/') ? '/embed/' : 'watch?v=').pop()
      const videoItm = getLinkToVideo(id, {
        autoplay: 1
      })
      const posterUrl = `https://www.youtube.com/embed/${id}`
      Object.assign(assetItem, {
        youtube: true,
        video: videoItm,
        isImg: false,
        poster: $utils.adjustDomain($cloudinary.image.url(posterUrl, {type: 'youtube'})),
        imageTransformation: {type: 'youtube'},
        asset_id: id
      })
    } else if (asset?.field === 'video' && asset?.video?.src) {
      const {metadata} = $utils.getCloudinaryAssetMetadata(asset.video.src)
      Object.assign(assetItem, {
        video: $utils.getCloudinaryAsset(asset.video.src, false),
        poster: asset.video.poster || ($utils.getCloudinaryAsset(asset.video.poster || asset.video.src, false, {
          effect: 'preview',
          resource_type: 'video'
        })),
        isImg: !!asset.video.poster,
        title: metadata?.context?.caption || metadata?.title,
        alt: metadata?.context?.alt || metadata?.description,
        asset_id: metadata?.asset_id
      })

      assetsSchema.value.push(createVideoObject({
        ...assetItem,
        poster: $utils.getCloudinaryAsset(asset.video.poster) || assetItem.poster,
        metadata
      }))
    }

    const showModal = () => {
      if (!inEditorEdit && mediaPreview.value?.showModal) {
        mediaPreview.value.showModal()
      }
    }

    const ga4click = (event, mediaType) => {
      const obj = mediaType ? {mediaType} : {...assetItem}
      $ga4?.events?.media_click(event, {...obj, title})
    }

    onMounted(() => {
      if (process.client && cardParagraph.value) {
        const links = cardParagraph.value?.querySelectorAll('a')
        links?.forEach((l) => {
          l?.addEventListener('click', event => ga4click(event, 'link'))
        })
      }
    })

    onBeforeUnmount(() => {
      const links = cardParagraph.value?.querySelectorAll('a')
      links?.forEach(l => l?.removeEventListener('click', ga4click))
    })

    return {
      assetItem,
      assetsSchema,
      mediaPreview,
      showModal,
      video,
      ga4click,
      cardParagraph
    }
  },
  jsonld() {
    return this.assetsSchema.filter(x => x)
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.bg-asset {
  filter: drop-shadow(0px 2px 4px rgba(12, 24, 30, 0.2));
  cursor: pointer;

  &:hover {
    opacity: .9;
  }

  video, img {
    object-fit: cover;
  }

  .asset-wrapper {
    padding-bottom: 66.6%; // 3:2 ratio
  }
}

.video-btn {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 4rem;

  &:hover {
    color: $primary !important;
  }
}

.btn-expand {
  top: .5rem;
  right: .5rem;
  width: 1.5rem;
  height: 1.5rem;

  i {
    width: 18px;
    height: 19px;
  }
}

.keyboard-focus:focus {
  outline: 2px solid $blue !important;
}

</style>
