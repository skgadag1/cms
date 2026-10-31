<template>
  <LazyHydrate when-visible>
    <section class="evolution-value-propostion"
             :class="
               [
                 VARIANTS_CLASSES[variant]?.background || '',
                 VARIANTS_CLASSES[variant]?.text || ''
               ]"
    >
      <div class="container">
        <div class="evolution-value-propostion-head">
          <div class="evolution-content pb-2 text-center" v-html="title" />
          <div class="evolution-content text-center" v-html="subtitle" />
        </div>

        <div class="evolution-value-propostion-cards"
             :class="
               [
                 `cards-${mappedCards.length}`,
                 VARIANTS_CLASSES[variant]?.cardsBackground || ''
               ]"
        >
          <div
            v-for="(card, i) in mappedCards"
            :key="card.id"
            :class="`item-${i}`"
            class="animation-on-hover item"
          >
            <div class="item-asset text-center flex-shrink-0" :class="'color-' + variant">
              <ImageSet
                v-if="card.asset?.src"
                :src="card.asset.src"
                :alt="card.asset.alt"
                :title="card.asset.title"
                :options="{'fluid-grow':false}"
                :height="card.asset.height || 75"
                :width="card.asset.width || 75"
              />
              <AnimatedIcon
                v-else-if="card.asset?.icon"
                :name="card.asset.icon"
                :variant="variant"
              />
            </div>
            <div class="item-content">
              <div class="w-100">
                <div
                  class="evolution-content text-center item-title"
                  :class="{
                    'item-title-long': card.longTitle
                  }"
                  v-html="card.title"
                />
                <div class="evolution-content text-center item-description" v-html="card.description" />
              </div>
              <DynamicButton
                v-if="card.link"
                size="sm"
                class="shadow-none item-link"
                :variant="BUTTON_NAMES.LINEAR"
                :color="buttonColor"
                :href="card.link.url"
                :target="card.link.target === '_blank_follow' ? '_blank' : card.link.target"
                :rel="card.link.target === '_blank' ? 'noopener nofollow' : null"
              >
                {{ card.link.label }}
                <b-icon class="ml-1" icon="chevron-right" />
              </DynamicButton>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {useContext, computed, ref, onMounted} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '@/constants/variants/evolutionValuePropostionVariants'
import AnimatedIcon from '@/components/AnimatedIcon'
import {convertToPlain} from '~/helpers/functional/AppHelpers'
import {BUTTON_NAMES} from '~/constants/buttons'

