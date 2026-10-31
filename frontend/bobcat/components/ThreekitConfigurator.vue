<template>
  <section class="container py-4" data-test-id="threekit-configurator">
    <h4 v-if="title">{{ title }}</h4>
    <div v-if="paragraph" v-html="paragraph" />
    <div
      ref="fillWrapper"
      class="threekit-responsive w-100"
      :style="heightStyle"
    >
      <iframe
        v-if="iframeSrc"
        ref="iframe"
        :src="iframeSrc"
        :title="iframeTitle || title || 'ThreeKit Configurator'"
        class="w-100 h-100 border-0"
        allow="fullscreen; xr-spatial-tracking; clipboard-write"
        allowfullscreen
        data-test-id="threekit-configurator-iframe"
        @load="postParentUrl"
      />
    </div>
  </section>
</template>

<script>
import {computed, onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

// Messages exchanged with the ThreeKit (bobcat-cpq) iframe. Contract provided
// by ThreeKit.
// Child -> parent: the user navigated inside the configurator.
const CHILD_LOCATION_MESSAGE = 'bobcat-cpq:location'
// Child -> parent: the configurator is ready to receive the parent URL.
const CHILD_READY_MESSAGE = 'bobcat-cpq:ready'
// Parent -> child: the current page URL, used by ThreeKit as the prefix for
// Share Config links so they reopen the configurator inside this page.
const PARENT_URL_MESSAGE = 'bobcat-cpq:parent-url'

// Attribution cookies forwarded to the configurator with the page URL.
const GA_COOKIE = '_ga'
const REFERRAL_COOKIE = 'Bobcat_ref'

// Build & Price params the configurator reads on load and reports back on
// navigation. Always passed into the iframe, even when an author sets
// allowedParams, so the parent URL and the configurator stay in sync.
export const SYNCED_PARAMS = ['cat', 'subcat', 'model']

// Share Config (saved build) params, e.g. ?b=105.vGXo…T450. `build` is the
// original long form, still honoured by ThreeKit for links already sent.
// Always passed into the iframe so share links open the saved build inside
// this page.
export const SHARE_CONFIG_PARAMS = ['b', 'build']

// Params always passed from the page URL into the iframe, regardless of the
// authored allowedParams.
export const PASSED_PARAMS = [...SYNCED_PARAMS, ...SHARE_CONFIG_PARAMS]

// Accepted shape for synced param values reported by the configurator.
const SLUG = /^[a-z0-9-]{1,100}$/
const parseAllowedParams = value => (value || '')
  .split(/[\s,]+/)
  .map(param => param.trim())
  .filter(Boolean)

// Hosts allowed to exchange messages (and receive the page URL and cookies)
// with this component: any subdomain of 3kit.ai.
const TRUSTED_HOST_SUFFIX = '.3kit.ai'

// Origin of the authored configurator URL when it is a trusted ThreeKit host
// (https, default port, *.3kit.ai), otherwise null. When null, the iframe
// still renders but no messages are sent or accepted (fail soft).
// The hostname is parsed rather than string-matched, so look-alikes such as
// evil3kit.ai or 3kit.ai.evil.com are rejected.
export const getTrustedOrigin = (src) => {
  let url
  try {
    url = new URL((src || '').trim())
  } catch (e) {
    return null
  }
  const trusted = url.protocol === 'https:' &&
    url.port === '' &&
    url.hostname.endsWith(TRUSTED_HOST_SUFFIX)
  return trusted ? url.origin : null
}

// Returns the parent page URL with the configurator's reported location
// applied, or null when the message should be ignored. A synced param that is
// missing or not a valid slug is removed, so each message is treated as the
// complete location (e.g. moving from a model page up to its subcategory
// drops `model`). Other page params are left untouched.
//
// Share Config params are always removed: the configurator consumes a share
// link when it opens, so once location messages arrive they no longer
// describe what is on screen (per ThreeKit's contract).
export const applyChildLocation = (href, data) => {
  if (!data || data.type !== CHILD_LOCATION_MESSAGE) { return null }

  const url = new URL(href)
  SYNCED_PARAMS.forEach((name) => {
    const value = data[name]
    if (typeof value === 'string' && SLUG.test(value)) {
      url.searchParams.set(name, value)
    } else {
      url.searchParams.delete(name)
    }
  })
  SHARE_CONFIG_PARAMS.forEach(name => url.searchParams.delete(name))
  return url.href
}

export const buildIframeSrc = (src, search = '', allowedParams = '') => {
  if (!src) { return '' }

  let url
  try {
    url = new URL(src.trim())
  } catch (e) {
    return src
  }

  // Allowlist: cat/subcat/model and the share params are always passed, plus any
  // additional params the author lists. An empty list passes only those;
  // every other page param is dropped.
  const allowed = [...PASSED_PARAMS, ...parseAllowedParams(allowedParams)]
  const pageParams = new URLSearchParams(search)
  const keys = [...new Set(pageParams.keys())]
    .filter(key => allowed.includes(key))

  keys.forEach((key) => {
    const values = pageParams.getAll(key).filter(value => value !== '')
    if (!values.length) { return }
    url.searchParams.delete(key)
    values.forEach(value => url.searchParams.append(key, value))
  })

  return url.toString()
}

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    iframeTitle: {
      type: String
    },
    paragraph: {
      type: String
    },
    src: {
      type: String,
      required: true
    },
    allowedParams: {
      type: String
    },
    desktopHeight: {
      type: String,
      default: '800'
    },
    mobileHeight: {
      type: String,
      default: '600'
    }
  },
  setup(props) {
    const pageSearch = ref(null)
    const fillWrapper = ref(null)
    // Vertical space that is NOT the fill region: everything above it (nav
    // header, sibling components such as the section-text component, this
    // component's own title/paragraph) plus anything the section renders below
    // it (e.g. container bottom padding). Measured at runtime, never
    // author-provided, so it stays correct across breakpoints, i18n copy
    // length and content reflow.
    const spaceOutsideRegion = ref(0)

    useMounted(props.nodeId)

    const iframeSrc = computed(() => pageSearch.value === null
      ? ''
      : buildIframeSrc(props.src, pageSearch.value, props.allowedParams))

    // True when the region starts within the first viewport, i.e. filling the
    // remaining height is possible without a page scrollbar. Stays false until
    // the first client-side measurement, so SSR output and first paint use the
    // CSS fallback (author-configured heights) instead of a full-viewport box.
    const fitsInViewport = ref(false)

    // Fill the dynamic viewport minus the space taken by everything else, and
    // nothing more. There's deliberately NO min-height while it fits: a floor and
    // "never exceed the viewport" are contradictory on a short window (a 600px
    // floor on a 577px viewport forces overflow and brings back the page
    // scrollbar we are trying to remove).
    // When it does NOT fit (component placed more than a viewport down the
    // page, or a viewport shorter than the space around it) the page already
    // scrolls, and the calc would go negative and collapse the configurator to
    // 0. In that case drop the inline height and let the CSS fallback apply.
    const heightStyle = computed(() => {
      const style = {
        '--threekit-desktop-height': `${parseInt(props.desktopHeight, 10) || 800}px`,
        '--threekit-mobile-height': `${parseInt(props.mobileHeight, 10) || 600}px`
      }
      if (fitsInViewport.value) {
        style.height = `calc(100dvh - ${spaceOutsideRegion.value}px)`
      }
      return style
    })

    // Origin of the authored configurator URL, if it is a trusted ThreeKit host.
    // Follows whichever ThreeKit environment/locale URL is entered in Magnolia.
    const threekitOrigin = computed(() => getTrustedOrigin(props.src))
    const iframe = ref(null)
    const {$cookies} = useContext()

    // Raw cookie value, or null when unset. parseJSON is off so values are
    // always passed through as strings.
    const readCookie = name => $cookies.get(name, {parseJSON: false}) ?? null

    // Send the current page URL into the iframe, plus the attribution cookies
    // the configurator cannot read across domains: `_ga` (GA client ID) and
    // `Bobcat_ref` (referral), the same cookies bobcat.com forms read.
    // Called on iframe load and whenever the configurator announces it is
    // ready (it may not be listening yet at load, and `_ga` may only be set
    // once GA has loaded). The target origin is the authored configurator's
    // origin, so these values are never delivered to any other page loaded in
    // the frame.
    const postParentUrl = () => {
      const target = iframe.value && iframe.value.contentWindow
      if (!target || !threekitOrigin.value) { return }
      target.postMessage(
        {
          type: PARENT_URL_MESSAGE,
          href: window.location.href,
          ga: readCookie(GA_COOKIE),
          ref: readCookie(REFERRAL_COOKIE)
        },
        threekitOrigin.value
      )
    }

    // Handle messages from the configurator:
    // - ready: reply with the page URL.
    // - location: mirror cat/subcat/model into the address bar. replaceState
    //   bypasses Vue Router and does not add a history entry; the iframe src is
    //   computed once on mount, so this never reloads it.
    // Messages must come from the trusted origin and from this component's own
    // iframe window, so another window on the same origin cannot drive it.
    const onMessage = (event) => {
      if (!threekitOrigin.value || event.origin !== threekitOrigin.value) { return }
      const source = iframe.value && iframe.value.contentWindow
      if (!source || event.source !== source) { return }
      if (event.data && event.data.type === CHILD_READY_MESSAGE) {
        postParentUrl()
        return
      }
      const nextHref = applyChildLocation(window.location.href, event.data)
      if (nextHref && nextHref !== window.location.href) {
        window.history.replaceState(window.history.state, '', nextHref)
      }
    }

    let resizeObserver = null
    let frameId = null

    const measure = () => {
      frameId = null
      if (!fillWrapper.value) { return }
      const section = fillWrapper.value.closest('[data-test-id="threekit-configurator"]')
      const rect = fillWrapper.value.getBoundingClientRect()
      // Space above the region (header + siblings + title/paragraph + container
      // top padding) as a document offset, so the result does not depend on the
      // current scroll position (mobile URL-bar resizes fire while scrolled).
      const spaceAbove = rect.top + window.scrollY
      // Space the section renders below the region (e.g. container bottom padding).
      // Guard against the section lookup failing.
      const spaceBelow = section
        ? section.getBoundingClientRect().bottom - rect.bottom
        : 0
      spaceOutsideRegion.value = Math.max(0, Math.round(spaceAbove + spaceBelow))
      // innerHeight tracks the dynamic viewport closely enough;
      // the fill itself still uses 100dvh and is re-evaluated on resize.
      fitsInViewport.value = spaceOutsideRegion.value < window.innerHeight
    }

    // Coalesce bursts of resize / ResizeObserver callbacks into one measurement per frame.
    const scheduleMeasure = () => {
      if (frameId !== null) { return }
      frameId = window.requestAnimationFrame(measure)
    }

    onMounted(() => {
      pageSearch.value = window.location.search
      measure()

      // Listen for the configurator reporting navigation inside the iframe.
      window.addEventListener('message', onMessage)

      // Fail soft: an untrusted configurator URL still renders, but URL sync
      // and Share Config are disabled. Warn so the cause is easy to find.
      if (props.src && !threekitOrigin.value) {
        // eslint-disable-next-line no-console
        console.warn(`ThreekitConfigurator: "${props.src}" is not a trusted ThreeKit URL (https://*${TRUSTED_HOST_SUFFIX}); URL sync and Share Config are disabled.`)
      }

      // Re-measure when the viewport changes or content above us reflows
      // (text wrapping, i18n length, breakpoint changes, siblings resizing).
      window.addEventListener('resize', scheduleMeasure)
      if (typeof ResizeObserver !== 'undefined') {
        resizeObserver = new ResizeObserver(scheduleMeasure)
        resizeObserver.observe(document.body)
      }
    })

    onBeforeUnmount(() => {
      window.removeEventListener('message', onMessage)
      window.removeEventListener('resize', scheduleMeasure)
      // Cancel any queued measure so it doesn't run against the unmounted component.
      if (frameId !== null) {
        window.cancelAnimationFrame(frameId)
        frameId = null
      }
      if (resizeObserver) {
        resizeObserver.disconnect()
        resizeObserver = null
      }
    })

    return {
      iframeSrc,
      heightStyle,
      fillWrapper,
      iframe,
      postParentUrl
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.threekit-responsive {
  // When the region fits in the first viewport, height is driven by the inline
  // heightStyle: calc(100dvh - spaceOutsideRegion). The rules below apply during
  // SSR / first paint, when the region starts below the first viewport, and in
  // browsers without dvh (the inline declaration is dropped as invalid there).
  height: var(--threekit-desktop-height, 800px);
  max-height: 100vh;

  @include media-breakpoint-down(sm) {
    height: var(--threekit-mobile-height, 600px);
  }
}
</style>
