<template>
  <section class="bg-gray-lighter" data-test-id="page-listing">
    <ListingWithFiltering :group="group"
                          :title="title"
                          :labels="labels"
                          :htag="htag"
                          :hide-filters="hideFiltersOpt"
                          :use-show-more="useShowMoreOpt"
                          :facet="facet"
                          :page-type="type"
                          :expandable="expandable"
                          :results-per-page="resultsPerPage"
                          @resultListUpdate="updateResults"
    >
      <template #results>
        <div v-if="loaded || $isBot" class="row" data-test-id="page-listing__results">
          <div v-for="(itm, i) in results"
               :key="`page-listing-result-${itm.name}-${itm['@id'] || itm.postDate || itm.publishDate}-${i}`"
               class="col-12 mb-4"
               :class="hideFiltersOpt ? 'col-md-4 col-lg-3' : 'col-md-6 col-lg-4'"
               :data-test-id="`page-listing-result-item__${i}`"
          >
            <PromoModel v-if="campaigns && campaigns.length && itm.isPromo" :promo-campaign="itm" />
            <ProductListingItem
              v-else
              :horizontal="false"
              :item="itm"
              :labels="labels"
              :show-chips="false"
              :show-indicator="true"
              :show-publish-date="showPublishDate === 'true'"
              :hide-description="hideDescriptionOpt"
              :hide-images="hideImagesOpt"
              :show-cta="showCTA"
              :cta-label="ctaLabel"
              :cta-label-url-target="ctaLabelUrlTarget"
            />
          </div>
        </div>
      </template>
    </ListingWithFiltering>
  </section>
</template>

<script>
import {inject, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    hideFilters: {
      type: String,
      default: 'false'
    },
    hideDescription: {
      type: String,
      default: 'false'
    },
    hideImages: {
      type: String,
      default: 'false'
    },
    showPublishDate: {
      type: String,
      default: 'false'
    },
    useShowMore: {
      type: String,
      default: 'false'
    },
    showCTA: {
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
    htag: String,
    group: {
      type: Object,
      default: () => {
        return {
          field: 'pages'
        }
      }
    },
    facet: Object,
    useCollapse: {
      type: String,
      default: 'true'
    },
    pageType: {
      type: Object,
      default: null
    },
    resultsPerPage: {
      type: String
    }
  },
  setup(props) {
    const results = ref([])
    const hideFiltersOpt = props.hideFilters === 'true'
    const hideImagesOpt = props.hideImages === 'true'
    const hideDescriptionOpt = props.hideDescription === 'true'
    const useShowMoreOpt = props.useShowMore === 'true'
    const loaded = ref(false)
    const expandable = props.useCollapse === 'true'
    const campaigns = inject('campaigns')
    const labels = ref({})
    const type = ref(props.pageType)
    const {$services} = useContext()
    useMounted(props.nodeId)

    if (type.value?.field === 'general') {
      type.value = {...type.value, field: ''}
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications'])
    })

    const compareIndexes = (a, b) => a.positionIndex - b.positionIndex

    const updateResults = (data) => {
      results.value = data?.results?.map((x) => {
        return {
          ...x,
          description: x.shortDescription,
          ...(x.pageType === 'video' && {
            showIndicator: 'true',
            videoIndicator: 'true'
          })
        }
      }) || []

      if (campaigns.value?.length > 0) {
        campaigns.value.sort(compareIndexes).forEach(c => results.value.splice(c?.positionIndex, 0, {...c, isPromo: true}))
      }
      loaded.value = true
    }

    return {
      results,
      hideFiltersOpt,
      useShowMoreOpt,
      hideImagesOpt,
      hideDescriptionOpt,
      loaded,
      updateResults,
      labels,
      campaigns,
      expandable,
      type
    }
  }
}
</script>
