<template>
  <ul :id="`megalist-column__${item.id}`"
      class="overflow-auto flex-column megalist-column bg-white border-right px-0 transition-base mb-0"
      :class="[active ? 'd-flex' : 'd-none', `menu-lvl${level + 1}`]"
      :data-test-id="`megalist-column__${item.id}`"
      role="menu"
      tabindex="-1"
  >
    <li v-if="item.link && item.link.url"
        class="list-unstyled d-flex align-items-center p-3 megalist-column-link megalist-view-all"
        role="presentation"
    >
      <a class="font-weight-bold user-select-none"
         :class="{'active' : item.isActive}"
         :href="item.link.url"
         :aria-label="`navigation level ${level + 1}`"
         role="menuitem"
         @click="$emit('link-click')"
      >{{ item.link.label || item.label }}</a>
      <b-icon class="ml-1 text-primary flex-shrink-0" icon="chevron-right" />
      <NavigationIndicator v-if="item.badge" :text="item.badge" class="ml-auto" />
    </li>

    <template v-for="itm in filteredItems">
      <li v-if="itm.itemsFiltered && itm.itemsFiltered.length"
          :key="`mega-list-column-more-${itm.id}`"
          class="list-unstyled"
          role="presentation"
          :aria-label="`${itm.label} ${parentLabel}`"
      >
        <button type="button"
                class="dropdown-item cursor-pointer user-select-none"
                :class="{ 'active': itm.isActive, 'opened': openedItems && openedItems.includes(itm.id) }"
                role="menuitem"
                :aria-label="`${itm.label} ${parentLabel}`"
                :aria-expanded="openedItems && openedItems.includes(itm.id) ? 'true' : 'false'"
                :aria-controls="`megalist-column__${itm.id}`"
                @click.stop="$emit('toggle', itm.id, item.id, level)"
        >
          <span class="d-flex align-items-center justify-content-between w-100">
            <span>{{ itm.label }}</span>
            <span class="d-flex align-items-center">
              <NavigationIndicator v-if="itm.badge" :text="itm.badge" />
              <b-icon icon="sort-down" class="dropdown ml-2" style="transform: rotate(-90deg)" />
            </span>
          </span>
        </button>
      </li>
      <li v-else :key="`mega-list-column-link-${itm.id}`" class="list-unstyled d-flex align-items-center megalist-column-link megalist-leaf-item px-3" role="presentation">
        <a :ref="`link-${itm.id}`"
           :href="itm.link ? itm.link.url : itm.url || undefined"
           class="dropdown-item user-select-none border-0 flex-grow-1 pl-0"
           :class="{ 'active': itm.isActive, 'opened': openedItems && openedItems.includes(itm.id) }"
           :aria-label="`${itm.label} ${parentLabel}`"
           role="menuitem"
           @click="$emit('link-click')"
        >{{ itm.label }}</a>
        <NavigationIndicator v-if="itm.badge" :text="itm.badge" class="ml-auto megalist-leaf-badge" @click="navigateToBadgeLink(itm)" />
      </li>
    </template>
  </ul>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import useNavigation from '~/helpers/composables/useNavigation'

export default {
  components: {
    NavigationIndicator: () => import('@/components/elements/Navigation/NavigationIndicator')
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean
    },
    level: {
      type: Number,
      default: 1
    },
    openedItems: {
      type: [Array, null]
    },
    parentLabel: {
      type: [String, null]
    }
  },
  setup(props) {
    const {filterHiddenNavigationItems} = useNavigation()

    const filteredItems = computed(() => filterHiddenNavigationItems(props.item.items || []))

    return {
      filteredItems
    }
  },
  methods: {
    navigateToBadgeLink(itm) {
      const linkRef = this.$refs[`link-${itm.id}`]
      if (linkRef) {
        const el = Array.isArray(linkRef) ? linkRef[0] : linkRef
        if (el && typeof el.click === 'function') { el.click() }
      }
      this.$emit('link-click')
    }
  }
}
</script>

<style scoped>
.megalist-column {
  min-width: 16rem;
}

.megalist-leaf-item {
  border-left: 4px solid transparent;
}

.dropdown-item,
.dropdown-item:hover,
.dropdown-item:focus,
.dropdown-item:active {
  color: inherit;
}

.dropdown-item:active {
  background-color: transparent;
}

.megalist-leaf-badge {
  cursor: pointer;
  pointer-events: auto;
}
</style>
