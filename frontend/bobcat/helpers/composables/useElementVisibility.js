import {ref, onMounted, onBeforeUnmount, nextTick} from '@nuxtjs/composition-api'

export function useElementVisibility(enableObserver = false, options = {threshold: 0.5}) {
  const isVisible = ref(!enableObserver)
  const observerTarget = ref(null)
  let observer = null

  const checkVisibility = () => {
    const rect = observerTarget.value?.getBoundingClientRect()
    if (enableObserver && rect) {
      isVisible.value = rect.top <= (window.innerHeight || document.documentElement.clientHeight)
    }
  }

  onMounted(async () => {
    if (enableObserver && 'IntersectionObserver' in window) {
      await nextTick()

      observer = new IntersectionObserver(([entry]) => {
        if (entry.isIntersecting) {
          isVisible.value = true
        }
      }, options)

      if (observerTarget.value) {
        setTimeout(() => { // wait for page load
          observer.observe(observerTarget.value)
        }, 500)
      }
    }
  })

  onBeforeUnmount(() => {
    if (enableObserver && observer && observerTarget.value) {
      observer.unobserve(observerTarget.value)
      observer.disconnect()
    }
  })

  return {
    isVisible,
    observerTarget,
    checkVisibility
  }
}
