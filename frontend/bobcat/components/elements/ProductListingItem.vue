<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base border-0"
          :class="[{ 'card-horizontal' : horizontal}, {'korean-font' : isKorean}, notClickable ? 'with-no-hover' : 'with-hover', tilesVariant]"
          no-body
          footer-tag="footer"
          footer-class="z-index-1"
  >
    <a v-if="item && item.url && !notClickable"
       :href="withAttachmentVariant(item.url)"
       class="stretched-link"
       :title="item.title || (!hideImages && imageTitle) || item['@name']"
       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-link`"
       :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : item.target || ctaLabelUrlTarget || '_parent'"
       :rel="item.target === '_blank' ? 'noopener nofollow' : null"
       @click="handleCardClick($event, {url: withAttachmentVariant(item.url), target: item.target})"
    >
      <div v-if="item.evolutionTitle" class="sr-only" v-html="item.evolutionTitle" />
      <div v-else class="sr-only" v-html="item.title || item['@name']" />
    </a>
    <a v-else-if="link && link.url && !notClickable"
       :href="withAttachmentVariant(link.url)"
       class="stretched-link"
       :title="item.title || (!hideImages && imageTitle) || item['@name']"
       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-link`"
       :target="link ? (link.target === '_blank_follow' ? '_blank' : link.target || '_self') : '_self'"
       :rel="link && link.target === '_blank' ? 'noopener nofollow' : null"
       @click="handleCardClick($event, {url: withAttachmentVariant(link.url), target: link.target})"
    >
      <div v-if="item.evolutionTitle" class="sr-only" v-html="item.evolutionTitle" />
      <div v-else class="sr-only" v-html="item.title || item['@name']" />
    </a>

    <template v-if="!horizontal">
      <ImageSet v-if="!hideImages"
                :src="imageUrl"
                :alt="imageAlt"
                :title="imageTitle"
                :img-width="imageWidth"
                :img-height="imageHeight"
                class="card-img-top card-img rounded-0"
                :preload="preload"
                :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}__image`"
      />
      <div v-if="showIndicator && newIndicator"
           :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-new-indicator`"
           class="card-indicator px-2 position-absolute text-white bg-primary font-weight-bold text-uppercase"
           :class="{'card-indicator__right': !imageUrl}"
      >
        {{ labels.new || 'New' }}
      </div>
      <div v-if="showIndicator && imageUrl && videoIndicator"
           :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-video-indicator`"
           class="card-indicator px-2 position-absolute d-flex align-items-center text-white bg-dark font-weight-bold text-uppercase"
      >
        <b-icon icon="video" class="mr-2" />
        {{ labels.video || 'Video' }}
      </div>
      <div v-if="showIndicator && imageUrl && customIndicator"
           :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-custom-indicator`"
           class="card-indicator px-2 position-absolute d-flex align-items-center text-white bg-dark font-weight-bold text-uppercase"
      >
        {{ customIndicator }}
      </div>

      <template v-if="!hideCaption && captions.length">
        <b-card-header v-for="(caption, idx) in captions"
                       :key="`listing-${item['@id']}-caption-${idx}`"
                       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-caption`"
                       :header-text-variant="captionColorVariant.text"
                       class="product-listing-item__caption"
                       :class="captionColorVariant.background"
        >
          {{ caption }}
        </b-card-header>
      </template>

      <b-card-body class="d-flex flex-column px-3 py-3 product-listing-item__body"
                   data-test-id="product-listing-item__content"
                   :class="[{'pt-6' : hideImages && showIndicator && (videoIndicator || newIndicator)}, textVariant]"
      >
        <b-card-text v-if="item.amountInGroup"
                     :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-description`"
                     text-tag="div"
                     :class="amountsVariant ? amountsVariant : 'bg-gray-lighter text-gray-dark-medium'"
                     class="p-2 h6"
                     v-html="`${item.amountInGroup} ${labels.availableAttachments || 'Available attachments'}`"
        />
        <b-card-title v-if="item.evolutionTitle"
                      :title-tag="'div'"
                      :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-evolution-title`"
                      :class="textVariant"
                      v-html="item.evolutionTitle"
        />
        <b-card-title v-else
                      :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-title`"
                      :title-tag="item.htag || 'h4'"
                      :class="textVariant"
        >
          {{ item.title }}
        </b-card-title>

        <b-card-text v-if="showChips && chips" text-tag="div">
          <b-badge v-for="(chip, index) in chips"
                   :key="`item-chips__${item['@id']}__${index}`"
                   :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-badge-${index}`"
                   pill
                   :variant="chipsVariant ? chipsVariant : 'dark'"
                   class="mr-1"
          >
            {{ chip }}
          </b-badge>
        </b-card-text>

        <b-card-text v-if="item.info"
                     :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-info`"
                     class="d-inline-block text-muted font-weight-bold small"
                     :class="textVariant"
        >
          {{ item.info }}
        </b-card-text>
        <b-card-text v-if="showPublishDate && publicationDate"
                     :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-date`"
                     class="d-inline-block text-muted font-weight-bold small"
        >
          {{ publicationDate }}
        </b-card-text>

        <b-card-text v-if="!hideDescription && item.description"
                     :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-description`"
                     text-tag="div"
                     v-html="stripEmptyParagraphs(addYoutubeNoCookie(item.description))"
        />

        <div class="flex-grow-1" />

        <b-card-text v-if="!hideDescription && properties"
                     data-test-id="product-listing-item__properties"
                     class="mb-0"
                     :class="{'pt-3': !hideDescription && item.description}"
        >
          <b-list-group flush>
            <b-list-group-item v-for="(property, idx) in item.properties"
                               :key="`${property.name}-${idx}`"
                               :class="[tilesVariant, property.name ? 'd-flex' : 'd-none d-md-flex', idx === item.properties.length - 1 ? 'pb-2' : 'pb-3']"
                               class="border-0 m-0 p-0 justify-content-between"
            >
              <div class="font-weight-bold flex product-listing-item__property-label small">{{ property.name }}</div>
              <div class="text-right small" v-html="property.value" />
            </b-list-group-item>
          </b-list-group>
        </b-card-text>

        <template v-if="showViewDetailsLink">
          <template
            v-for="(linkItem, index) in links"
          >
            <a
              v-if="linkItem.value && linkItem.value.url && linkItem.value.label"
              :key="`card-link-${index}`"
              :href="linkItem.value.url"
              class="font-weight-bold link align-items-center z-index-1"
              :class="textVariant"
              :target="linkItem.value.target === '_blank_follow' ? '_blank' : linkItem.value.target || '_self'"
              :rel="linkItem.value.target === '_blank' ? 'noopener nofollow' : null"
              data-test-id="product-listing-item-link"
              @click="handleLinkClick($event, linkItem.value)"
            >{{ linkItem.value.label }} <b-icon icon="chevron-right" />
            </a>
          </template>
        </template>
      </b-card-body>
    </template>

    <b-row v-else no-gutters>
      <b-col v-if="!hideImages || (showIndicator && (newIndicator || videoIndicator))"
             md="6"
             class="text-white d-flex align-items-center justify-content-center"
             :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}__image-wrapper`"
      >
        <ImageSet v-if="!hideImages"
                  :src="imageUrl"
                  :alt="imageAlt"
                  :title="imageTitle"
                  :img-width="imageWidth"
                  :img-height="imageHeight"
                  class="card-img-top card-img rounded-0"
                  :preload="preload"
                  :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}__image`"
        />
        <div v-if="showIndicator && newIndicator"
             :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-new-indicator`"
             class="card-indicator px-2 position-absolute bg-primary font-weight-bold text-uppercase"
             :class="{'card-indicator__right': !imageUrl}"
        >
          {{ labels.new || 'New' }}
        </div>
        <div v-if="showIndicator && imageUrl && videoIndicator"
             :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-video-indicator`"
             class="card-indicator px-2 position-absolute d-flex align-items-center bg-dark font-weight-bold text-uppercase"
        >
          <b-icon icon="video" class="mr-2" />
          {{ labels.video || 'Video' }}
        </div>

        <template v-if="showIndicator && item.listingIndicators && item.listingIndicators.length">
          <div v-for="indicator in item.listingIndicators"
               :key="indicator"
               :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-listing-indicator`"
               class="card-indicator px-2 position-absolute d-flex align-items-center bg-dark font-weight-bold text-uppercase"
          >
            {{ indicator }}
          </div>
        </template>
        <template v-if="imageUrl && !hideCaption && captions.length">
          <div v-for="(caption, idx) in captions"
               :key="`listing-${item['@id']}-caption-${idx}`"
               :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-caption`"
               class="card-header bg-dark position-absolute small"
               :class="[captionColorVariant.background, 'text-' + captionColorVariant.text]"
          >
            {{ caption }}
          </div>
        </template>
      </b-col>

      <b-col md="6"
             :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}__content`"
             :class="{'pt-6' : hideImages && showIndicator && (videoIndicator || newIndicator)}"
      >
        <b-card-body class="d-flex flex-column py-3 h-100">
          <b-card-text v-if="item.amountInGroup"
                       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-description`"
                       text-tag="div"
                       :class="amountsVariant ? amountsVariant : 'bg-gray-lighter text-gray-dark-medium'"
                       class="p-2 h6"
                       v-html="`${item.amountInGroup} ${labels.availableAttachments || 'Available attachments'}`"
          />
          <b-card-title v-if="item.evolutionTitle"
                        :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-evolution-title`"
                        :class="textVariant"
                        :title-tag="'div'"
                        v-html="item.evolutionTitle"
          />
          <b-card-title v-else
                        :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-title`"
                        :title-tag="item.htag || 'h5'"
                        :class="textVariant"
          >
            {{ item.title }}
          </b-card-title>

          <b-card-text v-if="showChips && chips" text-tag="div">
            <b-badge v-for="(c, i) in chips"
                     :key="`item-chips__${item['@id']}__${i}`"
                     pill
                     :variant="chipsVariant ? chipsVariant : 'dark'"
                     class="mr-1 mb-3"
                     :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-badge-${i}`"
            >
              {{ c }}
            </b-badge>
          </b-card-text>

          <b-card-text v-if="item.info"
                       class="d-inline-block text-muted font-weight-bold small"
                       :class="textVariant"
                       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-info`"
          >
            {{ item.info }}
          </b-card-text>
          <b-card-text v-if="showPublishDate && publicationDate"
                       class="d-inline-block text-muted font-weight-bold small"
                       :class="textVariant"
                       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-date`"
          >
            {{ publicationDate }}
          </b-card-text>

          <b-card-text v-if="!hideDescription && item.description"
                       :data-test-id="`${isCarouselCard ? 'content-carousel-item' : 'product-listing-item'}-description`"
                       text-tag="div"
                       :class="textVariant"
                       class="mb-2"
                       v-html="stripEmptyParagraphs(addYoutubeNoCookie(item.description))"
          />

          <div class="flex-grow-1" />

          <b-card-text v-if="!hideDescription && properties"
                       class="pb-0"
                       :class="tilesVariant"
                       data-test-id="product-listing-item__properties"
          >
            <b-list-group>
              <b-list-group-item v-for="(property, idx) in item.properties"
                                 :key="`${property.name}-${idx}`"
                                 class="small"
                                 :class="tilesVariant"
              >
                {{ property.name }} : {{ property.value }}
              </b-list-group-item>
            </b-list-group>
          </b-card-text>
          <div v-if="showViewDetailsLink" class="d-flex flex-column flex-md-row">
            <template
              v-for="(linkItem, index) in links"
            >
              <a
                v-if="linkItem.value && linkItem.value.url && linkItem.value.label"
                :key="`card-link-${index}`"
                :href="linkItem.value.url"
                class="font-weight-bold link mr-4 mb-2 align-items-center z-index-1"
                :class="textVariant"
                :target="linkItem.value.target === '_blank_follow' ? '_blank' : linkItem.value.target || '_self'"
                :rel="linkItem.value.target === '_blank' ? 'noopener nofollow' : null"
                data-test-id="product-listing-item-link"
                @click="handleLinkClick($event, linkItem.value)"
              >{{ linkItem.value.label }} <b-icon icon="chevron-right" />
              </a>
            </template>
          </div>
        </b-card-body>
      </b-col>
    </b-row>
    <div v-if="showBuildPriceButtonOpt" class="d-flex z-index-1">
      <b-button :href="bpLink"
                block
                variant="outline-primary"
                class="product-listing-item__button text-uppercase text-nowrap py-2 rounded-0 d-flex justify-content-center align-items-center"
                :title="generateConceptHint(BUILD_AND_PRICE_HINT_CONCEPT_KEY, labels[BUILD_AND_PRICE_HINT_CONCEPT_KEY])"
                :target="ANCHOR_TARGET.BLANK"
                rel="noopener nofollow"
                @click="ga4click"
      >
        {{ labels[BUILD_AND_PRICE_CONCEPT_KEY] || BUILD_AND_PRICE_BUTTON_LABEL }}
      </b-button>
      <b-button
        v-if="showCta === 'true' && ctaLabel"
        :href="withAttachmentVariant(link.url)"
        :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
        block
        variant="primary"
        data-test-id="cta-button"
        class="product-listing-item__button text-uppercase text-nowrap py-2 mt-0 rounded-0 d-flex justify-content-center align-items-center"
        @click="ga4click"
      >
        {{ ctaLabel }}
      </b-button>
    </div>
    <a
      v-if="!showBuildPriceButtonOpt && showCta === 'true' && ctaLabel"
      class="d-flex justify-content-center align-items-center px-5 btn btn-primary text-capitalize mx-3 z-index-1"
      :class="{'mb-3': !showCompare}"
      data-test-id="cta-button"
      :href="withAttachmentVariant(link.url)"
      :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
      :rel="item.target === '_blank' ? 'noopener nofollow' : null"
      @click="ga4click"
    >
      {{ ctaLabel }}
    </a>
    <template v-if="showCompare" #footer>
      <div class="d-flex align-content-center justify-content-end">
        <div class="font-weight-bold m-1 small cursor-pointer"
             :title="generateConceptHint('compare')"
             @click.prevent="() => { if(!disabled || (disabled && selected)) selected ? $emit('remove', item['@id']) : $emit('add', item)}"
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
import {computed, onMounted, ref, toRefs, useContext, useRouter} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '~/constants/variants/captionVariants'
import {addYoutubeNoCookie, appendVariantParam, getNodeName, stripEmptyParagraphs} from '~/helpers/functional/AppHelpers'
import {PRODUCT_NODE_TYPES} from '~/constants/magnolia'
import useConcepts from '~/helpers/composables/useConcepts'
import useContentLinkWatcher from '~/helpers/composables/useContentLinkWatcher'
import {buildAndPriceUrl} from '~/helpers/buildAndPriceUrl'
import {ANCHOR_TARGET, BUILD_AND_PRICE_BUTTON_LABEL, BUILD_AND_PRICE_CONCEPT_KEY, BUILD_AND_PRICE_HINT_CONCEPT_KEY} from '~/constants/buildAndPrice'

export default {
  props: {
    item: {
      type: Object,
      required: true
    },
    linkLabel: {
      type: String
    },
    horizontal: {
      type: Boolean,
      default: false
    },
    showIndicator: {
      type: Boolean,
      default: true
    },
    showChips: {
      type: Boolean,
      default: true
    },
    isCarouselCard: {
      type: Boolean,
      default: false
    },
    showCompare: {
      type: Boolean,
      default: false
    },
    showPublishDate: {
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
    isFlexibleListing: {
      type: Boolean,
      default: false
    },
    preload: {
      type: Boolean,
      default: false
    }
  },
  setup(props, {emit}) {
    const {showBuildPriceButton, linkLabel} = props
    const {item} = toRefs(props)
    const {$utils, $regions, $ga4} = useContext()
    const router = useRouter()
    const {generateConceptHint} = useConcepts()
    const isKorean = ref($regions.current.locale === 'ko-KR')
    const captionColorVariant = ref(VARIANTS_CLASSES[props.captionVariant] || {
      text: 'white',
      background: 'bg-dark'
    })

    const imageUrl = computed(() => props.item.image?.src)
    const metadata = $utils.getCloudinaryAssetMetadata(imageUrl.value)?.metadata

    const imageAlt = (item.value.image && item.value.image.imageAlt) ? item.value.image.imageAlt : metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name] || ''
    const imageTitle = (item.value.image && item.value.image.imageTitle) ? item.value.image.imageTitle : metadata?.context?.caption || metadata?.title || ''
    const imageWidth = computed(() => props.item.image?.imageWidth || null)
    const imageHeight = computed(() => props.item.image?.imageHeight || null)
    const newIndicator = computed(() => item.value.newIndicator && item.value.newIndicator !== 'false' ? (item.value.newIndicator === 'true' ? props.labels.new || 'New' : item.value.newIndicator) : false)
    const videoIndicator = computed(() => item.value.videoIndicator && item.value.videoIndicator === 'true')
    const customIndicator = computed(() => item.value.customIndicator)

    const publishDate = item.value.postDate || item.value.publishDate ? new Date(item.value.postDate || item.value.publishDate).toISOString().split('T')[0] : null
    const isoLangParam = $regions?.current?.locale || `${$regions?.current?.language.name}-us`
    const publicationDate = publishDate
      ? new Date(publishDate)?.toLocaleDateString(isoLangParam, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }) || publishDate
      : null

    const link = ref($utils.getLinkField(item.value.link, $regions.current.language.name) || item.value?.link)
    const link2 = ref($utils.getLinkField(item.value.link2, $regions.current.language.name) || item.value?.link2)
    const link3 = ref($utils.getLinkField(item.value.link3, $regions.current.language.name) || item.value?.link3)

    if (linkLabel && link.value) {
      link.value = {...link.value, label: linkLabel}
    } else if (item.value?.url) {
      link.value = {url: item.value?.url, label: linkLabel}
    }

    const notClickable = ref((!link.value?.url && !item.value?.url) || link2.value?.url || link3.value?.url)

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

    onMounted(() => {
      emit('mounted')
    })

    const properties = item.value.properties
    const captions = ref(item.value.caption ? [item.value.caption] : item.value.captions || [])

    const chips = item.value.chips && item.value.chips['@nodes'].length
      ? item.value.chips['@nodes'].map(x => item.value.chips[x]?.title)
      : []

    const ga4click = (event) => {
      const isProduct = !item.value?.pageType
      if (props.isFlexibleListing) {
        const ctaText = event?.target?.classList?.contains('stretched-link') ? '' : event?.target?.innerText
        emit('list-click', ctaText, item.value.evolutionTitle?.replace(/<[^>]*>/g, '') || item.value.title)
      } else if (isProduct) {
        $ga4.events.product_click(event, item.value)
      }

      if (props.isCarouselCard) {
        $ga4.events.carousel_click(event)
      }
    }

    const handleLinkClick = (event, linkItem) => {
      ga4click(event)
      const target = linkItem.target === '_blank_follow' ? '_blank' : linkItem.target || '_self'
      if (target === '_self') {
        event.preventDefault()
        try {
          const url = new URL(linkItem.url, window.location.origin)
          if (url.origin === window.location.origin) {
            router.push({path: url.pathname, query: Object.fromEntries(url.searchParams), hash: url.hash})
          } else {
            window.location.href = linkItem.url
          }
        } catch (e) {
          window.location.href = linkItem.url
        }
      }
    }

    const handleCardClick = (event, linkItem) => {
      if (props.isFlexibleListing && linkItem?.url) {
        handleLinkClick(event, linkItem)
      } else {
        ga4click(event)
      }
    }

    const descriptionRef = ref(null)
    useContentLinkWatcher(descriptionRef, ga4click)

    const links = computed(() => [
      link,
      link2,
      link3
    ])

    const attachmentVariantName = computed(() => {
      const isAttachment = item.value?.attachmentType === PRODUCT_NODE_TYPES.ATTACHMENT ||
        item.value?.['@nodeType'] === PRODUCT_NODE_TYPES.ATTACHMENT ||
        item.value?.type === 'attachment'
      return isAttachment ? getNodeName(item.value) : null
    })
    const withAttachmentVariant = url => appendVariantParam(url, attachmentVariantName.value)

    return {
      captionColorVariant,
      imageUrl,
      imageAlt,
      imageTitle,
      imageWidth,
      imageHeight,
      link,
      links,
      chips,
      newIndicator,
      properties,
      videoIndicator,
      customIndicator,
      showBuildPriceButtonOpt,
      bpLink,
      BUILD_AND_PRICE_BUTTON_LABEL,
      BUILD_AND_PRICE_CONCEPT_KEY,
      BUILD_AND_PRICE_HINT_CONCEPT_KEY,
      ANCHOR_TARGET,
      captions,
      generateConceptHint,
      publicationDate,
      isKorean,
      notClickable,
      addYoutubeNoCookie,
      stripEmptyParagraphs,
      withAttachmentVariant,
      ga4click,
      handleLinkClick,
      handleCardClick
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';
@import '[media-breakpoint-down] from ~bootstrap/scss/mixins';

.card {
  div[data-test-id="content-carousel-item-description"],
  div[data-test-id="product-listing-item-description"] {
    p {
      margin-bottom: 0 !important;
      margin-top: 0 !important;
    }
  }

  &.card-horizontal.evolution-shadow {
    .card-header {
      border-bottom-width: 0;
      border-top-width: 0;
    }
  }
}

.product-listing-item__body {
  gap: 1rem; // 16px uniform gap between every section

  > .card-title,
  > .card-text,
  > div {
    margin-bottom: 0 !important;
    margin-top: 0 !important;
  }

  .card-title {
    margin-bottom: 0 !important;
  }

  .card-text {
    margin-bottom: 0 !important;
  }

  // Collapse empty flex children so they don't contribute extra gap
  > .flex-grow-1 {
    margin: 0 !important;
    gap: 0;
    min-height: 0;
  }

  // Empty card-text elements (no visible content) should not take gap space
  > .card-text:empty,
  > div:empty:not(.flex-grow-1) {
    display: none;
  }

  > a,
  > template + a,
  > .link {
    margin-top: 0; // gap handles spacing
  }
}

// Hide flex-grow-1 spacer in carousel cards (single slides don't need bottom-alignment)
section[data-test-id="evolution-content-carousel"] .product-listing-item__body > .flex-grow-1 {
  display: none;
}

.product-listing-item__property-label {
  min-width: 50%;
  max-width: 75%;
}

.product-listing-item__caption {
  margin-bottom: 1px;
}

.product-listing-item__button {
  font-size: $font-size-sm;
}

.card-title {
  h1, h2, h3, h4, h5, h6, p {
    margin-bottom: 0 !important;
    margin-top: 0 !important;
  }

  // Tighten spacing between heading and subtitle within evolution title
  div, span {
    margin-bottom: 0 !important;
    margin-top: 0 !important;
  }
}

.korean-font {
  h2 {
    font-size: 2.5rem
  }

  h3 {
    font-size: 2rem
  }

  h4 {
    font-size: 1.3rem
  }

  @include media-breakpoint-down(sm) {
    h2 {
      font-size: 1.4rem
    }

    h3 {
      font-size: 1.3rem
    }

    h4 {
      font-size: 1.2rem
    }
  }
}
</style>
