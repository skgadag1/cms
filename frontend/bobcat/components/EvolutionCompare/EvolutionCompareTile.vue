<template>
  <div class="evolution-compare-tile d-flex flex-column min-h-0 w-100 text-dark"
       :class="{'evolution-compare-tile--footer-only': footerOnly}"
       :data-test-id="footerOnly ? 'evolution-compare-tile-footer' : 'evolution-compare-tile'"
  >
    <div v-if="!footerOnly"
         class="position-relative d-flex justify-content-center align-items-center bg-white flex-shrink-0 evolution-compare-tile__media mb-md-4 mb-0"
    >
      <ImageSet v-if="imageUrl"
                :src="imageUrl"
                :alt="imageAlt"
                :title="imageTitle"
                :img-width="imageWidth"
                :img-height="imageHeight"
                class="evolution-compare-tile__img"
                :options="{fluid: true}"
                data-test-id="evolution-compare-tile-image"
      />
      <div v-else
           class="w-100 bg-gray-lighter evolution-compare-tile__media-placeholder"
           aria-hidden="true"
      />
      <div class="position-absolute badge-wrapper d-flex flex-column align-items-start p-2">
        <span v-if="showNewBadge"
              class="card-indicator px-2 py-1 mb-1 bg-primary text-white font-weight-bold text-uppercase small"
              data-test-id="evolution-compare-tile-new"
        >{{ labels.new || 'New' }}</span>
        <span v-if="customIndicatorText"
              class="card-indicator px-2 py-1 mb-1 bg-black text-white font-weight-bold text-uppercase small"
              data-test-id="evolution-compare-tile-custom-indicator"
        >{{ customIndicatorText }}</span>
        <span v-for="(ind, idx) in listingIndicatorLabels"
              :key="`evolution-compare-tile-li-${idx}-${ind}`"
              class="card-indicator px-2 py-1 mb-1 bg-black text-white font-weight-bold text-uppercase small"
              data-test-id="evolution-compare-tile-indicator"
        >{{ ind }}</span>
      </div>
    </div>

    <div class="evolution-compare-tile__body min-h-0 pb-3 mt-3"
         :class="{'evolution-compare-tile__body--footer-only': footerOnly}"
    >
      <div v-show="!footerOnly" class="flex-shrink-0 mb-2">
        <p class="h4 font-weight-bold mb-0 evolution-compare-tile__title"
           data-test-id="evolution-compare-tile-title"
        >
          {{ model.title || model['@name'] }}
        </p>
      </div>

      <div v-show="!footerOnly" class="flex-shrink-0 mb-4">
        <div v-if="description"
             class="evolution-compare-tile__description"
             data-test-id="evolution-compare-tile-description"
             v-html="description"
        />
      </div>

      <div class="evolution-compare-tile__footer d-flex flex-column flex-shrink-0"
           :class="footerOnly ? 'flex-grow-1 min-h-0' : 'mt-auto'"
      >
        <div :class="footerOnly ? 'flex-grow-1 d-flex flex-column min-h-0' : ''">
          <div class="mb-3" data-test-id="evolution-compare-tile-price">
            <template v-if="hasPrice">
              <p v-if="merged.suggestedPrice"
                 class="h4 text-primary font-weight-bold mb-0"
              >
                {{ merged.suggestedPrice }}
              </p>
              <p class="small mb-0">
                {{ startingAtCaption }}
              </p>
            </template>
          </div>

          <div class="mb-3">
            <hr v-if="showFinancingDivider" class="border-gray-light my-3">
            <div v-if="hasOffers || (offerDetailsLink && offerDetailsLink.url)"
                 class="small mb-0 evolution-compare-tile__offers"
                 :class="showFinancingDivider ? '' : 'border-top border-gray-light pt-3 mt-2'"
                 data-test-id="evolution-compare-tile-offers"
            >
              <div v-if="merged.specialOfferDetails" v-html="merged.specialOfferDetails" />
              <a v-if="offerDetailsLink && offerDetailsLink.url"
                 :href="offerDetailsLink.url"
                 :target="offerDetailsLinkTarget"
                 :rel="offerDetailsLink.target === '_blank' ? 'noopener nofollow' : null"
                 class="d-inline-block text-primary small"
                 data-test-id="evolution-compare-tile-offer-details"
              >
                {{ offerDetailsLink.label || labels.offerDetails || 'Offer Details' }}
              </a>
            </div>
          </div>
        </div>

        <div class="d-flex flex-column"
             :class="footerOnly ? 'mt-auto flex-shrink-0' : ''"
        >
          <DynamicButton v-if="bpLink"
                         :variant="bqVariantName || 'primary'"
                         :color="bqVariantColor"
                         :href="bpLink"
                         :title="generateConceptHint(BUILD_AND_PRICE_HINT_CONCEPT_KEY, labels[BUILD_AND_PRICE_HINT_CONCEPT_KEY])"
                         :target="ANCHOR_TARGET.BLANK"
                         rel="noopener nofollow"
                         class="w-100 text-uppercase font-weight-bold px-1 px-md-2"
                         data-test-id="evolution-compare-tile-bp"
                         @click="$emit('bp-click', model)"
          >
            {{ labels[BUILD_AND_PRICE_CONCEPT_KEY] || BUILD_AND_PRICE_BUTTON_LABEL }}
          </DynamicButton>

          <DynamicButton v-if="detailsLink && detailsLink.url"
                         :variant="modelDetailsVariant || BUTTON_NAMES.LINEAR"
                         :color="modelDetailsColor"
                         :href="detailsLink.url"
                         :target="modelDetailsButtonTarget"
                         class="w-100 mt-1 text-uppercase font-weight-bold px-1 px-md-2"
                         data-test-id="evolution-compare-tile-details"
                         @click="$emit('details-click', model)"
          >
            {{ modelDetailsLabel || labels.modelDetails || 'Model details' }}
            <b-icon icon="chevron-right" class="ml-1 small" />
          </DynamicButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {computed, onServerPrefetch, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import ImageSet from '~/components/elements/ImageSet.vue'
import DynamicButton from '~/components/DynamicButton.vue'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'
import {fetchProductPricingOffers} from '~/helpers/fetchProductPricingOffers'
import {mergeModelCommerceFields} from '~/helpers/mergeModelCommerceFields'
import {BUTTON_NAMES} from '~/constants/buttons'
import {buildAndPriceUrl} from '~/helpers/buildAndPriceUrl'
import {ANCHOR_TARGET, BUILD_AND_PRICE_BUTTON_LABEL, BUILD_AND_PRICE_CONCEPT_KEY, BUILD_AND_PRICE_HINT_CONCEPT_KEY} from '~/constants/buildAndPrice'

const seedFromModel = model => ({
  suggestedPrice: model?.suggestedPrice || null,
  suggestedPrice2: model?.suggestedPrice2 || null,
  suggestedPriceAdditionalInfo: model?.suggestedPriceAdditionalInfo || null,
  specialOfferDetails: model?.specialOfferDetails || null,
  specialOfferDetails2: model?.specialOfferDetails2 || null,
  specialOffersDetailsAdditionalInfo: model?.specialOffersDetailsAdditionalInfo || null
})

const resolveAnchorTarget = (rawTarget, fallback = ANCHOR_TARGET.SELF) => {
  if (rawTarget === ANCHOR_TARGET.BLANK_FOLLOW) {
    return ANCHOR_TARGET.BLANK
  }
  return rawTarget || fallback
}

const resolveProductModelPageLink = (model, lang, $utils) => {
  if (!model || !$utils) {
    return null
  }
  const fromLink = $utils.getLinkField?.(model.link, lang)
  if (fromLink?.url) {
    return fromLink
  }
  const fromPage = $utils.getLinkField?.(model.page, lang)
  if (fromPage?.url) {
    return fromPage
  }
  const fromPageLinkPath = $utils.getPageUrl?.(model, lang)
  if (fromPageLinkPath) {
    return {url: fromPageLinkPath, target: '_self'}
  }
  const pr = model.pageRelated
  if (pr && lang) {
    const localized = pr[lang]
    if (localized?.url) {
      return {url: localized.url, target: '_self'}
    }
  }
  if (pr?.['@nodes']?.length) {
    for (const key of pr['@nodes']) {
      const node = pr[key]
      const u = node?.url
      if (u) {
        return {url: u, target: '_self'}
      }
    }
  }
  if (model.url) {
    return {url: model.url, target: model.target || '_self'}
  }
  return null
}

export default {
  components: {ImageSet, DynamicButton},
  props: {
    model: {
      type: Object,
      required: true
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    primaryCtaButton: {
      type: Object,
      default: () => ({})
    },
    modelDetailsLabel: {
      type: String
    },
    modelDetailsFallbackLink: {
      type: Object,
      default: null
    },
    modelDetailsCtaButton: {
      type: Object,
      default: () => ({})
    },
    modelDetailsTarget: {
      type: String,
      default: '_self'
    },
    startingAtPriceLabel: {
      type: String
    },
    footerOnly: {
      type: Boolean
    }
  },
  emits: ['bp-click', 'details-click'],
  setup(props) {
    const {$services, $utils, $regions} = useContext()
    const {generateConceptHint} = useConcepts()

    const merged = ref(seedFromModel(props.model))

    const bqVariantName = computed(() => props.primaryCtaButton?.field || null)
    const bqVariantColor = computed(() => props.primaryCtaButton?.color || null)

    const modelDetailsVariant = computed(() => props.modelDetailsCtaButton?.field || null)
    const modelDetailsColor = computed(() => props.modelDetailsCtaButton?.color || null)

    const firstModelImage = computed(() => $utils.products?.getFirstModelImage?.(props.model))
    const imageUrl = computed(() => {
      const src = firstModelImage.value?.img?.src || props.model?.image?.src
      if (!src) {
        return ''
      }
      const cdn = $utils.getCloudinaryAsset?.(src, true, {crop: 'pad', width: 480, height: 320})
      return cdn || src
    })
    const imageAlt = computed(() => firstModelImage.value?.img?.imageAlt || props.model?.image?.imageAlt || props.model.title || '')
    const imageTitle = computed(() => firstModelImage.value?.img?.imageTitle || props.model?.image?.imageTitle || props.model.title || '')
    const imageWidth = computed(() => firstModelImage.value?.img?.imageWidth || props.model?.image?.imageWidth || null)
    const imageHeight = computed(() => firstModelImage.value?.img?.imageHeight || props.model?.image?.imageHeight || null)

    const description = computed(() => {
      const candidates = [
        props.model.shortDescription,
        props.model.overview,
        props.model.description
      ]
      const raw = candidates.find(c => c != null && String(c).trim() !== '') || ''
      return raw ? addYoutubeNoCookie(String(raw)) : ''
    })

    const showNewBadge = computed(() => props.model.newIndicator && props.model.newIndicator !== 'false')

    const customIndicatorText = computed(() => {
      const ci = props.model?.customIndicator
      if (!ci) {
        return ''
      }
      if (typeof ci === 'string') {
        return ci.trim()
      }
      if (typeof ci === 'object') {
        const show = ci.showCustomIndicator
        if (show === 'false' || show === false) {
          return ''
        }
        const label = ci.label != null ? String(ci.label).trim() : ''
        return label
      }
      return ''
    })

    const listingIndicatorLabels = computed(() => {
      let li = props.model.listingIndicators

      if (li && !Array.isArray(li)) {
        li = [li]
      }
      if (!Array.isArray(li)) {
        return []
      }
      return li
        .flatMap((entry) => {
          if (typeof entry === 'string') {
            return [{label: entry}]
          }
          if (entry && typeof entry === 'object') {
            return Object.values(entry)
          }
          return []
        })
        .filter(ind => ind && typeof ind === 'object')
        .filter(ind => ind.showIndicator !== false && ind.showIndicator !== 'false')
        .map(ind => String(ind.label || ind.title || '').trim())
        .filter(Boolean)
    })

    const detailsLink = computed(() => {
      const lang = $regions.current.language?.name
      const fromModel = resolveProductModelPageLink(props.model, lang, $utils)
      if (fromModel?.url) {
        return fromModel
      }
      if (props.modelDetailsFallbackLink?.field === 'none') {
        return null
      }
      const fb = $utils.getLinkField?.(props.modelDetailsFallbackLink, lang)
      if (fb?.url) {
        return {
          url: fb.url,
          target: fb.target || '_self',
          label: fb.label
        }
      }
      return null
    })

    const offerDetailsLink = computed(() => {
      const lang = $regions.current.language?.name
      const link = $utils.getLinkField?.(props.model.offerDetailsLink, lang)
      return link?.url ? link : null
    })

    const offerDetailsLinkTarget = computed(() => resolveAnchorTarget(offerDetailsLink.value?.target, '_self'))

    const modelDetailsButtonTarget = computed(() => {
      const explicit = resolveAnchorTarget(props.modelDetailsTarget, '')
      if (explicit) {
        return explicit
      }
      return resolveAnchorTarget(detailsLink.value?.target, '_self')
    })

    const bpLink = computed(() => buildAndPriceUrl(props.model, {
      region: $regions.current?.name,
      language: $regions.current?.language?.name
    }))

    const hasPrice = computed(() => Boolean(merged.value.suggestedPrice))
    const hasOffers = computed(() => Boolean(merged.value.specialOfferDetails))

    const showFinancingDivider = computed(() => hasPrice.value && (hasOffers.value || Boolean(offerDetailsLink.value?.url)))

    const startingAtCaption = computed(() => (
      merged.value.suggestedPriceAdditionalInfo ||
      props.startingAtPriceLabel ||
      props.labels?.startingAtPrice
    ))

    const loadCommerce = async () => {
      try {
        const pc = await fetchProductPricingOffers(props.model, {$services, $utils})
        let apex = null
        try {
          apex = await $services.pricingApi?.getModelPricing?.(props.model.id, $regions.current.name)
        } catch (e) {
          apex = null
        }
        merged.value = mergeModelCommerceFields(pc, apex)
      } catch (e) {
        merged.value = seedFromModel(props.model)
      }
    }

    const {fetch} = useFetch(async () => {
      await loadCommerce()
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    watch(() => props.model?.id || props.model?.['@path'], () => {
      merged.value = seedFromModel(props.model)
      fetch()
    })

    return {
      BUTTON_NAMES,
      merged,
      generateConceptHint,
      imageUrl,
      imageAlt,
      imageTitle,
      imageWidth,
      imageHeight,
      description,
      showNewBadge,
      customIndicatorText,
      listingIndicatorLabels,
      detailsLink,
      offerDetailsLink,
      offerDetailsLinkTarget,
      modelDetailsButtonTarget,
      bpLink,
      bqVariantName,
      bqVariantColor,
      BUILD_AND_PRICE_BUTTON_LABEL,
      BUILD_AND_PRICE_CONCEPT_KEY,
      BUILD_AND_PRICE_HINT_CONCEPT_KEY,
      ANCHOR_TARGET,
      modelDetailsVariant,
      modelDetailsColor,
      hasPrice,
      hasOffers,
      showFinancingDivider,
      startingAtCaption
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.evolution-compare-tile {
  flex: 1 1 0%;
  min-height: 0;

  &__title {
    @include media-breakpoint-up(sm) {
      font-size: $h3-font-size;
      line-height: $headings-line-height;
    }
  }

  &__body {
    flex: 1 1 0%;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  &__media,
  &__media-placeholder {
    aspect-ratio: 3 / 2;
    overflow: hidden;
  }

  &__media ::v-deep .evolution-compare-tile__img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
  }

  &__offers ::v-deep p {
    margin-bottom: 0.25rem;
  }

  &__description ::v-deep p:last-child {
    margin-bottom: 0;
  }

  .badge-wrapper {
    top: 0.25rem;
    left: -.5rem;

    @include media-breakpoint-up(sm) {
      top: 1rem;
      left: 0;
    }
  }

  &--footer-only {
    flex: 1 1 0%;
    min-height: 0;
    align-self: stretch;
  }

  &__body--footer-only {
    flex: 1 1 0%;
    min-height: 0;
    margin-top: 0;
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
  }

  a[data-test-id="evolution-compare-tile-offer-details"] {
    text-decoration: underline !important;
  }
}
</style>
