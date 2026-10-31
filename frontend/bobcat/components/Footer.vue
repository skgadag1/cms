<template>
  <footer data-test-id="footer">
    <div class="bg-dark text-white">
      <LazyHydrate never>
        <div class="container">
          <div class="row pt-lg-4">
            <nav class="col-12 col-lg-9 py-3 order-0 order-lg-1" role="navigation" aria-label="Footer">
              <div class="row">
                <div class="col-12 col-md-3 order-0 order-md-1">
                  <div class="row mb-4 mb-md-0">
                    <div class="col-6 col-md-12">
                      <h3 class="text-uppercase footer-heading mb-3 h6" :title="generateConceptHint('languages')">{{ labels.languages }}</h3>
                      <a v-if="regionSelectorLink && regionSelectorLink.url"
                         class="h6 d-flex align-items-center text-uppercase text-decoration-none"
                         data-test-id="footer-languageRegionPicker"
                         :href="regionSelectorLink.url"
                         :target="regionSelectorLink.target === '_blank_follow' ? '_blank' : regionSelectorLink.target"
                         :title="regionSelectorLink.label"
                         :rel="regionSelectorLink.target === '_blank' ? 'noopener nofollow' : null"
                      >
                        <b-icon icon="globe" />
                        <span class="ml-2">{{ regionSelectorLink.label }}</span>
                      </a>
                    </div>

                    <div v-if="rightSectionLinks.length"
                         class="col-6 col-md-12 mt-md-4 text-uppercase"
                         data-test-id="footer-rightLinkSection"
                    >
                      <span class="invisible d-md-none">&nbsp;</span>
                      <a v-for="(itm, i) in rightSectionLinks"
                         :key="`footer-language__${i}`"
                         :href="itm.url"
                         :target="itm.target === '_blank_follow' ? '_blank' : itm.target"
                         :rel="itm.target === '_blank' ? 'noopener nofollow' : null"
                         class="h6 d-flex align-items-center text-decoration-none"
                      >{{ itm.label }}</a>
                    </div>
                  </div>
                </div>

                <div class="col-12 col-md-9 order-1 order-md-0 text-uppercase">
                  <div class="row" data-test-id="footer-links">
                    <div v-for="(l, i) in footerLinks" :key="`footer-group__${i}`" :data-test-id="`footer-links-group__${i}`" class="col-6 col-md-4 mb-4">
                      <h3 v-if="l.title" class="footer-heading h6 mb-3">{{ l.title }}</h3>
                      <a v-for="(itm, j) in l.items"
                         :key="`footer-group-${i}-link__${j}`"
                         :href="itm.url"
                         :target="itm.target === '_blank_follow' ? '_blank' : itm.target"
                         :rel="itm.target === '_blank' ? 'noopener nofollow' : null"
                         class="h6 d-block text-decoration-none"
                      >{{ itm.label }}</a>
                    </div>
                  </div>
                </div>
              </div>
            </nav>

            <div class="col-12 col-lg-3 py-3 order-1 order-lg-0">
              <div class="row justify-content-center">
                <div v-if="oneToughUrl" class="col-12 col-md-6 col-lg-12 order-1 order-md-0 d-flex align-items-center justify-content-center justify-content-md-end justify-content-lg-start">
                  <img :src="oneToughUrl"
                       :alt="oneTough.imageAlt"
                       :title="oneTough.imageTitle"
                       class="img-fluid mb-lg-3 w-auto h-auto"
                       width="300"
                       height="200"
                       loading="lazy"
                       decoding="async"
                  >
                </div>

                <ul v-if="socialLinks.length"
                    class="col-12 col-md-6 col-lg-12 order-0 order-md-1 d-flex align-items-center justify-content-center justify-content-md-start mb-3 mb-md-0 list-unstyled"
                    data-test-id="social-links"
                >
                  <li v-for="(s, i) in socialLinks" :key="`social-media-url__${s.icon}`" :class="{'mr-5 mr-md-3' : i !== socialLinks.length - 1}">
                    <a
                      :href="s.url"
                      :title="s.label || s.url"
                      class="text-decoration-none d-block w-auto h-auto"
                      target="_blank"
                      rel="noopener nofollow"
                    >
                      <b-icon :icon="s.icon" class="h4 mb-0" />
                    </a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </LazyHydrate>
    </div>

    <LazyHydrate never>
      <div class="container">
        <div class="row">
          <div v-if="logoUrl" class="col-12 col-lg-auto text-center">
            <img :src="logoUrl"
                 :alt="logo.imageAlt"
                 :title="logo.imageTitle"
                 class="img-fluid py-3 w-auto"
                 loading="lazy"
                 decoding="async"
                 width="300"
                 height="200"
            >
          </div>
          <div class="col-12 col-lg">
            <div class="py-lg-3 small" data-test-id="footer-disclaimer" v-html="disclaimer" />
          </div>
        </div>
      </div>
    </LazyHydrate>
  </footer>
