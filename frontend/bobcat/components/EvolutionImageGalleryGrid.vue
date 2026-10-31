<template>
  <section ref="observerTarget" :class="[selectedColorVariant.background]" class="evolution-image-gallery-grid">
    <div class="container py-5">
      <div class="row">
        <div v-if="isVisible" class="col-12">
          <div v-if="title"
               class="evolution-gallery-header"
               :class="{'mb-1': secondaryTitle}"
          >
            <div class="evolution-gallery-title" v-html="title" />
          </div>
          <div v-if="secondaryTitle" class="evolution-gallery-header">
            <div class="evolution-gallery-title" v-html="secondaryTitle" />
          </div>
          <div v-if="galleryImages && galleryImages.length" class="w-100">
            <CloudinaryGallery
              :images="galleryImages"
              :config-overrides="galleryConfig"
              :custom-styles="customStyles"
              @widget-click="ga4click"
            />
          </div>

          <div v-else class="d-flex justify-content-center align-items-center text-center py-5">
            <p class="text-muted">No images available for this model.</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, ref, inject, onMounted, useContext} from '@nuxtjs/composition-api'
import {useElementVisibility} from '~/helpers/composables/useElementVisibility'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/galleryVariants'
import GALLERY_CONFIG_VERTICAL from '~/constants/galleryConfigurationVertical'
import useCloudinary from '~/helpers/composables/useCloudinary'

export default {
  props: {
    title: {
      type: String
    },
    secondaryTitle: {
      type: String
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props) {
    const {isVisible, observerTarget, checkVisibility} = useElementVisibility()
    const {mapCloudinaryGalleryAssets} = useCloudinary()
    const {$ga4} = useContext()
    const galleryImages = ref([])
    const model = inject('model')

    const ga4click = (event, assetId) => {
      $ga4.events.image_gallery_grid_click(model, props.title, props.secondaryTitle, assetId, event)
    }

    const fetchModelImages = async () => {
      try {
        if (model) {
          const variantNodes = model['@nodes'] || []
          let allImages = []

          variantNodes.forEach((nodeName) => {
            const variant = model[nodeName]
            if (variant && variant['@nodeType'] === 'dxp:product' && variant.images) {
              const variantImages = mapCloudinaryGalleryAssets(variant.images)
              allImages.push(...variantImages)
            }
          })

          if (allImages.length === 0 && model.images) {
            allImages = mapCloudinaryGalleryAssets(model.images)
          }

          galleryImages.value = allImages
        }
      } catch (error) {
        galleryImages.value = []
      }
    }

    const galleryConfig = computed(() => ({
      ...GALLERY_CONFIG_VERTICAL,
      aspectRatio: '4:3',
      carouselOffset: 64,
      navigationButtonProps: {
        color: selectedColorVariant.value.arrows,
        iconColor: selectedColorVariant.value.arrows,
        size: 46
      },
      thumbnailProps: {
        navigationColor: selectedColorVariant.value.buttonBackground,
        navigationFloat: true,
        spacing: 16,
        navigationIconColor: selectedColorVariant.value.arrows,
        borderColor: selectedColorVariant.value.buttonBackground,
        selectedBorderColor: selectedColorVariant.value.buttonBackground,
        borderWidth: 0,
        navigationPosition: 'middle',
        selectedBorderWidth: 0,
        ...GALLERY_CONFIG_VERTICAL.thumbnailProps,
        height: 188,
        width: 251,
        navigationSize: 94
      },
      viewportBreakpoints: [
        {
          breakpoint: 1200,
          viewerProps: {
            width: 900,
            height: 675
          }
        },
        {
          breakpoint: 768,
          carouselOffset: 32,
          thumbnailProps: {height: 122, width: 163, selectedBorderWidth: 0},
          carouselAlignment: 'right'
        },
        {
          breakpoint: 576,
          carouselOffset: 32,
          thumbnailProps: {height: 57, width: 86, selectedBorderWidth: 0},
          carouselAlignment: 'right'
        }
      ]
    }))

    const selectedColorVariant = computed(() => VARIANTS_CLASSES[props.variant] || VARIANTS_CLASSES[WHITE_VARIANT])

    const customStyles = computed(() => ({
      buttonBackground: selectedColorVariant.value.buttonBackground,
      buttonColor: selectedColorVariant.value.arrows,
      buttonVisibility: 'hidden'
    }))

    onMounted(async () => {
      await fetchModelImages()
      setTimeout(checkVisibility, 500)
    })

    return {
      galleryImages,
      isVisible,
      observerTarget,
      selectedColorVariant,
      galleryConfig,
      customStyles,
      ga4click
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';

.evolution-gallery-no-images {
  min-height: 12.5rem;
}

</style>
