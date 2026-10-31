<template>
  <LazyHydrate when-visible>
    <aside ref="wrapper" class="anchor-alternative-nav d-print-none" data-test-id="anchor-alternative-nav">
      <div ref="stickyWrapper" data-test-id="anchor-alternative-nav-wrapper" class="w-100">
        <b-navbar toggleable="lg" :type="type" :variant="variant" class="anchor-alternative-nav-bar">
          <div class="container">
            <b-navbar-brand :tag="htag" class="h4 d-lg-inline-block m-auto">{{ title }}</b-navbar-brand>
            <b-navbar-toggle target="nav-collapse" @click="ga4click">
              <template #default="{ expanded }">
                <span class="d-flex align-items-center justify-conent-center m-0 transition-base"
                      style="font-size: 12px"
                      :style="!expanded ? 'transform: rotate(180deg)' : null"
                ><b-icon icon="sort-down" /></span>
              </template>
            </b-navbar-toggle>

            <div class="ml-auto d-block d-lg-none">
              <b-button
                v-if="showCTA === 'true' && ctaLinkMapped"
                :href="ctaLinkMapped.url"
                :target="ctaLinkMapped.target === '_blank_follow' ? '_blank' : ctaLinkMapped.target"
                :rel="ctaLinkMapped.target === '_blank' ? 'noopener nofollow' : null"
                data-test-id="anchor-alternative-nav-cta"
                :variant="ctaVariant"
                class="text-uppercase"
                :class="buttonStyleClass"
                @click="ga4click"
              >
                {{ ctaLinkMapped.label }}
              </b-button>
            </div>

            <b-collapse id="nav-collapse" is-nav>
              <b-navbar-nav class="ml-auto mt-3 mt-lg-0 align-items-center">
                <template v-for="(item, index) in mappedNavigation">
                  <b-nav-item v-if="item.link && item.link.label"
                              :key="item['@id']"
                              :href="item.link.url"
                              :target="item.link.target === '_blank_follow' ? '_blank' : item.link.target"
                              :rel="item.link.target === '_blank' ? 'noopener nofollow' : null"
                              :data-test-id="`anchor-alternative-nav-item-${index}`"
                              class="active font-weight-bold position-relative anchor-alternative-nav-item"
                              @click="ga4click"
                  >
                    {{ item.link.label }}
                  </b-nav-item>
                </template>
                <b-button v-if="showCTA === 'true' && ctaLinkMapped"
                          :href="ctaLinkMapped.url"
                          :target="ctaLinkMapped.target === '_blank_follow' ? '_blank' : ctaLinkMapped.target"
                          :rel="ctaLinkMapped.target === '_blank' ? 'noopener nofollow' : null"
                          data-test-id="anchor-alternative-nav-cta"
                          :variant="ctaVariant"
                          class="text-uppercase d-none d-lg-block ml-2"
                          :class="buttonStyleClass"
                          @click="ga4click"
                >
                  {{ ctaLinkMapped.label }}
                </b-button>
              </b-navbar-nav>
            </b-collapse>
          </div>
        </b-navbar>
      </div>
    </aside>
  </LazyHydrate>
</template>

<script>
import {nextTick, onBeforeUnmount, onMounted, ref, useContext, computed, inject} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor.vue'
import useMounted from '~/helpers/composables/useMounted'
import {
  LIGHT_VARIANT,
  DARK_VARIANT
} from '~/constants/variants/sectionTextVariants'

export const DEFAULT_COMPONENT_HEIGHT_PX = 80