</template>

<script>
import {computed, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useMounted from '../helpers/composables/useMounted'
import useConcepts from '~/helpers/composables/useConcepts'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  name: 'Footer',
  serverCacheKey: props => `footer-${props.ssrCurrentRegion?.name}-${props.ssrCurrentRegion?.language?.name}-${props.nodeId}`,
  props: {
    oneTough: {
      type: Object
    },
    social: {
      type: Object
    },
    links: {
      type: Object
    },
    rightSection: {
      type: Object
    },
    logo: {
      type: Object
    },
    disclaimer: {
      type: String
    },
    regionLink: {
      type: Object
    },
    ssrCurrentRegion: {
      type: Object,
      required: true
    },
    nodeId: {
      type: String
    }
  },
  setup(props, context) {
    const {social, rightSection, links, logo, oneTough, regionLink, ssrCurrentRegion, nodeId} = props
    const {$utils, $cloudinary, $config} = useContext()
    const regionSelectorLink = ref($utils.getLinkField(regionLink, ssrCurrentRegion.language.name))
    useMounted(nodeId)

    if (regionSelectorLink.value?.url && ssrCurrentRegion.language?.label) {
      const label = (regionSelectorLink.value?.label?.replace('%REGION%', ssrCurrentRegion.label)?.replace('%LANG%', ssrCurrentRegion.language.label)) || regionSelectorLink.value.label
      regionSelectorLink.value = {...regionSelectorLink.value, label}
    }

    const labels = ref({})
    const {generateConceptHint} = useConcepts()
    const {$services} = useContext()

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts()
    })

    const logoUrl = computed({
      get: () => {
        if (!logo || !logo.src) { return '' }
        const publicId = logo.src.split('cloudinary:')[1].split('#')[0]
        return $utils.adjustDomain($cloudinary.image.url(publicId, {crop: 'scale', height: 70}))
      },
      set: () => {}
    })

    const oneToughUrl = computed({
      get: () => {
        if (!oneTough || !oneTough.src) { return '' }
        const publicId = oneTough.src.split('cloudinary:')[1].split('#')[0]
        return $utils.adjustDomain($cloudinary.image.url(publicId, {crop: 'scale', height: 24}))
      },
      set: () => {}
    })

    const linksSource = resolveLegacyListSource(
      links, context.attrs, 'links', ssrCurrentRegion.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const footerLinks = linksSource
      ? linksSource['@nodes'].map((x) => {
        const group = linksSource[x]
        const itemsSource = resolveLegacyListSource(
          group.items, group, 'items', ssrCurrentRegion.language.name, $config.USE_LEGACY_I18N_STRUCTURE
        )
        return {
          title: group.title,
          items: $utils.getLinks(itemsSource, ssrCurrentRegion.language.name)
        }
      })
      : []

    const rightSectionSource = resolveLegacyListSource(
      rightSection, context.attrs, 'rightSection', ssrCurrentRegion.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const rightSectionLinks = $utils.getLinks(rightSectionSource, ssrCurrentRegion.language.name) || []
    const socialLinks = social ? social['@nodes'].map(x => social[x]) : []

    return {
      logoUrl,
      oneToughUrl,
      footerLinks,
      rightSectionLinks,
      socialLinks,
      regionSelectorLink,
      labels,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss">
footer {
  a {
    color: inherit;
  }

 .footer-heading {
    opacity: .7;
  }
}
</style>
