<template>
  <section data-test-id="feature-hero-spot" :class="{'container': !fullWidthOpt}">
    <div class="row mx-0">
      <LazyHydrate never>
        <div class="col-12 col-md-6 px-0">
          <div class="d-flex align-items-center justify-content-center w-100 h-100">
            <div v-if="(video && video.src) || youtube"
                 class="position-relative w-100 h-100 d-flex align-items-center"
            >
              <GTMVideo
                v-if="video && video.src"
                playsinline
                controls
                :poster="video.poster"
                data-test-id="feature-hero-spot-video"
                :src="video.src"
                :metadata="video.metadata"
              />
              <YoutubeIframe
                v-else-if="youtube"
                full-height
                :src="youtube"
                data-test-id="feature-hero-spot-youtube"
              />
            </div>
            <ImageSet
              v-else-if="image && image.src"
              :src="image.src"
              data-test-id="feature-hero-spot-image"
              :alt="image.alt"
              :title="image.title"
              :img-width="image.width"
              :img-height="image.height"
              class="w-100 h-100"
            />
          </div>
        </div>
      </LazyHydrate>
      <div class="col-12 col-md-6 px-0 d-flex align-items-center justify-content-center"
           :class="selectedColorVariant.background"
      >
        <div class="container d-flex justify-content-center">
          <div class="row">
            <div class="col-12 p-4 p-lg-6 content-wrapper">
              <LazyHydrate never>
                <div>
                  <header v-if="title">
                    <component :is="htag" :class="[htag, selectedColorVariant.headline]">
                      {{
                        title
                      }}
                    </component>
                  </header>
                  <div v-if="content"
                       :class="selectedColorVariant.headline"
                       class="text-break"
                       v-html="content"
                  />
                </div>
              </LazyHydrate>
              <LazyHydrate when-visible>
                <div v-if="ctas.length" class="d-flex justify-content-md-end flex-column flex-md-row mt-5">
                  <b-button v-for="button in ctas"
                            :key="`feature-hero-spot__button__${button.label}`"
                            :href="button.url"
                            :target="button.target === '_blank_follow' ? '_blank' : button.target"
                            :variant="button.config.variant"
                            :class="[button.config.class]"
                            class="font-condensed d-flex my-2 ml-md-3 justify-content-center align-items-center text-break"
                            data-test-id="feature-hero-spot__button"
                            @click="ga4click($event, button)"
                  >
                    {{ button.label }}
                  </b-button>
                </div>
              </LazyHydrate>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, ref, toRefs, useContext} from '@nuxtjs/composition-api'
import ImageSet from './elements/ImageSet.vue'
import useMounted from '~/helpers/composables/useMounted'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'

export default {
  components: {ImageSet},
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    },
    content: {
      type: String
    },
    asset: {
      type: Object
    },
    fullWidth: {
      type: String
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  setup(props) {
    const {nodeId, variant, cta, asset, fullWidth} = props
    const {content, title} = toRefs(props)
    const {$utils, $regions, $ga4} = useContext()
    const video = ref(null)
    const image = ref(null)
    const youtube = ref(null)
    const variants = [{variant: 'primary'}, {variant: 'outline-primary'}, {variant: 'transparent', class: 'text-primary'}]
    const fullWidthOpt = fullWidth === 'true'
    useMounted(nodeId)

    const ctas = computed(() => {
      return cta?.['@nodes']
        ? cta['@nodes'].map((x, i) => {
          const ctaButton = cta[x].link
          return {
            ...$utils.getLinkField(ctaButton, $regions.current.language.name),
            config: variants[i % variants.length]
          }
        }).reverse()
        : []
    })

    if (asset?.video?.src) {
      video.value = {
        src: $utils.getCloudinaryAsset(asset.video.src, false, {
          quality: 'auto:good',
          fetch_format: 'mp4',
          flags: 'streaming_attachment'
        }),
        poster:
          $utils.getCloudinaryAsset(asset.video.poster) ||
          $utils.getCloudinaryAsset(asset.video.src, false, {
            effect: 'preview',
            resource_type: 'video'
          }),
        metadata: $utils.getCloudinaryAssetMetadata(asset.video.src)?.metadata
      }
    } else if (asset?.img?.src) {
      image.value = {
        src: asset.img.src,
        title: asset.img?.imageTitle,
        alt: asset.img?.imageAlt,
        width: asset.img?.imageWidth || null,
        height: asset.img?.imageHeight || null
      }
    } else if (props?.asset?.youtube) {
      const id = asset.youtube
        .split(asset.youtube.includes('/embed/') ? '/embed/' : 'watch?v=')
        .pop()
      youtube.value = `https://www.youtube.com/embed/${id}`
    }

    const selectedColorVariant = ref(VARIANTS_CLASSES[variant])

    const ga4click = (event, button) => {
      $ga4.events.hero_spot_cta(event, {asset_id: button?.id, header: title.value, text: content.value})
    }

    return {
      selectedColorVariant,
      ctas,
      video,
      image,
      youtube,
      fullWidthOpt,
      ga4click
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

section[data-test-id="feature-hero-spot"] {
  .content-wrapper {
    max-width: 36rem;
  }

  iframe {
    width: 100%;
    aspect-ratio: 16/9;
  }

  video {
    width: 100%;
    aspect-ratio: 3/2;
  }

  .dark-btn.btn-primary {
    color: $black !important;

    &:hover {
      opacity: .9
    }
  }

  .dark-btn.btn-outline-primary {
    color: $white !important;
    background: transparent !important;
    border: 1px solid $white !important;

    &:hover {
      background: $white !important;
      color: $black !important;
    }
  }

  .dark-btn.btn-secondary {
    color: $white !important;
    background: transparent !important;

    &:hover {
      background: $white !important;
      color: $black !important;
    }
  }

  .light-btn.btn-outline-primary {
    background: transparent !important;

    &:hover {
      background: $primary !important;
      color: $white !important;
    }
  }

  .light-btn.btn-secondary {
    background: transparent !important;
    border: none !important;
  }
}

</style>
