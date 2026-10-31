<template>
  <div data-test-id="cloudinary-gallery"
       class="row align-items-center justify-content-center cld-gallery"
       :class="{'cld-gallery-multi': images.length > 1}"
  >
    <div data-test-id="cloudinary-gallery-container" class="col-12">
      <h2 v-if="title" data-test-id="cloudinary-gallery-title">{{ title }}</h2>
      <client-only>
        <div :id="containerId" :data-src="videoUrls" class="position-relative" @click.prevent.stop>
          <span role="button"
                data-test-id="gallery-btn-expand"
                class="btn-expand position-absolute justify-content-center align-items-center pe-none mr-2 mt-2 p-1 z-index-3 opacity-0 d-flex transition-base"
                :style="{
                  backgroundColor: customStyles.buttonBackground || 'white',
                  color: customStyles.buttonColor || 'inherit',
                  visibility: customStyles.buttonVisibility || 'visible'
                }"
          >
            <b-icon icon="expand" />
          </span>
        </div>
      </client-only>
    </div>
  </div>
</template>

<script>
/* global $nuxt */
import {
  computed,
  defineComponent,
  inject,
  onBeforeUnmount,
  onMounted,
  ref,
  useContext,
  useMeta
} from '@nuxtjs/composition-api'
import GALLERY_OPTIONS from '~/constants/galleryConfiguration'
import useSchema from '~/helpers/composables/useSchema'
import useCloudinary from '~/helpers/composables/useCloudinary'

