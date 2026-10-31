<template>
  <div class="p-0 bg-white feature-container transition-base" :class="{'expanded' : isVisible}" data-test-id="features-feature">
    <Accordion :toggle-cb="toggleCallback"
               :htag="secondaryAccordionHtag"
               :color="color"
               :title="feature.title"
               border
               :is-expanded="isVisible"
               :animation-enabled="animationEnabled"
               @expanded="$emit('expanded')"
               @click="ga4click"
    >
      <div class="d-flex flex-column px-3 pt-3 transition-base" :class="[selectedColorVariant.cardBackground, selectedColorVariant.cardText]">
        <b-card-text v-if="chips" text-tag="div">
          <template v-for="(chip, index) in chips">
            <b-badge
              v-if="chip && chip.text"
              :key="`feature-chips__${feature['@id']}__${index}`"
              :data-test-id="`feature-chips__${feature['@id']}__${index}`"
              pill
              :variant="selectedColorVariant.chipsColor"
              class="mr-1 mb-3"
              v-html="chip.text"
            />
          </template>
        </b-card-text>
        <div v-if="feature.shortDescription"
             data-test-id="feature-short-description-expanded"
             v-html="feature.shortDescription"
        />
        <b-row>
          <b-col v-if="feature.description" cols="12" :md="assets && assets.length ? 6 : 12">
            <div data-test-id="feature-description" v-html="feature.description" />
            <a
              v-if="link"
              :href="link.url"
              :target="link.target === '_blank_follow' ? '_blank' : link.target"
              :rel="link.target === '_blank' ? 'noopener nofollow' : null"
              class="d-block my-3 font-weight-bold"
              data-test-id="cta-button"
              :class="{'mr-3':(brochure && brochure.filePath)}"
              @click.stop="ga4click"
            >
              {{ link.label }} <b-icon icon="chevron-right" />
            </a>
            <a
              v-if="(brochure && brochure.filePath)"
              class="d-block my-3 font-weight-bold"
              data-test-id="brochure-button"
              target="_blank"
              rel="nofollow"
              :href="getBrochureUrl(brochure.filePath)"
              @click.prevent="(e) => {ga4click(e); download(brochure.filePath)}"
            >
              {{ brochure.label || 'Download brochure' }} <b-icon icon="chevron-right" />
            </a>
          </b-col>
          <b-col v-if="assets && assets.length" cols="12" :md="feature.description ? 6 : 12">
            <CloudinaryGallery v-if="isVisible" :ref="(el) => storeCldElement(el)" :images="assets" />
          </b-col>
        </b-row>
      </div>
    </Accordion>
    <div v-if="feature.shortDescription && !isVisible"
         class="px-3"
         data-test-id="feature-short-description"
         v-html="feature.shortDescription"
    />
  </div>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import useCloudinary from '../../../helpers/composables/useCloudinary'
import {COLORS, VARIANTS_CLASSES} from '../../../constants/variants/accordionGroupVariants'
import useModels from '@/helpers/composables/useModels'

export default {
  props: {
    feature: {
      type: Object,
      required: true
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    secondaryAccordionHtag: {
      type: String,
      default: 'h4'
    },
    color: {
      type: String,
      default: COLORS.WHITE
    },
    animationEnabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const {feature} = props
    const {model} = useModels()
    const {$utils, $regions, $ga4} = useContext()
    const {mapCloudinaryGalleryAssets} = useCloudinary()
    const link = $utils.getLinkField(feature.link, $regions.current.language.name)
    const isVisible = ref(props.isExpanded)
    const cloudinary = ref(null)
    const brochure = ref(feature?.brochure?.field === 'none' ? null : feature?.brochure)
    const chips = ref(feature?.chips?.map(c => ({...c, color: c?.chipcolor, text: c?.['value' + c?.value]})) || [])
    const selectedColorVariant = ref(VARIANTS_CLASSES[props.color])

    const toggleCallback = async (isAccordionExpanded) => {
      if (!isAccordionExpanded) {
        if (cloudinary.value?.destroy) { await cloudinary.value.destroy() }
      }
      isVisible.value = isAccordionExpanded
    }

    const storeCldElement = (el) => {
      cloudinary.value = el
    }

    const getBrochureUrl = (url) => {
      return $utils.getCloudinaryDocument(url)
    }

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const setVisible = (bool) => {
      isVisible.value = bool
    }

    const assets = mapCloudinaryGalleryAssets(feature.assets)

    const ga4click = (event) => {
      $ga4.events.product_features_click(event, model)
    }

    return {
      COLORS,
      link,
      assets,
      isVisible,
      setVisible,
      toggleCallback,
      selectedColorVariant,
      download,
      brochure,
      getBrochureUrl,
      ga4click,
      chips,
      storeCldElement
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.img-wrapper {
    padding-bottom: 66.6%; // 3:2 ratio
}

.cld-gallery {
  min-height: auto !important;
}
</style>
