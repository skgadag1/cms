<template>
  <b-collapse id="bobcat-nav__mobile" class="py-3 w-100 d-md-none">
    <ul class="col-12 list-unstyled font-condensed">
      <template v-for="(item, i) in menuitems" :key="i">
        <b-nav-item v-if="(!item.items && isAvailable(item)) || item.hideSubmenu"
                    class="bg-black text-uppercase"
                    :data-test-id="'nav-main-item__mobile_'+i"
                    :class="{ 'mt-2' : i>0 }"
                    :href="item.url"
                    :active="item.active"
        >
          <b-icon v-if="item.icon && item.icon !== 'null'" :icon="item.icon" class="text-white" fixed-width />
          <span class="ms-2 text-white text-uppercase dropdown">{{ item.title }}</span>
        </b-nav-item>

        <b-nav-text v-else-if="isAvailable(item)"
                    v-b-toggle="getToggleId(i)"
                    class="position-relative bg-black w-100 nav-link text-uppercase"
                    :data-test-id="'nav-main-item__mobile_'+i"
                    :class="{ 'mt-2' : i>0, 'active' : item.active }"
        >
          <b-icon v-if="item.icon && item.icon !== 'null'" :icon="item.icon" class="text-white" fixed-width />
          <span class="ms-2 text-white text-uppercase dropdown">
            {{ item.title }}
            <span class="float-end dropdown-toggle" />
          </span>
        </b-nav-text>

        <b-collapse v-if="item.items"
                    :id="getToggleId(i)"
                    tag="ul"
                    class="list-unstyled bg-white subnavigation"
                    :data-test-id="'nav-main-item-subnav__mobile_'+i"
        >
          <b-nav-item v-for="(s, j) in item.items"
                      :key="'primarymenu_mobile_'+i+'subnavigation_'+j"
                      link-class="text-black py-3"
                      :data-test-id="'nav-main-item-subnav__mobile_'+i+'_'+j"
                      :href="s.url"
                      :active="s.active"
                      :disabled="isDisabled(s)"
          >
            {{ s.title }}
          </b-nav-item>
        </b-collapse>
      </template>
    </ul>
  </b-collapse>
</template>

<script>
import {useMenu} from '@/composables/useMenu'

export default {
  setup() { return useMenu() },
  props: {
    menuitems: {
      type: Array,
      required: true
    }
  },
  methods: {
    getToggleId(id) {
      return 'primarymenu_mobile_subnavigation_' + id
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';

#bobcat-nav__mobile {
  background: $gray-800;
  color: $white;

  .nav-link.active {
    border-bottom: 4px solid $orange;
  }

  .subnavigation .nav-link.active {
    border-bottom: none;
    background: $orange;
    color: $white;
  }
}
</style>
