<template>
  <section class="container py-4 attachment-pricing"
           data-test-id="attachment-pricing"
  >
    <div class="row align-items-stretch">
      <div class="col-12 d-sm-none">
        <component :is="!desktop ? htag : 'div'"
                   v-if="title"
                   :class="htag"
                   data-test-id="attachment-pricing-title__mobile"
                   class="mb-lg-2"
        >
          <div class="evolution-heading h2 m-0">{{ modelVariant && modelVariant.title }}</div>
          <span v-html="title" />
          <div v-if="subtitle"
               class="h2"
               data-test-id="attachment-pricing-subtitle__mobile"
               v-html="subtitle"
          />
        </component>
      </div>

      <div class="col-12 col-lg-6 mb-4 mb-lg-0">
        <CloudinaryGallery v-if="images.length" :key="images[0]['@id']" :images="images" />
        <ImageSet v-else />

        <WarningAlert type="info" :title="generateConceptHint('attachmentImageDisclaimer')" class="mb-0">{{ labels.attachmentImageDisclaimer || "Attachment size and configuration may be different than image(s) provided. See dealer for full product details." }}</WarningAlert>
      </div>

      <div class="col-12 col-lg-6" data-test-id="attachment-pricing">
        <div class="row">
          <div class="col-12">
            <component :is="desktop ? htag : 'div'"
                       v-if="title"
                       :class="htag"
                       data-test-id="model-pricing-title__desktop"
                       class="d-none d-sm-block mb-4"
            >
              <div class="evolution-heading h2 m-0">{{ modelVariant && modelVariant.title }}</div>
              <span v-html="title" />
              <div v-if="subtitle"
                   class="h2"
                   data-test-id="model-pricing-subtitle__desktop"
                   v-html="subtitle"
              />
            </component>

            <div v-if="variantOptions.length > 2" class="mb-4">
              <div class="font-weight-bold">{{ labels.sizes || 'Available sizes' }}</div>
              <SwitchDropdown
                name="variant"
                :options="variantOptions"
                :selected-option="selectedVariantOption"
                :config="{noFlip: true}"
                @changed="onVariantChange"
              />
            </div>
          </div>

          <div v-if="compatibleMachines.length"
               data-test-id="attachment-compatible-machines"
               class="col-12"
          >
            <div class="mb-2 font-weight-bold">{{ labels.compatibleMachines || 'Compatible Machines:' }}</div>
            <b-badge v-for="(compatibleMachine, index) in visibleCompatibleMachines"
                     :key="`compatibleChip__${index}`"
                     :data-test-id="`compatible-machine-badge-${index}`"
                     pill
                     variant="black"
                     class="mr-2 mb-3 border border-color-tertiary"
            >
              {{ compatibleMachine }}
            </b-badge>
            <a v-if="compatibleMachines.length > 3 && !showAllCompatibleMachines" href="#" class="font-weight-bold d-block mb-3" @click="showCompatibleMachines">+{{ compatibleMachines.length - 3 }}&nbsp;{{ labels.more || 'More' }}</a>
          </div>
        </div>

        <div v-if="ctas.length">
          <DynamicButton
            v-for="(button, index) in ctas"
            :key="`hero-spot-cta-button-${index}`"
            :variant="button.variant"
            :color="button.color"
            :href="button.link"
            :target="button.target === '_blank_follow' ? '_blank' : button.target"
            class="w-100 btn-only-mobile-block text-uppercase"
            :class="index < ctas.length - 1 ? 'mb-3' : ''"
            data-test-id="cta-button"
            @click="ga4click"
          >
            {{ button.title }}
          </DynamicButton>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, onServerPrefetch, ref, unref, useContext, useFetch, useRoute} from '@nuxtjs/composition-api'
import useCloudinary from '../helpers/composables/useCloudinary'
import {MODEL_TYPE} from '../constants'
import {BUTTON_NAMES} from '../constants/buttons'
import useConcepts from '~/helpers/composables/useConcepts'
import {FIELD_TYPES} from '~/constants'
import useBreakpoints from '~/helpers/composables/useBreakpoints'
import useMounted from '~/helpers/composables/useMounted'
import useModels from '@/helpers/composables/useModels'
import useQueryParams from '~/helpers/composables/useQueryParams'

