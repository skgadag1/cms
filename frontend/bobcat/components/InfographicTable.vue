<template>
  <LazyHydrate when-visible>
    <section :class="currentVariant.background">
      <div
        class="container infographic-table d-flex justify-content-center flex-wrap py-4"
        :class="currentVariant.text"
      >
        <div class="infographic-table-content">
          <div
            class="infographic-table-title font-weight-bold"
            v-html="title"
          />
          <div class="infographic-table-description" v-html="description" />

          <div>
            <DynamicButton
              v-for="(button, index) in ctas"
              :key="`infographic-table-cta-button-${index}`"
              :variant="button.variant"
              :color="button.color"
              :href="button.link"
              :target="
                button.target === '_blank_follow' ? '_blank' : button.target
              "
              class="mr-0 mr-sm-3 mb-sm-0 text-uppercase"
              :class="{
                'mb-3': index < ctas.length - 1,
                'pl-0 text-left': button.variant === BUTTON_NAMES.LINEAR,
              }"
              data-test-id="cta-button"
            >
              {{ button.title }}
              <b-icon
                v-if="button.variant === BUTTON_NAMES.LINEAR"
                icon="chevron-right"
                class="mt-1"
              />
            </DynamicButton>
          </div>
        </div>

        <div
          v-if="useBentoLayout"
          ref="bentoRef"
          class="infographic-table-bento"
        >
          <div
            v-for="tile in bentoTiles"
            :key="tile.id"
            class="infographic-table-bento-tile"
            :class="getBentoTileClasses(tile)"
            :style="getBentoTileStyle(tile)"
          >
            <img
              v-if="tile.tileOption === BENTO_TILE_OPTIONS.imgBg && tile.imBg"
              class="infographic-table-bento-tile-bg"
              :src="tile.imBg"
              alt=""
              loading="lazy"
              decoding="async"
            >
            <div class="infographic-table-bento-tile-text">
              <div class="infographic-table-bento-tile-fit">
                <div
                  v-if="tile.before"
                  class="infographic-table-bento-tile-before"
                  v-html="tile.before"
                />
                <div
                  v-if="tile.tileOption === BENTO_TILE_OPTIONS.title && tile.contentTitle"
                  class="infographic-table-bento-tile-title font-weight-bold"
                  v-html="tile.contentTitle"
                />
                <img
                  v-else-if="tile.tileOption === BENTO_TILE_OPTIONS.img && tile.img"
                  class="infographic-table-bento-tile-img"
                  :src="tile.img"
                  alt=""
                  loading="lazy"
                  decoding="async"
                  @load="scheduleBentoFit"
                >
                <div
                  v-if="tile.after"
                  class="infographic-table-bento-tile-after"
                  v-html="tile.after"
                />
              </div>
            </div>
          </div>
        </div>

        <div
          v-else
          class="infographic-table-elements"
          :class="`infographic-table-elements-${componentSize}`"
        >
          <div
            v-for="element in elements"
            :key="element.id"
            class="infographic-table-element"
            :class="getElementRootClasses(element)"
            :style="
              element.contentType === CONTENT_TYPES.bgImage
                ? { backgroundImage: `url(${element.backgroundImage})` }
                : {}
            "
          >
            <div
              class="infographic-table-element-before"
              :class="{
                'font-weight-bold': componentSize !== COMPONENT_SIZES.small,
              }"
              v-html="element.before"
            />
            <div
              v-if="element.contentType === CONTENT_TYPES.title"
              class="infographic-table-element-content-title font-weight-bold"
              v-html="element.content"
            />
            <div
              v-else-if="element.contentType === CONTENT_TYPES.img"
              class="infographic-table-element-content-image"
            >
              <ImageSet
                :src="element.image.img"
                :alt="element.image.alt"
                :title="element.image.title"
                :img-width="element.image.imgWidth"
                :img-height="element.image.imgHeight"
                crop="limit"
                :sizes="[]"
                :aspect-ratio="null"
              />
            </div>
            <div
              class="infographic-table-element-after"
              :class="{
                'font-weight-bold': componentSize !== COMPONENT_SIZES.small,
              }"
              v-html="element.after"
            />
          </div>
        </div>

        <div class="infographic-table-content">
          <div class="infographic-table-info" v-html="textAfter" />
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  useContext
} from '@nuxtjs/composition-api'
import {
  LIGHT_GRAY_VARIANT,
  VARIANTS_CLASSES,
  CUSTOM_CARD_VARIANTS,
  CARD_OPTIONS
} from '~/constants/variants/infographicTableVariants'
import {BUTTON_NAMES} from '~/constants/buttons'

