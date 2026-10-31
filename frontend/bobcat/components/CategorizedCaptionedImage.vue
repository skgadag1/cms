<template>
  <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
    <section data-test-id="categorized-captioned-image" :class="selectedColorVariant.background" class="pb-4">
      <div class="container">
        <div class="d-flex flex-column w-100 justify-content-center align-items-center">
          <div v-if="gridTitle" class="border-bottom mb-5 mt-3 w-100">
            <component :is="htag" :class="[htag, selectedColorVariant.headline]">{{ gridTitle }}</component>
          </div>
          <Tabs :tabs="items" @tabSwitchCallback="ga4click">
            <TabContent v-for="(tab, index) in items" :key="tab.title" class="p-3 bg-white" :active="index === 0">
              <div
                class="row categorized-captioned-image-container"
                data-test-id="categorized-captioned-image-container"
              >
                <div v-for="(card, i) in tab.cards"
                     :key="`card-${card['@id']}`"
                     :class="currentVariant.styles"
                     :data-test-id="`categorized-captioned-image-${i}`"
                >
                  <LightWeight
                    :title="card.title"
                    :htag="card.htag"
                    :asset="card.asset"
                    :paragraph="addYoutubeNoCookie(card.paragraph)"
                    :is-single-column="columns === 1"
                  />
                </div>
              </div>
            </TabContent>
          </Tabs>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {computed, ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import {DARK_VARIANT, VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'

export default {
  props: {
    nodeId: {
      type: String
    },
    tabs: {
      type: Object
    },
    gridTitle: {
      type: String
    },
    columns: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    }
  },
  setup(props) {
    const {tabs, columns, gridTitle} = props
    const {$ga4} = useContext()
    useMounted(props.nodeId)

    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])

    const items = ref(tabs
      ? tabs['@nodes'].map(x => tabs[x])?.map(tab => ({...tab, cards: tab?.cards?.['@nodes']?.map(y => tab?.cards[y])}))
      : [])

    const activeTab = ref(0)

    const ga4click = (tabTitle) => {
      $ga4.events.categorized_captioned_image_click(gridTitle, tabTitle)
    }

    const currentVariant = computed(() => {
      if (parseInt(columns) === 1) {
        return {
          styles: 'col-12'
        }
      } else if (parseInt(columns) === 2) {
        return {
          styles: 'col-12 col-md-6'
        }
      } else {
        return {
          styles: 'col-12 col-sm-6 col-lg-4'
        }
      }
    })

    return {
      items,
      activeTab,
      currentVariant,
      addYoutubeNoCookie,
      inEditorEdit,
      selectedColorVariant,
      DARK_VARIANT,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.categorized-captioned-image-container {
  width: calc(100% + #{$grid-gutter-width});
}

.categorized-captioned-image-container {
  ::v-deep iframe {
    width: 100%;
  }
}
</style>
