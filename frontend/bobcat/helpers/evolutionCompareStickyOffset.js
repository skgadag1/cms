/**
 * Pixel offset from the viewport top for `position: sticky` on evolution-compare
 * model dropdowns, aligned with StickyProgressBar / MidBuyingTools patterns.
 *
 * @param {boolean} [isSmallMobile]
 * @returns {number}
 */
export function getEvolutionCompareDropdownStickyTopPx(isSmallMobile = false) {
  if (typeof document === 'undefined') {
    return 0
  }

  const header = document.querySelector('header[data-test-id="main-header"]')
  const headerH = header?.clientHeight ?? 0
  const notificationBar = document.querySelector('[data-test-id="notification-bar-content"]')

  /** Same base as StickyProgressBar (header + optional notification). */
  let top
  if (!isSmallMobile && notificationBar) {
    top = headerH - 1 + (notificationBar?.clientHeight ?? 0)
  } else {
    top = headerH - 1
  }

  const fixedNearTopSelectors = [
    '[data-test-id="progress-bar-sticky"]',
    '[data-test-id="mid-buying-tools-sticky-wrapper"]',
    '[data-test-id="anchor-alternative-nav-wrapper"]'
  ]

  for (let pass = 0; pass < 3; pass++) {
    const before = top
    for (const sel of fixedNearTopSelectors) {
      const el = document.querySelector(sel)
      if (!el) {
        continue
      }
      const cs = getComputedStyle(el)
      if (cs.position !== 'fixed') {
        continue
      }
      if (parseFloat(cs.opacity) < 0.04) {
        continue
      }
      const r = el.getBoundingClientRect()
      if (r.height < 2) {
        continue
      }
      if (r.top <= top + 4) {
        top = Math.max(top, r.bottom)
      }
    }
    if (top === before) {
      break
    }
  }

  return Math.max(0, Math.round(top))
}
