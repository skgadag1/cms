<template>
  <MegaListItemMobile v-if="item.type === 'item' && item.link"
                      :item="item"
                      :parent-label="path"
                      @click.native="$ga4.events.navigation_click($event, {navigation_hierarchy: path, navigation_location: 'sidebar-sublist-dropdown'})"
  />
  <MegaListItemMobile
    v-else-if="item.type === 'megaListColumn' && (!filteredItems || filteredItems.length === 0) && item.link"
    :item="item"
    :parent-label="path"
    @click.native="$ga4.events.navigation_click($event, {navigation_hierarchy: path, navigation_location: 'sidebar-sublist-dropdown'})"
  />

  <div v-else-if="filteredItems && level === 1">
    <MegaListItemMobile v-if="item.link && item.link.url"
                        :item="item"
                        :parent-label="path"
                        highlight
                        @click.native="$ga4.events.navigation_click($event, {navigation_hierarchy: path, navigation_location: 'sidebar-sublist'})"
    />
    <ul class="list-unstyled mb-0">
      <li v-for="(itm1, itmIdx) in filteredItems" :key="`subnavigation-mobile-lvl${level + 1}-${itmIdx}`">
        <CollapsibleColumnMobile :item="itm1"
                                 :level="level + 1"
                                 :path="path"
        />
      </li>
    </ul>
  </div>

  <div v-else>
    <template v-if="filteredItems && filteredItems.length">
      <button :key="`subnavigation-mobile-subitem-title__${item.id}`"
              v-b-toggle="`subnavigation-mobile-${item.id}`"
              type="button"
              class="col-12 mb-0 py-3 d-flex align-items-center justify-content-between menu-lvl2 border-0 bg-transparent w-100"
              :class="{ 'active': item.isActive }"
              @click="$ga4.events.navigation_click($event, {navigation_hierarchy: path, navigation_location: 'sidebar-sublist'})"
      >
        <span>{{ item.label }}</span>
        <span class="d-flex align-items-center">
          <NavigationIndicator v-if="item.badge" :text="item.badge" />
          <b-icon icon="sort-down" class="dropdown ml-2" />
        </span>
      </button>

      <b-collapse :id="`subnavigation-mobile-${item.id}`"
                  :key="`subnavigation-mobile-subitems__${item.id}`"
                  :accordion="`subnavigation-mobile-lvl${level}`"
                  class="pl-3"
      >
        <MegaListItemMobile v-if="item.link && item.link.url"
                            :item="item"
                            :parent-label="path"
                            highlight
                            @click.native="$ga4.events.navigation_click($event, {navigation_hierarchy: path + ' | ' + item.label, navigation_location: 'sidebar-sublist-dropdown'})"
        />

        <ul class="list-unstyled mb-0">
          <li v-for="(itm2, itmIdx) in filteredItems" :key="`subnavigation-mobile-lvl${level + 1}-${itmIdx}`">
            <CollapsibleColumnMobile :item="itm2"
                                     :level="level + 1"
                                     :path="path + ' | ' + item.label"
            />
          </li>
        </ul>
      </b-collapse>
    </template>
  </div>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import useNavigation from '~/helpers/composables/useNavigation'

export default {
  name: 'CollapsibleColumnMobile',
  components: {
    NavigationIndicator: () => import('@/components/elements/Navigation/NavigationIndicator')
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    level: {
      type: Number,
      default: 1
    },
    path: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const {filterHiddenNavigationItems} = useNavigation()
    const filteredItems = computed(() => filterHiddenNavigationItems(props.item?.items || []))

    return {filteredItems}
  }
}
</script>
