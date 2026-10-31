import {watch, nextTick, onMounted, onBeforeUnmount, unref} from '@nuxtjs/composition-api'

/**
 * Attaches click listeners to <a> tags inside HTML content (e.g., rendered with v-html)
 * and emits an event when a link is clicked.
 *
 * @param {Ref} target - A ref to the target DOM element.
 * @param {Function} onClick - Function from the component setup context.
 */
export default function useContentLinkWatcher(target, onClick = () => {}) {
  let activeLinks = []

  const detachLinkListeners = () => {
    activeLinks.forEach(link => link.removeEventListener('click', onClick))
    activeLinks = []
  }

  const attachLinkListeners = () => {
    nextTick(() => {
      detachLinkListeners()

      const el = unref(target)

      if (!el) { return }

      activeLinks = Array.from(el.querySelectorAll('a'))
      activeLinks.forEach(link => link.addEventListener('click', onClick))
    })
  }

  onMounted(attachLinkListeners)

  if (typeof target === 'function') {
    watch(target, attachLinkListeners)
  }

  onBeforeUnmount(detachLinkListeners)

  return {
    attachLinkListeners
  }
}
