import {ref, useContext, onBeforeMount, onBeforeUnmount} from '@nuxtjs/composition-api'

export default function useBreakpoints() {
  const {$utils, $breakpoints} = useContext()

  const mobile = ref($breakpoints?.mobile)
  const tablet = ref($breakpoints?.tablet)
  const desktop = ref($breakpoints?.desktop)

  const updateBreakpoints = () => {
    mobile.value = $breakpoints?.mobile || $utils.isMobile() || $utils.isSmallMobile()
    tablet.value = $breakpoints?.tablet || $utils.isTablet()
    desktop.value = $breakpoints?.desktop || $utils.isDesktopLG() || $utils.isDesktopXL()
  }

  if (process.client) {
    onBeforeMount(() => {
      window.addEventListener('resize', updateBreakpoints, {passive: true})
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateBreakpoints)
    })
  }

  if (!$breakpoints) { updateBreakpoints() }

  return {
    mobile,
    tablet,
    desktop
  }
}
