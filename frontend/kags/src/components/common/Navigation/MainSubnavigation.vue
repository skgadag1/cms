<template>
  <nav v-cloak
       class="subnavigation d-none d-md-flex row bg-white box-shadow rounded"
       :data-test-id="'nav-main-item__subnav_'+counter"
       :class="{ 'subnavigation__2cols' : hasExtraContent}"
  >
    <div class="p-0 py-2 font-condensed" :class="[ hasExtraContent ? 'col-6' : 'col-12' ]">
      <ul class="list-unstyled mb-0">
        <template v-for="(s, j) in item.items" :key="'primarymenu_'+counter+'_subnavigation_'+j">
          <b-dropdown-item v-if="isAvailable(s)"
                           :data-test-id="'nav-main-item__subnav_'+counter+'_item_'+counter"
                           :active="s.active"
                           :href="s.url"
                           :disabled="isDisabled(s)"
                           @click="handleClick"
          >
            {{ s.title }}
          </b-dropdown-item>
        </template>
      </ul>
    </div>

    <div v-if="hasExtraContent" class="col-6 bg-gray-100 text-black fw-normal">
      <div class="row mb-3">
        <b-img :src="item.extraContent.image.replace('{transformation}', 'c_thumb')" :alt="item.extraContent.alt" fluid />
      </div>
      <div v-html="item.extraContent.text" />
    </div>
  </nav>
</template>

<script>
import {useMenu} from '@/composables/useMenu'

export default {
  name: 'MainSubnavigation',
  setup() { return useMenu() },
  props: {
    item: {
      type: Object,
      required: true
    },
    counter: {
      required: true
    }
  },
  computed: {
    hasExtraContent() {
      return this.item.extraContent && Object.keys(this.item.extraContent).length
    }
  },
  methods: {
    handleClick(e) {
      // ToDo: remove this hack on b-dropdown-item
      window.location.href = e.target.href
    }
  }
}
</script>
