<template>
  <section data-test-id="related-links" :class="selectedColorVariant.background" class="py-4">
    <LazyHydrate never>
      <div class="container">
        <div class="row">
          <div class="col-12">
            <component :is="htag"
                       v-if="title"
                       class="border-bottom pb-3 mt-3 w-100"
                       :class="[htag, selectedColorVariant.headline]"
                       data-test-id="related-links-title"
            >
              {{ title }}
            </component>
          </div>
          <div v-if="firstColLinks.length"
               data-test-id="related-links-first-col"
               class="col-12 col-md-4"
               :class="`order-${firstOrder}`"
          >
            <component :is="firstHtag"
                       v-if="firstTitle"
                       class="w-100"
                       :class="[firstHtag, selectedColorVariant.headline]"
                       data-test-id="related-links-first-col-title"
            >
              {{ firstTitle }}
            </component>
            <div class="d-flex flex-column mb-md-0 mb-2">
              <div v-for="(link, i) in firstColLinks"
                   :key="`fc-${i}-${link.label}`"
                   class="d-flex align-items-center mb-3"
              >
                <a
                  :href="link.url"
                  :target="link.target === '_blank_follow' ? '_blank' : link.target"
                  :rel="link.target === '_blank' ? 'noopener nofollow' : null"
                  data-test-id="related-links-first-col-link"
                  class="cursor-pointer font-weight-bold"
                  :class="selectedColorVariant.headline"
                >
                  {{ link.label }}
                </a>
                <b-icon class="ml-2" :class="selectedColorVariant.arrows" icon="chevron-right" />
              </div>
            </div>
          </div>
          <div v-if="secondColLinks.length"
               data-test-id="related-links-second-col"
               class="col-12 col-md-4"
               :class="`order-${secondOrder}`"
          >
            <component :is="secondHtag"
                       v-if="secondTitle"
                       class="w-100"
                       :class="[secondHtag, selectedColorVariant.headline]"
                       data-test-id="related-links-second-col-title"
            >
              {{ secondTitle }}
            </component>
            <div class="d-flex flex-column mb-md-0 mb-2">
              <div v-for="(link, i) in secondColLinks"
                   :key="`sc-${i}-${link.label}`"
                   class="d-flex align-items-center mb-3"
              >
                <a
                  :href="link.url"
                  :target="link.target === '_blank_follow' ? '_blank' : link.target"
                  :rel="link.target === '_blank' ? 'noopener nofollow' : null"
                  data-test-id="related-links-second-col-link"
                  class="cursor-pointer font-weight-bold"
                  :class="selectedColorVariant.headline"
                >
                  {{ link.label }}
                </a>
                <b-icon class="ml-2" :class="selectedColorVariant.arrows" icon="chevron-right" />
              </div>
            </div>
          </div>
          <div v-if="thirdColLinks.length"
               data-test-id="related-links-third-col"
               class="col-12 col-md-4"
               :class="`order-${thirdOrder}`"
          >
            <component :is="thirdHtag"
                       v-if="thirdTitle"
                       class="w-100"
                       :class="[thirdHtag, selectedColorVariant.headline]"
                       data-test-id="related-links-third-col-title"
            >
              {{ thirdTitle }}
            </component>
            <div class="d-flex flex-column mb-md-0 mb-2">
              <div v-for="(link, i) in thirdColLinks"
                   :key="`tc-${i}-${link.label}`"
                   class="d-flex align-items-center mb-3"
              >
                <a
                  :href="link.url"
                  :target="link.target === '_blank_follow' ? '_blank' : link.target"
                  :rel="link.target === '_blank' ? 'noopener nofollow' : null"
                  data-test-id="related-links-third-col-link"
                  class="cursor-pointer font-weight-bold"
                  :class="selectedColorVariant.headline"
                >
                  {{ link.label }}
                </a>
                <b-icon class="ml-2" :class="selectedColorVariant.arrows" icon="chevron-right" />
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
import useMounted from '~/helpers/composables/useMounted'
import {LIGHT_VARIANT, VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    firstTitle: {
      type: String
    },
    secondTitle: {
      type: String
    },
    thirdTitle: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    firstHtag: {
      type: String,
      default: 'h4'
    },
    firstOrder: {
      type: String,
      default: '1'
    },
    secondOrder: {
      type: String,
      default: '2'
    },
    thirdOrder: {
      type: String,
      default: '3'
    },
    secondHtag: {
      type: String,
      default: 'h4'
    },
    thirdHtag: {
      type: String,
      default: 'h4'
    },
    grayBackground: {
      type: String,
      default: 'false'
    },
    first: {
      type: Object
    },
    second: {
      type: Object
    },
    third: {
      type: Object
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props, context) {
    const {first, second, third, grayBackground} = props
    const {$regions, $utils, $config} = useContext()
    const firstColLinks = ref([])
    const secondColLinks = ref([])
    const thirdColLinks = ref([])
    useMounted(props.nodeId)

    const selectedColorVariant = ref(grayBackground === 'true' ? VARIANTS_CLASSES[LIGHT_VARIANT] : VARIANTS_CLASSES[props.variant])

    const firstSource = resolveLegacyListSource(
      first, context.attrs, 'first', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const secondSource = resolveLegacyListSource(
      second, context.attrs, 'second', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const thirdSource = resolveLegacyListSource(
      third, context.attrs, 'third', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )

    firstColLinks.value = firstSource ? firstSource['@nodes'].map(x => $utils.getLinkField(firstSource[x]?.link, $regions.current.language.name)).filter(x => x) : []
    secondColLinks.value = secondSource ? secondSource['@nodes'].map(x => $utils.getLinkField(secondSource[x]?.link, $regions.current.language.name)).filter(x => x) : []
    thirdColLinks.value = thirdSource ? thirdSource['@nodes'].map(x => $utils.getLinkField(thirdSource[x]?.link, $regions.current.language.name)).filter(x => x) : []

    return {
      firstColLinks,
      secondColLinks,
      thirdColLinks,
      selectedColorVariant
    }
  }
}
</script>
