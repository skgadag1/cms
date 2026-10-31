<template>
  <div class="col-10 px-0 offset-1 bg-white megamenu-section transition-base position-absolute"
       :class="filteredItems.some(x => x.id === activeMenu) ? 'z-index-2' : 'z-index-0'"
       data-test-id="megamenu-section"
  >
    <div class="row">
      <div class="col-11">
        <MegaMenuItem v-for="item in filteredItems"
                      :key="`main-navigation-subnav__item-${item.id}`"
                      :item="item"
                      :active-menu="activeMenu"
                      :parent-label="item.label"
        />
      </div>
      <div class="col-1">
        <button :class="filteredItems.some(x => x.id === activeMenu) ? 'd-block' : 'd-none'"
                class="mt-4 position-absolute"
                title="Close"
                @click.prevent="$emit('close')"
        >
          <b-icon icon="close" />
        </button>
      </div>
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
      required: true
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
