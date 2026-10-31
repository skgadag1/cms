<template>
  <section data-test-id="model-header" class="bg-black">
    <div v-if="modelTitle" class="container d-block d-md-none pt-3">
      <div class="row">
        <div class="col-12">
          <div class="d-flex flex-column text-white mobile-header">
            <p class="h1 mb-0">{{ modelTitle }}</p>
            <p v-if="categoryTitle" class="h4 mb-3">{{ categoryTitle }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="position-relative w-100 header-wrapper">
      <video
        v-if="(video && video.src)"
        autoplay
        muted
        loop
        :data-src="video.src"
        preload="auto"
        playsinline
        class="w-100 h-100 header-asset"
        :poster="video.poster"
      >
        <source :src="video.src" type="video/webm">
        <source :src="video.src" type="video/mp4">
        Your browser does not support HTML5 video.
      </video>

      <ImageSet
        v-else-if="image && image.src"
        :src="image.src"
        :alt="image.alt"
        :title="image.title"
        :img-width="image.width"
        :img-height="image.height"
        :aspect-ratio="1440 / 572"
        :preload="true"
        class="w-100 h-100 header-asset"
      />
      <div class="position-absolute w-100 header-overlay" />
      <div class="container text-white d-none d-md-block">
        <div class="row">
          <div class="col-12 model-header-content">
            <component :is="htag" v-if="modelTitle" class="model-heading">
              <span class="h1 mb-0 model-title">{{ modelTitle }}</span>
              <span v-if="categoryTitle" class="h3 model-category">{{ categoryTitle }}</span>
            </component>
            <div v-if="specs && specs.length" class="d-flex specs-wrapper">
              <div
                v-for="(spec, index) in specs"
                :key="`spec-desktop-${index}`"
                class="d-flex flex-column align-items-start align-items-lg-end text-left text-lg-right specs-box"
              >
                <p v-if="spec.value && spec.label" class="h3 mb-0">
                  {{ spec.value }} {{ spec.unit }}
                </p>
                <p v-if="spec.value && spec.label" class="mb-0">{{ spec.label }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container mt-0">
      <div v-if="specs && specs.length" class="row d-block d-md-none">
        <div class="col-12 text-white">
          <div
            v-for="(spec, index) in specs"
            :key="`spec-mobile-${index}`"
            class="d-flex flex-column mb-3"
          >
            <p v-if="spec.value && spec.label" class="h4 mb-0">
              {{ spec.value }} {{ spec.unit }}
            </p>
            <p v-if="spec.label && spec.value" class="mb-0">{{ spec.label }}</p>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-12 col-md-12 col-lg-9">
          <p class="text-white text-large mb-0">
            <strong v-if="headline">
              {{ headline }}
            </strong>
          </p>
          <div v-if="modelOverview" class="text-white text-large model-long-text mt-3" v-html="modelOverview" />
          <div v-if="(ctas && ctas.length) || (printButton && printButton.length)" class="d-flex flex-column flex-md-row align-items-center buttons-white-space">
            <template v-if="ctas && ctas.length">
              <DynamicButton
                v-for="button in ctas"
                :key="`cta-button-${button.title}`"
                :variant="button.variant"
                :color="button.color"
                :href="button.link"
                :target="button.target === '_blank_follow' ? '_blank' : button.target"
                data-test-id="cta-button"
                @click="ga4click"
              >
                {{ button.title }}
              </DynamicButton>
            </template>
            <template v-if="printButton && printButton.length">
              <DynamicButton
                v-for="button in printButton"
                :key="`print-button-${button.title}`"
                :variant="button.variant"
                :color="button.color"
                :icon="button.icon"
                data-test-id="print-button"
                @click="handlePrint(button)"
              >
                {{ button.title }}
              </DynamicButton>
            </template>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, onServerPrefetch, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import {convertToPlain} from '../helpers/functional/AppHelpers'
import {AVAILABLE_ATTACHMENTS_LABEL, BUSINESS_REGIONS} from '~/constants'
import useSchema from '~/helpers/composables/useSchema'
import usePrintButtonMapper from '~/helpers/composables/usePrintButtonMapper'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    startingPriceLabel: String,
    startingPriceValue: String,
    horsepowerLabel: String,
    rocLabel: String,
    attachmentsLabel: String,
    headline: String,
    category: String,
    htag: {
      type: String,
      default: 'h2'
    },
    cta: {
      type: Object,
      default: () => ({})
    },
    printPDF: {
      type: Object,
      default: () => ({})
    },
    asset: Object,
    dynamicSpecs: String,
    spec1Label: String,
    spec1Value: String,
    spec2Label: String,
    spec2Value: String,
    spec3Label: String,
    spec3Value: String
  },
  setup(props, context) {
    const model = inject('model')
    const {$ga4, $services, $regions, $utils, route, $config} = useContext()
    const router = useRouter()
    const {createProductObject} = useSchema({}, $utils, $regions, $config)

    const {
      cta,
      printPDF,
      asset,
      startingPriceLabel,
      startingPriceValue,
      horsepowerLabel,
      rocLabel,
      attachmentsLabel,
      headline
    } = props

    const modelID = model?.id
    const modelTitle = model?.title
    const modelOverview = model?.overview
    let suggestedPrice = ''
    if (startingPriceValue) {
      suggestedPrice = `$${startingPriceValue}`
    } else {
      suggestedPrice = model?.suggestedPrice?.replaceAll('*', '')?.replace('USD', '')
    }

    const ctaSource = resolveLegacyListSource(
      cta, context.attrs, 'cta', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )

    const isNotNARegion = !(BUSINESS_REGIONS.na.includes($regions.current.name))
    const uom = isNotNARegion ? 'metric' : 'us'

    const video = ref(null)
    const image = ref(null)
    const attachmentsAmount = ref(0)
    const horsepower = ref({value: null, unit: null})
    const ratedOperatingCapacitySae = ref({value: null, unit: null})
    const specs = ref([])
    const {printButton, handlePrint} = usePrintButtonMapper(printPDF, route, modelID)
    const normalize = word => word?.toString()?.trim()?.replace(/_/g, ' ')?.replace(/-/g, ' ')?.replace(/\s+/g, ' ') || ''

    const capitalizeEachWord = phrase =>
      phrase
        ?.split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ') || ''

    const getProductCategory = () => {
      const path = router?.currentRoute?.path
      if (!model || !path) {
        return ''
      }

      const parts = path.split('/equipment/')[1]?.split('/') || []
      const category = parts[Math.min(parts.length - 1, 1)] || model.productTypeLabel || ''
      return capitalizeEachWord(normalize(category))
    }

    const ctas = computed({
      set: () => {
      },
      get: () => {
        return ctaSource?.['@nodes']
          ? ctaSource['@nodes'].map((x) => {
            const ctaButton = ctaSource[x].link
            const linkField = $utils.getLinkField(ctaButton, $regions.current.language.name)
            return {
              variant: ctaSource[x]?.field,
              color: ctaSource[x]?.color,
              link: linkField?.url || '',
              title: linkField?.label || '',
              target: linkField?.target || '_self',
              config: {
                class: ctaSource[x]?.variant || 'bg-primary text-white'
              }
            }
          })
          : []
      }
    })

    const buildSpecs = (dynamicData = []) => {
      const result = []
      const add = (l, v, u = '') => { if (l && v && result.length < 4) { result.push({label: l, value: v, unit: u}) } }

      // 1. Price
      if (suggestedPrice && startingPriceLabel) { add(startingPriceLabel, suggestedPrice) }

      // 2. Manual Specs (Props 1-3)
      for (let i = 1; i <= 3; i++) {
        add(props[`spec${i}Label`], props[`spec${i}Value`])
      }

      // 3. Engine/Performance (Only if space left)
      add(horsepowerLabel, horsepower.value.value, horsepower.value.unit)
      add(rocLabel, ratedOperatingCapacitySae.value.value, ratedOperatingCapacitySae.value.unit)

      // 4. Dynamic Specs & Attachments
      const attachLabel = attachmentsLabel || (dynamicData.some(s => s.label === AVAILABLE_ATTACHMENTS_LABEL) ? AVAILABLE_ATTACHMENTS_LABEL : '')

      dynamicData.forEach((spec) => {
        if (spec.label !== AVAILABLE_ATTACHMENTS_LABEL) { add(spec.label, spec.value, spec.unit) }
      })

      if (attachmentsAmount.value > 0) { add(attachLabel, attachmentsAmount.value) }

      return result
    }

    // Build specs synchronously from data already available at setup() time (price, manual
    // specs, dynamicSpecs prop) so isApplicable/jsonld() don't depend on the async fetch below —
    // required for static generation, where nuxt-jsonld reads jsonld() from created(), which
    // runs before onServerPrefetch's fetch resolves.
    specs.value = buildSpecs(JSON.parse(props.dynamicSpecs || '[]'))

    if (asset?.video?.src) {
      const {metadata} = $utils.getCloudinaryAssetMetadata(asset.video.src)
      video.value = {
        src: $utils.getCloudinaryAsset(asset.video.src, false, {
          quality: 'auto:good',
          fetch_format: 'mp4',
          flags: 'streaming_attachment'
        }),
        poster:
          $utils.getCloudinaryAsset(asset.video.poster) ||
          $utils.getCloudinaryAsset(asset.video.src, false, {
            effect: 'preview',
            resource_type: 'video'
          }),
        creativeId: metadata?.filename
      }
    } else if (asset?.img?.src) {
      const {metadata} = $utils.getCloudinaryAssetMetadata(asset.img.src)
      image.value = {
        src: asset.img.src,
        title: asset.img?.imageTitle,
        alt: asset.img?.imageAlt,
        width: asset.img?.imageWidth || null,
        height: asset.img?.imageHeight || null,
        creativeId: metadata?.filename
      }
    }

    const {fetch} = useFetch(async () => {
      const response = await $services.products.getModelSpecifications(model, uom, 3)
      if (response?.data) {
        const getProp = (secT, pId) => response.data.sections.find(s => s.title === secT)?.properties.find(p => p.id === pId)
        attachmentsAmount.value = model?.compatibleAttachmentsCount || 0
        horsepower.value = {value: getProp('Engine', 'horsepower')?.value, unit: getProp('Engine', 'horsepower')?.unit}
        ratedOperatingCapacitySae.value = {value: getProp('Performance', 'ratedOperatingCapacitySae')?.value, unit: getProp('Performance', 'ratedOperatingCapacitySae')?.unit}

        specs.value = buildSpecs(JSON.parse(props.dynamicSpecs || '[]'))
      } else {
        specs.value = buildSpecs()
      }
    })

    const categoryTitle = computed(() => {
      return props.category || getProductCategory() || ''
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    const ga4click = (event) => {
      $ga4.events.hero_spot_cta(event,
        {
          asset_id: video?.value?.creativeId || image?.value?.creativeId,
          header: headline,
          text: modelOverview
        }, undefined, {
          item_name: modelTitle,
          item_category: categoryTitle.value,
          item_id: model?.id,
          headline_1: suggestedPrice
        })
    }

    const productSchema = computed(() => {
      const isApplicable = !!specs.value?.some(
        spec => spec.label?.trim() && typeof spec.value === 'string' && spec.value.includes('$')
      )

      if (isApplicable) {
        const assetItem = {
          pageUrl: `${$config.PUBLIC_HOST}${router.currentRoute.path}`,
          name: modelTitle,
          imageUrl: asset?.img?.src,
          description: convertToPlain(modelOverview, true),
          category: props.category || model?.productTypeLabel,
          price: suggestedPrice?.replaceAll('$', '').trim()
        }

        return createProductObject(assetItem)
      }
    })

    return {
      modelTitle,
      suggestedPrice,
      categoryTitle,
      modelOverview,
      attachmentsAmount,
      horsepower,
      ratedOperatingCapacitySae,
      ctas,
      printButton,
      image,
      video,
      specs,
      ga4click,
      handlePrint,
      productSchema
    }
  },
  jsonld() {
    return [this.productSchema].filter(x => x)
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

section[data-test-id="model-header"] {
  .model-long-text { max-width: 38.9444em; }

  .header-wrapper {
    padding-top: 23.67%;
    overflow: hidden;

    .h1 {
      line-height: 1;
    }

    .h3 {
      line-height: 1;
    }

    .h1, .h3 {
      text-shadow: #000 0 0 8px;
    }

    .model-header-content {
      display: grid;
      grid-template-columns: max-content 1fr;
      align-items: end;
      column-gap: 3rem;
      padding-left: 0;
      padding-right: 0;

      @include media-breakpoint-down(md) {
        grid-template-columns: 1fr;
      }
    }

    .model-heading {
      display: contents;
    }

    .model-title {
      grid-column: 1 / -1;
      line-height: 1;
    }

    .model-category {
      margin-top: 0;
      margin-bottom: 0;
      line-height: 1;
      align-self: end;
    }

    .header-asset {
      top: 0;
      left: 0;
      object-fit: cover;
      position: absolute !important;

      @include media-breakpoint-down(sm) {
        position: static !important;
      }
    }

    .header-overlay {
      background: linear-gradient(
          to top,
          rgba(0, 0, 0, 1) 0%,
          rgba(0, 0, 0, 0) 100%
      );
      bottom: 0;
      left: 0;
      height: 8.5rem;
      pointer-events: none;

      @include media-breakpoint-down(sm) {
        height: 3rem;
      }
    }

    .specs-wrapper {
      gap: 1.5rem;
      margin-left: auto;
      justify-content: flex-end;
      align-items: flex-end;
      text-align: right;

      @include media-breakpoint-down(md) {
        margin-left: 0;
        justify-content: flex-start;
        align-items: flex-start;
        text-align: left;
      }
    }

    .specs-box {
      flex: 0 0 auto;
      align-items: flex-end;
    }

    @include media-breakpoint-down(sm) {
      padding-top: 0;
    }
  }

  .container.mt-0 {
    max-width: 100%;
    margin-top: 0 !important;
    padding: 1rem 10rem 2.5rem 10rem;

    .row {
      margin-left: 0;
      margin-right: 0;
    }

    .row > [class*="col-"] {
      padding-left: 0;
      padding-right: 0;
    }

    @include media-breakpoint-down(lg) {
      padding: 1rem 2.5rem 2.5rem 2.5rem;
    }

    @include media-breakpoint-down(sm) {
      padding: 1rem 1rem 1rem 1rem;
    }
  }

  .header-wrapper > .container {
    max-width: 100%;
    padding: 2.4375rem 10rem 3.9375rem 10rem;

    .row {
      margin-left: 0;
      margin-right: 0;
    }

    .row > [class*="col-"] {
      padding-left: 0;
      padding-right: 0;
    }

    @include media-breakpoint-down(lg) {
      padding: 2.4375rem 2rem 3.9375rem 2rem;
    }

    @include media-breakpoint-down(sm) {
      padding: 1rem 1rem 2rem 1rem;
    }
  }

  .text-large {
    margin-bottom: 0;
  }

  .buttons-white-space {
    white-space: nowrap;
    gap: 1rem;
    margin-top: 2rem !important;
  }

  .mobile-header {
    max-width: 21.4375rem;
    gap: 0;

    .h1 {
      font-size: 3rem;
      line-height: normal;
      line-height: normal;
    }
  }

  .edge-clipped-btn, .btn-lg {
    @include media-breakpoint-down(sm) {
      width: 100%;
    }
  }
}
</style>
