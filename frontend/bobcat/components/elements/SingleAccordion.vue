<template>
  <Accordion :id="accordion['@id']"
             :toggle-cb="toggleCallback"
             :is-expanded="isVisible"
             :border="border || [COLORS.WHITE, COLORS.DARK, COLORS.BLACK].includes(color)"
             :color="color || accordion.color"
             :title="accordion.title"
             :icon="accordion.icon"
             :is-primary-accordion="isPrimaryAccordion"
             :shadow="shadow"
             :htag="htag"
             :animation-enabled="animationEnabled"
             @expanded="$emit('expanded')"
             @click="event => $emit('click', event)"
  >
    <div class="p-4 transition-base accordion-content" :class="[selectedColorVariant.cardBackground, selectedColorVariant.cardText, {'has-media': hasMedia}]">
      <div class="accordion-content-text">
        <div v-if="accordion.description" ref="detailsRef" class="accordion-description" data-test-id="accordion-description" v-html="accordion.description" />
      </div>
      <div v-if="hasMedia" class="accordion-content-media">
        <CloudinaryGallery v-if="isVisible && accordion.assets && accordion.assets.length" :ref="(el) => storeCldElement(el)" :images="accordion.assets" class="mb-3" />
        <div v-for="(yt, index) in accordion.youtubeVideos"
             :key="yt + index"
             class="mt-2"
        >
          <YoutubeIframe v-if="yt" :src="yt" :height="500" />
        </div>
      </div>
      <div class="accordion-content-actions">
        <div class="d-flex flex-wrap flex-md-nowrap">
          <DynamicButton
            v-if="accordion.link && accordion.link.field && accordion.link.field !== 'none' && accordionLinkData.href"
            :variant="accordion.link.field"
            :color="accordion.link.color"
            :href="accordionLinkData.href"
            :target="accordionLinkData.target === '_blank_follow' ? '_blank' : accordionLinkData.target || '_self'"
            :rel="accordionLinkData.target === '_blank' ? 'noopener nofollow' : undefined"
            class="d-lg-flex justify-content-center px-5 py-2 my-3 btn btn-outline-primary btn-secondary align-self-start"
            :class="{'mr-3':(accordion.brochure && accordion.brochure.filePath)}"
            data-test-id="cta-button"
            @click.stop="event => $emit('click', event)"
          >
            {{ accordionLinkData.label }}
            <b-icon v-if="accordion.link.field === BUTTON_NAMES.LINEAR" icon="chevron-right" class="mt-1" />
          </DynamicButton>
          <a
            v-else-if="accordion.link && accordion.link.url"
            :href="accordion.link.url"
            :target="accordion.link.target === '_blank_follow' ? '_blank' : accordion.link.target || '_self'"
            :rel="accordion.link.target === '_blank' ? 'noopener nofollow' : null"
            class="d-lg-flex justify-content-center px-5 py-2 my-3 btn btn-outline-primary btn-secondary align-self-start"
            data-test-id="cta-button"
            :class="{'mr-3':(accordion.brochure && accordion.brochure.filePath)}"
            @click.stop="event => $emit('click', event)"
          >
            {{ accordion.link.label }}
          </a>
          <a
            v-if="(accordion.brochure && accordion.brochure.filePath)"
            :href="$utils.getCloudinaryDocument(accordion.brochure.filePath)"
            target="_blank"
            class="d-lg-flex justify-content-center px-5 py-2 my-3 btn btn-primary align-self-start"
            data-test-id="brochure-button"
            @click.prevent="download(accordion.brochure.filePath)"
          >
            {{ accordion.brochure.label || 'Download brochure' }}
          </a>
        </div>
        <b-card-text v-if="accordion.chips" text-tag="div">
          <b-badge v-for="(c, i) in accordion.chips"
                   :key="`accordion-chips_${i}`"
                   pill
                   :variant="selectedColorVariant.chipsColor"
                   class="mr-1 mb-3"
                   :data-test-id="`accordion-chips_${i}`"
          >
            {{ c.title }}
          </b-badge>
        </b-card-text>
      </div>
    </div>
  </Accordion>
