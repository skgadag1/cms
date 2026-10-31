<template>
  <aside class="mid-buying d-print-none" data-test-id="mid-buying-tools">
    <div ref="wrapper"
         data-test-id="mid-buying-tools-static-wrapper"
         class="bg-black mid-buying-static-wrapper d-flex align-items-center"
    >
      <div class="container">
        <div class="row no-gutters position-relative">
          <ul
            class="col-12 d-flex flex-column flex-md-wrap flex-nowrap align-items-start align-items-md-center justify-content-center mb-0 flex-md-row py-3 py-md-0"
          >
            <li v-for="(tool, index) in staticMidBuyingTools"
                :key="tool['@id']"
                class="d-flex px-4 py-3 py-xl-2"
            >
              <b-icon :data-test-id="`mid-buying-tools-icon-${index}`" class="text-primary mr-2" :icon="tool.icon" />
              <a
                v-if="tool.link && tool.link.label"
                :href="tool.link.url"
                :target="tool.link.target === '_blank_follow' ? '_blank' : tool.link.target"
                :rel="tool.link.target === '_blank' ? 'noopener nofollow' : null"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold"
                @click="ga4click"
              >
                {{ tool.link.label }}
              </a>
              <a
                v-else-if="tool.brochure && tool.brochure.filePath"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold link"
                :href="getBrochureUrl(tool.brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(tool.brochure.filePath)}"
              >
                {{ tool.brochure.label || 'Download brochure' }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div ref="stickyWrapper"
         data-test-id="mid-buying-tools-sticky-wrapper"
         :class="isSticky ? 'opacity-5' : 'opacity-0'"
         class="d-flex transition-fade bg-black mid-buying-sticky-wrapper align-items-center py-0 py-md-1 position-fixed z-index-n"
    >
      <div class="container">
        <div class="row no-gutters position-relative flex-nowrap align-items-center justify-content-between">
          <ul
            class="col-12 d-md-flex d-none flex-column flex-md-wrap flex-nowrap align-items-start align-items-md-center justify-content-center mb-0 flex-md-row"
          >
            <li v-for="(tool, index) in midBuyingTools"
                :key="tool['@id']"
                class="d-flex px-3 py-2"
            >
              <b-icon :data-test-id="`mid-buying-tools-icon-${index}`" class="text-primary mr-2" :icon="tool.icon" />
              <a
                v-if="tool.link && tool.link.label"
                :href="tool.link.url"
                :target="tool.link.target === '_blank_follow' ? '_blank' : tool.link.target"
                :rel="tool.link.target === '_blank' ? 'noopener nofollow' : null"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold"
                @click="ga4click"
              >
                {{ tool.link.label }}
              </a>
              <a
                v-else-if="tool.brochure && tool.brochure.filePath"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold link"
                :href="getBrochureUrl(tool.brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(tool.brochure.filePath)}"
              >
                {{ tool.brochure.label || 'Download brochure' }}
              </a>
            </li>
            <li
              v-if="midBuyingToolsInDropdown && midBuyingToolsInDropdown.length"
              class="d-lg-none d-md-flex d-none p-2 justify-content-center align-items-center"
            >
              <button
                role="button"
                class="d-flex justify-content-center align-items-center bg-transparent border-0 font-weight-bold text-primary"
                @click.prevent="isDropdownHidden = !isDropdownHidden"
              >
                <span class="d-flex align-items-center justify-conent-center m-0 transition-base"
                      style="font-size: 6px"
                      :style="!isDropdownHidden ? 'transform: rotate(180deg)' : null"
                ><b-icon icon="sort-down" /></span>
              </button>
            </li>
          </ul>
          <div v-if="!isDropdownHidden"
               class="vh-100 vw-100 fixed-top z-index-n1"
               @click.prevent="isDropdownHidden = true"
          />
          <ul v-if="!isDropdownHidden"
              class="dropdown shadow bg-black d-md-flex d-none flex-column m-0 p-0 position-absolute"
              data-test-id="mid-buying-tools-dropdown"
          >
            <li v-for="(tool, index) in midBuyingToolsInDropdown"
                :key="tool['@id']"
                class="d-flex px-3 py-2 w-100"
            >
              <b-icon :data-test-id="`mid-buying-tools-icon-${index}`" class="text-primary mr-2" :icon="tool.icon" />
              <a
                v-if="tool.link && tool.link.label"
                :href="tool.link.url"
                :target="tool.link.target === '_blank_follow' ? '_blank' : tool.link.target"
                :rel="tool.link.target === '_blank' ? 'noopener nofollow' : null"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold text-uppercase py-1"
                @click="ga4click"
              >
                {{ tool.link.label }}
              </a>
              <a
                v-else-if="tool.brochure && tool.brochure.filePath"
                :data-test-id="`mid-buying-tools-link-${index}`"
                class="font-weight-bold text-uppercase py-1 link"
                :href="getBrochureUrl(tool.brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(tool.brochure.filePath)}"
              >
                {{ tool.brochure.label || 'Download brochure' }}
              </a>
            </li>
          </ul>
          <ul class="mobile-list col-12 d-md-none d-flex align-items-center justify-content-center py-1 px-0 mb-0">
            <li
              v-if="midBuyingTools[0]"
              class="d-flex px-2 py-2 w-100 justify-content-center align-items-center"
            >
              <b-icon data-test-id="mid-buying-tools-mobile-0-icon"
                      class="text-primary mr-2"
                      :icon="midBuyingTools[0].icon"
              />
              <a
                v-if="midBuyingTools[0].link && midBuyingTools[0].link.label"
                :href="midBuyingTools[0].link.url"
                :target="midBuyingTools[0].link.target === '_blank_follow' ? '_blank' : midBuyingTools[0].link.target"
                :rel="midBuyingTools[0].link.target === '_blank' ? 'noopener nofollow' : null"
                data-test-id="mid-buying-tools-link-mobile-0"
                class="font-weight-bold text-uppercase small"
                @click="ga4click"
              >
                {{ midBuyingTools[0].link.label }}
              </a>
              <a
                v-else-if="midBuyingTools[0].brochure && midBuyingTools[0].brochure.filePath"
                data-test-id="mid-buying-tools-link-mobile-0"
                class="font-weight-bold text-uppercase small link"
                :href="getBrochureUrl(midBuyingTools[0].brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(midBuyingTools[0].brochure.filePath)}"
              >
                {{ midBuyingTools[0].brochure.label || 'Download brochure' }}
              </a>
            </li>
            <li
              v-if="midBuyingTools[1]"
              class="d-flex px-2 py-2 w-100 justify-content-center align-items-center"
            >
              <b-icon data-test-id="mid-buying-tools-mobile-1-icon"
                      class="text-primary mr-2"
                      :icon="midBuyingTools[1].icon"
              />
              <a
                v-if="midBuyingTools[1].link && midBuyingTools[1].link.label"
                :href="midBuyingTools[1].link.url"
                :target="midBuyingTools[1].link.target === '_blank_follow' ? '_blank' : midBuyingTools[1].link.target"
                :rel="midBuyingTools[1].link.target === '_blank' ? 'noopener nofollow' : null"
                data-test-id="mid-buying-tools-link-mobile-1"
                class="font-weight-bold text-uppercase small"
                @click="ga4click"
              >
                {{ midBuyingTools[1].link.label }}
              </a>
              <a
                v-else-if="midBuyingTools[1].brochure && midBuyingTools[1].brochure.filePath"
                data-test-id="mid-buying-tools-link-mobile-0"
                class="font-weight-bold text-uppercase small link"
                :href="getBrochureUrl(midBuyingTools[1].brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(midBuyingTools[1].brochure.filePath)}"
              >
                {{ midBuyingTools[1].brochure.label || 'Download brochure' }}
              </a>
            </li>
            <li
              v-if="midBuyingToolsInDropdown.length"
              class="d-flex px-3 py-2 justify-content-center align-items-center"
            >
              <button
                role="button"
                class="d-flex justify-content-center align-items-center bg-transparent border-0 font-weight-bold text-primary"
                @click.prevent="isDropdownHidden = !isDropdownHidden"
              >
                <span class="d-flex align-items-center justify-conent-center m-0 transition-base"
                      style="font-size: 6px"
                      :style="!isDropdownHidden ? 'transform: rotate(180deg)' : null"
                ><b-icon icon="sort-down" /></span>
              </button>
            </li>
          </ul>
          <ul v-if="!isDropdownHidden"
              class="dropdown shadow bg-black d-flex d-md-none flex-column m-0 p-0 position-absolute"
              data-test-id="mid-buying-tools-dropdown-mobile"
          >
            <li v-for="(tool, index) in midBuyingToolsInDropdown"
                :key="tool['@id']"
                class="d-flex px-3 py-2 w-100"
            >
              <b-icon :data-test-id="`mid-buying-tools-mobile-icon-${index}`"
                      class="text-primary mr-2"
                      :icon="tool.icon"
              />
              <a
                v-if="tool.link && tool.link.label"
                :href="tool.link.url"
                :target="tool.link.target === '_blank_follow' ? '_blank' : tool.link.target"
                :rel="tool.link.target === '_blank' ? 'noopener nofollow' : null"
                :data-test-id="`mid-buying-tools-mobile-link-${index}`"
                class="font-weight-bold text-uppercase py-1"
                @click="ga4click"
              >
                {{ tool.link.label }}
              </a>
              <a
                v-else-if="tool.brochure && tool.brochure.filePath"
                :data-test-id="`mid-buying-tools-mobile-link-${index}`"
                class="font-weight-bold text-uppercase py-1 link"
                :href="getBrochureUrl(tool.brochure.filePath)"
                @click.prevent="(e) => {ga4click(e); download(tool.brochure.filePath)}"
              >
                {{ tool.brochure.label || 'Download brochure' }}
              </a>
            </li>
          </ul>
          <div v-if="!isDropdownHidden" class="overlay d-md-none d-flex" @click.prevent="isDropdownHidden = true" />
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
import {inject, nextTick, onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor.vue'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    tools: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const {$utils, $regions, $ga4} = useContext()
    const model = inject('model')
    const mappedTools = props.tools['@nodes']?.map(el => ({
      ...props.tools[el],
      link: (props.tools[el]?.link?.link && !props.tools[el]?.link?.brochure) ? $utils.getLinkField(props.tools[el]?.link, $regions.current.language.name) : null,
      brochure: props.tools[el]?.link?.brochure?.field === 'none' ? null : props.tools[el]?.link?.brochure
    }) || [])
    const midBuyingTools = ref(mappedTools)
    const staticMidBuyingTools = ref(mappedTools)
    const isDropdownHidden = ref(true)
    const isSticky = ref(false)
    const midBuyingToolsInDropdown = ref([])
    const wrapper = ref(null)
    const stickyWrapper = ref(null)
    useMounted(props.nodeId)

    if (process.client) {
      const onResize = (sticky = false) => {
        if (($utils.isTablet() || $utils.isMobile()) && sticky) {
          const numberOfVisibleItems = $utils.isMobile() ? 2 : 4
          midBuyingToolsInDropdown.value = staticMidBuyingTools.value.slice(numberOfVisibleItems)
          midBuyingTools.value = staticMidBuyingTools.value.slice(0, numberOfVisibleItems)
        } else if (sticky) {
          midBuyingTools.value = staticMidBuyingTools.value
        }
      }

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
          onResize(condition)
          const position = `${document.querySelector('header[data-test-id="main-header"] nav.bg-primary')?.clientHeight - 1 + (document.querySelector('nav[data-test-id="main-header-mobile-region-selector"]')?.clientHeight || 0)}px`
          stickyWrapper.value.style.top = position

          if (condition) {
            stickyWrapper.value.classList.remove('z-index-n')
            stickyWrapper.value.classList.add('zindex-modal-backdrop')
          } else {
            stickyWrapper.value.classList.remove('zindex-modal-backdrop')
            stickyWrapper.value.classList.add('z-index-n')
          }

          setTimeout(() => {
            isSticky.value = condition
          }, 200)
        }
      }

      onMounted(async () => {
        await nextTick(function () {
          window.addEventListener('resize', onResize, {passive: true})
          window.addEventListener('scroll', onScroll, {passive: true})
          onResize()
        })
      })

      onBeforeUnmount(() => {
        window.removeEventListener('resize', onResize)
        window.removeEventListener('scroll', onScroll)
      })
    }

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const getBrochureUrl = (url) => {
      return $utils.getCloudinaryDocument(url)
    }

    // gtm ga4 handler
    const ga4click = (event) => {
      const isProduct = ['dxp:product', 'dxp:productGroup'].includes(model?.['@nodeType'])
      $ga4.events.mid_page_cta(event, isProduct ? model : undefined)
    }

    return {
      ga4click,
      midBuyingTools,
      staticMidBuyingTools,
      midBuyingToolsInDropdown,
      isDropdownHidden,
      isSticky,
      wrapper,
      stickyWrapper,
      download,
      getBrochureUrl
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.mid-buying {
  &-static-wrapper {
    min-height: 11.5rem;

    @include media-breakpoint-up(lg) {
      min-height: 9rem;
    }
  }

  &-sticky-wrapper {
    @include position-top;

    @include media-breakpoint-up(sm) {
      min-height: calc(4rem + 2px);
    }
  }

  .zindex-modal-backdrop {
    z-index: $zindex-fixed;

    @include media-breakpoint-up(lg) {
      z-index: $zindex-modal-backdrop;
    }
  }

  a, span {
    color: $gray-100;
  }

  .mobile-list {
    min-height: calc(3rem + 1px);

    li {
      border-right: 1px solid $gray-800;

      &:last-child {
        border-right: none;
      }
    }
  }

  .dropdown {
    top: 4rem;
    right: .5rem;
    min-width: 18rem;

    @include media-breakpoint-down(sm) {
      right: 50%;
      transform: translateX(50%);
      min-width: unset;
      width: 96%;
    }

    &:before {
      content: '';
      position: absolute;
      top: -8px;
      right: .5rem;
      border-left: .5rem solid transparent;
      border-right: .5rem solid transparent;
      border-bottom: .5rem solid $black;
    }

    li {
      border-bottom: 1px solid $gray-800;
    }
  }
}
</style>
