import {ref} from '@nuxtjs/composition-api'

const localTimer = ref(null)

export default function useDebounce(loadingTimer = localTimer) {
  const debounce = (func, timeout = 300) => {
    return ((...args) => {
      clearTimeout(loadingTimer.value)

      loadingTimer.value = setTimeout(() => {
        func.apply(this, args)
      }, timeout)
    })()
  }

  return {
    debounce
  }
}