export default {
  props: {
    nodeId: {
      type: String
    },
    navigation: {
      type: Object,
      required: true
    },
    variant: {
      type: String
    },
    ctaVariant: {
      type: String
    },
    ctaLink: {
      type: Object
    },
    showCTA: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String
    },
    buttonStyle: {
      type: String
    },
    notSticky: {
      type: String
    }
  },
  setup(props) {
    const {$utils, $regions, $ga4} = useContext()
    const model = inject('model')
    const wrapper = ref(null)
    const stickyWrapper = ref(null)

    const mappedNavigation = computed(() => props.navigation['@nodes']?.map(el => ({
      ...props.navigation[el],
      link: (props.navigation[el]?.link?.link) ? $utils.getLinkField(props.navigation[el]?.link, $regions.current.language.name) : null
    }) || []))

    const ctaLinkMapped = computed(() => {
      if (props.ctaLink?.link) {
        return $utils.getLinkField(props.ctaLink, $regions.current.language.name)
      } return null
    })

    const buttonStyleClass = computed(() => {
      if (props.buttonStyle === 'edge') { return 'edge-clipped-btn' }
      return ''
    })

    const type = computed(() => props.variant === DARK_VARIANT ? DARK_VARIANT : LIGHT_VARIANT)

    useMounted(props.nodeId)

    const isScrolledIntoView = (el) => {
      const rect = el.getBoundingClientRect()
      const elemBottom = rect.bottom

      const totalHeight = (document.querySelector('header[data-test-id="main-header"] nav.main-nav')?.clientHeight || 1) - 1 +
        document.querySelector('nav[data-test-id="main-header-mobile-region-selector"]')?.clientHeight || 0 +
        document.querySelector('nav.main-nav')?.clientHeight || 0

      return totalHeight >= elemBottom
    }

    const onScroll = () => {
      if (!inEditorEdit) {
        const condition = isScrolledIntoView(wrapper.value)
        const position = `${document.querySelector('header[data-test-id="main-header"] nav.bg-primary')?.clientHeight - 1 + (document.querySelector('nav[data-test-id="main-header-mobile-region-selector"]')?.clientHeight || 0)}px`

        const animateWrapper = () => {
          stickyWrapper.value.classList.add('opacity-0')
          setTimeout(() => {
            stickyWrapper.value.classList.add('transition-fade')
          }, 100)
          setTimeout(() => {
            stickyWrapper.value.classList.remove('opacity-0')
          }, 300)
          setTimeout(() => {
            stickyWrapper.value.classList.remove('transition-fade')
          }, 600)
        }

        if (condition) {
          document.body.classList.add('anchor-alternative-nav-sticky-active')
          stickyWrapper.value.style.top = position
          if (!stickyWrapper.value.classList.contains('position-fixed')) {
            animateWrapper()
          }
          stickyWrapper.value.classList.add('position-fixed')
          stickyWrapper.value.classList.add('anchor-alternative-nav-sticky')
        } else {
          document.body.classList.remove('anchor-alternative-nav-sticky-active')
          stickyWrapper.value.style.top = 0
          stickyWrapper.value.classList.remove('position-fixed')
          stickyWrapper.value.classList.remove('anchor-alternative-nav-sticky')
          stickyWrapper.value.classList.remove('opacity-0')
          stickyWrapper.value.classList.remove('transition-fade')
        }
      }
    }

    onMounted(async () => {
      await nextTick(function () {
        if (props.notSticky === 'false') { window.addEventListener('scroll', onScroll, {passive: true}) }
      })
    })

    onBeforeUnmount(() => {
      if (props.notSticky === 'false') { window.removeEventListener('scroll', onScroll) }
      document.body.classList.remove('anchor-alternative-nav-sticky-active')
    })

    const ga4click = (event) => {
      const isProduct = ['dxp:product', 'dxp:productGroup'].includes(model?.['@nodeType'])
      $ga4.events.anchor_link_navigation_click(event, isProduct ? model : undefined, props.title)
    }

    return {
      mappedNavigation,
      ctaLinkMapped,
      wrapper,
      stickyWrapper,
      type,
      buttonStyleClass,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.anchor-alternative-nav {
  min-height: 4.25rem;

  &-sticky {
    z-index: $zindex-modal-backdrop + 1;
  }

  @include media-breakpoint-down(md) {
    min-height: 3.125rem;
  }

  &-bar {
    padding-top: 0.875rem;
    padding-bottom: 0.875rem;
  }

  :deep(.navbar-toggler) {
    border-color: transparent !important;
    color: $black;
  }

  :deep(.navbar-dark .navbar-toggler) {
    color: $white;
  }

  @include media-breakpoint-down(md) {
    &-item {
      width: calc(100% + 2rem);
      padding: 0 1rem;
      transition: $transition-base;
    }

    :deep(.navbar-light.bg-light .anchor-alternative-nav-item:hover) {
      background: $white;
    }

    :deep(.navbar-light.bg-white .anchor-alternative-nav-item:hover) {
      background: $light;
    }

    :deep(.navbar-dark .anchor-alternative-nav-item:hover) {
      background: $white;
    }

    :deep(.navbar-dark .anchor-alternative-nav-item:hover .nav-link) {
      color: $dark !important;
    }
  }

  @include media-breakpoint-up(lg) {
    &-item {
      &:after {
        content: "";
        position: absolute;
        background: transparent;
        width: 100%;
        height: 4px;
        bottom: -20px;
        left: 0;
        transition: $transition-base;
      }

      &:hover::after {
        background: $black;
      }
    }

    :deep(.navbar-dark .anchor-alternative-nav-item:hover::after) {
      background: $white;
    }
  }
}
</style>
