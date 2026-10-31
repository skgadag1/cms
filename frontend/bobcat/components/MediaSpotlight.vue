<template>
  <LazyHydrate never :trigger-hydration="Boolean(image || video || youtube)">
    <section data-test-id="media-spotlight" :class="selectedColorVariant.background">
      <div class="container">
        <div class="row">
          <div class="col-12 col-md-6 d-md-flex align-items-center mb-4 mb-md-0 justify-content-center" :class="currentVariant.asset">
            <div class="w-100 mt-md-0">
              <div v-if="(video && video.src) || youtube"
                   class="position-relative w-100 d-flex align-items-center"
                   :class="{'h-100' : youtube}"
              >
                <GTMVideo
                  v-if="video && video.src"
                  playsinline
                  controls
                  :poster="video.poster"
                  data-test-id="media-spotlight-video"
                  :src="video.src"
                  :metadata="video.metadata"
                />
                <YoutubeIframe
                  v-else-if="youtube"
                  :src="youtube"
                  data-test-id="media-spotlight-youtube"
                />
              </div>
              <ImageSet
                v-else-if="image && image.src"
                :src="image.src"
                data-test-id="media-spotlight-image"
                :alt="image.alt"
                :title="image.title"
                :img-width="image.width"
                :img-height="image.height"
              />
            </div>
          </div>

          <div class="col-12 col-md-6 media-spotlight-content" :class="currentVariant.content">
            <div
              class="media-spotlight-content-inner d-flex flex-column justify-content-center h-100"
              :class="currentVariant.padding"
            >
              <div data-test-id="cta-wrapper" class="media-spotlight-heading mb-3 mb-md-0">
                <div v-if="evolutionTitle" class="evolution-title" :class="selectedColorVariant.headline" v-html="evolutionTitle" />
                <component :is="htag" v-else-if="title" :class="[htag, selectedColorVariant.headline]" class="mb-0">
                  {{ title }}
                </component>
                <div v-if="evolutionSubtitle" class="mb-0" :class="selectedColorVariant.headline" v-html="evolutionSubtitle" />
                <p v-else-if="subtitle" class="h4 mb-0" :class="selectedColorVariant.headline">{{ subtitle }}</p>
              </div>
              <div class="media-spotlight-body">
                <div v-if="description"
                     class="text-break mt-4"
                     :class="selectedColorVariant.headline"
                     v-html="addYoutubeNoCookie(description)"
                />
                <div class="d-flex flex-column flex-md-row mt-4">
                  <div v-if="ctas.length">
                    <DynamicButton
                      v-for="(button,index) in ctas"
                      :key="`evolution-section-text-button-${index}`"
                      :variant="button.variant"
                      :color="button.color"
                      :href="button.link"
                      :target="button.target === '_blank_follow' ? '_blank' : button.target"
                      class="mr-0 mr-md-3 mt-3 btn-mobile text-uppercase"
                      data-test-id="cta-button"
                      @click="ga4click"
                    >
                      {{ button.title }}
                    </DynamicButton>
                  </div>
                  <template v-else-if="linkField && linkField.label">
                    <b-button
                      v-if="linkField['@name'] === 'brochure'"
                      :href="getBrochureUrl(linkField.filePath)"
                      variant="primary"
                      size="lg"
                      :class="selectedColorVariant.buttonBg"
                      @click.prevent="(e) => { ga4click(e); download(linkField.filePath); }"
                    >
                      {{ linkField.label }}
                    </b-button>

                    <b-button
                      v-else
                      :href="linkField.url"
                      :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
                      :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
                      variant="primary"
                      size="lg"
                      :class="selectedColorVariant.buttonBg"
                      @click="ga4click"
                    >
                      {{ linkField.label }}
                    </b-button>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {computed, ref, useContext} from '@nuxtjs/composition-api'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    subtitle: {
      type: String
    },
    evolutionTitle: {
      type: String
    },
    evolutionSubtitle: {
      type: String
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    },
    description: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    link: {
      type: Object
    },
    variant: {
      type: String,
      required: true
    },
    asset: {
      type: Object
    },
    colorVariant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props, context) {
    const {link, asset, variant, title, subtitle, colorVariant, cta} = props
    const {$regions, $utils, $ga4, $config} = useContext()

    const ctaSource = resolveLegacyListSource(
      cta, context.attrs, 'cta', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const image = ref(null)
    const youtube = ref(null)
    const video = ref(null)
    const linkField = computed(() => {
      switch (link?.field) {
      case 'brochure':
        return {field: 'brochure', ...link.brochure}
      case 'page':
      case 'custom':
      case 'localAnchor':
        return $utils.getLinkField(link, $regions.current.language.name)
      }
      return null
    })
    useMounted(props.nodeId)

    const selectedColorVariant = ref(VARIANTS_CLASSES[colorVariant])

    const getBrochureUrl = (url) => {
      return $utils.getCloudinaryDocument(url)
    }

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const ctas = computed({
      set: () => {
      },
      get: () => {
        return ctaSource?.['@nodes']
          ? ctaSource['@nodes'].map((x) => {
            const ctaButton = ctaSource[x].link
            const linkField = $utils.getLinkField(ctaButton, $regions.current.language.name)
            return {
              variant: ctaSource[x]?.field,
              color: ctaSource[x]?.color,
              link: linkField?.url || '',
              title: linkField?.label || '',
              target: linkField?.target || '_self',
              config: {
                class: ctaSource[x]?.variant || 'bg-primary text-white'
              }
            }
          })
          : []
      }
    })

    const currentVariant = computed(() => {
      if (variant === 'left') {
        return {asset: 'order-2 order-md-1', content: 'order-3 order-md-2', padding: 'media-spotlight-padding-left'}
      } else {
        return {asset: 'order-2 order-md-2', content: 'order-3 order-md-1', padding: 'media-spotlight-padding-right'}
      }
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

    const ga4click = (event) => {
      $ga4.events.media_spotlight_cta_click(props.evolutionTitle?.replace(/<[^>]*>/g, '') || title, props.evolutionSubtitle?.replace(/<[^>]*>/g, '') || subtitle, event?.target?.innerText || undefined)
    }

    return {
      ctas,
      linkField,
      image,
      video,
      youtube,
      currentVariant,
      getBrochureUrl,
      download,
      addYoutubeNoCookie,
      ga4click,
      selectedColorVariant
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

section[data-test-id='media-spotlight'] {
  padding: 2rem 0 !important; // 32px vertical, 0 horizontal

  // AV-3205: match prod — do NOT zero the container's horizontal padding.
  // Zeroing it made local Media Spotlight wider (content 1140) than prod
  // (content 1100 with 10px padding). Inherit the global `body .container`
  // padding (0.625rem = 10px at md+) so local aligns with prod.

  // AV-3205: match prod — do NOT zero the Bootstrap column gutters. Zeroing
  // them made the text content sit flush against the edge in local. Keeping
  // the standard column padding (gutters) insets the text like prod so it no
  // longer overlaps the container edge.

  @include media-breakpoint-down(lg) {
    padding: 2rem 0 !important;
  }

  @include media-breakpoint-down(sm) {
    padding: 2rem 0 !important;
  }

  .media-spotlight-padding-left {
    padding-left: 1.5rem; // 24px
    padding-right: 0;
  }

  .media-spotlight-padding-right {
    padding-right: 1.5rem; // 24px
    padding-left: 0;
  }

  @media (max-width: 767.98px) {
    .media-spotlight-content,
    .media-spotlight-content-inner {
      display: contents !important;
    }

    .media-spotlight-heading,
    .media-spotlight-body {
      flex: 0 0 100%;
      max-width: 100%;
      padding-left: $grid-gutter-width / 2;
      padding-right: $grid-gutter-width / 2;
      position: relative;
      width: 100%;
    }

    .media-spotlight-heading {
      order: 1;
    }

    .media-spotlight-body {
      order: 3;
    }

    .media-spotlight-padding-left,
    .media-spotlight-padding-right {
      padding-left: 0;
      padding-right: 0;
    }
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

  iframe {
    width: 100%;
    aspect-ratio: 16/9;
  }

  .evolution-title {
    h1, h2, h3, h4, h5, h6 {
      font-size: 1rem; // autosize heading based on child span (ckeditor inserts span)
      margin: 0;
      line-height: 1.375; // correct line-height per Figma (66px/48px)
    }
  }

  .text-break {
    p {
      margin-bottom: 0 !important;
    }
  }

  // Remove paragraph margin inside subtitle rendered via v-html
  .mb-0 p {
    margin-bottom: 0 !important;
  }
}
</style>
