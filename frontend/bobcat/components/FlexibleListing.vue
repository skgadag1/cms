<template>
  <section ref="observerTarget" :class="selectedColorVariant.background" class="py-4">
    <LazyHydrate :when-visible="!isEagerLoad">
      <div class="container" data-test-id="flexible-listing" :data-test-uid="`flexible-listing__${componentId}`">
        <div v-if="title || evolutionTitle || linkField" class="flexible-listing-header border-bottom border-black" :class="selectedColorVariant.divider">
          <div v-if="evolutionTitle" class="flexible-listing-title d-flex align-items-start justify-content-between" :class="selectedColorVariant.headline">
            <div v-html="evolutionTitle" />
            <a v-if="linkField && linkField.label"
               :href="linkField.url"
               :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
               :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
               class="h5 link mb-0 d-none d-md-inline-block mr-4 mt-5 text-nowrap"
               data-test-id="flexible-listing__more-desktop"
               :class="selectedColorVariant.headline"
               @click="ga4click(linkField.label)"
            >
              {{ linkField.label }} <b-icon icon="chevron-right" />
            </a>
          </div>
          <component
            :is="htag"
            v-else
            class="flexible-listing-title d-flex align-items-baseline justify-content-between"
            :class="[htag, selectedColorVariant.headline]"
          >
            <span>{{ title }}</span>
            <a v-if="linkField && linkField.label"
               :href="linkField.url"
               :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
               :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
               class="h5 link mb-0 d-none d-md-inline-block"
               data-test-id="flexible-listing__more-desktop"
               :class="selectedColorVariant.headline"
               @click="ga4click(linkField.label)"
            >
              {{ linkField.label }} <b-icon icon="chevron-right" />
            </a>
          </component>
        </div>
        <div v-else class="without-title" />
        <div v-if="allCards.length" class="flexible-listing-card">
          <transition-group tag="div" class="row" data-test-id="flexible-listing__cards" :name="animationEnabled === 'true' ? 'slide-bottom' : ''" mode="out-in">
            <div v-for="(itm, i) in allCards"
                 v-show="isVisible"
                 :key="`listing-${itm['@id'] || itm.publishDate}-${i}`"
                 class="col-12 mb-4"
                 :class="[currentVariant.styles,`transition-delay-${Math.floor(i / columns) + 1}`]"
                 :data-test-id="`flexible-listing-item__${i}`"
                 :data-test-uid="`flexible-listing-item__${itm['@id']}`"
            >
              <ProductListingItem :item="itm"
                                  :hide-images="showImages === 'false'"
                                  :horizontal="newHorizontal"
                                  :show-indicator="indicators === 'true'"
                                  :show-chips="showChips === 'true'"
                                  show-view-details-link
                                  :tiles-variant="selectedColorVariant.tiles"
                                  :amounts-variant="selectedColorVariant.amounts"
                                  :text-variant="selectedColorVariant.text"
                                  :chips-variant="selectedColorVariant.chips"
                                  :caption-variant="selectedColorVariant.caption"
                                  :labels="labels"
                                  is-flexible-listing
                                  :preload="shouldPreloadCard(i)"
                                  @mounted="handleMounted"
                                  @list-click="ga4click"
              />
            </div>
          </transition-group>
        </div>
        <WarningAlert v-else-if="inEditorEdit" data-test-id="flexible-listing__empty">No cards configured in component.</WarningAlert>

        <div v-if="linkField && linkField.label" class="row d-md-none mt-4">
          <div class="col-12">
            <a :href="linkField.url"
               :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
               :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
               class="h5 mb-0"
               data-test-id="flexible-listing__more-mobile"
               :class="selectedColorVariant.headline"
            >
              {{ linkField.label }} <b-icon icon="chevron-right" />
            </a>
          </div>
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {computed, ref, useContext, useFetch, inject} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'
import useVariants from '~/helpers/composables/useVariants'
import {useElementVisibility} from '~/helpers/composables/useElementVisibility'

