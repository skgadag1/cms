<template>
  <LazyHydrate :on-interaction="['click', 'touchstart']">
    <section :class="selectedColorVariant.background">
      <div :id="id" class="container py-5 section-text">
        <header v-if="title" class="row">
          <component :is="htag" :class="[htag, selectedColorVariant.headline]" class="col-12">{{ title }}</component>
        </header>
        <div class="row">
          <CollapsibleContent :labels="labels"
                              :is-truncated="truncated === 'true'"
                              :content-wrapper="contentWrapper"
                              :font-size="fontSize"
                              :selected-color-variant="selectedColorVariant"
                              :content="addYoutubeNoCookie(content)"
                              :variant="variant"
          >
            <div v-if="ctas.length"
                 class="col-12 d-flex justify-content-md-end flex-column flex-md-row mt-5"
            >
              <b-button v-for="button in ctas"
                        :key="`section-text__button__${button.title}`"
                        :href="button.link"
                        :target="button.target === '_blank_follow' ? '_blank' : button.target"
                        :variant="button.config.variant"
                        :class="[button.config.class, selectedColorVariant.buttonBg]"
                        class="font-condensed d-block my-2 ml-md-3"
                        data-test-id="section-text__button"
              >
                {{ button.title }}
              </b-button>
            </div>
          </CollapsibleContent>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {computed, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import CollapsibleContent from '@/components/CollapsibleContent'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import useMedia from '~/helpers/composables/useMedia'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'

export default {
  components: {
    CollapsibleContent
  },
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    content: {
      type: String
    },
    fontSize: {
      type: String
    },
    id: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    },
    truncated: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {cta} = props
    const variants = [{variant: 'primary'}, {variant: 'outline-primary'}, {
      variant: 'secondary',
      class: 'text-primary'
    }]
    const {$services, $utils, $regions} = useContext()
    const {updateMediaElements} = useMedia()

    const contentWrapper = ref(null)
    const labels = ref({})

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/%region%/common/labels/functional'])
    })

    const ctas = computed(() => {
      return cta?.['@nodes']
        ? cta['@nodes'].map((x, i) => {
          const linkField = $utils.getLinkField(cta[x].link, $regions.current.language.name)
          return {
            link: linkField?.url || '',
            title: linkField?.label || '',
            target: linkField?.target || 'self',
            config: variants[i % variants.length]
          }
        }).reverse()
        : []
    })
    useMounted(props.nodeId)

    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])

    watch(contentWrapper, (newVal) => {
      if (process.client && newVal) {
        updateMediaElements(newVal, 'video')
        updateMediaElements(newVal, 'iframe')
      }
    })

    return {
      ctas,
      addYoutubeNoCookie,
      selectedColorVariant,
      contentWrapper,
      labels
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.section-text {
  p {
    margin-bottom: 0;
  }

  header .col-12 {
    margin-bottom: 0.5rem;
  }

  .content-wrapper h1,
  .content-wrapper h2,
  .content-wrapper h3,
  .content-wrapper h4,
  .content-wrapper h5,
  .content-wrapper h6 {
    margin-bottom: 0.5rem;
  }

  .dark-btn.btn-primary {
    color: $black !important;

    &:hover {
      opacity: .9
    }
  }

  iframe {
    width: 100%;
  }

  .dark-btn.btn-outline-primary {
    color: $white !important;
    background: $dark !important;
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
    background: $light !important;

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
