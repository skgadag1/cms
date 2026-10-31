<template>
  <aside id="notificationBar"
         class="notification-bar-container container-fluid p-0 d-flex d-print-none flex-column align-items-center transition-base"
         :class="{'fixed-top' : !inEditorEdit}"
         :style="`top: calc(${headerHeight}px - 1px);`"
  >
    <div v-if="!isHidden && paragraph && paragraph.length"
         id="notification-bar-content"
         class="container-fluid py-4 d-flex d-print-none flex-column justify-content-center align-items-center bg-dark shadow transition-base"
         data-test-id="notification-bar-content"
         data-noindex
    >
      <div class="container p-0 position-relative">
        <div class="w-100 row mx-0 justify-content-center">
          <div data-test-id="notification-bar-content-container" class="w-100 d-flex justify-content-center align-items-center flex-column text-white">
            <div class="d-flex justify-content-center align-items-start mb-3">
              <span data-test-id="notification-bar-icon" class="info-wrapper mr-2 mt-1 h5"><b-icon :icon="icon" /></span>
              <div class="paragraph-container mr-4">
                <p data-test-id="notification-bar-paragraph" class="m-0 text-center">{{ paragraph }}</p>
              </div>
              <button id="notificationBarCloseBtn"
                      data-test-id="notification-bar-close-btn"
                      class="close-btn d-flex justify-content-center align-items-center p-2 ml-3 bg-transparent text-white border-0 position-absolute"
                      style="font-size: 0.75rem"
                      type="button"
                      @click.prevent="handleClose"
              >
                <b-icon icon="close" />
              </button>
            </div>
            <a v-if="linkField && linkField.label"
               :href="linkField.url"
               :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
               :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
               class="text-primary text-center font-weight-bold"
               data-test-id="notification-bar-btn"
            >
              {{ linkField.label }}
            </a>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!(paragraph && paragraph.length)" id="notification-bar-placeholder" />
  </aside>
</template>

<script>
import {ref, onMounted, onBeforeUnmount, useContext, nextTick} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: ['paragraph', 'link', 'icon', 'nodeId'],
  setup(props) {
    const {$utils, $regions} = useContext()
    const linkField = ref($utils.getLinkField(props.link, $regions.current.language.name))
    const headerHeight = ref(121)
    const isHidden = ref(false)
    useMounted(props.nodeId)

    const setHeaderClientHeight = () => {
      if (process.client && !inEditorEdit) {
        const header = document.querySelector('header')
        const midBuyingTools = document.querySelector('div[data-test-id="mid-buying-tools-sticky-wrapper"].opacity-5')
        if (midBuyingTools) {
          if (!$utils.isMobile() && $utils.isTablet()) {
            headerHeight.value = header?.clientHeight + midBuyingTools.clientHeight - 1
          } else if ($utils.isMobile()) {
            headerHeight.value = header?.clientHeight + midBuyingTools.clientHeight - 1 - 48
          } else {
            headerHeight.value = header?.clientHeight + midBuyingTools.clientHeight - 1 - 64
          }
        } else {
          headerHeight.value = header?.clientHeight
        }
      }
    }

    const handleClose = () => {
      isHidden.value = true
      if (process.client && !inEditorEdit) { window.removeEventListener('resize', setHeaderClientHeight) }
    }

    if (process.client && !inEditorEdit) {
      const header = document.querySelector('header')
      if (header) {
        onMounted(() => {
          nextTick(function () {
            setHeaderClientHeight()
          })
        })
        window.addEventListener('resize', setHeaderClientHeight, {passive: true})
        window.addEventListener('scroll', setHeaderClientHeight, {passive: true})
      } else {
        headerHeight.value = 0
      }

      onBeforeUnmount(() => {
        window.removeEventListener('resize', setHeaderClientHeight)
        window.removeEventListener('scroll', setHeaderClientHeight)
      })
    }

    return {
      linkField,
      inEditorEdit,
      headerHeight,
      isHidden,
      handleClose,
      setHeaderClientHeight
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.notification-bar-container {
  top: 121px;
  left: 0;

  @include media-breakpoint-down(md) {
     top: 57px;
  }

  @include media-breakpoint-down(xs) {
    top: unset !important;
    bottom: 0;
  }

  .info-wrapper {
    margin-top: 2px;
  }

  .close-btn {
    right: 0;
    top: 0;
  }

  .paragraph-container {
    max-width: 45rem;
  }
}
</style>
