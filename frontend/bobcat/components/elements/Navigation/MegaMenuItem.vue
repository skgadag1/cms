<template>
  <div v-if="filteredRows && filteredRows.length"
       :id="`megamenu-panel-${item.id}`"
       :data-test-id="`megamenu-item__${item.id}`"
       :class="{'megamenu-hidden': activeMenu !== item.id}"
       :aria-hidden="activeMenu !== item.id ? 'true' : 'false'"
       :aria-label="parentLabel ? `${parentLabel} submenu` : 'Submenu'"
       role="region"
       class="py-5 px-4 transition-fade"
  >
    <b-row v-for="(row) in filteredRows" :key="`main-navigation-desktop-row__${row.id}`">
      <b-col v-for="i in 3"
             :key="`main-navigation-desktop-col__${row.id}-${i}`"
             cols="4"
      >
        <template v-for="col in row.columns.filter(c => c.position === `column${i}`)">
          <div :key="`main-navigation-desktop-item-title__${row.id}-${col.id}`"
               class="h6 text-uppercase border-bottom pl-3 pb-2 mb-0 d-flex align-items-center justify-content-between"
          >
            <span>{{ col.label }}</span>
            <NavigationIndicator v-if="col.badge" :text="col.badge" />
          </div>

          <ul :key="`main-navigation-desktop-item-list__${row.id}-${col.id}`"
              class="list-unstyled"
              role="menu"
              :aria-label="col.label"
          >
            <div v-for="(itm, idx) in col.items"
                 :key="`main-navigation-subitem__${col.id}-${itm.id}`"
                 class="dropdown-item-row d-flex align-items-center justify-content-between"
                 role="presentation"
            >
              <a :href="$utils.getPageUrl(itm.link, $regions.current.language.name)"
                 :target="itm.link ? (itm.link.target === '_blank_follow' ? '_blank' : itm.link.target || '_self') : (itm.target === '_blank_follow' ? '_blank' : itm.target || '_self')"
                 :rel="(itm.link ? itm.link.target : itm.target) === '_blank' ? 'noopener nofollow' : null"
                 :class="{ 'active': itm.isActive }"
                 :data-test-id="`subnavigation-megamenu-lvl3-${item.id}__${idx}`"
                 :aria-label="`${itm.label} ${parentLabel}`"
                 class="dropdown-item flex-grow-1 border-0 bg-transparent menu-lvl3 position-relative text-wrap"
                 @click="$ga4.events.navigation_click($event, {navigation_hierarchy: item.label, navigation_location: 'megamenu-section'})"
              >{{ itm.label }}</a>
              <span class="d-flex align-items-center flex-shrink-0">
                <NavigationIndicator v-if="itm.badge" :text="itm.badge" />
                <b-icon v-if="itm.link && itm.link.external" icon="launch" class="icon-small ml-2" />
              </span>
            </div>
          </ul>
        </template>
      </b-col>
    </b-row>
  </div>
</template>

<script>
import {computed, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  components: {
    NavigationIndicator: () => import('@/components/elements/Navigation/NavigationIndicator')
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    activeMenu: {
      type: [String, null]
    },
    parentLabel: {
      type: [String, null]
    }
  },
  setup(props) {
    const {$ga4} = useContext()

    const filterItems = (items) => {
      return items
        .filter(item => !item.hideFromNavigation)
        .map(item => ({
          ...item,
          columns: item.columns
            ? item.columns.filter(col => !col.hideFromNavigation).map(col => ({
              ...col,
              items: col.items ? filterItems(col.items) : []
            }))
            : []
        }))
    }

    const filteredRows = computed(() => {
      return props.item.rows
        ? props.item.rows
          .filter(row => !row.hideFromNavigation)
          .map(row => ({
            ...row,
            columns: row.columns
              ? row.columns
                .filter(col => !col.hideFromNavigation)
                .map(col => ({
                  ...col,
                  items: filterItems(col.items || [])
                }))
              : []
          }))
        : []
    })

    return {
      filteredRows,
      inEditorEdit,
      $ga4
    }
  }
}
</script>
