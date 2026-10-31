<template>
  <div class="col-11 offset-1 megalist-section transition-base position-absolute"
       :class="[ filteredItems && filteredItems.some(x => x.id === activeMenu) ? 'z-index-2' : 'z-index-0' ]"
       data-test-id="megalist-section"
       @click="$emit('close')"
  >
    <div class="row">
      <MegaListColumnWrapper v-for="item in filteredItems"
                             :key="`main-navigation-subnav__item-${item.id}`"
                             :list="item"
                             :active-menu="activeMenu"
                             :parent-label="item.label"
      />
    </div>
  </div>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'
import useNavigation from '~/helpers/composables/useNavigation'

export default {
  props: {
    items: {
      type: Array,
      required: true,
      default: () => []
    },
    activeMenu: {
      type: [String, null]
    }
  },
  setup(props) {
    const {filterHiddenNavigationItems} = useNavigation()
    const filteredItems = computed(() => filterHiddenNavigationItems(props.items || []))

    return {
      filteredItems
    }
  }
}
</script>
