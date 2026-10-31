<template>
  <nav class="main-nav d-none d-lg-flex bg-white justify-content-center z-index-0 flex-column"
       role="navigation"
       aria-label="Main desktop"
  >
    <div class="navbar py-0 container justify-content-center">
      <ul class="d-none d-md-flex align-items-center m-0 p-0 subnavigation-main" data-test-id="subnavigation-main">
        <li v-for="(item, i) in filteredItems"
            :key="`main-navigation__item-${item.id}`"
            :class="{ 'active': item.isActive, 'opened': activeMenu === item.id }"
            :data-test-id="`sn-${item.type === 'linkMenu' ? 'link' : (item.type === 'listMenu' ? 'dropdown' : (item.type === 'megaMenu' ? 'megamenu' : 'megalist'))}-${item.id}`"
            class="text-black d-flex align-items-center dropdown-nav-link"
        >
          <span v-if="item.type === 'linkMenu'"
                class="d-flex align-items-center h-100"
          >
            <a :href="$utils.getPageUrl(item.link, $regions.current.language.name)"
               :target="item.link ? (item.link.target === '_blank_follow' ? '_blank' : item.link.target || '_self') : (item.target === '_blank_follow' ? '_blank' : item.target || '_self')"
               :rel="(item.link ? item.link.target : item.target) === '_blank' ? 'noopener nofollow' : null"
               :disabled="inEditorEdit"
               class="d-flex align-items-center p-3 text-black text-decoration-none h-100"
               @click="$ga4.events.navigation_click($event, {navigation_location: 'header'})"
            >
              <span class="text-uppercase text-center py-1 font-weight-bold">{{ item.label }}</span>
            </a>
            <NavigationIndicator v-if="item.badge" :text="item.badge" />
          </span>

          <b-dropdown v-else-if="item.type === 'listMenu'"
                      variant="transparent"
                      toggle-class="d-flex align-items-center p-3 btn-narrow"
                      :disabled="inEditorEdit"
                      @show="showActiveMenu(item.id, $event)"
                      @hide="hideActiveMenu(item.id, $event)"
          >
            <template #button-content>
              <span class="text-uppercase m-0 pr-2 py-1 font-weight-bold d-flex align-items-center" :class="{'text-dark' : inEditorEdit}">
                {{ item.label }}
                <NavigationIndicator v-if="item.badge" :text="item.badge" />
              </span>
            </template>

            <template v-if="!inEditorEdit">
              <div v-for="(j, idx) in item.itemsFiltered"
                   :key="`main-navigation-subitem__${j.id}`"
                   class="dropdown-item-row d-flex align-items-center justify-content-between border-bottom"
                   role="presentation"
              >
                <a :href="$utils.getPageUrl(j.link, $regions.current.language.name)"
                   :target="j.link ? (j.link.target === '_blank_follow' ? '_blank' : j.link.target || '_self') : (j.target === '_blank_follow' ? '_blank' : j.target || '_self')"
                   :rel="(j.link ? j.link.target : j.target) === '_blank' ? 'noopener nofollow' : null"
                   :class="{ 'active': j.isActive }"
                   :data-test-id="`subnavigation-dropdown-${i}__${idx}`"
                   :aria-label="`${j.label} ${item.label}`"
                   class="dropdown-item flex-grow-1 border-0 bg-transparent text-truncate"
                   @click="$ga4.events.navigation_click($event, {navigation_hierarchy: item.label, navigation_location: 'header'})"
                >{{ j.label }}</a>
                <a
                  :href="$utils.getPageUrl(j.link, $regions.current.language.name)"
                  :target="j.link ? (j.link.target === '_blank_follow' ? '_blank' : j.link.target || '_self') : (j.target === '_blank_follow' ? '_blank' : j.target || '_self')"
                  :rel="(j.link ? j.link.target : j.target) === '_blank' ? 'noopener nofollow' : null"
                  class="d-flex align-items-center flex-shrink-0 text-decoration-none"
                  @click="$ga4.events.navigation_click($event, {navigation_hierarchy: item.label, navigation_location: 'header'})"
                >
                  <NavigationIndicator v-if="j.badge" :text="j.badge" />
                  <b-icon v-if="j.link && j.link.external" icon="launch" class="icon-small ml-2" />
                </a>
              </div>
            </template>

            <b-dropdown-item
              v-if="item.link && item.link.url"
              :key="`main-navigation-subitem__${item.id}`"
              :href="$utils.getPageUrl(item.link, $regions.current.language.name)"
              :target="item.link ? (item.link.target === '_blank_follow' ? '_blank' : item.link.target || '_self') : (item.target === '_blank_follow' ? '_blank' : item.target || '_self')"
              :rel="(item.link ? item.link.target : item.target) === '_blank' ? 'noopener nofollow' : null"
              :disabled="inEditorEdit"
              :link-class="{ 'active': $utils.getPageUrl(item.link, $regions.current.language.name).endsWith(currentPath) }"
              :data-test-id="`subnavigation-dropdown-${i}__customlink`"
              class="text-uppercase font-weight-bold border-bottom"
              @click.native="$ga4.events.navigation_click($event, {navigation_hierarchy: item.label, navigation_location: 'header'})"
            >
              {{ item.label }}
            </b-dropdown-item>
          </b-dropdown>

          <button v-else-if="item.type === 'megaMenu'"
                  :ref="`toggle-${item.id}`"
                  type="button"
                  class="d-flex align-items-center p-3 h-100"
                  :disabled="inEditorEdit"
                  :aria-expanded="activeMenu === item.id ? 'true' : 'false'"
                  aria-haspopup="dialog"
                  :aria-controls="`megamenu-panel-${item.id}`"
                  @click="toggleActiveMenuWithFocus(item.id, $event)"
                  @keydown.esc.prevent="closeActiveMenu(item.id)"
          >
            <span class="text-uppercase m-0 pr-2 py-1 font-weight-bold" :class="{'text-dark' : inEditorEdit}">{{ item.label }}</span>
            <NavigationIndicator v-if="item.badge" :text="item.badge" />
            <div class="d-flex align-items-center">
              <b-icon icon="sort-down" class="dropdown" />
            </div>
          </button>

          <button v-else-if="item.type === 'megaList'"
                  :ref="`toggle-${item.id}`"
                  type="button"
                  class="d-flex align-items-center p-3 h-100"
                  :disabled="inEditorEdit"
                  :aria-expanded="activeMenu === item.id ? 'true' : 'false'"
                  aria-haspopup="dialog"
                  :aria-controls="`megalist-panel-${item.id}`"
                  @click="toggleActiveMenuWithFocus(item.id, $event)"
                  @keydown.esc.prevent="closeActiveMenu(item.id)"
          >
            <span class="text-uppercase m-0 pr-2 py-1 font-weight-bold" :class="{'text-dark' : inEditorEdit}">{{ item.label }}</span>
            <NavigationIndicator v-if="item.badge" :text="item.badge" />
            <div class="d-flex align-items-center">
              <b-icon icon="sort-down" class="dropdown" />
            </div>
          </button>
        </li>
      </ul>
    </div>

    <section v-if="!inEditorEdit"
             class="container z-index-0 position-relative"
             @keydown.esc.prevent="closeActiveMenuFromPanel"
             @keydown="onPanelKeydown"
    >
      <MegaMenu :items="filteredItems.filter(itm => itm.type === 'megaMenu')"
                :active-menu="activeMenu"
                @close="closeActiveMenuFromPanel"
      />
      <MegaList :items="filteredItems.filter(itm => itm.type === 'megaList')"
                :active-menu="activeMenu"
                @close="closeActiveMenuFromPanel"
      />
    </section>
    <transition name="fade">
      <div v-if="!inEditorEdit && activeMenu" class="overlay z-index-n2" @click="activeMenu = null" />
    </transition>
  </nav>
