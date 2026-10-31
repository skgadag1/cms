<template>
  <section :class="currentBackgroundClass" class="py-4">
    <div class="container">
      <LazyHydrate v-if="title" when-visible>
        <div class="row mb-3">
          <div class="col-12"
               :class="currentTextColor"
          >
            <div class="d-flex w-100 justify-content-between align-items-start evolution-titles">
              <div class="d-flex flex-column">
                <div v-if="headline" class="mb-0 h3 d-flex" v-html="headline" />
                <div v-if="title" class="mb-2 h5" v-html="title" />
                <span v-if="mainCta && mainCta.label && mainCta.url" class="d-flex align-items-center text-primary">
                  <a :href="mainCta.url"
                     :target="mainCta.target === '_blank_follow' ? '_blank' : mainCta.target"
                     :rel="mainCta.target === '_blank' ? 'noopener nofollow' : null"
                     class="mb-0 link-with-icon h5"
                     @click="ga4click"
                  >
                    {{ mainCta.label }}
                  </a>
                  <b-icon class="ml-2" icon="chevron-right" />
                </span>
              </div>
              <span v-if="viewAll && viewAll.label && viewAll.url" class="d-flex align-items-center text-primary">
                <a :href="viewAll.url"
                   :target="viewAll.target === '_blank_follow' ? '_blank' : viewAll.target"
                   :rel="viewAll.target === '_blank' ? 'noopener nofollow' : null"
                   class="mb-0 link-with-icon h5 text-nowrap"
                   @click="ga4click"
                >
                  {{ viewAll.label }}
                </a>
                <b-icon class="ml-2" icon="chevron-right" />
              </span>
            </div>
          </div>
        </div>
      </LazyHydrate>

      <div v-if="loaded && allResults && allResults.length" class="evolution-compatible-attachments-grid">
        <div
          v-for="(itm, i) in allResults"
          :key="`evolution-attachment-listing-result-${itm.name}-${itm['@id']}-${i}`"
          class="evolution-grid-item"
          :class="currentBorderClass"
        >
          <EvolutionCompatibleAttachmentsItem
            :horizontal="false"
            :item="itm"
            :labels="labels"
            :show-chips="false"
            :show-indicator="true"
            :show-cta="showCTA"
            :cta-label="ctaLabel"
            :cta-label-url-target="ctaLabelUrlTarget"
            :cta-links-type="ctaLinksType"
            @click="ga4click"
          />
        </div>
      </div>
      <div class="row">
        <div class="col-12 d-flex justify-content-center my-3">
          <DynamicButton
            v-if="allResults.length < total"
            :variant="showMoreVariant?.field || BUTTON_NAMES.HEXAGONAL"
            :color="showMoreVariant?.color"
            @click.prevent="(e) => {ga4click(e); loadMore()}"
          >
            {{ labels?.showMore || 'Show More' }}
          </DynamicButton>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {inject, onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import {PRODUCT_NODE_TYPES} from '../constants/magnolia'
import useMounted from '~/helpers/composables/useMounted'
import {VARIANT_PROPERTIES, WHITE_VARIANT} from '~/constants/variants/loadersEvolutionVariants'
import EvolutionCompatibleAttachmentsItem
  from '~/components/LoadersEvolution/Listings/EvolutionCompatibleAttachmentsItem.vue'
import {MODEL_TYPE} from '~/constants'
import {BUTTON_NAMES} from '~/constants/buttons'
import {CTA_LINKS_TYPE} from '~/constants/compatibleAttachments'

export default {
  components: {
    EvolutionCompatibleAttachmentsItem
  },
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    facet: {
      type: [Object, String],
      default: null
    },
    sortingDirection: {
      type: Object
    },
    resultsPerPage: {
      type: String
    },
    showCTA: {
      type: String,
      default: 'true'
    },
    ctaLabel: {
      type: String
    },
    ctaLabelUrlTarget: {
      type: String,
      default: '_parent'
    },
    showMoreVariant: {
      type: Object
    },
    variant: {
      type: Object
    },
    mainCTA: {
      type: Object
    },
    viewAllCTA: {
      type: Object
    },
    headline: {
      type: String
    },
    displayAtGroupLevel: {
      type: String
    },
    attachmentDisplayMode: {
      type: String,
      default: 'showRelatedAttachments'
    },
    manualAttachments: {
      type: Array,
      default: () => []
    },
    ctaLinksType: {
      type: String,
      default: CTA_LINKS_TYPE.CURRENT_LEVEL
    }
  },
  setup(props) {
    const {$services, $ga4, $regions, $utils} = useContext()
    const allResults = ref([])
    const loaded = ref(false)
    const isNA = ref($regions.current.name === 'na')
    const currentVariant = ref(props.variant?.field)
    const currentBackgroundClass = ref(VARIANT_PROPERTIES[currentVariant.value || WHITE_VARIANT]?.backgroundClass)
    const currentBorderClass = ref(VARIANT_PROPERTIES[currentVariant.value || WHITE_VARIANT]?.borderClass)
    const currentTextColor = ref(VARIANT_PROPERTIES[currentVariant.value || WHITE_VARIANT]?.textClass)
    const mainCta = $utils.getLinkField(props?.mainCTA, $regions.current.language.name)
    const viewAll = $utils.getLinkField(props?.viewAllCTA, $regions.current.language.name)
    const model = inject('model')
    const total = ref(0)
    const page = ref(0)
    const loading = ref(false)

    useMounted(props.nodeId)

    const labels = ref({})

    const ga4click = (event) => {
      $ga4.events.compatible_attachments_click(model, props.title, props.headline, event?.target?.innerText)
    }

    useFetch(async () => {
      if (!isNA.value) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications'])
      }
      await loadMore()
    })

    const loadMore = async (e) => {
      if (e?.target) {
        e.target.blur()
      }

      if (loading.value) {
        return
      }

      loading.value = true

      try {
        const facetPath = props?.facet?.['@path']?.split('/').slice(0, -1).join('/')
        const sortingKey = props?.facet?.id
        const sortDir = props?.sortingDirection?.field || 'asc'
        const sort = `${sortingKey}:${sortDir}`
        const modelParam = model?.id || model?.['@name']

        let attachments = []
        const resultsPerPage = parseInt(props?.resultsPerPage || 8)

        if (props.attachmentDisplayMode === 'manualTilesPerChoice') {
          attachments = await $services.content.getAttachmentsByIds(props.manualAttachments || [], {
            from: page.value * resultsPerPage,
            size: resultsPerPage
          })
        } else if (props.attachmentDisplayMode === 'fromApiRegionHierarchy' || model?.['@nodeType'] === 'mgnl:folder') {
          attachments = await $services.content.getRelatedAttachments([], {
            from: page.value * resultsPerPage,
            size: resultsPerPage
          }, false)
        } else if ([PRODUCT_NODE_TYPES.ATTACHMENT, PRODUCT_NODE_TYPES.ATTACHMENT_GROUP, PRODUCT_NODE_TYPES.ATTACHMENT_CATEGORY].includes(model?.['@nodeType'])) {
          const useRelatedAttachments = props.attachmentDisplayMode === 'showRelatedAttachments'
          attachments = await $services.content.getRelatedAttachments([model['@id']], {
            from: page.value * resultsPerPage,
            size: resultsPerPage
          }, useRelatedAttachments)
        } else {
          attachments = await $services.content.getSearchResultsByType({
            contentType: MODEL_TYPE.ATTACHMENT,
            facet: facetPath,
            page: page.value,
            maxItems: resultsPerPage,
            sort,
            useOnlyShortDescription: true,
            displayOnGroupLevel: String(props?.displayAtGroupLevel === 'true'),
            model: modelParam
          })
        }

        allResults.value = [...allResults.value, ...(attachments?.data?.results || [])]
        total.value = attachments?.data?.total || 0
        page.value += 1
        loaded.value = true
      } catch (e) {
        // eslint-disable-next-line
        console.error('[Evolution Compatible Attachments] Error loading data:', e)
      } finally {
        loading.value = false
      }
    }

    onMounted(async () => {
      if (isNA.value && process.client) {
        const currentEquipmentPath = window?.location?.pathname?.split('/equipment/')?.[1]
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications', `/%region%/products/${currentEquipmentPath}`])
      }
    })

    return {
      allResults,
      loaded,
      labels,
      ga4click,
      isNA,
      currentBackgroundClass,
      currentTextColor,
      currentBorderClass,
      mainCta,
      viewAll,
      total,
      loading,
      loadMore,
      BUTTON_NAMES
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-down] from ~bootstrap/scss/mixins';

.evolution-compatible-attachments-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: .5rem;

  @include media-breakpoint-down(lg) {
    grid-template-columns: repeat(3, 1fr);
  }

  @include media-breakpoint-down(md) {
    grid-template-columns: repeat(2, 1fr);
  }

  @include media-breakpoint-down(sm) {
    grid-template-columns: 1fr;
  }
}

.evolution-titles {
  h1, h2, h3, h4, h5, h6 {
    margin-bottom: 0;
    display: contents;
  }
}
</style>