const COMPONENT_SIZES = {
  small: 'small',
  medium: 'medium',
  large: 'large'
}

const CONTENT_TYPES = {
  title: 'title',
  img: 'img',
  bgImage: 'bgImage'
}

const BENTO_TILE_OPTIONS = {
  title: 'title',
  img: 'img',
  imgBg: 'imgBg'
}

const BENTO_DATA_SOURCE = 'bento'
const BENTO_DEVICES = ['desktop', 'tablet', 'mobile']
const BENTO_DEFAULT_RESPONSIVE = {x: 0, y: 0, w: 1, h: 1}

function normalizeBentoCell(value, fallback) {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

function normalizeBentoResponsive(raw = {}) {
  const normalized = {}
  BENTO_DEVICES.forEach((device) => {
    const source = raw[device] || {}
    normalized[device] = {
      x: normalizeBentoCell(source.x, BENTO_DEFAULT_RESPONSIVE.x),
      y: normalizeBentoCell(source.y, BENTO_DEFAULT_RESPONSIVE.y),
      w: Math.max(1, normalizeBentoCell(source.w, BENTO_DEFAULT_RESPONSIVE.w)),
      h: Math.max(1, normalizeBentoCell(source.h, BENTO_DEFAULT_RESPONSIVE.h))
    }
  })
  return normalized
}

function parseBentoLayout(value) {
  if (!value || typeof value !== 'string') {
    return []
  }
  try {
    const parsed = JSON.parse(value)
    if (!parsed || !Array.isArray(parsed.tiles)) {
      return []
    }
    return parsed.tiles
  } catch {
    return []
  }
}

export default {
  props: {
    title: {
      type: String
    },
    description: {
      type: String
    },
    textAfter: {
      type: String
    },
    variant: {
      type: Object
    },
    componentSize: {
      type: String,
      default: COMPONENT_SIZES.small
    },
    cta: {
      type: Object
    },
    items: {
      type: Object
    },
    bentoLayout: {
      type: String,
      default: ''
    },
    listDataSource: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const {$utils, $regions} = useContext()
    const {cta, items} = props
    const ctas = computed({
      set: () => {},
      get: () => {
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
      }
    })

    const getImageObject = (item) => {
      if (item?.content?.field === CONTENT_TYPES.img) {
        const {publicId, metadata} = $utils.getCloudinaryAssetMetadata(
          item?.content?.src
        )
        return {
          file_name: publicId,
          img: item?.content?.src || '',
          title:
            item?.content?.imageTitle ||
            metadata?.context?.caption ||
            metadata?.title,
          alt:
            item?.content?.imageAlt ||
            metadata?.context?.alt ||
            metadata?.description ||
            metadata?.['alt_' + $regions.current.language.name],
          imgWidth: item?.content?.imageWidth || null,
          imgHeight: item?.content?.imageHeight || null,
          ...metadata
        }
      }
      return {}
    }

    const getClassVariant = (item) => {
      if (item.bgColor && item.bgColor !== CARD_OPTIONS.none) {
        return CUSTOM_CARD_VARIANTS[item.bgColor] || ''
      }
      return (
        VARIANTS_CLASSES[props.variant?.field || LIGHT_GRAY_VARIANT]?.card || ''
      )
    }

    const elements = computed(() => {
      return items?.['@nodes']
        ? items['@nodes'].map((x, index) => {
          return {
            id: items[x]['@id'] || 'infographic-table-element-' + index,
            before: items[x].before || '',
            contentType: items[x]?.content?.field || '',
            content: items[x]?.content?.elementTitle || '',
            image: getImageObject(items[x]),
            after: items[x].after || '',
            classVariant: getClassVariant(items[x]),
            backgroundImage:
                items[x]?.content?.field === CONTENT_TYPES.bgImage
                  ? $utils.getCloudinaryAsset(items[x]?.content.src, true)
                  : '',
            textPosition: items[x]?.content?.textPosition || 'top'
          }
        })
        : []
    })

    const getElementRootClasses = (element) => {
      const isBgImage = element.contentType === CONTENT_TYPES.bgImage
      return {
        [element.classVariant]: !isBgImage,
        [`infographic-table-element-content-${element.textPosition}`]:
          isBgImage,
        'infographic-table-element-background': isBgImage
      }
    }

    const currentVariant = computed(() => {
      return VARIANTS_CLASSES[props.variant?.field || LIGHT_GRAY_VARIANT]
    })

    const bentoTiles = computed(() => {
      return parseBentoLayout(props.bentoLayout).map((tile, index) => {
        const responsive = normalizeBentoResponsive(tile.responsive)
        return {
          id: tile.id || `bento-tile-${index}`,
          tileOption: Object.values(BENTO_TILE_OPTIONS).includes(tile.tileOption)
            ? tile.tileOption
            : BENTO_TILE_OPTIONS.title,
          contentTitle: typeof tile.contentTitle === 'string' ? tile.contentTitle : '',
          img: typeof tile.img === 'string' ? tile.img : '',
          imBg: typeof tile.imBg === 'string' ? tile.imBg : '',
          bgColor: CARD_OPTIONS[tile.bgColor] ? tile.bgColor : CARD_OPTIONS.none,
          before: typeof tile.before === 'string' ? tile.before : '',
          after: typeof tile.after === 'string' ? tile.after : '',
          responsive
        }
      })
    })

    const useBentoLayout = computed(() => {
      return props.listDataSource === BENTO_DATA_SOURCE
    })

    const getBentoTileClasses = (tile) => {
      const variantCardClass =
        VARIANTS_CLASSES[props.variant?.field || LIGHT_GRAY_VARIANT]?.card || ''
      const customBgClass =
        tile.bgColor && tile.bgColor !== CARD_OPTIONS.none
          ? CUSTOM_CARD_VARIANTS[tile.bgColor] || ''
          : variantCardClass
      return {
        [customBgClass]: !!customBgClass,
        'infographic-table-bento-tile--has-bg-image':
          tile.tileOption === BENTO_TILE_OPTIONS.imgBg && !!tile.imBg
      }
    }

    const getBentoTileStyle = (tile) => {
      const {desktop, tablet, mobile} = tile.responsive
      return {
        '--bento-d-col': `${desktop.x + 1} / span ${desktop.w}`,
        '--bento-d-row': `${desktop.y + 1} / span ${desktop.h}`,
        '--bento-t-col': `${tablet.x + 1} / span ${tablet.w}`,
        '--bento-t-row': `${tablet.y + 1} / span ${tablet.h}`,
        '--bento-m-col': `${mobile.x + 1} / span ${mobile.w}`,
        '--bento-m-row': `${mobile.y + 1} / span ${mobile.h}`
      }
    }

    const bentoRef = ref(null)
    let fitObserver = null
    let fitFrame = null
    let onFontsLoaded = null
    const BENTO_FIT_SAFETY = 2

    const fitBentoTile = (outer) => {
      const fit = outer.querySelector('.infographic-table-bento-tile-fit')
      if (!fit) { return }
      fit.style.transform = ''
      const availH = fit.clientHeight
      if (availH <= 0) { return }
      const gap = parseFloat(getComputedStyle(fit).rowGap) || 0
      const children = Array.from(fit.children)
      let contentH = gap * Math.max(0, children.length - 1)
      children.forEach((el) => { contentH += el.offsetHeight })
      if (contentH > availH + 1) {
        const scale = Math.max(0.1, (availH - BENTO_FIT_SAFETY) / contentH)
        fit.style.transform = `scale(${scale})`
      }
    }

    const refreshBentoFit = () => {
      const root = bentoRef.value
      if (!root) { return }
      if (!fitObserver && typeof ResizeObserver !== 'undefined') {
        fitObserver = new ResizeObserver((entries) => {
          entries.forEach((entry) => {
            const outer = entry.target.closest(
              '.infographic-table-bento-tile-text'
            )
            if (outer) { fitBentoTile(outer) }
          })
        })
      }
      if (fitObserver) { fitObserver.disconnect() }
      root
        .querySelectorAll('.infographic-table-bento-tile-text')
        .forEach((outer) => {
          fitBentoTile(outer)
          if (fitObserver) { fitObserver.observe(outer) }
        })
    }

    const scheduleBentoFit = () => {
      if (typeof requestAnimationFrame === 'undefined') {
        refreshBentoFit()
        return
      }
      if (fitFrame) { cancelAnimationFrame(fitFrame) }
      fitFrame = requestAnimationFrame(() => {
        fitFrame = null
        refreshBentoFit()
      })
    }

    onMounted(() => {
      if (!useBentoLayout.value) { return }
      scheduleBentoFit()
      if (typeof document !== 'undefined' && document.fonts) {
        if (document.fonts.ready) {
          document.fonts.ready.then(() => scheduleBentoFit())
        }
        onFontsLoaded = () => scheduleBentoFit()
        document.fonts.addEventListener('loadingdone', onFontsLoaded)
      }
    })

    onBeforeUnmount(() => {
      if (fitFrame && typeof cancelAnimationFrame !== 'undefined') {
        cancelAnimationFrame(fitFrame)
      }
      if (fitObserver) {
        fitObserver.disconnect()
        fitObserver = null
      }
      if (onFontsLoaded && typeof document !== 'undefined' && document.fonts) {
        document.fonts.removeEventListener('loadingdone', onFontsLoaded)
        onFontsLoaded = null
      }
    })

    return {
      ctas,
      elements,
      currentVariant,
      getElementRootClasses,
      bentoTiles,
      useBentoLayout,
      getBentoTileClasses,
      getBentoTileStyle,
      scheduleBentoFit,
      bentoRef,

      COMPONENT_SIZES,
      CONTENT_TYPES,
      BENTO_TILE_OPTIONS,
      BUTTON_NAMES
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.evolution-gradient {
  background-image: url('~/assets/evolution-gradient.png');
  background-size: 100%;

  @include media-breakpoint-down(md) {
    background-size: cover;
    background-position: center;
  }
}

.infographic-table {
  &-title {
    text-transform: uppercase;
    text-align: center;

    :deep(p) {
      font-size: $font-size-base * 3;

      @include media-breakpoint-down(md) {
        font-size: $h3-font-size;
      }
    }
  }

  &-description,
  &-info {
    :deep(h1) {
      font-size: $font-size-base;
    }
  }

  @include media-breakpoint-up(md) {
    &-content {
      max-width: 38.75rem;
    }
  }

  &-elements {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    padding: 2rem 0;
    width: 100%;

    &-small {
      gap: 0.5rem;

      @include media-breakpoint-down(md) {
        gap: 1rem;
      }

      .infographic-table-element {
        min-height: 8.75rem;
        width: calc((100% - 40px) / 6);
        padding: 1rem 0.5rem;

        &-before,
        &-after {
          font-size: $font-size-sm;
        }

        &-content-title {
          font-size: $h4-font-size;
        }

        &-content-image :deep(img) {
          max-height: 3rem;
        }

        @include media-breakpoint-down(lg) {
          width: calc((100% - 40px) / 3);
        }

        @include media-breakpoint-down(md) {
          width: 100%;
        }
      }
    }

    &-medium {
      gap: 2rem 0.5rem;

      @include media-breakpoint-down(md) {
        gap: 1rem;
      }

      .infographic-table-element {
        min-height: 10rem;
        width: calc((100% - 30px) / 4);
        padding: 1.5rem 1rem;

        &-before,
        &-after {
          font-size: $font-size-base;
        }

        &-content-title {
          font-size: $h3-font-size;
        }

        &-content-image :deep(img) {
          max-height: 5rem;
        }

        @include media-breakpoint-down(lg) {
          width: calc((100% - 40px) / 2);
        }

        @include media-breakpoint-down(md) {
          width: 100%;
        }
      }
    }

    &-large {
      gap: 2rem 0.5rem;

      @include media-breakpoint-down(md) {
        gap: 1rem;
      }

      .infographic-table-element {
        min-height: 14.375rem;
        width: calc((100% - 20px) / 3);
        padding: 2.5rem 1rem;

        &-before,
        &-after {
          font-size: $font-size-base * 1.125;
        }

        &-content-title {
          font-size: $h2-font-size;
        }

        &-content-image :deep(img) {
          max-height: 6rem;
        }

        @include media-breakpoint-down(lg) {
          width: calc((100% - 40px) / 2);
        }

        @include media-breakpoint-down(md) {
          width: 100%;
        }
      }
    }
  }

  &-element {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    align-content: center;
    border-radius: 2px;

    &-background {
      background-size: cover;
      background-position: center;
    }

    &-content-top {
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
    }

    &-content-bottom {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
    }

    &-before,
    &-content-title,
    &-after {
      width: 100%;
      text-align: center;

      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4),
      :deep(h5),
      :deep(h6),
      :deep(p) {
        margin-bottom: 0;
      }
    }
  }

  &-bento {
    display: grid;
    width: 100%;
    grid-template-columns: repeat(6, 1fr);
    grid-auto-rows: calc((100vw - #{$grid-gutter-width} - (0.75rem * 5)) / 6);
    gap: 0.75rem;
    padding: 2rem 0;

    @include media-breakpoint-up(lg) {
      grid-auto-rows: 9.875rem;
    }

    &-tile {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 2px;
      overflow: hidden;
      padding: 0.375rem 0.5rem;
      min-width: 0;

      grid-column: var(--bento-m-col);
      grid-row: var(--bento-m-row);

      @include media-breakpoint-up(md) {
        grid-column: var(--bento-t-col);
        grid-row: var(--bento-t-row);
      }

      @include media-breakpoint-up(lg) {
        grid-column: var(--bento-d-col);
        grid-row: var(--bento-d-row);
      }

      &--has-bg-image {
        padding: 0;
      }

      &-bg {
        position: absolute;
        inset: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &-text {
        position: relative;
        z-index: 1;
        width: 100%;
        height: 100%;
        overflow: hidden;
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      &-fit {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.25rem;
        width: 100%;
        max-width: 100%;
        height: 100%;
        transform-origin: center center;
        word-break: break-word;
        overflow-wrap: anywhere;
      }

      &--has-bg-image &-text {
        padding: 0.375rem 0.5rem;
        color: $white;
        text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
      }

      &-img {
        flex: 0 1 auto;
        min-height: 0;
        width: auto;
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
        margin: 0 auto;
      }

      &-before,
      &-after,
      &-title {
        flex: 0 0 auto;
        width: 100%;
        min-width: 0;

        :deep(h1),
        :deep(h2),
        :deep(h3),
        :deep(h4),
        :deep(h5),
        :deep(h6),
        :deep(p) {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>
