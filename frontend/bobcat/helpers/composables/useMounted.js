import {onMounted, ssrRef} from '@nuxtjs/composition-api'

const componentsToLoad = ssrRef({}, 'globalState')
let hasLoaded = false

export default (nodeId) => {
  const clearComponentsStack = () => {
    componentsToLoad.value = {}
  }

  if (nodeId) {
    if (process.server) {
      componentsToLoad.value[nodeId] = false
    }

    if (process.client) {
      onMounted(() => {
        if (!hasLoaded) {
          if (componentsToLoad.value[nodeId] === false) {
            componentsToLoad.value[nodeId] = true
          }
          if (!Object.values(componentsToLoad.value).includes(false)) {
            const loadedEvent = new Event('componentsLoaded')
            hasLoaded = true
            document.dispatchEvent(loadedEvent)
          }
        }
      })
    }
  }

  return {
    clearComponentsStack
  }
}
