<template>
  <aside class="d-none d-md-block bg-gray-100 box-shadow rounded font-condensed position-relative overflow-hidden">
    <overlay-wrapper :show="loading">
      <b-nav vertical class="nav-vertical d-none d-lg-flex" data-test-id="nav-vertical-desktop" :class="{ 'py-5' : loading && !items.length }">
        <template v-for="(itm, i) in items" :key="'page_navigation_item_'+i">
          <b-nav-item v-if="isAvailable(itm)"
                      :href="itm.url"
                      :data-test-id="'nav-page-vertical-item_'+i"
                      :active="itm.active"
                      :disabled="isDisabled(itm)"
          >
            <b-icon v-if="itm.icon && itm.icon !== 'null'" :icon="itm.icon" class="me-3 mb-0 h3" fixed-width />
            {{ itm.title }}
          </b-nav-item>
        </template>
      </b-nav>
      <div v-if="disabled"
           class="mask disabled"
      />
    </overlay-wrapper>
  </aside>
</template>

<script>
import {useMenu} from '@/composables/useMenu'

export default {
  setup() { return useMenu() },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    loading: {
      type: Boolean,
      required: false,
      default: true
    },
    items: {
      type: Array,
      required: true,
      default() { return [] }
    }
  }
}
</script>
