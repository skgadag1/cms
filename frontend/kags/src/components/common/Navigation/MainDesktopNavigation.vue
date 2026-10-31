<template>
  <b-navbar :container="false" class="navbar-dark nav main-nav col-auto d-none d-md-flex px-3">
    <b-collapse id="bobcat-nav" is-nav>
      <template v-for="(item, i) in menuitems" :key="'primarymenu_'+i">
        <b-nav-item v-if="(!item.items && isAvailable(item)) || item.hideSubmenu"
                    link-class="position-relative d-flex align-content-center"
                    :data-test-id="'nav-main-item_'+i"
                    :href="item.url"
                    :active="item.active"
        >
          <b-icon v-if="item.icon && item.icon !== 'null'" :icon="item.icon" class="text-white me-1" />
          <span class="d-none d-lg-inline-block ms-1 text-uppercase">{{ item.title }}</span>
        </b-nav-item>

        <b-nav-item v-else-if="isAvailable(item)"
                    link-class="position-relative d-flex align-content-center"
                    :data-test-id="'nav-main-item_'+i"
                    :active="item.active"
                    @click.prevent
        >
          <b-icon v-if="item.icon && item.icon !== 'null'" :icon="item.icon" class="text-white me-1" />
          <span class="d-none d-lg-inline-block ms-1 text-uppercase">{{ item.title }}</span>

          <MainSubnavigation :key="'secondarymenu_'+i"
                             :item="item"
                             :counter="i"
          />
        </b-nav-item>
      </template>
    </b-collapse>
  </b-navbar>
</template>

<script>
import MainSubnavigation from './MainSubnavigation'
import {useMenu} from '@/composables/useMenu'

export default {
  components: {
    MainSubnavigation
  },
  setup() { return useMenu() },
  props: {
    menuitems: {
      type: Array,
      required: true
    }
  }
}
</script>
