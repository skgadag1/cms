<template>
  <LazyHydrate when-idle :trigger-hydration="init">
    <BSidebar :id="id"
              ref="sidebarRef"
              backdrop
              shadow
              no-header
              bg-variant="white"
              class="d-lg-none position-relative"
              data-test-id="subnavigation-mobile"
              role="navigation"
              aria-label="Main mobile"
              @change="$emit('change', $event)"
    >
      <ul class="list-unstyled mb-0">
        <li v-for="(item, idx) in filteredItems"
            :key="`subnavigation-mobile-item__${item.id}`"
            class="border-bottom border-light"
        >
          <span v-if="item.type === 'linkMenu'"
                class="col-12 m-0 py-3 text-uppercase h5 text-dark d-flex align-items-center justify-content-between menu-lvl1"
          >
            <a :class="{ 'active': item.isActive }"
               class="flex-grow-1 text-dark text-decoration-none d-flex align-items-center justify-content-between"
               :href="getPageUrl(item.link, $regions.current.language.name)"
               :target="item.link ? (item.link.target === '_blank_follow' ? '_blank' : item.link.target || '_self') : (item.target === '_blank_follow' ? '_blank' : item.target || '_self')"
               :rel="(item.link ? item.link.target : item.target) === '_blank' ? 'noopener nofollow' : null"
               :data-test-id="`subnavigation-mobile-lvl1-${idx}__${item.id}`"
               @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar'})"
            >
              <span class="flex-grow-1">{{ item.label }}</span>
              <NavigationIndicator v-if="item.badge" :text="item.badge" />
            </a>
          </span>

          <template v-else>
            <button v-b-toggle="`subnavigation-mobile-${idx}__${item.id}`"
                    type="button"
                    class="h5 col-12 m-0 py-3 text-uppercase d-flex align-items-center justify-content-between menu-lvl1 border-0 text-left w-100 subnavigation-mobile-toggle-btn"
                    :class="{ 'active': item.isActive }"
                    @click="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar'})"
            >
              <span>{{ item.label }}</span>
              <span class="d-flex align-items-center">
                <NavigationIndicator v-if="item.badge"
                                     :text="item.badge"
                                     @click.stop="item.link && navigateToItemLink(item)"
                />
                <b-icon icon="sort-down" class="dropdown ml-2" />
              </span>
            </button>

            <a v-if="item.link"
               :ref="`item-link-${item.id}`"
               :href="getPageUrl(item.link, $regions.current.language.name)"
               :target="item.link ? (item.link.target === '_blank_follow' ? '_blank' : item.link.target || '_self') : (item.target === '_blank_follow' ? '_blank' : item.target || '_self')"
               :rel="(item.link ? item.link.target : item.target) === '_blank' ? 'noopener nofollow' : null"
               class="d-none"
               tabindex="-1"
               @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar'})"
            />

            <b-collapse :id="`subnavigation-mobile-${idx}__${item.id}`" accordion="subnavigation-mobile-lvl1" @shown="toggleShadowBasedOnScroll" @hidden="toggleShadowBasedOnScroll">
              <template v-if="item.type === 'listMenu'">
                <ul class="pl-3 list-unstyled mb-0">
                  <li v-for="itm2 in item.itemsFiltered" :key="`subnavigation-mobile-subitem__${itm2.id}`">
                    <div class="col-12 mb-0 py-3 text-dark d-flex align-items-center justify-content-between menu-lvl2">
                      <a :class="{ 'active': itm2.isActive }"
                         class="flex-grow-1 text-dark text-decoration-none"
                         :href="getPageUrl(itm2.link, $regions.current.language.name)"
                         :target="itm2.link ? (itm2.link.target === '_blank_follow' ? '_blank' : itm2.link.target || '_self') : (itm2.target === '_blank_follow' ? '_blank' : itm2.target || '_self')"
                         :rel="(itm2.link ? itm2.link.target : itm2.target) === '_blank' ? 'noopener nofollow' : null"
                         :data-test-id="`subnavigation-mobile-lvl2__${itm2.id}`"
                         :aria-label="`${itm2.label} ${item.label}`"
                         @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist', navigation_hierarchy: item.label})"
                      >{{ itm2.label }}</a>
                      <span class="d-flex align-items-center flex-shrink-0">
                        <template v-if="itm2.link">
                          <a
                            :href="getPageUrl(itm2.link, $regions.current.language.name)"
                            :target="itm2.link ? (itm2.link.target === '_blank_follow' ? '_blank' : itm2.link.target || '_self') : (itm2.target === '_blank_follow' ? '_blank' : itm2.target || '_self')"
                            :rel="(itm2.link ? itm2.link.target : itm2.target) === '_blank' ? 'noopener nofollow' : null"
                            class="text-decoration-none d-flex align-items-center"
                            @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist', navigation_hierarchy: item.label})"
                          >
                            <NavigationIndicator v-if="itm2.badge" :text="itm2.badge" />
                            <b-icon v-if="itm2.link && itm2.link.external" icon="launch" class="icon-small ml-2" />
                          </a>
                        </template>
                        <template v-else>
                          <NavigationIndicator v-if="itm2.badge" :text="itm2.badge" />
                          <b-icon v-if="itm2.link && itm2.link.external" icon="launch" class="icon-small ml-2" />
                        </template>
                      </span>
                    </div>
                  </li>
                </ul>
              </template>
              <ul v-if="item.type === 'megaList'" class="list-unstyled mb-0">
                <li v-for="(itm, itmIdx) in item.itemsFiltered" :key="`subnavigation-mobile-subitem-list-${itmIdx}`">
                  <CollapsibleColumnMobile
                    :item="itm"
                    :level="1"
                    class="ml-3"
                    :path="item.label"
                  />
                </li>
              </ul>

              <ul v-else class="list-unstyled mb-0">
                <template v-for="(row, rowIdx) in item.rowsFiltered">
                  <li v-for="(col, colIdx) in row.columnsFiltered"
                      :key="`subnavigation-mobile-subitem-title__${rowIdx}-${colIdx}__${col.id}`"
                  >
                    <div class="pl-3">
                      <div v-b-toggle="`subnavigation-mobile-${col.id}`"
                           class="col-12 mb-0 py-3 d-flex align-items-center justify-content-between menu-lvl2"
                           @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist', navigation_hierarchy: item.label })"
                      >
                        <span>{{ col.label }}</span>
                        <span class="d-flex align-items-center">
                          <template v-if="col.link">
                            <a
                              :href="getPageUrl(col.link, $regions.current.language.name)"
                              :target="col.link ? (col.link.target === '_blank_follow' ? '_blank' : col.link.target || '_self') : (col.target === '_blank_follow' ? '_blank' : col.target || '_self')"
                              :rel="(col.link ? col.link.target : col.target) === '_blank' ? 'noopener nofollow' : null"
                              class="text-decoration-none d-flex align-items-center"
                              @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist', navigation_hierarchy: item.label })"
                            >
                              <NavigationIndicator v-if="col.badge" :text="col.badge" />
                              <b-icon icon="sort-down" class="dropdown ml-2" />
                            </a>
                          </template>
                          <template v-else>
                            <NavigationIndicator v-if="col.badge" :text="col.badge" />
                            <b-icon icon="sort-down" class="dropdown ml-2" />
                          </template>
                        </span>
                      </div>

                      <b-collapse :id="`subnavigation-mobile-${col.id}`"
                                  :key="`subnavigation-mobile-subitems__${col.id}`"
                                  accordion="subnavigation-mobile-lvl2"
                      >
                        <ul class="list-unstyled mb-0">
                          <li v-for="(itm3, itm3Idx) in col.itemsFiltered"
                              :key="`subnavigation-mobile-lvl3-${rowIdx}-${colIdx}__${itm3Idx}`"
                              class="pl-3"
                          >
                            <div class="col-12 py-3 text-dark d-flex align-items-center justify-content-between menu-lvl3">
                              <a
                                class="flex-grow-1 text-dark text-decoration-none"
                                :class="{ 'active': itm3.isActive }"
                                :href="getPageUrl(itm3.link, $regions.current.language.name)"
                                :target="itm3.link ? (itm3.link.target === '_blank_follow' ? '_blank' : itm3.link.target || '_self') : (itm3.target === '_blank_follow' ? '_blank' : itm3.target || '_self')"
                                :rel="(itm3.link ? itm3.link.target : itm3.target) === '_blank' ? 'noopener nofollow' : null"
                                :data-test-id="`subnavigation-mobile-lvl3-${rowIdx}__${itm3Idx}`"
                                :aria-label="`${itm3.label} ${item.label}`"
                                @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist-dropdown', navigation_hierarchy: item.label + ' | ' + col.label})"
                              >{{ itm3.label }}</a>
                              <span class="d-flex align-items-center flex-shrink-0">
                                <template v-if="itm3.link">
                                  <a
                                    :href="getPageUrl(itm3.link, $regions.current.language.name)"
                                    :target="itm3.link ? (itm3.link.target === '_blank_follow' ? '_blank' : itm3.link.target || '_self') : (itm3.target === '_blank_follow' ? '_blank' : itm3.target || '_self')"
                                    :rel="(itm3.link ? itm3.link.target : itm3.target) === '_blank' ? 'noopener nofollow' : null"
                                    class="text-decoration-none d-flex align-items-center"
                                    @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-sublist-dropdown', navigation_hierarchy: item.label + ' | ' + col.label})"
                                  >
                                    <NavigationIndicator v-if="itm3.badge" :text="itm3.badge" />
                                    <b-icon v-if="itm3.link && itm3.link.external" icon="launch" class="icon-small ml-2" />
                                  </a>
                                </template>
                                <template v-else>
                                  <NavigationIndicator v-if="itm3.badge" :text="itm3.badge" />
                                  <b-icon v-if="itm3.link && itm3.link.external" icon="launch" class="icon-small ml-2" />
                                </template>
                              </span>
                            </div>
                          </li>
                        </ul>
                      </b-collapse>
                    </div>
                  </li>
                </template>
              </ul>
            </b-collapse>
          </template>
        </li>
      </ul>

      <ul v-if="quickLinksItems.length" class="list-unstyled mb-0 sub-nav-quick-links" data-test-id="subnavigation-mobile-quick-links">
        <li v-for="(quickLink, idx) in quickLinksItems" :key="`subnavigation-mobile-quick-link__${idx}`">
          <a class="d-flex align-items-center text-white text-decoration-none px-3 py-2"
             :href="quickLink.url"
             :target="quickLink.target === '_blank_follow' ? '_blank' : quickLink.target"
             :rel="quickLink.target === '_blank' ? 'noopener nofollow' : null"
             :data-test-id="`subnavigation-mobile-quick-link-${idx}`"
             @click.stop="$ga4?.events?.navigation_click($event, {navigation_location: 'sidebar-quick-links'})"
          >
            <b-icon v-if="quickLink.icon !== 'none'" :icon="quickLink.icon" />
            <span class="ml-2 font-weight-bold text-uppercase">{{ quickLink.label }}</span>
          </a>
        </li>
      </ul>

      <div ref="overlayRef" :class="showShadow ? 'opacity-5' : 'opacity-0'" class="w-100 transition-base sub-nav-overlay position-fixed" />
    </BSidebar>
  </LazyHydrate>
