<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base border rounded-0 position-relative evolution-attachment-item"
          :class="{'korean-font' : isKorean}"
          no-body
          footer-tag="footer"
          footer-class="z-index-1"
  >
    <a v-if="primaryHref"
       :href="primaryHref"
       class="stretched-link"
       :title="item.title || item.name || item['@name']"
       :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : item.target || ctaLabelUrlTarget || '_parent'"
       :rel="item.target === '_blank' ? 'noopener nofollow' : null"
       @click="event => $emit('click', event)"
    >
      <div v-html="item.title || item.name || item['@name']" />
    </a>
    <a v-else-if="link && link.url"
       :href="appendVariantParam(link.url, getNodeName(item))"
       class="stretched-link"
       :title="item.title || item.name || item['@name']"
       :target="link ? (link.target === '_blank_follow' ? '_blank' : link.target || '_self') : '_self'"
       :rel="link && link.target === '_blank' ? 'noopener nofollow' : null"
       @click="event => $emit('click', event)"
    >
      <div v-html="item.title || item.name || item['@name']" />
    </a>

    <ImageSet v-if="!hideImages"
              :src="imageUrl"
              :alt="imageAlt"
              :title="imageTitle"
              :img-width="imageWidth"
              :img-height="imageHeight"
              data-test-id="compatible-attachments-item__image"
    />
    <div class="attachment-line position-absolute w-100 d-flex transition-base" />
    <div class="indicators-wrapper d-flex mb-2 flex-column align-items-start position-absolute w-100">
      <div v-if="showIndicator && newIndicator"
           data-test-id="compatible-attachments-item-new-indicator"
           class="card-indicator px-1 mb-1 bg-primary text-white font-weight-bold text-uppercase"
           :class="{'card-indicator__right': !imageUrl}"
      >
        {{ labels.new || 'New' }}
      </div>

      <div v-if="showIndicator && imageUrl && customIndicator"
           data-test-id="compatible-attachments-item-custom-indicator"
           class="card-indicator px-1 mr-1 mb-1 d-flex justify-content-center align-items-center text-white bg-black font-weight-bold text-uppercase"
      >
        {{ customIndicator }}
      </div>

      <template v-if="showIndicator && item.listingIndicators && item.listingIndicators.length">
        <div v-for="indicator in item.listingIndicators"
             :key="indicator"
             data-test-id="compatible-attachments-item-listing-indicator"
             class="card-indicator px-1 mr-1 mb-1 d-flex justify-content-center align-items-center text-white bg-black font-weight-bold text-uppercase"
        >
          {{ indicator }}
        </div>
      </template>
    </div>

    <b-card-body class="d-flex flex-column px-3 py-2"
                 data-test-id="compatible-attachments-item__content"
                 :class="[{'pt-6' : hideImages && showIndicator && newIndicator}]"
    >
      <b-card-text v-if="item.amountInCategory"
                   text-tag="div"
                   class="h6 mb-0"
                   v-html="`${item.amountInCategory}  ${labels.optionsAvailable || 'Options Available'}`"
      />
      <b-card-text v-if="item.amountInGroup"
                   text-tag="div"
                   class="h6 mb-0"
                   v-html="`${item.amountInGroup} ${labels.availableAttachments || 'Available attachments'}`"
      />
      <b-card-title v-if="item.name"
                    :title-tag="item.htag || 'h4'"
                    class="my-1"
      >
        {{ item.name }}
      </b-card-title>

      <b-card-text v-if="!hideDescription && item.shortDescription"
                   text-tag="div"
                   v-html="addYoutubeNoCookie(item.shortDescription)"
      />

      <div class="flex-grow-1" />
    </b-card-body>

    <div v-if="showCta === 'true' && ctaHref" class="d-flex justify-content-start pb-2 z-index-1">
      <DynamicButton
        :variant="BUTTON_NAMES.LINEAR"
        color="red"
        :href="ctaHref"
        :target="ctaLabelUrlTarget === '_blank_follow' ? '_blank' : ctaLabelUrlTarget || '_parent'"
        :rel="item.target === '_blank' ? 'noopener nofollow' : null"
        class="d-flex justify-content-start align-items-center pt-0 pb-2 small-variant"
        @click="event => $emit('click', event)"
      >
        {{ ctaLabel }}
        <b-icon icon="chevron-right" class="ml-2 small" />
      </DynamicButton>
    </div>
  </b-card>
</template>

<script>
import {computed, onMounted, ref, toRefs, useContext} from '@nuxtjs/composition-api'
import {addYoutubeNoCookie, appendVariantParam, getNodeName} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'
import {BUTTON_NAMES} from '~/constants/buttons'
import {CTA_LINKS_TYPE} from '~/constants/compatibleAttachments'

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
    hideImages: {
      type: Boolean,
      default: false
    },
    hideDescription: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      default: () => ({})
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
    ctaLinksType: {
      type: String,
      default: CTA_LINKS_TYPE.CURRENT_LEVEL
    }
  },
  setup(props, {emit}) {
    const {linkLabel} = props
    const {item} = toRefs(props)
    const {$utils, $regions} = useContext()
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

    if (linkLabel && link.value) {
      link.value = {...link.value, label: linkLabel}
    } else if (item.value?.url) {
      link.value = {url: item.value?.url, label: linkLabel}
    }

    const ownUrl = computed(() => link.value?.url || null)

    const isGroupTile = computed(() => item.value.amountInGroup != null)
    const isCategoryTile = computed(() => item.value.amountInCategory != null)
    const isModelTile = computed(() => !isGroupTile.value && !isCategoryTile.value)

    const childLevelUrl = computed(() => {
      const url = ownUrl.value
      if (!url) { return null }
      return isModelTile.value ? appendVariantParam(url, getNodeName(item.value)) : url
    })

    const parentLevelUrl = computed(() => {
      const url = ownUrl.value
      if (!url) { return null }
      if (isModelTile.value) { return url }
      const normalizedUrl = url.split(/[?#]/)[0].replace(/\/+$/, '')
      const parts = normalizedUrl.split('/')
      return parts.length > 2 ? parts.slice(0, -1).join('/') : null
    })

    const goesToParentLevel = computed(() => props.ctaLinksType === CTA_LINKS_TYPE.PARENT_LEVEL)

    const primaryHref = computed(() => goesToParentLevel.value ? (parentLevelUrl.value || childLevelUrl.value) : childLevelUrl.value)
    const ctaHref = computed(() => primaryHref.value)

    onMounted(() => {
      emit('mounted')
    })

    const properties = item.value.properties

    return {
      imageUrl,
      imageAlt,
      imageTitle,
      imageWidth,
      imageHeight,
      link,
      newIndicator,
      properties,
      customIndicator,
      generateConceptHint,
      isKorean,
      addYoutubeNoCookie,
      appendVariantParam,
      getNodeName,
      primaryHref,
      ctaHref,
      BUTTON_NAMES
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';

.evolution-grid-item {
  &.gray-darker .evolution-attachment-item {
    border: 1px solid $gray-800 !important;
  }

  &.black .evolution-attachment-item {
    border: 1px solid $black !important;
  }

  .evolution-attachment-item {
    border: 1px solid $tertiary !important;
    box-shadow: none !important;

    .indicators-wrapper {
      top: .5rem
    }

    &:hover {
      box-shadow: .25rem .25rem .25rem .25rem rgba(0, 0, 0, 0.1) !important;

      .attachment-line {
        border-color: $primary;
      }
    }

    .attachment-line {
      top: 0;
      left: 0;
      border: 2px solid transparent;
    }
  }
}
</style>
