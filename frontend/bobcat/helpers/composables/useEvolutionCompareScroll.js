import {computed, nextTick, onBeforeUnmount, onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import {getEvolutionCompareDropdownStickyTopPx} from '~/helpers/evolutionCompareStickyOffset'

/**
 * Sticky dropdown bar, mobile tile sliders and synced horizontal scroll of the spec rows
 * for EvolutionCompareComponent.
 */
export default function useEvolutionCompareScroll({specsFullyExpanded, specsLoadingOverlayActive, showOnlyDifferences}) {
  const {$utils} = useContext()

  const tilesScrollRow = ref(null)
  const tilesSliderAtEnd = ref(false)
  const bottomTilesScrollRow = ref(null)
  const bottomTilesSliderAtEnd = ref(true)
  const compareDropdownStickyTopPx = ref(0)
  const compareSpecsStickySentinel = ref(null)
  const compareSpecsStickyEndSentinel = ref(null)
  const compareSpecsRegionRef = ref(null)
  const showSpecsStickyDropdowns = ref(false)

  const updateSpecsStickyBarVisibility = () => {
    if (!process.client || !specsFullyExpanded.value) {
      showSpecsStickyDropdowns.value = false
      return
    }
    const topEl = compareSpecsStickySentinel.value
    if (!topEl || typeof topEl.getBoundingClientRect !== 'function') {
      return
    }
    const topPx = compareDropdownStickyTopPx.value
    const {top: topSentinelTop} = topEl.getBoundingClientRect()
    const pastCheckbox = topSentinelTop <= topPx

    const endEl = compareSpecsStickyEndSentinel.value
    if (!endEl || typeof endEl.getBoundingClientRect !== 'function') {
      showSpecsStickyDropdowns.value = pastCheckbox
      return
    }
    const {top: endSentinelTop} = endEl.getBoundingClientRect()
    const pastSpecsEnd = endSentinelTop <= topPx
    showSpecsStickyDropdowns.value = pastCheckbox && !pastSpecsEnd
  }

  let compareStickyTopRaf = null
  const scheduleCompareDropdownStickyTop = () => {
    if (!process.client) {
      return
    }
    if (compareStickyTopRaf != null) {
      cancelAnimationFrame(compareStickyTopRaf)
    }
    compareStickyTopRaf = requestAnimationFrame(() => {
      compareStickyTopRaf = null
      if (!specsFullyExpanded.value) {
        compareDropdownStickyTopPx.value = 0
        showSpecsStickyDropdowns.value = false
        return
      }
      compareDropdownStickyTopPx.value = getEvolutionCompareDropdownStickyTopPx($utils.isSmallMobile())
      updateSpecsStickyBarVisibility()
    })
  }

  const compareStickyCssVars = computed(() => ({
    '--ec-dropdown-sticky-top': `${compareDropdownStickyTopPx.value}px`
  }))

  const isEcCompareTilesSliderLayout = () =>
    process.client && $utils.isSmallMobile()

  const updateTilesSliderScrollEnd = () => {
    if (!isEcCompareTilesSliderLayout()) {
      tilesSliderAtEnd.value = true
      return
    }
    const el = tilesScrollRow.value?.$el
    if (!el) {
      tilesSliderAtEnd.value = true
      return
    }
    const {scrollLeft, scrollWidth, clientWidth} = el
    const epsilon = 8
    if (scrollWidth <= clientWidth + epsilon) {
      tilesSliderAtEnd.value = true
      return
    }
    tilesSliderAtEnd.value = scrollLeft + clientWidth >= scrollWidth - epsilon
  }

  const updateBottomTilesSliderScrollEnd = () => {
    if (!isEcCompareTilesSliderLayout()) {
      bottomTilesSliderAtEnd.value = true
      return
    }
    const el = bottomTilesScrollRow.value?.$el
    if (!el) {
      bottomTilesSliderAtEnd.value = true
      return
    }
    const {scrollLeft, scrollWidth, clientWidth} = el
    const epsilon = 8
    if (scrollWidth <= clientWidth + epsilon) {
      bottomTilesSliderAtEnd.value = true
      return
    }
    bottomTilesSliderAtEnd.value = scrollLeft + clientWidth >= scrollWidth - epsilon
  }

  let tilesSliderResizeObserver = null
  let bottomTilesSliderResizeObserver = null
  let tilesSliderWinResize = null

  const isEcCompareHScrollBreakpoint = () =>
    process.client && !($utils.isDesktopLG() || $utils.isDesktopXL())

  const updateEcHScrollGradientStates = () => {
    if (!process.client || !isEcCompareHScrollBreakpoint()) {
      return
    }
    const root = compareSpecsRegionRef.value
    if (!root) {
      return
    }
    const epsilon = 8
    root.querySelectorAll('[data-ec-compare-hscroll]').forEach((el) => {
      const wrap = el.closest('[data-ec-compare-hscroll-wrap]')
      if (!wrap) {
        return
      }
      const noOverflow = el.scrollWidth <= el.clientWidth + epsilon
      const atEnd = noOverflow || el.scrollLeft + el.clientWidth >= el.scrollWidth - epsilon
      wrap.classList.toggle('ec-compare-hscroll-wrap--at-end', atEnd)
    })
  }

  const bindTilesSliderObservers = () => {
    if (!process.client) {
      return
    }
    if (tilesSliderResizeObserver) {
      tilesSliderResizeObserver.disconnect()
      tilesSliderResizeObserver = null
    }
    if (bottomTilesSliderResizeObserver) {
      bottomTilesSliderResizeObserver.disconnect()
      bottomTilesSliderResizeObserver = null
    }
    if (tilesSliderWinResize) {
      window.removeEventListener('resize', tilesSliderWinResize)
      tilesSliderWinResize = null
    }
    if (!isEcCompareTilesSliderLayout()) {
      tilesSliderAtEnd.value = true
      bottomTilesSliderAtEnd.value = true
      return
    }
    const topEl = tilesScrollRow.value?.$el
    const bottomEl = bottomTilesScrollRow.value?.$el

    const scheduleStripUpdates = () => {
      updateTilesSliderScrollEnd()
      updateBottomTilesSliderScrollEnd()
      updateEcHScrollGradientStates()
    }

    if ((!topEl && !bottomEl) || typeof ResizeObserver === 'undefined') {
      nextTick(() => scheduleStripUpdates())
      return
    }
    tilesSliderWinResize = () => scheduleStripUpdates()
    window.addEventListener('resize', tilesSliderWinResize)
    if (topEl) {
      tilesSliderResizeObserver = new ResizeObserver(scheduleStripUpdates)
      tilesSliderResizeObserver.observe(topEl)
    }
    if (bottomEl) {
      bottomTilesSliderResizeObserver = new ResizeObserver(scheduleStripUpdates)
      bottomTilesSliderResizeObserver.observe(bottomEl)
    }
    scheduleStripUpdates()
    requestAnimationFrame(() => scheduleStripUpdates())
  }

  let compareSpecsHScrollCleanup = null
  let ecCompareHScrollProgrammatic = false
  let ecCompareHScrollPostRaf = null

  const flushEcCompareHScrollPostScroll = () => {
    ecCompareHScrollPostRaf = null
    updateEcHScrollGradientStates()
    updateBottomTilesSliderScrollEnd()
  }

  const scheduleEcCompareHScrollPostScroll = () => {
    if (ecCompareHScrollPostRaf != null) {
      return
    }
    ecCompareHScrollPostRaf = requestAnimationFrame(flushEcCompareHScrollPostScroll)
  }

  const onCompareSpecsHScrollPortScroll = (e) => {
    if (ecCompareHScrollProgrammatic) {
      return
    }
    const root = compareSpecsRegionRef.value
    if (!root) {
      return
    }
    const src = e.target
    const left = src.scrollLeft
    ecCompareHScrollProgrammatic = true
    try {
      root.querySelectorAll('[data-ec-compare-hscroll]').forEach((el) => {
        if (el !== src && Math.abs(el.scrollLeft - left) > 0.5) {
          el.scrollLeft = left
        }
      })
    } finally {
      ecCompareHScrollProgrammatic = false
    }
    scheduleEcCompareHScrollPostScroll()
  }

  const bindCompareSpecsHScrollSync = () => {
    compareSpecsHScrollCleanup?.()
    compareSpecsHScrollCleanup = null
    if (!process.client) {
      return
    }
    nextTick(() => {
      const root = compareSpecsRegionRef.value
      if (!root || !isEcCompareHScrollBreakpoint()) {
        return
      }
      const ports = [...root.querySelectorAll('[data-ec-compare-hscroll]')]
      if (!ports.length) {
        return
      }
      ports.forEach((el) => {
        el.addEventListener('scroll', onCompareSpecsHScrollPortScroll, {passive: true})
      })
      updateEcHScrollGradientStates()
      updateBottomTilesSliderScrollEnd()
      compareSpecsHScrollCleanup = () => {
        if (ecCompareHScrollPostRaf != null) {
          cancelAnimationFrame(ecCompareHScrollPostRaf)
          ecCompareHScrollPostRaf = null
        }
        ports.forEach((el) => {
          el.removeEventListener('scroll', onCompareSpecsHScrollPortScroll)
        })
        compareSpecsHScrollCleanup = null
      }
    })
  }

  const clampWindowScrollToDocument = () => {
    if (!process.client) {
      return
    }
    const doc = document.documentElement
    const maxTop = Math.max(0, doc.scrollHeight - window.innerHeight)
    if (window.scrollY > maxTop) {
      window.scrollTo(0, maxTop)
    }
  }

  const onCompareWindowResize = () => {
    scheduleCompareDropdownStickyTop()
    bindCompareSpecsHScrollSync()
  }

  const refreshScrollBindings = () => {
    nextTick(() => {
      bindTilesSliderObservers()
      updateTilesSliderScrollEnd()
      bindCompareSpecsHScrollSync()
    })
  }

  watch(specsFullyExpanded, () => {
    if (process.client) {
      nextTick(() => {
        scheduleCompareDropdownStickyTop()
        bindCompareSpecsHScrollSync()
      })
    }
  })

  watch(specsLoadingOverlayActive, () => {
    if (process.client && specsFullyExpanded.value) {
      nextTick(() => scheduleCompareDropdownStickyTop())
    }
  })

  watch(showOnlyDifferences, () => {
    if (!process.client) {
      return
    }
    nextTick(() => {
      requestAnimationFrame(() => {
        clampWindowScrollToDocument()
        scheduleCompareDropdownStickyTop()
        bindCompareSpecsHScrollSync()
      })
    })
  })

  onMounted(() => {
    scheduleCompareDropdownStickyTop()
    window.addEventListener('scroll', scheduleCompareDropdownStickyTop, {passive: true})
    window.addEventListener('resize', onCompareWindowResize, {passive: true})
    refreshScrollBindings()
  })

  onBeforeUnmount(() => {
    if (process.client) {
      window.removeEventListener('scroll', scheduleCompareDropdownStickyTop)
      window.removeEventListener('resize', onCompareWindowResize)
      if (compareStickyTopRaf != null) {
        cancelAnimationFrame(compareStickyTopRaf)
        compareStickyTopRaf = null
      }
      compareSpecsHScrollCleanup?.()
      compareSpecsHScrollCleanup = null
      if (ecCompareHScrollPostRaf != null) {
        cancelAnimationFrame(ecCompareHScrollPostRaf)
        ecCompareHScrollPostRaf = null
      }
    }
    if (tilesSliderResizeObserver) {
      tilesSliderResizeObserver.disconnect()
      tilesSliderResizeObserver = null
    }
    if (bottomTilesSliderResizeObserver) {
      bottomTilesSliderResizeObserver.disconnect()
      bottomTilesSliderResizeObserver = null
    }
    if (tilesSliderWinResize) {
      window.removeEventListener('resize', tilesSliderWinResize)
      tilesSliderWinResize = null
    }
  })

  return {
    tilesScrollRow,
    tilesSliderAtEnd,
    bottomTilesScrollRow,
    bottomTilesSliderAtEnd,
    compareSpecsStickySentinel,
    compareSpecsStickyEndSentinel,
    compareSpecsRegionRef,
    showSpecsStickyDropdowns,
    compareStickyCssVars,
    updateTilesSliderScrollEnd,
    onCompareSpecsHScrollPortScroll,
    refreshScrollBindings
  }
}
