<template>
  <div
    class="d-print-none d-flex align-items-end"
  >
    <div
      v-if="displayStickyCta"
      class="sticky-cta position-fixed d-flex d-md-none w-100 justify-content-center align-items-center z-index-5 transition-base"
      :style="{'bottom': `${stickyCtaButtonPosition}px`}"
    >
      <button
        role="button"
        class="d-flex justify-content-center align-items-center bg-white border-0 font-weight-bold text-primary w-100 py-3"
        data-test-id="specification-expand-button-mobile"
        @click.prevent="expandAllToggle"
      >
        {{ toggleLabel }}
        <span class="d-flex align-items-center justify-conent-center m-0 ml-2 transition-base" style="font-size: 6px" :style="isExpandedAll ? 'transform: rotate(180deg)' : null">
          <b-icon icon="sort-down" />
        </span>
      </button>
    </div>

    <div class="d-none d-md-flex align-items-end">
      <button
        role="button"
        class="d-flex justify-content-center align-items-center pr-3 bg-transparent border-0 font-weight-bold text-primary"
        data-test-id="specification-expand-button"
        @click.prevent="expandAllToggle"
      >
        {{ toggleLabel }}
        <span class="d-flex align-items-center justify-conent-center m-0 ml-2 transition-base" style="font-size: 6px" :style="isExpandedAll ? 'transform: rotate(180deg)' : null">
          <b-icon icon="sort-down" />
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, useContext, onMounted, onBeforeUnmount} from '@nuxtjs/composition-api'

const props = defineProps({
  isExpandedAll: Boolean,
  labels: Object
})
const emit = defineEmits(['expandAll'])

const {$utils} = useContext()
const displayStickyCta = ref(false)
const stickyCtaButtonPosition = ref(0)

const toggleLabel = computed(() => props.isExpandedAll ? props.labels.collapesAll || 'Collapse all' : props.labels.expandAll || 'Expand all')

const checkVisibility = (entries) => {
  entries.forEach((entry) => {
    displayStickyCta.value = entry.isIntersecting
  })
}

const updateCtaPosition = () => {
  if (!$utils.isMobile()) { return }

  let addedHeight = 0
  const stickyCta = document.querySelector('section[data-test-id="sticky-cta"]')
  const backToTopWrapper = document.querySelector('[data-test-id="back-to-top-button-wrapper"]')

  if (stickyCta && stickyCta.getAttribute('data-is-visible') === 'true') {
    addedHeight += stickyCta.offsetHeight
  }
  if (backToTopWrapper) {
    addedHeight += backToTopWrapper.offsetHeight
  }
  stickyCtaButtonPosition.value = addedHeight
}

const expandAllToggle = () => {
  emit('expandAll', !props.isExpandedAll)
}

if (process.client && window) {
  onMounted(() => {
    const specificationSection = document.querySelector('[data-test-id="product-specifications"]')
    const attachmentSection = document.querySelector('[data-test-id="attachment-specifications"]')

    if (specificationSection || attachmentSection) {
      const observer = new IntersectionObserver(checkVisibility, {
        root: props.tablesRef,
        threshold: 0.1,
        rootMargin: '0px 0px 0px 0px'
      })

      if (specificationSection) {
        observer.observe(specificationSection)
      }

      if (attachmentSection) {
        observer.observe(attachmentSection)
      }
    }

    window.addEventListener('resize', updateCtaPosition)
    window.addEventListener('scroll', updateCtaPosition)
  })
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateCtaPosition)
  window.removeEventListener('scroll', updateCtaPosition)
})

</script>
