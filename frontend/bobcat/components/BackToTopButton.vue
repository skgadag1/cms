<template>
  <div>
    <div class="d-none">&nbsp;</div><!-- hack for component not empty JAR-376 -->
    <aside v-if="isComponentVisible && isSectionVisible"
           data-test-id="back-to-top-button-wrapper"
           :style="`bottom: ${currentYPosition}px;`"
           :class="[isButtonVisible ? 'opacity-5' : 'opacity-0', type.field === 'full' ? 'w-100' : '']"
           class="d-flex transition-base d-sm-none position-fixed align-items-center justify-content-end z-index-5"
    >
      <button
        v-if="type.field === 'full' && label"
        class="d-flex justify-content-center w-100 btn btn-outline-primary btn-secondary align-items-center shadow"
        data-test-id="full-width-button"
        @click.prevent="scrollTo"
      >
        {{ label }}
        <b-icon v-if="icon" :icon="icon" class="ml-2" />
      </button>
      <b-button v-else-if="type.field === 'small' && label"
                size="lg"
                class="small-btn shadow mr-3 mb-3 font-weight-bold"
                pill
                variant="primary"
                data-test-id="small-button"
                @click.prevent="scrollTo"
      >
        {{ label }}
      </b-button>
    </aside>
  </div>
</template>

<script>
import {onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  props: {
    icon: {
      type: String
    },
    type: {
      type: Object,
      required: true
    },
    label: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const {$utils} = useContext()
    const isButtonVisible = ref(false)
    const isSectionVisible = ref(false)
    const isComponentVisible = ref(false)
    const currentYPosition = ref(0)
    const adjustedYPosition = ref(0)

    const scrollTo = () => {
      if (process.client && !inEditorEdit) {
        window.scrollTo({top: 0, behavior: 'smooth'})
      }
    }

    if (process.client) {
      const getStickyCTAElement = () => document.querySelector('[data-test-id="sticky-cta"]')
      const getNotificationBarElement = () => document.getElementById('notificationBar')

      const updateStickyCTABottomPosition = () => {
        const stickyCTA = getStickyCTAElement()
        if (stickyCTA) {
          stickyCTA.style.bottom = `${adjustedYPosition.value}px`
        }
      }

      const calculateYPositionForSmallType = (stickyCTA, notificationBar) => {
        let position = 0

        if (notificationBar) {
          position += notificationBar.clientHeight || 0
        }

        if (stickyCTA && isSectionVisible.value) {
          position += stickyCTA.clientHeight
        }

        return position
      }

      const updateNotificationBarPosition = (notificationBar) => {
        if (notificationBar && $utils.isSmallMobile()) {
          notificationBar.style.bottom = isSectionVisible.value ? '40px' : '0px'
        }
      }

      const checkStickyComponentsVisibility = () => {
        const stickyCTA = getStickyCTAElement()
        const notificationBar = getNotificationBarElement()
        let yPos = 0

        if (props.type.field === 'small') {
          currentYPosition.value = calculateYPositionForSmallType(stickyCTA, notificationBar)
        } else if ($utils.isSmallMobile()) {
          updateNotificationBarPosition(notificationBar)
          if (stickyCTA) {
            yPos = isSectionVisible.value ? 40 : 0
          }
        }

        adjustedYPosition.value = yPos
        updateStickyCTABottomPosition()
      }

      const checkScroll = () => {
        const shouldBeVisible = window.scrollY > window.innerHeight * 2
        if (shouldBeVisible) {
          isSectionVisible.value = shouldBeVisible
          checkStickyComponentsVisibility()
          setTimeout(() => {
            isButtonVisible.value = shouldBeVisible
          }, 200)
        } else {
          isButtonVisible.value = shouldBeVisible
          setTimeout(() => {
            isSectionVisible.value = shouldBeVisible
            checkStickyComponentsVisibility()
          }, 200)
        }
      }

      const checkComponentVisibility = () => {
        const height = document.body.scrollHeight
        isComponentVisible.value = height >= window.innerHeight * 4
      }

      onMounted(() => {
        const notificationBarCloseBtn = document.getElementById('notificationBarCloseBtn')
        if (notificationBarCloseBtn) {
          notificationBarCloseBtn.addEventListener('click', () => {
            currentYPosition.value = 0
            checkStickyComponentsVisibility()
          })
        }
        checkScroll()
        checkStickyComponentsVisibility()
        setTimeout(checkComponentVisibility, 200)

        window.addEventListener('scroll', checkScroll, {passive: true})
        window.addEventListener('resize', checkStickyComponentsVisibility, {passive: true})
        window.addEventListener('resize', checkComponentVisibility, {passive: true})
      })

      onBeforeUnmount(() => {
        window.removeEventListener('scroll', checkScroll)
        window.removeEventListener('resize', checkStickyComponentsVisibility)
        window.removeEventListener('resize', checkComponentVisibility)
      })
    }

    return {
      scrollTo,
      isButtonVisible,
      isSectionVisible,
      isComponentVisible,
      currentYPosition,
      adjustedYPosition
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

aside {
  bottom: 0;
  right: 0;

  .small-btn {
    font-size: 0.875rem;
    padding: 0.75rem !important;
  }
}
</style>
