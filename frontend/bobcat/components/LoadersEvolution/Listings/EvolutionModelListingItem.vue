<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base border-0 evolution-listing-item rounded-0 text-black"
          :class="[{'korean-font' : isKorean}, {'with-no-hover' : notClickable}, tilesVariant, {'pb-3' : !showCompare}]"
          no-body
          footer-tag="footer"
          footer-class="z-index-1"
  >
    <a v-if="item && item.url"
       :href="item.url"
       class="stretched-link"
       :title="item.title || item['@name']"
       data-test-id="product-listing-item-link"
       :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
       :rel="item.target === '_blank' ? 'noopener nofollow' : null"
       @click="ga4click"
    >{{ item.title || item['@name'] }}</a>
    <a v-else-if="link && link.url"
       :href="link.url"
       class="stretched-link"
       :title="item.title || item['@name']"
       data-test-id="product-listing-item-link"
       :target="link ? (link.target === '_blank_follow' ? '_blank' : link.target || '_self') : '_self'"
       :rel="link && link.target === '_blank' ? 'noopener nofollow' : null"
       @click="ga4click"
    >{{ item.title || item['@name'] }}</a>

    <div class="d-flex flex-column py-2">
      <div class="d-flex justify-content-center align-items-center position-relative">
        <ImageSet v-if="!hideImages"
                  :src="imageUrl"
                  :alt="imageAlt"
                  :title="imageTitle"
                  :img-width="imageWidth"
                  :img-height="imageHeight"
                  :options="{height: 132}"
                  data-test-id="product-listing-item__image"
        />
        <div class="indicators-wrapper d-flex mb-2 flex-column align-items-start position-absolute w-100">
          <div v-if="showIndicator && newIndicator"
               data-test-id="product-listing-item-new-indicator"
               class="card-indicator px-1 mb-1 bg-primary text-white font-weight-bold text-uppercase"
               :class="{'card-indicator__right': !imageUrl}"
          >
            {{ labels.new || 'New' }}
          </div>

          <div v-if="showIndicator && imageUrl && customIndicator"
               data-test-id="product-listing-item-custom-indicator"
               class="card-indicator px-1 mr-1 mb-1 d-flex justify-content-center align-items-center text-white bg-black font-weight-bold text-uppercase"
          >
            {{ customIndicator }}
          </div>

          <template v-if="showIndicator && item.listingIndicators && item.listingIndicators.length">
            <div v-for="indicator in item.listingIndicators"
                 :key="indicator"
                 data-test-id="product-listing-item-listing-indicator"
                 class="card-indicator px-1 mr-1 mb-1 d-flex justify-content-center align-items-center text-white bg-black font-weight-bold text-uppercase"
            >
              {{ indicator }}
            </div>
          </template>
        </div>
      </div>
      <div class="px-3">
        <b-card-title data-test-id="`product-listing-item-title"
                      :title-tag="item.htag || 'h4'"
                      :class="textVariant"
                      class="mb-0"
        >
          {{ item.title }}
        </b-card-title>

        <div
          v-if="item.suggestedPrice && showSuggestedPrice"
          class="gradient-border position-relative bg-white z-index-0 d-flex align-items-center w-100 justify-content-between p-2"
        >
          <p class="mb-0">{{ labels.startingAtPrice || 'Starting at' }}</p>
          <p class="text-primary mb-0 h5" data-test-id="product-listing-suggested-price">
            {{ item.suggestedPrice }}
          </p>
        </div>
      </div>
    </div>

    <template v-if="!hideCaption && captions.length">
      <b-card-header v-for="(caption, idx) in captions"
                     :key="`listing-${item['@id']}-caption-${idx}`"
                     data-test-id="product-listing-item-caption"
                     :header-text-variant="(captionVariant === 'dark' || captionVariant === 'black') ? 'white' : 'black'"
                     class="product-listing-item__caption py-1 px-3"
                     :class="captionVariant ? `bg-${captionVariant}` : ''"
      >
        {{ caption }}
      </b-card-header>
    </template>

    <b-card-text v-if="!hideDescription && item.description"
                 data-test-id="product-listing-item-description"
                 text-tag="div"
                 class="p-3"
                 v-html="addYoutubeNoCookie(item.description)"
    />

    <b-card-body
      v-if="displayItemDescWrapper"
      class="d-flex flex-column px-3 py-2"
      data-test-id="product-listing-item__content"
      :class="[{'pt-6' : hideImages && showIndicator && newIndicator}, textVariant]"
    >
      <b-card-text v-if="item.amountInGroup"
                   data-test-id="product-listing-item-amountInGroup"
                   text-tag="div"
                   :class="amountsVariant ? amountsVariant : 'bg-gray-lighter text-gray-dark-medium'"
                   class="p-2 h6"
                   v-html="`${item.amountInGroup} ${labels.availableAttachments || 'Available attachments'}`"
      />

      <b-card-text v-if="showChips && chips" text-tag="div">
        <b-badge v-for="(chip, index) in chips"
                 :key="`item-chips__${item['@id']}__${index}`"
                 :data-test-id="`product-listing-item-badge-${index}`"
                 pill
                 :variant="chipsVariant ? chipsVariant : 'dark'"
                 class="mr-1 mb-3"
        >
          {{ chip }}
        </b-badge>
      </b-card-text>

      <b-card-text v-if="item.info"
                   data-test-id="product-listing-item-info"
                   class="d-inline-block font-weight-bold"
                   :class="textVariant ? textVariant : 'text-muted'"
      >
        {{ item.info }}
      </b-card-text>

      <div class="flex-grow-1" />

      <EvolutionDivider v-if="dividerAboveProperties && !hideDescription && properties && properties.length" class="mb-2" />

      <b-card-text v-if="!hideDescription && properties && properties.length"
                   data-test-id="product-listing-item__properties"
                   class="mb-0"
      >
        <b-list-group flush>
          <b-list-group-item v-for="(property, idx) in properties"
                             :key="`${property.name}-${idx}`"
                             :class="[tilesVariant, property.name ? 'd-flex' : 'd-none d-md-flex', idx === properties.length - 1 ? 'pb-2' : 'pb-3']"
                             class="border-0 m-0 p-0 justify-content-between align-items-center"
          >
            <div v-if="property.value && property.name" class="flex evolution-product-listing-item__property-label">
              {{ property.name }}:
            </div>
            <div v-if="property.value && property.name" class="text-right" v-html="property.value" />
          </b-list-group-item>
        </b-list-group>
      </b-card-text>

      <div v-if="showViewDetailsLink && link && link.url && link.label" class="py-2">
        <a
          :href="link.url"
          class="font-weight-bold d-inline-flex align-items-center text-underline-hover"
          :target="link.target === '_blank_follow' ? '_blank' : link.target || '_self'"
          :rel="link.target === '_blank' ? 'noopener nofollow' : null"
          data-test-id="product-listing-item-link"
          :class="textVariant"
          @click="ga4click"
        >{{ link.label }}
          <b-icon icon="chevron-right" class="ml-1 small" />
        </a>
      </div>
    </b-card-body>
    <div v-else class="flex-grow-1" />

    <div v-if="!dividerAboveProperties" class="px-3">
      <EvolutionDivider class="mb-2" />
    </div>

    <div v-if="showInventorySearchButton || showBuildPriceButtonOpt || showModelDetailsButton"
         class="d-flex z-index-1 flex-column px-3"
    >
      <DynamicButton
        v-if="showModelDetailsButton && link && link.url"
        :variant="modelDetailsVariantName"
        :color="modelDetailsVariantColor"
        :href="link.url"
        :target="link.target === '_blank_follow' ? '_blank' : link.target || '_self'"
        :rel="link.target === '_blank' ? 'noopener nofollow' : null"
        :title="generateConceptHint('modelDetails')"
        class="w-100"
        @click="ga4click"
      >
        {{ modelDetailsLabel || labels.modelDetails || 'MODEL DETAILS' }}
      </DynamicButton>

      <DynamicButton
        v-if="showBuildPriceButtonOpt"
        :variant="bqVariantName"
        :color="bqVariantColor"
        :href="bpLink"
        :title="generateConceptHint(BUILD_AND_PRICE_HINT_CONCEPT_KEY, labels[BUILD_AND_PRICE_HINT_CONCEPT_KEY])"
        :target="ANCHOR_TARGET.BLANK"
        rel="noopener nofollow"
        class="w-100 text-uppercase"
        :class="{'mt-3': showModelDetailsButton}"
        @click="ga4click"
      >
        {{ labels[BUILD_AND_PRICE_CONCEPT_KEY] || BUILD_AND_PRICE_BUTTON_LABEL }}
      </DynamicButton>

      <DynamicButton
        v-if="showInventorySearchButton && searchLink && searchLink.url"
        :variant="inventorySearchVariantName"
        :color="inventorySearchVariantColor"
        :href="searchLink.url"
        :target="searchLink.target === '_blank_follow' ? '_blank' : searchLink.target || '_parent'"
        class="w-100 mt-3"
        @click="ga4click"
      >
        {{ searchLink.label || 'Search Inventory' }}
      </DynamicButton>
    </div>

    <div v-if="showCta === 'true' && ctaLabel" class="px-3 mt-3 z-index-1">
      <DynamicButton
        :variant="ctaVariantName"
        :color="ctaVariantColor"
        :href="link.url"
        :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
        :rel="item.target === '_blank' ? 'noopener nofollow' : null"
        class="w-100"
        @click="ga4click"
      >
        {{ ctaLabel }}
      </DynamicButton>
    </div>
    <template v-if="showCompare && compareMode === 'link' && compareHref" #footer>
      <div class="d-flex align-content-center justify-content-end px-3 pb-2">
        <a :href="compareHref"
           :target="compareTarget === '_blank_follow' ? '_blank' : compareTarget || '_self'"
           :rel="compareTarget === '_blank' ? 'noopener nofollow' : null"
           class="font-weight-bold d-inline-flex align-items-center text-uppercase text-underline-hover text-primary"
           :title="generateConceptHint('compare')"
           @click="ga4click"
        >
          {{ compareLabel || labels.compare || 'Compare' }}
          <b-icon icon="chevron-right" class="ml-1" />
        </a>
      </div>
    </template>
    <template v-else-if="showCompare && compareMode !== 'link'" #footer>
      <div class="d-flex align-content-center justify-content-start px-1">
        <div class="d-flex justify-content-start m-1 cursor-pointer w-100"
             :title="generateConceptHint('compare')"
             @click.prevent="() => { if(!disabled || (disabled && selected)) selected ? $emit('remove', item['@id']) : $emit('add', item, item.suggestedPrice ? {headline: item.suggestedPrice} : {})}"
        >
          <b-form-checkbox :checked="[selected]"
                           :disabled="disabled"
                           class="d-inline-block mr-n1 mr-1 custom-control-input__dark transition-base"
          />
          {{ labels.compare || 'Compare' }}
        </div>
      </div>
    </template>
  </b-card>
