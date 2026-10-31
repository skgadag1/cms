<template>
  <div>
    <header ref="header"
            class="shadow w-100"
            :class="[{
              'fixed-top' : !inEditorEdit,
              'zindex-modal-backdrop' : isOpened || isSearchVisible
            }, desktop ? 'bg-white' : 'bg-black']"
            data-test-id="main-header"
    >
      <nav v-if="!isCollapsedNav && (mobile || tablet)"
           data-test-id="main-header-mobile-region-selector"
           class="d-lg-none navbar transition-base bg-black justify-content-center"
           role="navigation"
           aria-label="Region selector"
           :class="showRegionSelector ? 'region-visible' : 'region-invisible'"
      >
        <LazyHydrate never>
          <div class="d-flex region-selector-picker" data-test-id="region-selector-picker">
            <b-icon icon="globe" />
            <a v-if="regionSelectorLink && regionSelectorLink.url"
               class="text-gray d-flex align-items-center small"
               data-test-id="region-selector-picker__link"
               :href="regionSelectorLink.url"
               :target="regionSelectorLink.target === '_blank_follow' ? '_blank' : regionSelectorLink.target"
               :title="regionSelectorLink.label"
               :rel="regionSelectorLink.target === '_blank' ? 'noopener nofollow' : null"
               :disabled="inEditorEdit"
               @click="ga4click"
            >
              <span class="ml-2">{{ regionSelectorLink.label }}</span>
            </a>
          </div>
        </LazyHydrate>
      </nav>

      <nav class="bg-primary">
        <div class="container">
          <div class="navbar row">
            <button v-b-toggle.sidebar-navigation
                    :class="['col-auto d-flex text-white justify-content-center align-items-center pl-1 mr-md-3', {'d-lg-none': !isCollapsedNav}]"
                    type="button"
                    data-toggle="collapse"
                    :aria-expanded="(usesInlineSecondaryNav ? isSecondaryNavOpen : isOpened) ? 'true' : 'false'"
                    aria-controls="sidebar-navigation"
                    aria-label="Toggle navigation"
                    title="Toggle navigation"
                    :disabled="inEditorEdit"
                    @click="onHamburgerClick"
            >
              <b-icon :icon="hamburgerIcon" />
            </button>
            <LazyHydrate never>
              <div v-if="logoUrl" class="d-flex justify-content-center col-md-2 col px-0" data-test-id="main-header-logo">
                <b-navbar-brand class="text-white" :href="mainRoute" :title="logo.imageAlt || logo.imageTitle">
                  <img :src="logoUrl"
                       :alt="logo.imageAlt"
                       :title="logo.imageTitle || 'Bobcat logo'"
                       height="38"
                       width="143"
                       style="margin-top: -0.5rem; width: auto !important; max-width: none;"
                       fetchpriority="high"
                       loading="eager"
                       decoding="async"
                  >
                </b-navbar-brand>
              </div>
            </LazyHydrate>
            <LazyHydrate never>
              <ul v-if="topNavItems.length || regionSelectorLink"
                  :class="navClasses"
                  :style="navStyles"
                  data-test-id="main-header-sub-nav-links"
              >
                <li v-for="item in topNavItems" :key="'header-subnav__' + item['id']" class="text-white d-flex align-items-center mx-1 small">
                  <b-icon v-if="item.icon !== 'none'" :icon="item.icon" />
                  <a class="link text-white px-3 px-lg-2 py-1 font-weight-bold upper-links overflow-hidden fs-nav"
                     :href="item.url"
                     :target="item.target === '_blank_follow' ? '_blank' : item.target"
                     :rel="item.target === '_blank' ? 'noopener nofollow' : null"
                     :disabled="inEditorEdit"
                     @click="ga4click"
                  >
                    {{ item.label }}
                  </a>
                </li>
                <li v-if="regionSelectorLink && regionSelectorLink.url" class="text-white d-none d-lg-flex align-items-center mx-1 small">
                  <b-icon icon="globe" />
                  <a class="link text-white px-2 py-1 font-weight-bold region-selector overflow-hidden text-truncate fs-nav"
                     :href="regionSelectorLink.url"
                     :target="regionSelectorLink.target === '_blank_follow' ? '_blank' : regionSelectorLink.target"
                     :rel="regionSelectorLink.target === '_blank' ? 'noopener nofollow' : null"
                     :disabled="inEditorEdit"
                     @click="ga4click"
                  >
                    {{ regionSelectorLink.label }}
                  </a>
                </li>
              </ul>
            </LazyHydrate>
            <button class="col-auto d-flex d-sm-flex d-lg-none justify-content-center align-items-center text-white pr-1"
                    type="button"
                    aria-label="Toggle search"
                    data-test-id="main-header-toggle-search"
                    :disabled="inEditorEdit"
                    @click.prevent="isSearchVisible = !isSearchVisible"
            >
              <b-icon :icon="isSearchVisible ? 'times' : 'search'" />
            </button>
            <LazyHydrate :on-interaction="['mouseenter', 'click', 'focus']">
              <div :class="searchClasses" data-test-id="main-header-search">
                <SearchInput form-id="cludo-search-form" type="text" :search-page-url="pageUrl" placeholder="Search" />
              </div>
            </LazyHydrate>
          </div>
        </div>
      </nav>

      <LazyHydrate v-if="tablet || mobile || !desktop"
                   when-idle
      >
        <div class="d-sm-flex d-lg-none position-absolute w-100"
             data-test-id="main-header-mobile-search"
        >
          <SearchInput form-id="cludo-search-form-mobile" :visible="isSearchVisible" :search-page-url="pageUrl" type="text" />
        </div>
      </LazyHydrate>

      <LazyHydrate never>
        <div v-if="!isCollapsedNav && (mobile || tablet) && topNavItems.length" class="d-flex d-md-none">
          <div v-for="(topNavItem, idx) in topNavItems.slice(0, 3)"
               :key="`main-header-sub-nav-mobile-link-${idx}`"
               :data-test-id="`main-header-sub-nav-mobile-link-${idx}`"
               class="col p-0 m-0"
          >
            <a class="nav-link py-3 px-2 w-100 bg-black text-gray d-flex align-items-center justify-content-center height-5 small"
               :class="{'mid-border' : idx !== 2}"
               :href="topNavItem.url"
               :target="topNavItem.target === '_blank_follow' ? '_blank' : topNavItem.target"
               :rel="topNavItem.target === '_blank' ? 'noopener nofollow' : null"
               :disabled="inEditorEdit"
               @click="ga4click"
            >
              <b-icon v-if="topNavItem.icon !== 'none'" :icon="topNavItem.icon" />
              <span class="text-gray-dark font-weight-bold text-center" :class="{'ml-1' : topNavItem.icon !== 'none'}">{{ topNavItem.label }}</span>
            </a>
          </div>
        </div>
      </LazyHydrate>

      <SubnavigationMobile v-if="checkMobileOrTablet"
                           id="sidebar-navigation"
                           :items="items"
                           :init="initMobile"
                           :quick-links="isCollapsedNav ? topNavItems : null"
                           :region-link="isCollapsedNav ? regionSelectorLink : null"
                           @change="sidebarToggle"
      />
      <SubnavigationMain v-if="!checkMobileOrTablet && (!isCollapsedNav || isSecondaryNavOpen)" :items="items" />
    </header>

    <div v-if="!inEditorEdit" class="header-space" :class="{'header-space--collapsed-nav': isCollapsedNav}" :style="headerStyle" />
  </div>