</template>

<script>
import {computed, nextTick, ref, useContext, useRouter} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useNavigation from '~/helpers/composables/useNavigation'

export default {
  components: {
    NavigationIndicator: () => import('@/components/elements/Navigation/NavigationIndicator')
  },
  props: {
    items: {
      type: Array,
      required: true
    }
  },
  setup(props, {refs}) {
    const activeMenu = ref(null)
    const showTimer = ref(null)
    const router = useRouter()
    const {$ga4} = useContext()
    const currentPath = router.currentRoute.path

    const {filterHiddenNavigationItems} = useNavigation()
    const filteredItems = computed(() => filterHiddenNavigationItems(props.items || []))

    const hideActiveMenu = (id, event) => {
      $ga4?.events?.navigation_click(event, {navigation_location: 'header', navigation_action: 'collapse'})
      clearTimeout(showTimer.value)
      showTimer.value = setTimeout(() => {
        activeMenu.value = activeMenu.value === id ? null : activeMenu.value
      }, 100)
    }

    const showActiveMenu = (id, event) => {
      $ga4?.events?.navigation_click(event, {
        navigation_location: 'header',
        navigation_action: activeMenu.value === id ? 'collapse' : 'expand'
      })
      clearTimeout(showTimer.value)
      activeMenu.value = activeMenu.value === id ? null : id
    }

    const toggleActiveMenu = (id, event) => {
      showActiveMenu(id, event)
    }

    const focusToggle = (id) => {
      const toggleRef = refs[`toggle-${id}`]
      const el = Array.isArray(toggleRef) ? toggleRef[0] : toggleRef
      el?.focus?.()
    }

    const closeActiveMenu = (id) => {
      if (activeMenu.value === id) {
        activeMenu.value = null
      }
      focusToggle(id)
    }

    const closeActiveMenuFromPanel = () => {
      const openId = activeMenu.value
      if (!openId) {
        return
      }
      activeMenu.value = null
      focusToggle(openId)
    }

    const getActivePanelEl = () => {
      const openId = activeMenu.value
      if (!openId) {
        return null
      }
      return (
        document.getElementById(`megamenu-panel-${openId}`) ||
        document.getElementById(`megalist-panel-${openId}`)
      )
    }

    const isElementVisible = (el) => {
      if (el.offsetParent === null && el.getClientRects().length === 0) {
        return false
      }
      const style = window.getComputedStyle(el)
      return style.visibility !== 'hidden' && style.display !== 'none'
    }

    const getFocusableItems = (panelEl) => {
      if (!panelEl || !isElementVisible(panelEl)) {
        return []
      }
      const nodes = panelEl.querySelectorAll('a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])')
      return Array.from(nodes).filter(isElementVisible)
    }

    const onPanelKeydown = (event) => {
      if (!activeMenu.value) {
        return
      }
      const {key} = event
      const navigationKeys = ['ArrowDown', 'ArrowUp', 'ArrowRight', 'ArrowLeft', 'Home', 'End']
      if (!navigationKeys.includes(key)) {
        return
      }

      const panelEl = getActivePanelEl()
      const items = getFocusableItems(panelEl)
      if (!items.length) {
        return
      }

      event.preventDefault()

      const currentIndex = items.indexOf(document.activeElement)
      let nextIndex = currentIndex

      switch (key) {
      case 'ArrowDown':
      case 'ArrowRight':
        nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % items.length
        break
      case 'ArrowUp':
      case 'ArrowLeft':
        nextIndex = currentIndex <= 0 ? items.length - 1 : currentIndex - 1
        break
      case 'Home':
        nextIndex = 0
        break
      case 'End':
        nextIndex = items.length - 1
        break
      }

      items[nextIndex]?.focus()
    }

    const focusFirstPanelItem = () => {
      nextTick(() => {
        requestAnimationFrame(() => {
          const items = getFocusableItems(getActivePanelEl())
          items[0]?.focus()
        })
      })
    }

    const toggleActiveMenuWithFocus = (id, event) => {
      const willOpen = activeMenu.value !== id
      toggleActiveMenu(id, event)
      const keyboardTriggered = event && event.detail === 0
      if (willOpen && keyboardTriggered) {
        focusFirstPanelItem()
      }
    }

    return {
      activeMenu,
      currentPath,
      filteredItems,
      inEditorEdit,
      showActiveMenu,
      hideActiveMenu,
      toggleActiveMenu,
      toggleActiveMenuWithFocus,
      closeActiveMenu,
      closeActiveMenuFromPanel,
      onPanelKeydown
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

$nav-bar-height: 4rem;

nav.main-nav {
  height: $nav-bar-height;

  a:focus-visible,
  button:focus-visible,
  .dropdown-toggle:focus-visible,
  .dropdown-item:focus-visible {
    outline: 0.125rem solid $primary;
    outline-offset: -0.125rem;
  }

  @supports not selector(:focus-visible) {
    a:focus,
    button:focus,
    .dropdown-toggle:focus,
    .dropdown-item:focus {
      outline: 0.125rem solid $primary;
      outline-offset: -0.125rem;
    }
  }

  a.dropdown-item,
  .dropdown-item a {
    &:focus,
    &:focus-visible,
    &:hover {
      color: inherit;
    }
  }

  .subnavigation-main {
    & > .dropdown {
      &-nav-link,
      &-item {
        &.active,
        & span.active {
          background: inherit;
          color: inherit;
          border-left: none;

          &:after {
            transform: scaleX(1);
            -ms-text-size-adjust: none;
            -moz-text-size-adjust: none;
            -webkit-text-size-adjust: none;
          }
        }
      }
    }
  }

  .dropdown {
    &-nav-link,
    &-item {
      position: relative;
      transition: $transition-base;
      border-left: 4px solid transparent;

      & > button > div.d-flex {
        -webkit-perspective: 500px;
      }

      &:after,
      & span:after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 8px;
        transition: $transition-base;
        transform: scaleX(0);
        background-color: $primary;
        -ms-text-size-adjust: none;
        -moz-text-size-adjust: none;
        -webkit-text-size-adjust: none;
      }

      &.menu-lvl2 {
        &:after {
          left: 1rem;
          right: 1rem;
        }
      }

      &.menu-lvl3 {
        &:after {
          height: 4px;
        }
      }

      & span.menu-lvl3 {
        &:after {
          height: 4px;
          bottom: -2px;
        }
      }

      &:hover {
        background-color: $gray-100 !important;

        & > a {
          color: inherit;
        }
      }

      &.active,
      & span.active {
        background: inherit;
        color: inherit;
        border-left-color: $primary !important;
      }

      &.opened {
        background-color: $gray-100;
      }

      &.opened button,
      &.opened button:focus {
        color: $primary !important;

        .dropdown.bobcat-icon-sort-down {
          transform: rotateX(180deg);
        }
      }
    }

    &-toggle {
      font-size: inherit !important;
      box-shadow: none !important;
      border: none;

      &:hover {
        box-shadow: none;
        background: $light;
      }
    }
  }

  .megamenu-section {
    overflow-y: auto;
    overflow-x: hidden;
  }

  .megamenu-section,
  .megalist-section {
    @include position-top;
    max-height: calc((100vh - 120px) * 0.75);

    .megamenu-hidden,
    .megalist-hidden {
      transform: scale(0) translateY(-100vh);
      position: absolute;
      height: 0;
      visibility: hidden;
      pointer-events: none;
    }
  }

  .megalist-section li.megalist-view-all:hover {
    background: transparent !important;
  }

  .megalist-section li.megalist-column-link:not(.megalist-view-all) {
    &:hover,
    &:focus-within {
      background-color: $gray-100 !important;
    }

    a {
      background: transparent !important;

      &:hover {
        background: transparent !important;
      }
    }
  }

  .megalist-section .dropdown-item {
    &:hover,
    &:focus,
    &:focus-visible {
      background-color: $gray-100 !important;
    }
  }

  .dropdown-item-row {
    &:hover,
    &:focus-within {
      background-color: $gray-100 !important;
    }

    a {
      background: transparent !important;

      &:hover {
        background: transparent !important;
      }
    }
  }

  .subnavigation-main > li.dropdown-nav-link {
    &:hover,
    &:focus-within {
      background-color: $gray-100 !important;
    }

    > span a,
    a {
      background: transparent !important;

      &:hover {
        background: transparent !important;
      }
    }
  }

  .subnavigation-main > li {
    height: $nav-bar-height;
  }
}
</style>