export default {
  props: ['cards', 'showChips', 'showImages', 'indicators', 'columns', 'title', 'link', 'htag', 'evolutionTitle', 'metadata', 'horizontal', 'nodeId', 'variant', 'animationEnabled', 'runCampaign', 'eagerLoad'],
  emits: ['component-name'],
  setup(props, {emit}) {
    const campaigns = inject('campaigns')
    useMounted(props.nodeId)
    emit('component-name', 'flexible-listing')
    const {cards, metadata, link, columns, horizontal, variant, animationEnabled, runCampaign, eagerLoad} = props
    const {getColorVariantStylesForListing} = useVariants()
    // horizontal is for old prod settings, newHorizontal is for current component version
    const newHorizontal = ref(false)
    const {isVisible, observerTarget, checkVisibility} = useElementVisibility(animationEnabled === 'true')
    const {$utils, $regions, $ga4, $services} = useContext()
    const labels = ref({})

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications'])
    })

    const allCards = cards && cards['@nodes']
      ? cards['@nodes'].map((x) => {
        return cards[x]
      })
      : []

    if (runCampaign === 'true' && campaigns?.value?.length > 0) {
      const compareIndexes = (a, b) => a.positionIndex - b.positionIndex
      const sortedCampaigns = [...campaigns.value]
      sortedCampaigns.sort(compareIndexes).forEach(c => allCards?.splice(c?.positionIndex, 0, {
        ...c
      }))
    }

    const currentVariant = computed(() => {
      if (parseInt(columns) === 2) {
        return {
          styles: 'col-md-6'
        }
      } else if (parseInt(columns) === 3) {
        return {
          styles: 'col-sm-6 col-lg-4'
        }
      } else if (parseInt(columns) === 4) {
        return {
          styles: 'col-sm-6 col-md-4 col-lg-3'
        }
      } else if (horizontal === 'true' || parseInt(columns) === 1) {
        newHorizontal.value = true
        return {
          styles: ''
        }
      } else {
        return {styles: 'col-md-6 col-lg mb-lg-0'}
      }
    })

    const FLEXIBLE_LISTING_EAGER_COUNT = 2
    const isEagerLoad = eagerLoad === true || eagerLoad === 'true'
    const shouldPreloadCard = index => isEagerLoad || index < FLEXIBLE_LISTING_EAGER_COUNT
    const selectedColorVariant = ref(getColorVariantStylesForListing(variant))
    const linkField = ref($utils.getLinkField(link, $regions.current.language.name))
    const componentId = metadata ? metadata['@id'] : Math.random().toString(16).substr(2, 8)

    const handleMounted = () => {
      setTimeout(checkVisibility, 300) // delay to ensure all cards are rendered
    }

    const ga4click = (cta, itemName) => {
      const config = {
        headline: props.evolutionTitle?.replace(/<[^>]*>/g, '') || props.title,
        cta_text: cta,
        item_name: itemName
      }
      $ga4.events.flexible_listing_click(config)
    }

    return {
      observerTarget,
      allCards,
      handleMounted,
      isVisible,
      linkField,
      componentId,
      currentVariant,
      newHorizontal,
      inEditorEdit,
      selectedColorVariant,
      ga4click,
      labels,
      campaigns,
      isEagerLoad,
      shouldPreloadCard
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/_colors';

.flexible-listing + .flexible-listing .without-title {
  margin-top: -3rem;
}

.flexible-listing-header {
  margin-bottom: 2.5rem; // 40px gap between header/line and cards
  padding-bottom: 0.5rem;
}

.flexible-listing .flexible-listing-title {
  h1, h2, h3, h4, h5, h6, p {
    margin-bottom: 0;
    margin-top: 0;
    line-height: 1.375; // correct line-height for H2 (66px / 48px)
  }

  // 8px gap between subtitle and H2 inside evolution title v-html
  > div > * + * {
    margin-top: 0.5rem !important;
  }
}

.flexible-listing .flexible-listing-title .text-white:hover,
.flexible-listing-card .link.text-white:hover,
.flexible-listing .card.with-hover:hover a,
.flexible-listing a:hover {
  color: $primary-hover !important;
}

div[data-test-id='flexible-listing'] {
  iframe {
    width: 100%;
  }
}

.flexible-listing-card {
  min-height: 10rem;

  .row > [class^='col-'],
  .row > [class*=' col-'] {
    padding-left: 1rem !important;
    padding-right: 1rem !important;
  }
}

.evolution-gradient {
  .card-body {
    background-image: url('~/assets/evolution-gradient.png');
    background-size: contain;
    background-repeat: no-repeat;
    background-position: top right;
  }
}
</style>