export default {
  components: {
    AnimatedIcon
  },
  props: {
    variant: {
      type: String
    },
    title: {
      type: String
    },
    subtitle: {
      type: String
    },
    buttonColor: {
      type: String
    },
    cards: {
      type: Object
    }
  },
  setup(props) {
    const {$utils, $regions} = useContext()

    const mappedCards = computed(() => {
      const numberOfLongTitle = props.cards['@nodes'].length === 4 ? 20 : 28
      const longTitle = props.cards['@nodes'].some(nodeKey => convertToPlain(props.cards[nodeKey]?.title)?.length > numberOfLongTitle)
      return props.cards['@nodes'].map((nodeKey) => {
        const cardData = props.cards[nodeKey]
        const asset = getAsset(cardData.asset)
        return {
          id: nodeKey + cardData['mgnl:lastModified'] + cardData['@id'],
          title: cardData.title,
          longTitle,
          description: cardData.description,
          link: (cardData.link?.link) ? $utils.getLinkField(cardData.link, $regions.current.language.name) : null,
          asset
        }
      })
    })

    const getAsset = (asset) => {
      if (asset.field === 'svg-animation' && asset['svg-animation']) {
        return {icon: asset['svg-animation']}
      } else if (asset.field === 'img' && asset?.img?.src) {
        const obj = {}
        const {metadata} = $utils.getCloudinaryAssetMetadata(asset?.img?.src)
        const isSvg = metadata?.format === 'svg'
        Object.assign(obj, {
          src: asset?.img?.src.startsWith('cloudinary:')
            ? $utils.getCloudinaryAsset(asset?.img?.src, true, {}, isSvg)
            : asset?.img?.src,
          isSvg,
          title: asset?.img?.imageTitle || metadata?.context?.caption || metadata?.title,
          alt: asset?.img?.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
          width: asset?.img?.imageWidth || null,
          height: asset?.img?.imageHeight || null,
          svgContent: ref(null), // placeholder for inline SVG
          ...metadata
        })
        return obj
      }

      return {}
    }

    onMounted(() => {
      mappedCards.value.forEach((card) => {
        if (card.asset?.isSvg && card.asset?.src) {
          fetch(card.asset.src)
            .then(res => res.text())
            .then((svg) => {
              card.asset.svgContent.value = svg
            })
            // eslint-disable-next-line no-console
            .catch(err => console.error('SVG fetch failed:', err))
        }
      })
    })

    return {
      BUTTON_NAMES,
      mappedCards,
      VARIANTS_CLASSES
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.evolution-value-propostion {
  padding-bottom: 3.125rem;
  padding-top: 3.125rem;

  :deep(.evolution-content) {
    h1, h2, h3, h4, h5, h6, p {
      margin-bottom: 0;
      line-height: 1.1;
      text-align: inherit!important;
      word-break: break-word;
    }
  }

  &-head {
    padding-bottom: 2.5rem;
  }

  &-cards {
    display: grid;

    .item {
      grid-template-rows: auto 1fr;
      display: grid;
      padding: 2rem 1rem;
      text-align: center;
      min-height: 26.0625rem; // 417px desktop

      @include media-breakpoint-down(md) {
        min-height: 21.5rem; // 344px tablet/mobile
        padding: 2rem 1rem;
      }
    }

    .item-asset {
      height: 6.6875rem; // 107px = 32px top padding + 75px icon
      width: 100%;
      display: flex;
      align-items: flex-end;
      justify-content: center;
      padding-bottom: 2rem;

      img,
      :deep(svg) {
        max-height: 4.6875rem; // 75px
        max-width: 6.0625rem; // ~97px max for wider icons
        width: auto;
        height: auto;
        object-fit: contain;
      }
    }

    .item-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-between;
      text-align: center;
      height: 100%;
    }

    .item-title {
      padding-bottom: 1rem;

      :deep(h1, h2, h3, h4, h5, h6, p) {
        text-transform: uppercase;
      }
    }

    .item-description {
      padding-bottom: 1.5rem;

      :deep(p) {
        line-height: 1.5;
      }
    }

    .item-link {
      margin-top: auto;
      padding-left: 0;
      padding-bottom: 0.75rem; // ~12px from bottom content area (32px total with card padding)

      :deep(.btn) {
        padding-left: 0;
      }
    }

    &.cards-4 {
      grid-template-columns: 1fr 1fr 1fr 1fr;
      grid-template-areas: "item-0 item-1 item-2 item-3";

      .item {
        border: 1px solid $tertiary;
        border-left: none;
      }
      .item-0 {
        border-left: 1px solid $tertiary;
      }

      .item-title-long {
        box-sizing: content-box;

        @include media-breakpoint-down(lg) {
          min-height: auto;
        }
      }

      @include media-breakpoint-down(md) {
        grid-template-columns: 1fr 1fr;
        grid-template-areas:
          "item-0 item-1"
          "item-2 item-3";

        .item-2, .item-3 {
          border-top: none;
        }
        .item-2 {
          border-left: 1px solid $tertiary;
        }
      }

      @include media-breakpoint-down(sm) {
        grid-template-columns: 1fr;
        grid-template-areas:
          "item-0"
          "item-1"
          "item-2"
          "item-3";

        .item {
          border-left: 1px solid $tertiary;
        }

        .item-1, .item-2, .item-3 {
          border-top: none;
        }
      }
    }

    &.cards-3 {
      grid-template-columns: 1fr 1fr 1fr;
      grid-template-areas: "item-0 item-1 item-2";

      .item-title-long {
        box-sizing: content-box;
        @include media-breakpoint-down(lg) {
          min-height: auto;
        }
      }

      .item {
        border: 1px solid $tertiary;
      }

      .item-1, .item-2 {
        border-left: none;
      }

      @include media-breakpoint-down(lg) {
        grid-template-columns: 1fr 1fr;
        grid-template-areas:
          "item-0 item-1"
          "item-2 item-2";

        .item-2 {
          border-top: none;
          border-left: 1px solid $tertiary;
        }
      }

      @include media-breakpoint-down(md) {
        grid-template-columns: 1fr;
        grid-template-areas:
          "item-0"
          "item-1"
          "item-2";

        .item-1 {
          border-top: none;
          border-left: 1px solid $tertiary;
        }
      }
    }

    &.cards-2 {
      grid-template-columns: 1fr 1fr;
      grid-template-areas: "item-0 item-1";

      .item {
        border: 1px solid $tertiary;
      }
      .item-1 {
        border-left: none;
      }

      @include media-breakpoint-down(md) {
        grid-template-columns: 1fr;
        grid-template-areas:
          "item-0"
          "item-1";

        .item-1 {
          border-left: 1px solid $tertiary;
          border-top: none;
        }
      }
    }

    &.cards-1 {
      grid-template-columns: 1fr;
      grid-template-areas: "item-0";

      .item-0 {
        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-gap: 2rem;
        text-align: left;

        a {
          padding-left: 0;
        }

        .item-asset {
          display: flex;
          justify-content: center;
          min-height: 5rem;
        }

        .item-content {
          min-height: auto;
          justify-content: start;

          a {
            margin-top: 0.5rem;
          }
        }

        @include media-breakpoint-down(md) {
          grid-template-columns: 1fr;
          grid-gap: 0;
          text-align: center;

          a {
            padding-left: 1rem;
          }
        }
      }
    }

    .item-0 {
      grid-area: item-0;
    }

    .item-1 {
      grid-area: item-1;
    }

    .item-2 {
      grid-area: item-2;
    }

    .item-3 {
      grid-area: item-3;
    }
  }
}
</style>
