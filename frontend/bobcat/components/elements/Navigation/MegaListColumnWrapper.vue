<template>
  <div v-if="filteredItems && filteredItems.length"
       :id="`megalist-panel-${list.id}`"
       :data-test-id="`megalist-item__${list.id}`"
       :class="{'megalist-hidden': activeMenu !== list.id}"
       :aria-hidden="activeMenu !== list.id ? 'true' : 'false'"
       :aria-label="parentLabel ? `${parentLabel} submenu` : 'Submenu'"
       role="region"
       class="megalist-column-wrapper transition-fade"
  >
    <div v-if="!inEditorEdit && filteredItems.length"
         class="row justify-content-start align-items-stretch flex-row position-relative transition-base h-100"
    >
      <template v-for="items_lvl1 in filteredItems">
        <MegaListColumn :key="`mega-list-column-lvl1-${items_lvl1.id}`"
                        :item="items_lvl1"
                        :active="true"
                        :opened-items="openedItems"
                        :parent-label="parentLabel"
                        @toggle="toggleSubmenu"
                        @link-click="ga4Click(true)"
        />
        <template v-for="items_lvl2 in items_lvl1.itemsFiltered">
          <MegaListColumn :key="`mega-list-column-lvl2-${items_lvl2.id}`"
                          :item="items_lvl2"
                          :active="openedItems.some(x => x === items_lvl2.id)"
                          :level="2"
                          :opened-items="openedItems"
                          :parent-label="parentLabel"
                          @toggle="toggleSubmenu"
                          @link-click="ga4Click"
          />

          <template v-for="items_lvl3 in items_lvl2.itemsFiltered">
            <MegaListColumn :key="`mega-list-column-lvl3-${items_lvl3.id}`"
                            :item="items_lvl3"
                            :active="openedItems.some(x => x === items_lvl3.id)"
                            :level="3"
                            :opened-items="openedItems"
                            :parent-label="parentLabel"
                            @toggle="toggleSubmenu"
                            @link-click="ga4Click"
            />

            <template v-for="items_lvl4 in items_lvl3.itemsFiltered">
              <MegaListColumn :key="`mega-list-column-lvl4-${items_lvl4.id}`"
                              :item="items_lvl4"
                              :active="openedItems.some(x => x === items_lvl4.id)"
                              :level="4"
                              :opened-items="openedItems"
                              :parent-label="parentLabel"
                              @toggle="toggleSubmenu"
                              @link-click="ga4Click"
              />
            </template>
          </template>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
import {computed, nextTick, ref, useContext, watch} from '@nuxtjs/composition-api'
import MegaListColumn from './MegaListColumn.vue'
import {inEditorEdit} from '~/helpers/vue-editor'
import {treeSearch} from '~/helpers/functional/AppHelpers'
import useNavigation from '~/helpers/composables/useNavigation'

const props = defineProps({
  list: {
    type: Object,
    required: true
  },
  activeMenu: {
    type: [String, null]
  },
  parentLabel: {
    type: [String, null]
  }
})

const {$ga4} = useContext()

const ga4Click = (lvl1 = false) => {
  $ga4?.events?.navigation_click(event, {
    navigation_hierarchy: [props.activeMenu, ...openedItems.value].map(id => treeSearch([props.list], id)?.label).join(' | '),
    navigation_location: lvl1 ? 'megalist-section' : 'megalist-column'
  })
}

const openedItems = ref([props.list.items?.[0]?.id || props.list.id])
const {filterHiddenNavigationItems} = useNavigation()
const filteredItems = computed(() => filterHiddenNavigationItems(props.list.items || []))

const toggleSubmenu = (id, parentId, lvl) => {
  const isOpen = openedItems.value.includes(id)
  const isParentOpen = openedItems.value.includes(parentId)

  if (!isOpen) {
    $ga4?.events?.navigation_click(event, {
      navigation_hierarchy: [props.activeMenu, ...openedItems.value].map(id => treeSearch([props.list], id)?.label).join(' | '),
      navigation_location: lvl === 1 ? 'megalist-section' : 'megalist-column',
      navigation_action: 'expand'
    })
  }

  if (isParentOpen) {
    openedItems.value = openedItems.value.slice(0, openedItems.value.indexOf(parentId) + 1)
    if (!isOpen) {
      openedItems.value.push(id)
    }
  } else if (isOpen) {
    openedItems.value = openedItems.value.slice(0, openedItems.value.indexOf(id))
  } else {
    openedItems.value.push(id)
  }

  if (isOpen) {
    $ga4?.events?.navigation_click(event, {
      navigation_hierarchy: [props.activeMenu, ...openedItems.value].map(id => treeSearch([props.list], id)?.label).join(' | '),
      navigation_location: lvl === 1 ? 'megalist-section' : 'megalist-column',
      navigation_action: 'collapse'
    })
  }

  nextTick(() => {
    requestAnimationFrame(() => {
      alignLeftPosition(props.activeMenu)
    })
  })
}

const alignLeftPosition = (val = props.activeMenu) => {
  let triggerEl = document.querySelector(`[data-test-id="sn-megalist-${val}"] button`)
  if (!triggerEl) {
    triggerEl = document.querySelector(`[data-test-id="sn-megalist-${val}"] a`)
  }
  if (!triggerEl) {
    triggerEl = document.querySelector(`[data-test-id="sn-megalist-${val}"]`)
  }

  const panelEl = document.querySelector(`[data-test-id="megalist-item__${val}"]`)

  if (!triggerEl || !panelEl) {
    return
  }

  const parentEl = panelEl.offsetParent || panelEl.parentElement
  if (!parentEl) {
    return
  }

  const parentRect = parentEl.getBoundingClientRect()
  const triggerRect = triggerEl.getBoundingClientRect()
  const panelWidth = panelEl.offsetWidth

  const padding = 8

  let left = triggerRect.left - parentRect.left

  const panelRight = triggerRect.left + panelWidth
  const viewportRight = window.innerWidth

  if (panelRight > viewportRight - padding) {
    left = triggerRect.right - parentRect.left - panelWidth

    const panelLeft = parentRect.left + left
    if (panelLeft < padding) {
      left = padding - parentRect.left
    }
  }

  panelEl.style.left = `${Math.round(left)}px`
}

watch(() => props.activeMenu, (val) => {
  if (val === props.list.id) {
    nextTick(() => {
      requestAnimationFrame(() => {
        alignLeftPosition(val)
      })
    })
  } else {
    openedItems.value = [props.list.items?.[0]?.id || props.list.id]
  }
})
</script>

<style lang="scss">
.megalist-column-wrapper {
  position: absolute;
  left: 0;
  max-height: calc(100vh - 120px);
  min-height: 200px;
  overflow: visible;

  .row {
    flex-wrap: nowrap;
    min-width: max-content;
  }
}

.megalist-column {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