</template>

<script>
import {computed, ref, toRefs, useContext} from '@nuxtjs/composition-api'
import EvolutionDivider from './EvolutionDivider.vue'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'
import {buildAndPriceUrl} from '~/helpers/buildAndPriceUrl'
import {ANCHOR_TARGET, BUILD_AND_PRICE_BUTTON_LABEL, BUILD_AND_PRICE_CONCEPT_KEY, BUILD_AND_PRICE_HINT_CONCEPT_KEY} from '~/constants/buildAndPrice'

export default {
  components: {EvolutionDivider},
  props: {
    item: {
      type: Object,
      required: true
    },
    linkLabel: {
      type: String
    },
    showIndicator: {
      type: Boolean,
      default: true
    },
    showChips: {
      type: Boolean,
      default: true
    },
    showCompare: {
      type: Boolean,
      default: false
    },
    hideImages: {
      type: Boolean,
      default: false
    },
    hideDescription: {
      type: Boolean,
      default: false
    },
    hideCaption: {
      type: Boolean,
      default: false
    },
    showBuildPriceButton: {
      type: Boolean,
      default: false
    },
    showModelDetailsButton: {
      type: Boolean,
      default: false
    },
    modelDetailsLabel: {
      type: String
    },
    modelDetailsVariant: {
      type: Object
    },
    bqVariant: {
      type: Object
    },
    showInventorySearchButton: {
      type: Boolean,
      default: false
    },
    showSuggestedPrice: {
      type: Boolean,
      default: false
    },
    inventorySearchLink: {
      type: Object
    },
    showViewDetailsLink: {
      type: Boolean,
      default: false
    },
    ctaVariant: {
      type: Object
    },
    inventorySearchVariant: {
      type: Object
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    selected: {
      type: Boolean
    },
    disabled: {
      type: Boolean,
      default: false
    },
    isMobile: {
      type: Boolean,
      default: false
    },
    showCta: {
      type: String,
      default: 'false'
    },
    ctaLabel: {
      type: String
    },
    ctaLabelUrlTarget: {
      type: String,
      default: '_parent'
    },
    tilesVariant: {
      type: String,
      default: ''
    },
    chipsVariant: {
      type: String,
      default: ''
    },
    textVariant: {
      type: String,
      default: ''
    },
    amountsVariant: {
      type: String,
      default: ''
    },
    captionVariant: {
      type: String,
      default: 'dark'
    },
    dividerAboveProperties: {
      type: Boolean,
      default: false
    },
    compareMode: {
      type: String,
      default: 'checkbox',
      validator: value => ['checkbox', 'link'].includes(value)
    },
    compareHref: {
      type: String
    },
    compareTarget: {
      type: String,
      default: '_self'
    },
    compareLabel: {
      type: String
    }
  },
  setup(props) {
    const {showBuildPriceButton, linkLabel, inventorySearchLink} = props
    const {item} = toRefs(props)
    const {$utils, $regions, $ga4} = useContext()
    const {generateConceptHint} = useConcepts()
    const isKorean = ref($regions.current.locale === 'ko-KR')

    const imageUrl = computed(() => props.item.image?.src)
    const metadata = $utils.getCloudinaryAssetMetadata(imageUrl.value)?.metadata

    const imageAlt = (item.value.image && item.value.image.imageAlt) ? item.value.image.imageAlt : metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name] || ''
    const imageTitle = (item.value.image && item.value.image.imageTitle) ? item.value.image.imageTitle : metadata?.context?.caption || metadata?.title || ''
    const imageWidth = computed(() => props.item.image?.imageWidth || null)
    const imageHeight = computed(() => props.item.image?.imageHeight || null)
    const newIndicator = computed(() => item.value.newIndicator && item.value.newIndicator !== 'false' ? (item.value.newIndicator === 'true' ? props.labels.new || 'New' : item.value.newIndicator) : false)
    const customIndicator = computed(() => item.value.customIndicator)

    const link = ref($utils.getLinkField(item.value.link, $regions.current.language.name) || item.value?.link)
    const searchLink = $utils.getLinkField(inventorySearchLink, $regions.current.language.name)

    const bqVariantName = ref(props?.bqVariant?.field)
    const bqVariantColor = ref(props?.bqVariant?.color)
    const modelDetailsVariantName = ref(props?.modelDetailsVariant?.field)
    const modelDetailsVariantColor = ref(props?.modelDetailsVariant?.color)
    const ctaVariantName = ref(props?.ctaVariant?.field)
    const ctaVariantColor = ref(props?.ctaVariant?.color)
    const inventorySearchVariantName = ref(props?.inventorySearchVariant?.field)
    const inventorySearchVariantColor = ref(props?.inventorySearchVariant?.color)

    if (linkLabel && link.value) {
      link.value = {...link.value, label: linkLabel}
    } else if (item.value?.url) {
      link.value = {url: item.value?.url, label: linkLabel}
    }

    const notClickable = ref(!link.value?.url && !item.value?.url)

    const bpLink = computed(() => {
      if (!showBuildPriceButton) {
        return null
      }
      return buildAndPriceUrl(item.value, {
        region: $regions.current?.name,
        language: $regions.current?.language?.name
      })
    })

    const showBuildPriceButtonOpt = computed(() => Boolean(bpLink.value))

    const properties = item.value.properties.filter(p => p?.name && p?.value)
    const captions = ref(item.value.caption ? [item.value.caption] : item.value.captions || [])

    const chips = item.value.chips && item.value.chips['@nodes'].length
      ? item.value.chips['@nodes'].map(x => item.value.chips[x]?.title)
      : []

    const displayItemDescWrapper = ref(item.value?.amountInGroup || (props.showChips && chips) || item.value.info || (!props.hideDescription && properties?.length) || (props.showViewDetailsLink && link.value?.url && link.value?.label))

    const ga4click = (event) => {
      const count = $ga4.getListingCount(
        event,
        'section[data-test-id="evolution-model-listing"]',
        'span[data-test-id="evolution-model-listing-results__count"]'
      )

      const ga4Context = {
        subhead: count
      }

      if (item.value?.suggestedPrice) {
        ga4Context.headline = item.value.suggestedPrice
      }

      $ga4.events.product_click(event, item.value, ga4Context)
    }

    return {
      imageUrl,
      imageAlt,
      imageTitle,
      imageWidth,
      imageHeight,
      link,
      chips,
      newIndicator,
      properties,
      customIndicator,
      showBuildPriceButtonOpt,
      bpLink,
      captions,
      generateConceptHint,
      isKorean,
      notClickable,
      addYoutubeNoCookie,
      ga4click,
      searchLink,
      displayItemDescWrapper,
      BUILD_AND_PRICE_BUTTON_LABEL,
      BUILD_AND_PRICE_CONCEPT_KEY,
      BUILD_AND_PRICE_HINT_CONCEPT_KEY,
      ANCHOR_TARGET,
      bqVariantName,
      bqVariantColor,
      modelDetailsVariantName,
      modelDetailsVariantColor,
      ctaVariantName,
      ctaVariantColor,
      inventorySearchVariantName,
      inventorySearchVariantColor
    }
  }
}
</script>
