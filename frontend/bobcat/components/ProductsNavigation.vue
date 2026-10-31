<template>
  <LazyHydrate when-visible>
    <section>
      <div data-test-id="products-navigation-tabs" class="bg-white">
        <div class="container">
          <div class="row">
            <div class="col-12">
              <h2 v-if="title" class="border-bottom mb-5 mt-3 w-100 pb-3 h2" data-test-id="products-navigation-title">
                {{ title }}
              </h2>
              <ul v-if="navTabs && navTabs.length > 1" class="nav nav-tabs evolution-tabs d-flex flex-nowrap">
                <li v-for="(tab) of navTabs" :key="`tab-${tab.name}`" class="nav-item">
                  <a class="nav-link h4 font-weight-bold mr-0" :class="tab.name === activeTab ? 'active' : ''" href="#" @click="handleTabClick($event, tab.name, tab.title)">{{ tab.title }}</a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div data-test-id="products-navigation" class="py-3" :class="variant === 'gray' ? 'bg-gray-lighter' : ''">
        <div class="container">
          <div class="row">
            <div class="col-12">
              <transition-group v-if="navTabs && navTabs.length > 0" name="fade-tabs">
                <div v-for="(tab) of navTabs"
                     v-show="tab.name === activeTab"
                     :key="`tab-items-${tab.name}`"
                     class="multitab-container"
                >
                  <ul
                    data-test-id="products-navigation-wrapper"
                    class="row products-navigation-items d-flex justify-content-center list-unstyled mb-0 mt-md-3 mt-0"
                    :class="{'mt-md-4 mt-2': !title}"
                  >
                    <li v-for="(item) in tab.items"
                        :key="item['@id']"
                        class="col-12 col-lg-2 col-md-3 mb-2 mb-md-3"
                    >
                      <a
                        v-if="item.link && item.link.url && item.link.target"
                        class="px-2 py-2 py-md-3 p-item rounded overflow-hidden transition-base w-100 h-100 d-flex flex-md-column justify-content-between align-items-center cursor-pointer"
                        :class="variant === 'white' ? 'bg-gray-lighter' : 'bg-white'"
                        data-test-id="products-navigation-item"
                        :href="item.link.url"
                        :title="item.link.label || item.link.url"
                        :rel="item.link.target === '_blank' ? 'noopener nofollow' : null"
                        :target="item.link.target === '_blank_follow' ? '_blank' : item.link.target"
                        @click="ga4click($event, item.link.label)"
                      >
                        <div class="product-container w-100 px-2 px-md-0 d-flex flex-md-column justify-content-start justify-content-md-center align-items-center">
                          <b-icon v-if="item && item.icon" data-test-id="item-icon" class="product-image-container p-icon text-black" :icon="item.icon" />
                          <div v-if="item.img && item.img.isSvg && item.img.svgContent" class="product-image-container d-flex align-items-center">
                            <div v-html="item.img.svgContent" />
                          </div>
                          <ImageSet
                            v-else-if="item && item.img && item.img.src"
                            :src="item.img.src"
                            :alt="item.img.alt"
                            :title="item.img.title"
                            :img-width="item.img.imgWidth"
                            :img-height="item.img.imgHeight"
                            class="w-100 product-image-container transition-base"
                          />
                          <component :is="item.htag" v-if="item.link && item.link.label && item.htag" class="h6 text-left text-md-center m-0 mt-md-2 ml-4 ml-md-0 z-index-1">{{ item.link.label }}</component>
                        </div>
                        <b-icon class="text-primary d-md-none d-flex mr-2" icon="chevron-right" />
                      </a>
                    </li>
                  </ul>
                </div>
              </transition-group>
              <ul v-else-if="navItems && navItems.length"
                  data-test-id="products-navigation-wrapper"
                  class="row products-navigation-items d-flex justify-content-center list-unstyled mb-0"
                  :class="{'mt-md-4 mt-2': !title}"
              >
                <li v-for="(item) in navItems"
                    :key="item['@id']"
                    class="col-12 col-lg-2 col-md-3"
                >
                  <a
                    v-if="item.link && item.link.url && item.link.target"
                    class="px-2 py-2 py-md-3 p-item rounded overflow-hidden transition-base w-100 h-100 d-flex flex-md-column justify-content-between align-items-center cursor-pointer"
                    :class="variant === 'white' ? 'bg-gray-lighter' : 'bg-white'"
                    data-test-id="products-navigation-item"
                    :href="item.link.url"
                    :title="item.link.label || item.link.url"
                    :rel="item.link.target === '_blank' ? 'noopener nofollow' : null"
                    :target="item.link.target === '_blank_follow' ? '_blank' : item.link.target"
                    @click="ga4click($event, item.link.label)"
                  >
                    <div class="product-container w-100 px-2 px-md-0 d-flex flex-md-column justify-content-start justify-content-md-center align-items-center">
                      <b-icon v-if="item && item.icon" data-test-id="item-icon" class="product-image-container p-icon text-black" :icon="item.icon" />
                      <ImageSet
                        v-else-if="item && item.img && item.img.src"
                        :src="item.img.src"
                        :alt="item.img.alt"
                        :title="item.img.title"
                        :img-width="item.img.imgWidth"
                        :img-height="item.img.imgHeight"
                        class="w-100 product-image-container transition-base"
                      />
                      <component :is="item.htag" v-if="item.link && item.link.label && item.htag" class="h6 text-left text-md-center m-0 mt-md-2 ml-4 ml-md-0 z-index-1">{{ item.link.label }}</component>
                    </div>
                    <b-icon class="text-primary d-md-none d-flex mr-2" icon="chevron-right" />
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {inject, ref, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    items: {
      type: Object
    },
    tabs: {
      type: Object
    },
    variant: {
      type: String,
      default: 'white'
    },
    title: {
      type: String
    }
  },
  setup(props, context) {
    const {tabs} = props
    const {$utils, $regions, $ga4, $config} = useContext()
    const navItems = ref([])
    const navTabs = ref([])
    const activeTab = ref('')
    useMounted(props.nodeId)
    const model = inject('model')

    const items = resolveLegacyListSource(
      props.items, context.attrs, 'items', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )

    const mapItems = (obj, x) => {
      const sItem = obj[x]
      const img = (sItem?.asset?.field === 'img' && {src: sItem?.asset?.src || null, alt: sItem?.asset?.alt, title: sItem?.asset?.title, imgWidth: sItem?.asset?.imageWidth || null, imgHeight: sItem?.asset?.imageHeight || null}) || {}

      if (sItem?.asset?.src && sItem?.asset?.field === 'img') {
        const {metadata} = $utils.getCloudinaryAssetMetadata(sItem?.asset?.src)
        const isSvg = metadata?.format === 'svg'
        Object.assign(img, {
          src: sItem?.asset?.src.startsWith('cloudinary:')
            ? $utils.getCloudinaryAsset(sItem?.asset?.src, true, {}, isSvg)
            : sItem?.asset?.src,
          title: sItem?.asset?.title || metadata?.context?.caption || metadata?.title,
          alt: sItem?.asset?.alt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
          svgContent: ref(null), // placeholder for inline SVG
          ...metadata
        })
      }

      return {
        '@id': sItem['@id'],
        link: $utils.getLinkField(sItem?.link, $regions.current.language.name) || null,
        icon: (sItem?.asset?.field === 'icon' && sItem?.asset?.icon) || null,
        htag: sItem?.htag || 'h6',
        img
      }
    }

    if (props.tabs) {
      navTabs.value = props.tabs['@nodes']
        ? props.tabs['@nodes'].map((x) => {
          return {
            name: x,
            title: tabs[x].title,
            items: tabs[x].items['@nodes'].map(node => mapItems(tabs[x].items, node))
          }
        })
        : []
    }

    if (props.tabs && props.tabs['@nodes']?.length) {
      activeTab.value = props.tabs['@nodes'][0]
    } else {
      navItems.value = items
        ? items['@nodes'].map(node => mapItems(items, node))
        : []
    }

    const handleTabClick = (event, tabName, tabTitle) => {
      activeTab.value = tabName
      $ga4.events.product_navigation_click(model, tabTitle, event)
    }

    const ga4click = (event, ctaText) => {
      $ga4.events.product_navigation_click(model, ctaText, event)
    }

    return {
      navItems,
      navTabs,
      activeTab,
      handleTabClick,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/buttons';

.p-item {
  box-shadow: 0px 2px 4px rgba(12, 24, 30, 0.2);
  color: $black;

  &:hover {
    box-shadow: 0px 8px 16px rgba(12, 24, 30, 0.2);
    text-decoration: none;

    img {
      transform: scale(1.15);
    }
  }

  .product-image-container {
    min-height: 6.25rem;

    &:deep(svg) {
      max-height: 6rem;
      max-width: 8rem;
    }
  }

  .p-icon {
    font-size: 4rem;
  }

  &.bg-gray-lighter {
    box-shadow: none;
  }

  @include media-breakpoint-down(sm) {
    .p-icon {
      font-size: 2rem;
    }

    .product-image-container {
      min-height: auto;
      width: 64px;

      &:deep(svg) {
        max-height: 2rem;
        max-width: 4rem;
      }
    }

    .product-container {
      width: auto !important;

      img {
        width: 4rem !important;
      }
    }
  }
}

$products-navigation-gap: 1rem;

.products-navigation-items {
  margin-left: -$products-navigation-gap * 0.5;
  margin-right: -$products-navigation-gap * 0.5;

  > li {
    padding-left: $products-navigation-gap * 0.5;
    padding-right: $products-navigation-gap * 0.5;
    margin-bottom: $products-navigation-gap;
  }
}

.evolution-tabs {
  column-gap: $spacer * 0.5;
  row-gap: $spacer * 0.5;
  width: 100%;

  @include media-breakpoint-down(sm) {
    overflow-x: scroll;
    -webkit-overflow-scrolling: touch;

    .nav-item {
      flex-shrink: 0;
    }
  }

  @include media-breakpoint-up(md) {
    flex-wrap: wrap;
    overflow-x: visible;
    row-gap: $products-navigation-gap;
  }

  .nav-link {
    white-space: nowrap;

    @include media-breakpoint-down(sm) {
      & {
        font-size: $h5-font-size;
      }
    }
  }
}
</style>
