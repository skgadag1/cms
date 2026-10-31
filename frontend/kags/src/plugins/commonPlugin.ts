import type {App, Directive} from 'vue'
import http from '@/utils'
import api from '@/api/apiUrls'
import OverlayWrapper from '@/components/common/OverlayWrapper.vue'

const clickOutsideHandlers = new WeakMap<HTMLElement, (event: MouseEvent) => void>()
const clickOutside: Directive<HTMLElement, (event: MouseEvent) => void> = {
  beforeMount(el, binding) {
    const handler = (event: MouseEvent) => {
      // composedPath() is captured at dispatch time, so a click on something the date picker
      // re-renders (and detaches) during the same click still counts as inside.
      const path = event.composedPath ? event.composedPath() : []
      const target = event.target as Node | null
      const _isOutsideEvent = !(el === target || el.contains(target) || path.includes(el))
      const _isNotFromDatePicker = !path.some(node => node instanceof Element && node.classList.contains('dp__menu'))

      if (_isOutsideEvent && _isNotFromDatePicker && typeof binding.value === 'function') binding.value(event)
    }
    clickOutsideHandlers.set(el, handler)
    document.body.addEventListener('click', handler)
  },
  unmounted(el) {
    const handler = clickOutsideHandlers.get(el)
    if (handler) document.body.removeEventListener('click', handler)
    clickOutsideHandlers.delete(el)
  }
}

export default {
  install(app: App) {
    // Avoid duplicate registration when another setup path registered these globally already.
    if (!app.component('overlay-wrapper')) app.component('overlay-wrapper', OverlayWrapper)
    if (!app.directive('click-outside')) app.directive('click-outside', clickOutside)

    app.config.globalProperties.$http = http
    app.config.globalProperties.$api = api
  }
}