export default {
  props: {
    nodeId: {type: String},
    title: {type: String},
    subtitle: {type: String},
    cta: {type: Object},
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const {
      cta
    } = props

    const {$services, $ga4, $utils, $regions} = useContext()
    const {desktop} = useBreakpoints()

    const {mapCloudinaryGalleryAssets} = useCloudinary()
    const labels = ref({})
    const showAllCompatibleMachines = ref(false)

    const {generateConceptHint} = useConcepts()
    const {model, models, modelVariant, handleVariantChange} = useModels(MODEL_TYPE.ATTACHMENT)
    const {updateQueryParams} = useQueryParams()
    const route = useRoute()

    const initialVariant = route.value?.query?.variant || ''
    if (initialVariant && models.some(m => m?.['@name'] === initialVariant)) {
      handleVariantChange(initialVariant)
    }

    const selectedVariantOption = computed(() => {
      if (!modelVariant.value) { return null }
      return {label: modelVariant.value.title, value: modelVariant.value['@name']}
    })

    useMounted(props.nodeId)

    const ctas = computed(() => {
      return cta?.['@nodes']
        ? cta['@nodes'].map((x) => {
          const ctaButton = cta[x].link
          const linkField = $utils.getLinkField(ctaButton, $regions.current.language.name)
          return {
            variant: cta[x]?.field,
            color: cta[x]?.color,
            link: linkField?.url || '',
            title: linkField?.label || '',
            target: linkField?.target || '_self',
            config: {
              class: cta[x]?.variant || 'bg-primary text-white'
            }
          }
        })
        : []
    })

    // Combine a model's single "Subcategory image" with all of its gallery assets
    const collectMedia = (source) => {
      if (!source) { return [] }
      const singleImage = source.image?.src ? [source.image] : []
      const galleryAssets = mapCloudinaryGalleryAssets(source.images || [])
      return [...singleImage, ...galleryAssets]
    }

    const images = computed(() => {
      // Always source the gallery from the Attachment Subcategory (group) image + assets,
      // so every variant/component on the page shows the full subcategory media set.
      const subcategoryMedia = collectMedia(unref(model))

      if (subcategoryMedia.length) {
        return subcategoryMedia
      }

      // Fallback to the selected variant's own media when the subcategory has none.
      return collectMedia(modelVariant.value)
    })

    const compatibleMachines = computed(() => {
      const allCompatibleMachines = [
        ...new Set(
          models
            .flatMap(model => model.compatibleMachines?.machines ?? [])
            .map(m => m.name)
        )
      ]

      return modelVariant?.value?.compatibleMachines?.machines
        ? modelVariant?.value?.compatibleMachines.machines.map(x => x.name)
        : allCompatibleMachines
    })

    const variantOptions = computed(() => {
      const selectedName = modelVariant.value?.['@name']
      const options = models?.filter(variant => variant['@name'] !== selectedName).map(variant => ({label: variant.title, value: variant['@name']})) || []
      options.unshift({label: labels.value.selectVariant || 'Select', value: '__none__'})
      return options
    })

    const onVariantChange = (option) => {
      const value = option.value === '__none__' ? '' : option.value
      handleVariantChange(value)
      updateQueryParams({variant: value || ''})
    }

    const {fetch} = useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    const showCompatibleMachines = (event) => {
      $ga4.events.product_page_cta(event, modelVariant.value || model, {
        item_category: 'Attachments',
        subhead: modelVariant?.value?.title || undefined,
        cta_type: 'show more'
      })
      showAllCompatibleMachines.value = true
    }

    const visibleCompatibleMachines = computed(() => {
      return compatibleMachines.value.slice(0, showAllCompatibleMachines.value ? compatibleMachines.value.length : 3)
    })

    const ga4click = (event) => {
      $ga4.events.product_page_cta(event, modelVariant.value || model, {
        item_category: 'Attachments',
        subhead: modelVariant?.value?.title || undefined
      })
    }

    return {
      labels,
      images,
      FIELD_TYPES,
      generateConceptHint,
      desktop,
      variantOptions,
      selectedVariantOption,
      ctas,
      onVariantChange,
      modelVariant,
      compatibleMachines,
      showCompatibleMachines,
      visibleCompatibleMachines,
      showAllCompatibleMachines,
      BUTTON_NAMES,
      ga4click
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

.attachment-pricing {
  .custom-switch-dropdown .dropdown-menu {
    top: 100% !important;
    bottom: auto !important;
    transform: none !important;

    &[x-placement="top-start"],
    &[x-placement="top-end"] {
      top: 100% !important;
      bottom: auto !important;
      transform: none !important;
    }
  }

  .cld-gallery {
    @include media-breakpoint-up(md) {
      min-height: calc((50vw - 2rem) * .75 + 1rem);

      &-multi {
        min-height: calc((50vw - 2rem) * .75 + 121px + 1rem);
      }
    }

    @include media-breakpoint-up(xl) {
      min-height: calc((570px - 2rem) * .75 + 1rem);

      &-multi {
        min-height: calc((570px - 2rem) * .75 + 121px + 1rem);
      }
    }
  }
}
</style>