export default defineComponent({
  props: {
    containerId: {
      type: String,
      default() {
        return 'cgallery-' + Math.random().toString(16).substr(2, 8)
      }
    },
    cloudName: {
      type: String,
      default: null
    },
    images: {
      type: Array,
      required: true
    },
    title: {
      type: String
    },
    configOverrides: {
      type: Object,
      default: () => ({})
    },
    customStyles: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props, {emit}) {
    /* WARNING!
    * You must destroy cloudinary widget before unmount component - call async destroy(), otherwise it will cause a memory leak and browser crash.
    * onBeforeUnmount doesn't work
    * */
    const {containerId, cloudName, images} = props
    const {$utils, $regions, $config} = useContext()
    const videoIds = ref([])
    const assetsSchema = ref([])
    const {createVideoObject, createImageObject} = useSchema({}, $utils, $regions, $config)
    const shouldPreload = inject('shouldPreload')
    const videoUrls = ref([])
    const galleryItems = computed(() => {
      if (!images || images.length === 0) {
        return []
      }

      return images.map((x) => {
        if (x.field === 'spin' && x.tag) {
          const asset = {
            tag: x.tag,
            mediaType: 'spin',
            altText: x.imageAlt || x.imageTitle || null,
            thumbnailTransformation: {crop: 'fill'}
          }
          return asset
        }

        if (x.src) {
          let posterMeta = null
          try {
            posterMeta = $utils.getCloudinaryAssetMetadata(x?.poster)
          } catch {}

          try {
            const {publicId, metadata} = $utils.getCloudinaryAssetMetadata(x.src)
            if (!publicId) {
              return null
            }

            const {getGalleryPlaceholders} = useCloudinary()
            const asset = {
              publicId,
              mediaType: $utils.getCloudinaryAssetMediaType(metadata),
              altText: x.imageAlt || x.imageTitle || metadata?.context?.alt || metadata?.context?.caption || metadata?.title || metadata?.description || null,
              thumbnailTransformation: {crop: 'fill'}
            }

            if ($utils.getCloudinaryAssetMediaType(metadata) === 'video') {
              videoIds.value.push(publicId)
              assetsSchema.value.push(createVideoObject({
                video: $utils.getCloudinaryAsset(x.src, false),
                poster: $utils.getCloudinaryAsset(x.poster) || $utils.getCloudinaryAsset(x.src, false, {effect: 'preview', resource_type: 'video'}),
                metadata
              }))

              if (metadata.bytes > 100000000) { // stream video when larger than 100MB,
                asset.videoPlayerSource = {
                  sourceTypes: ['hls'], // TODO: JAR-1875: disable dash temporarily
                  sourceTransformation: {
                    hls: [{streaming_profile: 'full_hd'}] // use sp_full_hd/m3u8 transformation
                    // dash: [{streaming_profile: 'full_hd'}] // use sp_full_hd/mpd transformation (MPEG-DASH)
                  }
                }
              }

              if (posterMeta?.publicId) {
                const overlay = encodeURI(posterMeta?.publicId.replace(/\//g, ':'))
                asset.thumbnailTransformation = {overlay, crop: 'fill'}
                asset.videoPlayerSource = asset.videoPlayerSource || {}
                asset.videoPlayerSource.poster = {publicId: posterMeta?.publicId, height: '1200', transformation: {fetch_format: 'jpg'}}
              }

              asset.transformation = {background: '#000000', crop: 'pad', duration: '100p'}
            } else {
              assetsSchema.value.push(createImageObject(x))

              const {previewUrl, thumbnailUrl} = getGalleryPlaceholders(x.src, publicId)

              asset.previewUrl = previewUrl
              asset.thumbnailUrl = thumbnailUrl
              asset.transformation = metadata.width > 1200 && metadata.height > 800
                ? {background: 'transparent', crop: 'pad', gravity: 'center'}
                : {background: 'transparent'}
            }

            return asset
          } catch (error) {
            return null
          }
        }

        return null
      }).filter((x) => {
        const isValid = x && x.mediaType !== 'raw'
        return isValid
      })
    })

    const toggleElement = (el, hide) => {
      if (hide) {
        el.classList.replace('d-flex', 'd-none')
      } else {
        el.classList.replace('d-none', 'd-flex')
      }
    }

    const injectBtn = () => {
      setTimeout(() => {
        const $el = document.querySelector(`#${containerId} > .btn-expand`)
        const $viewerWrap = document.querySelector(`#${containerId} .viewer-wrap`)
        if ($el && $viewerWrap && images.length > 1) {
          $viewerWrap.append($el)
          if (galleryItems.value.length && galleryItems.value[0].mediaType !== 'image') { toggleElement($el, true) }
          setTimeout(() => {
            $el.classList.remove('opacity-0')
          }, 10)
        }
      }, 100)
    }

    let widget = {}

    const initGallery = async () => {
      if (process.client && window) {
        widget = window.cloudinary.galleryWidget({
          ...GALLERY_OPTIONS,
          ...props.configOverrides,
          carouselStyle: images.length > 1 ? 'thumbnails' : 'none',
          container: `#${containerId}`,
          cloudName: cloudName || $config.CLOUDINARY_CLOUDNAME,
          ...($regions.current.name === 'cn' && {secureDistribution: process.env.CLOUDINARY_DOMAIN_CN}),
          mediaAssets: galleryItems.value
        })

        await widget.render()
        injectBtn()

        // NGB-1412 hack to close after backdrop click
        widget.on('zoomin', () => {
          setTimeout(() => {
            const popup = document.querySelector('body > div[role=dialog][data-test=popup][class^=css-]')
            popup.addEventListener('click', (e) => {
              if (e.target.dataset.test === 'img-zoom-wrap' && popup) { popup.querySelector(`button[shape=none][size="${GALLERY_OPTIONS.zoomPopupProps.buttonSize}"][class^=css-]`).click() }
            })
          }, 100) // timeout needed because event is fired BEFORE popup is injected into DOM
        })

        widget.on('thumbclick', (data) => {
          const $el = document.querySelector(`#${containerId} .btn-expand`)
          if ($el) {
            toggleElement($el, videoIds.value.includes(data.label))
          }
          emit('widget-click', 'gallery click', data.label)
        })

        widget.on('zoomin', (data) => {
          emit('widget-click', 'expand image', data.label)
        })

        widget.on('thumbnext', (data) => {
          emit('widget-click', 'arrow click', data.label)
        })

        widget.on('thumbprev', (data) => {
          emit('widget-click', 'arrow click', data.label)
        })

        widget.on('viewernext', (data) => {
          emit('widget-click', 'arrow click', data.label)
        })

        widget.on('viewerprev', (data) => {
          emit('widget-click', 'arrow click', data.label)
        })

        // just in case btn isn't injected yet
        widget.on('mouseenter', () => {
          injectBtn()
        })

        if (atob) {
          images.forEach((image) => {
            if (image && image.src) {
              const base64EncodedString = image?.src?.split('#')?.[1]
              const jsonString = atob(base64EncodedString)
              const imageInfo = JSON.parse(jsonString)
              if (imageInfo?.resource_type === 'video') {
                videoUrls.value.push(imageInfo.url)
              }
            }
          })

          videoUrls.value = videoUrls.value.join(',')
        }
      }
    }

    if (process.client && window) {
      onMounted(() => {
        if (window.cloudinary) {
          initGallery()
        } else {
          window?.$nuxt?.$on('cloudinary-loaded', initGallery)
        }
      })

      onBeforeUnmount(async () => {
        window?.$nuxt?.$off('cloudinary-loaded')
        if (widget.destroy) {
          await widget.destroy()
        }
      })
    }

    // preloading images upfront
    const preloadedImages = ref([...(galleryItems.value?.slice(0, 4).map(itm => itm.thumbnailUrl)), galleryItems.value?.[0]?.previewUrl].filter(x => x))
    useMeta({
      ...(shouldPreload && preloadedImages.value.length && {
        link: preloadedImages.value.map((_src) => {
          return {
            rel: 'preload',
            as: 'image',
            fetchpriority: 'highest',
            href: _src
          }
        })
      })
    })

    const destroy = async () => {
      await widget.destroy()
    }

    return {
      galleryItems,
      assetsSchema,
      videoUrls,
      destroy
    }
  },
  head: {
    script: [{
      hid: 'cld-gallery',
      vmid: 'cld-gallery',
      once: true,
      type: 'text/javascript',
      src: '//product-gallery.cloudinary.com/all.js',
      body: true,
      defer: true,
      callback() { $nuxt?.$emit('cloudinary-loaded') }
    }]
  },
  jsonld() {
    return this.assetsSchema.filter(x => x)
  }
})
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';

*[data-test="fade-item-wrap"] {
  border: 1px solid $light;
}

.btn-expand {
  @include absolute-top-right;
  font-size: large;
}
</style>

<style lang="scss">
.cld-gallery div[data-test="gallery-spacer"] {
    display: none;
}

.cld-gallery,
body > div[role=dialog][data-test=popup][class^=css-] {
  button {
    path {
      transition: .2s;
    }
    &:hover path {
      fill: #ff3600;
      stroke: transparent !important;
    }
  }
}
</style>
