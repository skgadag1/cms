<template>
  <section ref="observerTarget" :class="selectedColorVariant.background" class="evolution-section-text">
    <LazyHydrate when-visible>
      <div v-if="useShowMore !== 'true'" :id="id" class="container py-5">
        <div class="row" :class="selectedAlignmentVariant">
          <transition :name="animationEnabled === 'true' ? 'slide-top-right' : ''">
            <div v-show="isVisible" class="col-12" :class="[selectedColorVariant.headline]" v-html="addYoutubeNoCookie(content)" />
          </transition>

          <div class="evolution-section-text-buttons col-12 d-flex flex-column flex-md-row">
            <transition :name="animationEnabled === 'true' ? 'slide-bottom-left' : ''">
              <div v-if="ctas.length" v-show="isVisible" class="mt-5">
                <DynamicButton
                  v-for="(button,index) in ctas"
                  :key="`evolution-section-text-button-${index}`"
                  :variant="button.variant"
                  :color="button.color"
                  :href="button.link"
                  :target="button.target === '_blank_follow' ? '_blank' : button.target"
                  class="mr-0 mr-md-3 mt-3 mt-lg-0 text-uppercase"
                  data-test-id="cta-button"
                  @click="handleCtaClick($event, button)"
                >
                  {{ button.title }}
                  <b-icon v-if="button.variant === BUTTON_NAMES.LINEAR" icon="chevron-right" class="mt-1" />
                </DynamicButton>
              </div>
            </transition>
          </div>
        </div>
      </div>
      <EvolutionCollapsibleContent v-else
                                   :labels="labels"
                                   :is-truncated="true"
                                   :content-wrapper="contentWrapper"
                                   :selected-color-variant="selectedColorVariant"
                                   :content="addYoutubeNoCookie(content)"
                                   :variant="variant"
                                   :class="selectedAlignmentVariant"
      >
        <div class="container">
          <div class="collapsible-inner evolution-section-text-buttons d-flex flex-column flex-md-row">
            <div v-if="ctas.length" class="mt-5">
              <DynamicButton
                v-for="(button,index) in ctas"
                :key="`evolution-section-text-button-${index}`"
                :variant="button.variant"
                :color="button.color"
                :href="button.link"
                :target="button.target === '_blank_follow' ? '_blank' : button.target"
                class="mr-0 mr-md-3 mt-3 mt-lg-0 text-uppercase"
                data-test-id="cta-button"
                @click="handleCtaClick($event, button)"
              >
                {{ button.title }}
                <b-icon v-if="button.variant === BUTTON_NAMES.LINEAR" icon="chevron-right" class="mt-1" />
              </DynamicButton>
            </div>
          </div>
        </div>
      </EvolutionCollapsibleContent>
    </LazyHydrate>
  </section>
</template>

<script>
import {computed, onMounted, ref, useContext, useFetch, useRouter, watch} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '~/constants/buttons'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import useMedia from '~/helpers/composables/useMedia'
import {useElementVisibility} from '~/helpers/composables/useElementVisibility'
import {ALIGNMENT_CLASSES, LEFT_ALIGNMENT, VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    content: {
      type: String
    },
    id: {
      type: String
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    },
    animationEnabled: {
      type: String
    },
    useShowMore: {
      type: String
    },
    sectionAlignment: {
      type: String,
      default: LEFT_ALIGNMENT
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props, context) {
    const {cta, animationEnabled} = props
    const {$services, $utils, $regions, $config} = useContext()

    const ctaSource = resolveLegacyListSource(
      cta, context.attrs, 'cta', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const router = useRouter()
    const {updateMediaElements} = useMedia()
    const labels = ref({})
    const contentWrapper = ref(null)
    const {isVisible, observerTarget, checkVisibility} = useElementVisibility(animationEnabled === 'true')

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/%region%/common/labels/functional'])
    })

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

    // Same-page parameter-based anchors (e.g. ?facets=...#models) must go through Vue Router
    // so listing components can apply filters; plain href clicks are intercepted and only scroll.
    const handleCtaClick = (event, button) => {
      if (!button?.link) {
        return
      }
      const target = button.target === '_blank_follow' ? '_blank' : button.target || '_self'
      if (target !== '_self') {
        return
      }
      event.preventDefault()
      try {
        const url = new URL(button.link, window.location.href)
        if (url.origin === window.location.origin) {
          router.push({
            path: url.pathname,
            query: Object.fromEntries(url.searchParams),
            hash: url.hash
          }).catch(() => {})
        } else {
          window.location.href = button.link
        }
      } catch (e) {
        window.location.href = button.link
      }
    }

    useMounted(props.nodeId)

    onMounted(() => {
      setTimeout(checkVisibility, 500)
    })

    watch(contentWrapper, (newVal) => {
      if (process.client && newVal) {
        updateMediaElements(newVal, 'video')
        updateMediaElements(newVal, 'iframe')
      }
    })

    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])
    const selectedAlignmentVariant = ref(ALIGNMENT_CLASSES[props.sectionAlignment])

    return {
      ctas,
      isVisible,
      observerTarget,
      addYoutubeNoCookie,
      selectedColorVariant,
      selectedAlignmentVariant,
      labels,
      BUTTON_NAMES,
      contentWrapper,
      handleCtaClick
    }
  }
}
</script>

<style lang="scss">
$constrained-content-width: 41.875rem;
$constrained-gutter-width: 1.875rem;
$constrained-inner-width: $constrained-content-width - $constrained-gutter-width;

.evolution-section-text {
  h1, h2, h3, h4, h5, h6 {
    margin-bottom: 0.5rem;
    line-height: 1.375; // correct line-height (Figma H2: 48px/66px = 1.375)
  }

  p {
    margin-top: 0; // remove margin-top: 24px on paragraph blocks
    margin-bottom: 0;
  }

  // Tighten gap between heading and paragraph text in content blocks
  .evolution-content {
    > * + * {
      margin-top: 0.5rem; // 8px between consecutive elements
    }

    p + p {
      margin-top: 0.25rem; // tighter between paragraphs
    }
  }

  &.evolution-gradient {
    background-image: url('~/assets/evolution-gradient.png');
    background-size: 500px;
    background-repeat: no-repeat;
    background-position: top right;
  }

  iframe {
    width: 100%;
  }

  .row.constrained > div:first-child,
  .row.constrained .evolution-section-text-buttons {
    max-width: $constrained-content-width;
  }

  .evolution-collapsible-content.constrained .collapsible-inner {
    max-width: $constrained-inner-width;
  }

  .evolution-collapsible-content.constrained.justify-content-center .collapsible-inner {
    margin-left: auto;
    margin-right: auto;
  }

  .constrained.justify-content-center {
    .evolution-section-text-buttons {
      justify-content: center;
    }
  }
}
</style>
