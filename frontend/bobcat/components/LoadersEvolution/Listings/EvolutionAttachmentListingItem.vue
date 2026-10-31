<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base evolution-listing-attachment-item rounded-0 text-black"
          :class="[{'korean-font' : isKorean}, {'with-no-hover' : notClickable}, tilesVariant, {'pb-3' : !showCompare}]"
          no-body
          footer-tag="footer"
          footer-class="z-index-1"
  >
    <a v-if="item && item.url"
       :href="appendVariantParam(item.url, getNodeName(item))"
       class="stretched-link"
       :title="item.title || item['@name']"
       data-test-id="product-listing-item-link"
       :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
       :rel="item.target === '_blank' ? 'noopener nofollow' : null"
       @click="ga4click"
    >{{ item.title || item['@name'] }}</a>
    <a v-else-if="link && link.url"
       :href="appendVariantParam(link.url, getNodeName(item))"
       class="stretched-link"
       :title="item.title || item['@name']"
       data-test-id="product-listing-item-link"
       :target="link ? (link.target === '_blank_follow' ? '_blank' : link.target || '_self') : '_self'"
       :rel="link && link.target === '_blank' ? 'noopener nofollow' : null"
       @click="ga4click($event, item)"
    >{{ item.title || item['@name'] }}</a>

    <div class="d-flex flex-column">
      <div class="d-flex justify-content-center align-items-center position-relative">
        <ImageSet v-if="!hideImages"
                  :src="imageUrl"
                  :alt="imageAlt"
                  :title="imageTitle"
                  :img-width="imageWidth"
                  :img-height="imageHeight"
                  :cloudinary-options="{
                    crop: 'fill',
                    gravity: 'auto'
                  }"
                  :options="{
                    fluid: true,
                    style: 'width:100%;height:154px;object-fit:fill;display:block;'
                  }"
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
        <div v-if="item.subCategoryTitle || item.categoryTitle" class="font-weight-bold text-gray-dark-medium">
          {{ item.subCategoryTitle || item.categoryTitle }}
        </div>
        <div v-if="item.amountInCategory" class="text-gray-dark-medium">
          {{ item.amountInCategory }} {{ labels.optionsAvailable || 'Options Available' }}
        </div>
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

    <b-card-body
      v-if="displayItemDescWrapper"
      class="d-flex flex-column px-3 py-2"
      data-test-id="product-listing-item__content"
      :class="[{'pt-6' : hideImages && showIndicator && newIndicator}, textVariant]"
    >
      <b-card-text v-if="compatibleMachines"
                   data-test-id="product-listing-item-compatible-machines"
                   text-tag="div"
                   class="border-top border-color-tertiary"
      >
        <div class="mt-2 mb-1">{{ labels.compatibleMachines || 'Compatible Machines:' }}</div>
        <b-badge v-for="(compatibleMachine, index) in compatibleMachines.slice(0, 3)"
                 :key="`item-chips__${item['@id']}__${index}`"
                 :data-test-id="`product-listing-item-badge-${index}`"
                 pill
                 variant="white"
                 class="mr-1 mb-1 border border-color-tertiary"
        >
          {{ compatibleMachine }}
        </b-badge>
        <span v-if="compatibleMachines.length > 3">+{{ compatibleMachines.length - 3 }} {{ labels.more || 'More' }}</span>
      </b-card-text>

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
    </b-card-body>
    <div v-else class="flex-grow-1" />

    <div v-if="showCta === 'true' && ctaLabel" class="px-3 mt-3 z-index-1">
      <DynamicButton
        :variant="ctaVariantName"
        :color="ctaVariantColor"
        :href="appendVariantParam(link.url, getNodeName(item))"
        :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
        :rel="item.target === '_blank' ? 'noopener nofollow' : null"
        class="w-100"
        @click="ga4click"
      >
        {{ ctaLabel }}
      </DynamicButton>
    </div>
  </b-card>
</template>

<script>
import {computed, ref, toRefs, useContext} from '@nuxtjs/composition-api'
import {addYoutubeNoCookie, appendVariantParam, getNodeName} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
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
    showSuggestedPrice: {
      type: Boolean,
      default: false
    },
    showViewDetailsLink: {
      type: Boolean,
      default: false
    },
    ctaVariant: {
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
    captionVariant: {
      type: String,
      default: 'dark'
    }
  },
  setup(props) {
    const {linkLabel} = props
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

    const ctaVariantName = ref(props?.ctaVariant?.field)
    const ctaVariantColor = ref(props?.ctaVariant?.color)

    if (linkLabel && link.value) {
      link.value = {...link.value, label: linkLabel}
    } else if (item.value?.url) {
      link.value = {url: item.value?.url, label: linkLabel}
    }

    const notClickable = ref(!link.value?.url && !item.value?.url)

    const properties = (item.value.properties || []).filter(p => p?.name && p?.value)
    const captions = ref(item.value.caption ? [item.value.caption] : item.value.captions || [])

    const chips = item.value.chips && item.value.chips['@nodes'].length
      ? item.value.chips['@nodes'].map(x => item.value.chips[x]?.title)
      : []

    const compatibleMachines = item.value.compatibleMachines?.machines
      ? item.value.compatibleMachines.machines.map(x => x.name)
      : []

    const displayItemDescWrapper = ref(item.value?.amountInGroup || (props.showChips && chips) || item.value.info || (!props.hideDescription && properties?.length) || (props.showViewDetailsLink && link.value?.url && link.value?.label))

    const ga4click = (event, item) => {
      const count = $ga4.getListingCount(
        event,
        'section[data-test-id="evolution-model-listing"]',
        'span[data-test-id="evolution-model-listing-results__count"]'
      )

      const ga4Context = {
        subhead: count,
        item_category: 'Attachments',
        click_text: event.target.innerText
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
      captions,
      generateConceptHint,
      isKorean,
      notClickable,
      addYoutubeNoCookie,
      appendVariantParam,
      getNodeName,
      ga4click,
      displayItemDescWrapper,
      ctaVariantName,
      ctaVariantColor,
      compatibleMachines
    }
  }
}
</script>