</template>

<script>
import {ref, useContext, computed} from '@nuxtjs/composition-api'
import {COLORS, VARIANTS_CLASSES} from '../../constants/variants/accordionGroupVariants'
import {BUTTON_NAMES} from '~/constants/buttons'
import useContentLinkWatcher from '~/helpers/composables/useContentLinkWatcher'

export default {
  props: {
    accordion: {
      type: Object,
      required: true
    },
    toggleAccordion: {
      type: Function,
      required: false
    },
    nested: {
      type: Boolean,
      default: false
    },
    isPrimaryAccordion: {
      type: Boolean,
      default: false
    },
    htag: {
      type: String,
      default: 'h4'
    },
    color: {
      type: String,
      default: COLORS.WHITE
    },
    border: {
      type: Boolean,
      default: false
    },
    shadow: {
      type: Boolean,
      default: false
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    animationEnabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props, {emit}) {
    const {$utils} = useContext()
    const cloudinary = ref(null)
    const detailsRef = ref(null)
    const isVisible = ref(props.isExpanded)
    const selectedColorVariant = ref(VARIANTS_CLASSES[props.color])

    const hasMedia = computed(() => {
      const acc = props.accordion
      return Boolean((acc?.assets && acc.assets.length) || (acc?.youtubeVideos && acc.youtubeVideos.length))
    })

    const accordionLinkData = computed(() => {
      const link = props.accordion?.link
      if (!link || !link.field || link.field === 'none') {
        return {href: '', label: '', target: '_self'}
      }
      const linkSwitch = link.link
      if (!linkSwitch) {
        return {href: '', label: '', target: '_self'}
      }
      const linkType = linkSwitch.field
      if (!linkType || linkType === 'none') {
        return {href: '', label: '', target: '_self'}
      }
      const linkData = linkSwitch.link
      if (!linkData) {
        return {href: '', label: '', target: '_self'}
      }
      return {
        href: linkData[linkType + 'Url'] || '',
        label: linkData[linkType + 'Label'] || '',
        target: linkData.target || '_self'
      }
    })

    const onClick = (event) => {
      emit('click', event)
    }
    useContentLinkWatcher(detailsRef, onClick)

    useContentLinkWatcher(detailsRef, emit)

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const storeCldElement = (el) => {
      cloudinary.value = el
    }

    const toggleCallback = async (isAccordionExpanded) => {
      if (!isAccordionExpanded) {
        if (cloudinary.value) { await cloudinary.value.destroy() }
      }
      isVisible.value = isAccordionExpanded
    }

    return {
      COLORS,
      BUTTON_NAMES,
      detailsRef,
      download,
      storeCldElement,
      selectedColorVariant,
      isVisible,
      toggleCallback,
      accordionLinkData,
      hasMedia
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.cld-gallery {
  min-height: auto !important;
}

.accordion-description {
  max-width: 38.9375rem;

  ::v-deep p {
    max-width: 38.9375rem;
  }
}

.accordion-content {
  &.has-media {
    @include media-breakpoint-down(md) {
      display: flex;
      flex-direction: column;

      .accordion-description,
      .accordion-description ::v-deep p {
        max-width: 100%;
      }
    }

    @include media-breakpoint-up(lg) {
      display: grid;
      grid-template-columns: minmax(18rem, 1fr) minmax(18rem, 37.5rem);
      grid-template-rows: auto auto 1fr;
      column-gap: 1rem;
      align-items: start;

      .accordion-content-text {
        grid-column: 1;
        grid-row: 1;
      }

      .accordion-content-media {
        grid-column: 2;
        grid-row: 1 / span 3;
        align-self: center;
      }

      .accordion-content-actions {
        grid-column: 1;
        grid-row: 2;
      }
    }
  }

  &:not(.has-media) {
    display: flex;
    flex-direction: column;
  }
}
</style>