</template>

<script>
import {
  onBeforeUnmount,
  onBeforeMount,
  ref,
  computed,
  inject,
  useContext,
  nextTick,
  useFetch,
  useRouter,
  onServerPrefetch,
  onMounted
} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'
import {inEditorEdit} from '~/helpers/vue-editor'
import useBreakpoints from '~/helpers/composables/useBreakpoints'
import {BUILD_AND_PRICE_PAGE_TEMPLATE_NAME} from '~/constants/pageTemplates'

export default {
  components: {
    SubnavigationMain: () => import('@/components/elements/Navigation/SubnavigationMain'),
    SubnavigationMobile: () => import('@/components/elements/Navigation/SubnavigationMobile')
  },
  props: ['logo', 'topLinks', 'regionLink', 'pageUrl', 'nodeId'],
  setup(props) {
    const {logo, topLinks, regionLink} = props
    const items = ref([])
    const $router = useRouter()
    const isSearchVisible = ref(false)
    const lastScrollPosition = ref(0)
    const showRegionSelector = ref(true)
    const header = ref(null)
    const headerHeight = ref(null)
    const headerStyle = computed({get: () => headerHeight.value ? `margin-bottom: ${headerHeight.value}px` : '', set: () => {}})
    const initMobile = ref(false)
    const {$regions, $utils, $services, $ga4} = useContext()
    const {mobile, tablet, desktop} = useBreakpoints()
    const currentLanguage = ref($regions?.current?.language?.name)
    const currentRegion = ref($regions?.current?.name)
    const regionSelectorLink = ref($utils.getLinkField(regionLink, $regions.current.language.name))
    const isOpened = ref(false)

    const checkMobileOrTablet = ref(!!mobile.value || !!tablet.value)

    const pageTemplateId = inject('pageTemplateId', null)
    const isCollapsedNav = computed(() => pageTemplateId === BUILD_AND_PRICE_PAGE_TEMPLATE_NAME)
    const isSecondaryNavOpen = ref(false)

    const hasCompactLayout = computed(() => {
      const navItemCount = topNavItems.value.length
      const hasRegion = !!regionSelectorLink.value?.url
      const totalNavItems = navItemCount + (hasRegion ? 1 : 0)

      const totalTextLength = topNavItems.value.reduce((sum, item) =>
        sum + (item.label?.length || 0), 0
      ) + (regionSelectorLink.value?.label?.length || 0)

      return totalNavItems > 4 ||
             totalTextLength > 60 ||
             (navItemCount > 3 && hasRegion)
    })

    const navClasses = computed(() => [
      'd-none',
      'd-md-flex',
      'col-md-8',
      'col-lg-6',
      'm-0',
      'p-0',
      'justify-content-center',
      'align-items-center',
      {'pr-lg-3,justify-content-lg-around': !hasCompactLayout.value}
    ])

    const navStyles = computed(() =>
      hasCompactLayout.value ? {paddingLeft: '40px !important'} : null
    )

    const searchClasses = computed(() => [
      'd-none',
      'd-lg-flex',
      'px-0',
      (hasCompactLayout.value || isCollapsedNav.value) ? 'col-md-3' : 'col-md-4'
    ])

    useMounted(props.nodeId)

    const ga4click = (event) => {
      $ga4?.events?.navigation_click(event, {navigation_location: 'header'})
    }

    const usesInlineSecondaryNav = computed(() => isCollapsedNav.value && !checkMobileOrTablet.value)
    const hamburgerIcon = computed(() =>
      (usesInlineSecondaryNav.value ? isSecondaryNavOpen.value : isOpened.value) ? 'times' : 'menu')

    const onHamburgerClick = () => {
      initMobile.value = true
      if (usesInlineSecondaryNav.value) {
        isSecondaryNavOpen.value = !isSecondaryNavOpen.value
      }
    }

    const sidebarToggle = (_visible) => {
      $ga4?.events?.navigation_click(event, {navigation_action: _visible ? 'expand' : 'collapse', navigation_location: 'hamburger'})
      isOpened.value = _visible
    }

    if (regionSelectorLink.value?.url && $regions?.current?.language?.label) {
      const label = (regionSelectorLink.value?.label?.replace('%REGION%', $regions.current.label)?.replace('%LANG%', $regions.current.language.label)) || regionSelectorLink.value.label
      regionSelectorLink.value = {...regionSelectorLink.value, label}
    }

    const logoUrl = $utils.getCloudinaryAsset(logo?.src, true, {height: 152, crop: 'scale'}, true)

    const topNavItems = ref(topLinks
      ? topLinks['@nodes']?.map(x => ({
        ...$utils.getLinkField(topLinks[x].link, $regions.current.language.name),
        icon: topLinks[x].icon
      })) || []
      : [])

    if (process.client) {
      if (!inEditorEdit) {
        const shouldFire = window.innerWidth > 576
        let notificationBarHeight = shouldFire ? document.querySelector('#notificationBar')?.clientHeight : 0

        const setHeaderSpaceMargin = () => {
          notificationBarHeight = shouldFire ? document.querySelector('#notificationBar')?.clientHeight : 0
          headerHeight.value = header.value?.clientHeight + (notificationBarHeight || 0)
        }

        onBeforeMount(() => {
          nextTick(function () {
            setTimeout(() => {
              headerHeight.value = header.value?.clientHeight + (shouldFire ? (document.querySelector('#notificationBar')?.clientHeight || 0) : 0)
              document.querySelector('#notificationBarCloseBtn')?.addEventListener('click', () => {
                headerHeight.value = header.value?.clientHeight
              })
              window.addEventListener('resize', setHeaderSpaceMargin, {passive: true})
            }, 0)
          })
        })

        onBeforeUnmount(() => window.removeEventListener('resize', setHeaderSpaceMargin))
      }

      const onScroll = () => {
        const currentScrollPosition = window.pageYOffset || document.documentElement.scrollTop
        if (currentScrollPosition < 0 || Math.abs(currentScrollPosition - lastScrollPosition.value) < 60) {
          return
        }
        showRegionSelector.value = currentScrollPosition < lastScrollPosition.value
        lastScrollPosition.value = currentScrollPosition
        document.body.classList.toggle('region-visible', showRegionSelector.value)
      }

      const toggleBodyClass = () => {
        document.body.classList.toggle('mobile', $utils.isMobile())
        document.body.classList.toggle('tablet', $utils.isTablet())
      }
      toggleBodyClass()
      const checkSize = () => {
        initMobile.value = $utils.isMobile() || $utils.isTablet()
        checkMobileOrTablet.value = $utils.isMobile() || $utils.isTablet()
        toggleBodyClass()
      }

      onMounted(() => {
        checkSize()
      })

      onBeforeMount(() => {
        window.addEventListener('resize', checkSize, {passive: true})
        window.addEventListener('scroll', onScroll, {passive: true})
      })

      onBeforeUnmount(() => {
        window.removeEventListener('resize', checkSize)
        window.removeEventListener('scroll', onScroll)
      })
    }

    const mainRoute = computed({get: () => `/${currentRegion.value}/${currentLanguage.value}`, set: () => {}})

    const selectActive = (_items) => {
      let isActive = false
      if (_items?.length) {
        _items.map((x) => {
          if ((x?.link?.url && $utils.isCurrentActivePage(x?.link?.url, $router?.currentRoute?.path)) ||
            (x.items && selectActive(x.items)) ||
            (x.rows && selectActive(x.rows)) ||
            (x.columns && selectActive(x.columns))
          ) {
            x.isActive = isActive = true
          }
          return x
        })
      }
      return isActive
    }

    const {fetch} = useFetch(async () => {
      if ($regions?.current?.name) {
        try {
          const snav = await $services.bobcatapi.fetchNavigation($regions?.current?.name)
          selectActive(snav)
          items.value = snav
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error(`[MainHeader] Unable to fetch subnavigation, ${e}`)
        }
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    return {
      checkMobileOrTablet,
      mobile,
      tablet,
      desktop,
      showRegionSelector,
      isSearchVisible,
      lastScrollPosition,
      logoUrl,
      topNavItems,
      headerHeight,
      headerStyle,
      inEditorEdit,
      initMobile,
      currentLanguage,
      currentRegion,
      mainRoute,
      regionSelectorLink,
      items,
      sidebarToggle,
      isOpened,
      ga4click,
      hasCompactLayout,
      navClasses,
      navStyles,
      searchClasses,
      isCollapsedNav,
      isSecondaryNavOpen,
      usesInlineSecondaryNav,
      hamburgerIcon,
      onHamburgerClick
    }
  },
  head() {
    return {
      link: [
        {
          rel: 'preload',
          as: 'image',
          fetchpriority: 'highest',
          href: this?.logoUrl
        }
      ]
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';
@import '{.border-right} from ~bootstrap/scss/bootstrap';

header[data-test-id="main-header"] {
  @include media-breakpoint-up(lg) {
    z-index: $zindex-modal-backdrop !important;
  }

  nav {
    &.region {
      &-visible {
        opacity: 1;
        height: 3rem;
      }

      &-invisible {
        opacity: 0;
        height: 0;
        padding: 0;
      }
    }

    .region-selector-picker {
      &:hover {
        i {
          color: $primary;
        }
      }

      i {
        color: $white;
      }
    }

    .upper-links, .region-selector {
      max-width: 16rem;
      line-height: 1rem;
    }

    button {
      border: none;
      outline: none;
      background: none;
      cursor: pointer;

      span {
        margin-left: .5rem;
      }

      &:focus-visible {
        outline: 0.125rem solid $white;
        outline-offset: -0.125rem;
      }
    }

    // the button/span margin above was written for the top utility bar's icon
    // buttons, but being unscoped it also reaches the mega-list toggle buttons
    // nested further down this header - undo it there so rows stay aligned
    // with the sibling leaf-link rows (which are plain <a>, not <button>)
    .megalist-column button span {
      margin-left: 0;
    }
  }

  .nav-link.mid-border {
    position: relative;

    &::after {
      @extend .border-right;
      border-right-width: 2px!important;
      border-color: $dark!important;
      position: absolute;
      content: '';
      height: 2rem;
      width: 100%;
    }
  }

  &.zindex-modal-backdrop {
    z-index: $zindex-modal-backdrop;
  }

  &:has(.search-wrapper:focus-within) {
    z-index: $zindex-modal !important;
  }

  [data-test-id="main-header-search"] {
    pointer-events: none;

    form,
    input,
    button,
    .search-wrapper,
    .search-panel,
    .input-group,
    .overlay {
      pointer-events: auto;
    }
  }

  .icon-small {
    font-size: 0.75rem;
  }
}

body.anchor-alternative-nav-sticky-active {
  header[data-test-id="main-header"] {
    &.zindex-modal-backdrop {
      background-color: transparent !important;
      box-shadow: none !important;
      z-index: $zindex-modal !important;
    }

    &:has(.search-wrapper:focus-within) {
      background-color: transparent !important;
      box-shadow: none !important;
    }

    nav.main-nav {
      pointer-events: none;
      visibility: hidden;
    }

    [data-test-id^="main-header-sub-nav-mobile-link"] {
      pointer-events: none;
      visibility: hidden;
    }
  }
}

.header-space {
  margin-bottom: 153px;

  @include media-breakpoint-up(md) {
    margin-bottom: 105px;
  }

  @include media-breakpoint-up(lg) {
    margin-bottom: 120px;
  }
}

.header-space.header-space--collapsed-nav {
  margin-bottom: $collapsed-nav-header-height;
}

@media (min-width: 992px) {
  .fs-nav {
    font-size: 1.125rem !important;
    white-space: nowrap !important;
  }
}

</style>
