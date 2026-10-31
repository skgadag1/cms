<template>
  <section data-test-id="captioned-image" :class="[selectedColorVariant.background, extraMargin === 'true' ? 'pt-5' : '']">
    <div class="container">
      <div class="d-flex flex-column w-100 justify-content-center align-items-center">
        <LazyHydrate never>
          <div v-if="gridTitle" class="border-bottom mb-5 mt-3 w-100">
            <component :is="htag" :class="[htag, selectedColorVariant.headline]">{{ gridTitle }}</component>
          </div>
        </LazyHydrate>

        <div v-if="items.length" data-test-id="captioned-image-container" class="row captioned-image-container">
          <div v-for="(item, i) in items"
               :key="`item-${item['@id']}`"
               :class="currentVariant.styles"
               :data-test-id="`captioned-image-${i}`"
          >
            <LightWeight :title="item.title"
                         :htag="item.htag"
                         :asset="item.asset"
                         :paragraph="addYoutubeNoCookie(item.paragraph)"
                         :is-single-column="columns === 1"
                         :is-dark-mode="variant === DARK_VARIANT"
            />
          </div>
        </div>
        <WarningAlert v-else-if="inEditorEdit">No cards configured in the component.</WarningAlert>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, ref} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import {DARK_VARIANT, VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'

export default {
  props: {
    nodeId: {
      type: String
    },
    cards: {
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
    },
    extraMargin: {
      type: String,
      default: 'true'
    }
  },
  setup(props) {
    const {cards, columns} = props
    useMounted(props.nodeId)

    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])

    const items = ref(cards
      ? cards['@nodes'].map(x => cards[x])
      : [])

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
      currentVariant,
      addYoutubeNoCookie,
      inEditorEdit,
      selectedColorVariant,
      DARK_VARIANT
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.captioned-image-container {
  width: calc(100% + #{$grid-gutter-width});
}

.captioned-image-container {
  ::v-deep iframe {
    width: 100%;
  }
}
</style>
