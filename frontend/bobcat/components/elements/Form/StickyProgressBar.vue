<template>
  <div ref="wrapper">
    <div ref="stickyWrapper" data-test-id="progress-bar-sticky" :class="isSticky ? 'opacity-5' : 'opacity-0'" class="bg-white w-100 py-4 sticky-bar position-fixed transition-base">
      <div class="container">
        <div class="row">
          <div class="col-12">
            <BProgress height="1.25rem" :max="max">
              <BProgressBar :value="currentValue" />
            </BProgress>
            <h5 class="mt-3 text-center">{{ counter }}</h5>
          </div>
        </div>
      </div>
    </div>
    <div data-test-id="progress-bar-static" :class="isSticky ? 'opacity-0' : 'opacity-5'" class="bg-white w-100 py-4 sticky-bar transition-base">
      <div class="container">
        <div class="row">
          <div class="col-12">
            <BProgress height="1.25rem" :max="max">
              <BProgressBar :value="currentValue" />
            </BProgress>
            <h5 class="mt-3 text-center">{{ counter }}</h5>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {onMounted, onBeforeUnmount, ref, nextTick, useContext, watch} from '@nuxtjs/composition-api'
import {BProgress, BProgressBar} from 'bootstrap-vue'
import {inEditorEdit} from '~/helpers/vue-editor.vue'

export default {
  components: {BProgress, BProgressBar},
  props: {
    fieldsAmount: {
      type: Number,
      default: 0
    },
    filledFieldsAmount: {
      type: Number,
      default: 0
    },
    progressLabel: {
      type: String,
      default: 'completed'
    }
  },
  setup(props) {
    const {fieldsAmount, filledFieldsAmount, progressLabel} = props
    const max = ref(parseInt(fieldsAmount))
    const {$utils} = useContext()
    const currentValue = ref(parseInt(filledFieldsAmount))
    const counter = ref(`0% ${progressLabel}`)
    const stickyWrapper = ref(null)
    const isSticky = ref(false)
    const wrapper = ref(null)

    if (max.value > 0 && currentValue.value > 0) {
      counter.value = `${((currentValue.value / max.value) * 100).toFixed()}% ${progressLabel}`
    }

    watch(() => props.fieldsAmount, (newVal) => {
      max.value = parseInt(newVal)
      counter.value = `${((currentValue.value / newVal) * 100).toFixed()}% ${progressLabel}`
    })

    watch(() => props.filledFieldsAmount, (newVal) => {
      currentValue.value = parseInt(newVal)
      if (newVal > 0) {
        counter.value = `${((currentValue.value / max.value) * 100).toFixed()}% ${progressLabel}`
      } else {
        counter.value = `0% ${progressLabel}`
      }
    })

    if (process.client) {
      const isScrolledIntoView = (el) => {
        const rect = el.getBoundingClientRect()
        const top = rect.top

        const totalHeight = document.querySelector('header[data-test-id="main-header"]')?.clientHeight

        return totalHeight >= top
      }

      const setStickyWrapperPosition = () => {
        const notificationBar = document.querySelector('div[data-test-id="notification-bar-content"]')
        if (!$utils.isSmallMobile() && notificationBar) {
          stickyWrapper.value.style.top = `${document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1 + notificationBar?.clientHeight}px`
        } else { stickyWrapper.value.style.top = `${document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1}px` }
      }

      const onScroll = () => {
        if (!inEditorEdit) {
          const condition = isScrolledIntoView(wrapper.value)
          if (condition) {
            stickyWrapper.value.classList.remove('z-index-n')
            stickyWrapper.value.classList.add('zindex-modal-backdrop')
            setStickyWrapperPosition()
          } else {
            stickyWrapper.value.classList.remove('zindex-modal-backdrop')
            stickyWrapper.value.classList.add('z-index-n')
            setStickyWrapperPosition()
          }
          isSticky.value = condition
        }
      }

      onMounted(async () => {
        await nextTick(function () {
          window.addEventListener('scroll', onScroll, {passive: true})
          document.getElementById('notificationBarCloseBtn')?.addEventListener('click', setStickyWrapperPosition)
        })
      })

      onBeforeUnmount(() => {
        window.removeEventListener('scroll', onScroll)
      })
    }

    return {
      currentValue,
      max,
      counter,
      isSticky,
      wrapper,
      stickyWrapper
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~bootstrap/scss/progress';

.sticky-bar {
  box-shadow: 0px 2px 4px rgba(12, 24, 30, 0.2);
}

.zindex-modal-backdrop {
  z-index: $zindex-fixed;
}
</style>