</template>

<script>
import {computed, getCurrentInstance, ref, useContext, useRouter, watch} from '@nuxtjs/composition-api'
import {BSidebar} from 'bootstrap-vue'
import useNavigation from '~/helpers/composables/useNavigation'

export default {
  components: {
    BSidebar,
    NavigationIndicator: () => import('@/components/elements/Navigation/NavigationIndicator'),
    CollapsibleColumnMobile: () => import('@/components/elements/Navigation/CollapsibleColumnMobile')
  },
  props: ['id', 'items', 'init', 'quickLinks', 'regionLink'],
  setup(props) {
    const {$utils, $regions} = useContext()
    const vm = getCurrentInstance()
    const router = useRouter()
    const currentPath = router.currentRoute.path
    const overlayRef = ref(null)
    const sidebarRef = ref(null)
    const showShadow = ref(false)
    const lastScrollPosition = ref(0)

    const {filterHiddenMobileNavigationItems} = useNavigation()
    const filteredItems = computed(() => filterHiddenMobileNavigationItems(props.items || []))

    const navigateToItemLink = (item) => {
      const linkRef = vm?.proxy?.$refs?.[`item-link-${item.id}`]
      const el = Array.isArray(linkRef) ? linkRef[0] : linkRef
      if (el && typeof el.click === 'function') { el.click() }
    }

    const quickLinksItems = computed(() => [
      ...(props.quickLinks || []),
      ...(props.regionLink?.url ? [{...props.regionLink, icon: 'globe'}] : [])
    ].filter(link => link?.url))

    const getPageUrl = (_p) => {
      return $utils?.getPageUrl(_p, $regions.current.language.name) || ''
    }

    const checkIfScrollIsOnTheEnd = (e) => {
      const threshold = 5
      const isAlmostAtEnd = e.offsetHeight + e.scrollTop + threshold >= e.scrollHeight
      showShadow.value = !isAlmostAtEnd
    }

    const adjustOverlayPosition = () => {
      if (!overlayRef.value) { return }

      const currentScrollPosition = window.pageYOffset || document.documentElement.scrollTop

      if (Math.abs(currentScrollPosition - lastScrollPosition.value) < 60) {
        return
      }

      if (currentScrollPosition < lastScrollPosition.value || currentScrollPosition <= 48) {
        overlayRef.value.style.bottom = '98px'
      } else {
        overlayRef.value.style.bottom = '50px'
      }

      lastScrollPosition.value = currentScrollPosition
    }

    const toggleShadowBasedOnScroll = () => {
      const container = document.querySelector('div.b-sidebar-body')
      showShadow.value = container.scrollHeight > container.clientHeight && container.scrollHeight - container.scrollTop > container.clientHeight
    }

    if (process.client) {
      watch(sidebarRef, (newValue, oldValue) => {
        const container = document.querySelector('div.b-sidebar-body')
        if (newValue) {
          toggleShadowBasedOnScroll()
          container.addEventListener('scroll', () => checkIfScrollIsOnTheEnd(container))
          window.addEventListener('scroll', adjustOverlayPosition)
          window.addEventListener('resize', toggleShadowBasedOnScroll)
        }
        if (oldValue) {
          container.removeEventListener('scroll', () => checkIfScrollIsOnTheEnd(container))
          window.removeEventListener('scroll', adjustOverlayPosition)
          window.removeEventListener('resize', toggleShadowBasedOnScroll)
        }
      }, {immediate: true, deep: true})

      watch(overlayRef, () => {
        adjustOverlayPosition()
      })
    }

    return {
      getPageUrl,
      currentPath,
      overlayRef,
      sidebarRef,
      showShadow,
      filteredItems,
      quickLinksItems,
      toggleShadowBasedOnScroll,
      navigateToItemLink
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';
@import '~bootstrap/scss/close';

.b-sidebar {
  &,
  &-backdrop {
    top: 106px;
    padding-bottom: 106px;
    transition: all 0.2s ease;
  }

  .h5.active,
  .h6.active,
  div.active,
  a.active {
    position: relative;
    border-left: 3px solid $primary;
    background: inherit;
  }

  .h5.not-collapsed,
  .h6.not-collapsed,
  div.not-collapsed,
  button.not-collapsed,
  a.not-collapsed {
    background-color: $gray-100;
  }

  .subnavigation-mobile-toggle-btn {
    background-color: transparent;
  }

  .sub-nav-quick-links {
    background-color: $gray-800;

    li + li a {
      border-top: 1px solid $gray-600;
    }

    a {
      font-size: .875rem;
      letter-spacing: .3px;
      line-height: 1.3125rem;
    }
  }

  .sub-nav-overlay {
    box-shadow: $dropdown-overlay;
    bottom: 98px;
    right: 0;
    height: 50px;
  }
}

.region-invisible ~ .b-sidebar-outer {
  .b-sidebar,
  .b-sidebar-backdrop {
    top: 57px;
    padding-bottom: 57px;
  }
}
</style>
