<template>
  <section data-test-id="sticky-cta"
           :class="[{ 'opacity-5': isVisible, 'opacity-0': !isVisible }, {'py-lg-2' : !imageSrc}, {'position-fixed' : !inEditorEdit}, {'py-0 py-sm-3 py-lg-1': displayStickyCta}]"
           class="d-flex justify-content-between align-items-center w-100 bg-white z-index-4 sticky-cta transition-base opacity-0"
           :data-is-visible="isVisible"
  >
    <div
      v-if="displayStickyCta && !inEditorEdit"
      class="d-flex w-100"
    >
      <div class="d-none d-md-block container">
        <div class="d-flex justify-content-between align-items-center">
          <div v-if="model" class="d-none d-lg-flex align-items-center">
            <ImageSet
              v-if="imageSrc"
              :options="{ width: 76 }"
              class="mr-1"
              :title="model.name || model.title"
              :src="imageSrc"
            />
            <p v-if="model.name || model.title" class="h4 mb-0 text-nowrap">{{ model.name || model.title }}</p>
          </div>
          <StickyCTAButtons
            :button-actions="buttonActions"
            :primary-button="primaryButton"
            @handle-button-click="handleButtonClick"
          />
        </div>
      </div>
      <button
        class="d-none d-md-flex align-items-center justify-content-center bg-transparent border-0 m-0 px-3 px-lg-6"
        @click.prevent="displayStickyCta = false"
      >
        <b-icon icon="close" />
      </button>
      <StickyCTAMobileButtons
        :button-actions="buttonActions"
        :primary-button="primaryButton"
        @handle-button-click="handleButtonClick"
      />
    </div>
    <WarningAlert v-else-if="inEditorEdit" class="d-flex justify-content-center w-100">
      Component won't be visible in the editor edit - only on the public environment.
    </WarningAlert>
  </section>
</template>

<script>
import {inject, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import useStickyButtons from '~/helpers/composables/useStickyButtons'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  setup() {
    const model = inject('model')
    const {initButtons} = useStickyButtons()
    const displayStickyCta = ref(true)
    const isVisible = ref(false)
    const imageSrc = ref(model?.image?.src || model?.images?.images0?.asset?.img?.src)
    const buttonActions = ref([])
    const primaryButton = ref(null)
    const {$utils} = useContext()

    const triggerButtonClick = (selector) => {
      const btnElement = typeof selector === 'string' ? document.querySelector(selector) : selector
      if (btnElement) {
        btnElement.click()
      }
    }

    const handleButtonClick = (item) => {
      if (item?.selector) {
        if (item.selector?.toString()?.includes('info-sheet-download-btn')) {
          item.isLoading = true
          setTimeout(() => {
            item.isLoading = false
          }, 9000)
        } else if (item.selector?.toString()?.includes('share-btn')) {
          const originalLabel = item.label
          item.label = 'URL copied'
          setTimeout(() => {
            item.label = originalLabel
          }, 5000)
        }
        triggerButtonClick(item.selector)
      }
    }

    const handleScroll = (entries) => {
      entries.forEach((entry) => {
        isVisible.value = !entry.isIntersecting && entry.boundingClientRect.top < 0
      })
    }

    if (process.client && window) {
      onMounted(() => {
        const isDealerInventoryPage = document.querySelector('section[data-test-id="dealer-inventory-model-pricing"]') !== null
        initButtons(isDealerInventoryPage, buttonActions, primaryButton)

        const setupIntersectionObserver = (pricingSection) => {
          if (pricingSection) {
            let observer = new IntersectionObserver(handleScroll, {
              root: null,
              threshold: 0,
              rootMargin: '-120px 0px 0px 0px'
            })

            observer.observe(pricingSection)

            const updateObserverMargin = () => {
              let rootMargin
              if ($utils.isDesktopXL()) {
                rootMargin = '-120px'
              } else if ($utils.isTablet()) {
                rootMargin = '-56px'
              } else {
                rootMargin = '-104px'
              }

              observer.disconnect()
              observer = new IntersectionObserver(handleScroll, {
                root: null,
                threshold: 0,
                rootMargin: `${rootMargin} 0px 0px 0px`
              })
              observer.observe(pricingSection)
            }

            window.addEventListener('resize', updateObserverMargin)
          }
        }

        const checkForModelPricingSection = () => {
          const pricingSection = document.querySelector(
            isDealerInventoryPage
              ? 'section[data-test-id="dealer-inventory-model-pricing"]'
              : 'section[data-test-id="model-pricing"]'
          )

          if (pricingSection) {
            setupIntersectionObserver(pricingSection)
          }
        }

        // Set up a MutationObserver to detect when the model pricing section is added
        const mutationObserver = new MutationObserver(() => {
          checkForModelPricingSection()
        })

        mutationObserver.observe(document.body, {
          childList: true,
          subtree: true
        })

        checkForModelPricingSection()
      })
    }

    return {
      isVisible,
      model,
      imageSrc,
      buttonActions,
      primaryButton,
      triggerButtonClick,
      handleButtonClick,
      displayStickyCta,
      inEditorEdit
    }
  }
}
</script>

<style lang="scss">
.sticky-cta {
  bottom: 0;
  right: 0;
  box-shadow: 0px -8px 8px 0px rgba(0, 0, 0, 0.2);
}
</style>
