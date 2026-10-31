<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base border-0"
          :class="[{'korean-font' : isKorean}, {'with-no-hover' : notClickable}, tilesVariant]"
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

    <div class="d-flex py-2">
      <div class="col-6 p-0 pl-1">
        <ImageSet v-if="!hideImages"
                  :src="imageUrl"
                  :alt="imageAlt"
                  :title="imageTitle"
                  :img-width="imageWidth"
                  :img-height="imageHeight"
                  :preload="preload"
                  data-test-id="product-listing-item__image"
        />
      </div>
      <div class="col-6 p-0 pl-2">
        <b-card-title data-test-id="`product-listing-item-title"
                      :title-tag="item.htag || 'h5'"
                      :class="textVariant"
                      class="mb-0"
        >
          {{ item.title }}
        </b-card-title>
        <div class="d-flex mb-2 flex-wrap">
          <div v-if="showIndicator && imageUrl && customIndicator"
               data-test-id="product-listing-item-custom-indicator"
               class="card-indicator px-1 mr-1 mb-1 d-flex justify-content-center align-items-center text-white bg-dark font-weight-bold text-uppercase"
          >
            {{ customIndicator }}
          </div>
          <div v-if="showIndicator && newIndicator"
               data-test-id="product-listing-item-new-indicator"
               class="card-indicator px-1 mb-1 bg-primary text-white font-weight-bold text-uppercase"
               :class="{'card-indicator__right': !imageUrl}"
          >
            {{ labels.new || 'New' }}
          </div>
          <template v-if="showIndicator && item.listingIndicators && item.listingIndicators.length">
            <div v-for="indicator in item.listingIndicators"
                 :key="indicator"
                 data-test-id="product-listing-item-listing-indicator"
                 class="card-indicator px-1 mb-1 position-absolute d-flex align-items-center bg-dark font-weight-bold text-uppercase"
            >
              {{ indicator }}
            </div>
          </template>
        </div>
        <template v-if="item.suggestedPrice && showSuggestedPrice">
          <p class="mb-0 small">{{ labels.suggestedPrice || 'Suggested Price' }}</p>
          <p class="text-primary mb-0" :class="isMobile ? 'h4' : 'h5'" data-test-id="product-listing-suggested-price">
            {{ item.suggestedPrice }}
          </p>
        </template>
      </div>
    </div>

    <template v-if="!hideCaption && captions.length">
      <b-card-header v-for="(caption, idx) in captions"
                     :key="`listing-${item['@id']}-caption-${idx}`"
                     data-test-id="product-listing-item-caption"
                     :header-text-variant="(captionVariant === 'dark' || captionVariant === 'black') ? 'white' : 'black'"
                     class="small product-listing-item__caption py-1 px-3"
                     :class="captionVariant ? `bg-${captionVariant}` : ''"
      >
        {{ caption }}
      </b-card-header>
    </template>

    <b-card-text v-if="!hideDescription && item.description"
                 data-test-id="product-listing-item-description"
                 text-tag="div"
                 class="p-3 small"
                 v-html="addYoutubeNoCookie(item.description)"
    />

    <Divider v-if="displayItemDescWrapper" />

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
                   class="d-inline-block font-weight-bold small"
                   :class="textVariant ? textVariant : 'text-muted'"
      >
        {{ item.info }}
      </b-card-text>

      <div class="flex-grow-1" />

      <b-card-text v-if="!hideDescription && properties && properties.length"
                   data-test-id="product-listing-item__properties"
                   class="mb-0"
      >
        <b-list-group flush>
          <b-list-group-item v-for="(property, idx) in properties"
                             :key="`${property.name}-${idx}`"
                             :class="[tilesVariant, property.name ? 'd-flex' : 'd-none d-md-flex', idx === properties.length - 1 ? 'pb-2' : 'pb-3']"
                             class="border-0 m-0 p-0 justify-content-between"
          >
            <div class="font-weight-bold flex product-listing-item__property-label small">{{ property.name }}</div>
            <div class="text-right small" v-html="property.value" />
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
    <Divider />

    <div v-if="showInventorySearchButton || showBuildPriceButtonOpt"
         class="d-flex z-index-1 flex-column"
    >
      <b-button v-if="showBuildPriceButtonOpt"
                :href="bpLink"
                block
                variant="outline-primary"
                class="product-listing-item__button text-uppercase text-nowrap p-2 rounded-0 d-flex justify-content-center align-items-center w-100"
                :title="generateConceptHint(BUILD_AND_PRICE_HINT_CONCEPT_KEY, labels[BUILD_AND_PRICE_HINT_CONCEPT_KEY])"
                :target="ANCHOR_TARGET.BLANK"
                rel="noopener nofollow"
                @click="ga4click"
      >
        {{ labels[BUILD_AND_PRICE_CONCEPT_KEY] || BUILD_AND_PRICE_BUTTON_LABEL }}
      </b-button>

      <b-button
        v-if="showInventorySearchButton && searchLink && searchLink.url"
        :href="searchLink.url"
        block
        variant="outline-primary"
        class="product-listing-item__button text-uppercase text-nowrap py-2 rounded-0 d-flex justify-content-center align-items-center m-0 w-100"
        :target="searchLink.target === '_blank_follow' ? '_blank' : searchLink.target || '_parent'"
        @click="ga4click"
      >
        {{ searchLink.label || 'Search Inventory' }}
      </b-button>
    </div>

    <a
      v-if="showCta === 'true' && ctaLabel"
      class="d-flex justify-content-center align-items-center px-5 btn btn-primary text-uppercase"
      data-test-id="cta-button"
      :href="link.url"
      :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
      :rel="item.target === '_blank' ? 'noopener nofollow' : null"
      @click="ga4click"
    >
      {{ ctaLabel }}
    </a>
    <template v-if="showCompare" #footer>
      <div class="d-flex align-content-center justify-content-end">
        <div class="d-flex justify-content-end font-weight-bold m-1 small cursor-pointer w-100"
             :title="generateConceptHint('compare')"
             @click.prevent="() => { if(!disabled || (disabled && selected)) selected ? $emit('remove', item['@id']) : $emit('add', item, item.suggestedPrice ? {headline: item.suggestedPrice} : {})}"
        >
          {{ labels.compare || 'Compare' }}
          <b-form-checkbox :checked="[selected]"
                           :disabled="disabled"
                           class="d-inline-block mr-n1 ml-1 custom-control-input__dark transition-base"
          />
        </div>
      </div>
    </template>
  </b-card>
</template>

<script>
import {computed, ref, toRefs, useContext} from '@nuxtjs/composition-api'
import ImageSet from './ImageSet.vue'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'
import {buildAndPriceUrl} from '~/helpers/buildAndPriceUrl'
import {ANCHOR_TARGET, BUILD_AND_PRICE_BUTTON_LABEL, BUILD_AND_PRICE_CONCEPT_KEY, BUILD_AND_PRICE_HINT_CONCEPT_KEY} from '~/constants/buildAndPrice'

export default {
  components: {ImageSet},
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
    preload: {
      type: Boolean,
      default: false
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
      const isProduct = !item.value?.pageType
      if (isProduct) {
        $ga4.events.product_click(event, item.value, item.value?.suggestedPrice ? {headline: item.value.suggestedPrice} : {})
      }
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
      BUILD_AND_PRICE_BUTTON_LABEL,
      BUILD_AND_PRICE_CONCEPT_KEY,
      BUILD_AND_PRICE_HINT_CONCEPT_KEY,
      ANCHOR_TARGET,
      captions,
      generateConceptHint,
      isKorean,
      notClickable,
      addYoutubeNoCookie,
      ga4click,
      searchLink,
      displayItemDescWrapper
    }
  }
}
</script>
