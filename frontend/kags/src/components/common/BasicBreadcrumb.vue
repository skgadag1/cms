<template>
  <b-row class="mb-4" data-test-id="breadcrumb">
    <b-col cols="12">
      <span v-for="(itm, i) in getBreadcrumbItems"
            :key="'breadcrumb_item_'+i"
            :data-test-id="'breadcrumb-item_'+i"
      >
        <span v-if="i>0"> &gt; </span>
        <b-link :href="itm.url"
                class="text-black"
                :class="i == getBreadcrumbItems.length - 1 ? 'breadcrumb-active' : ''"
        >
          {{ itm.title }}
        </b-link>
      </span>
    </b-col>
  </b-row>
</template>

<script>
import {getMainNavigationStructure} from '@/utils'
import {contentService} from '@/services'

export default {
  data() {
    return {
      loading: true,
      menuitems: []
    }
  },
  computed: {
    getBreadcrumbItems() {
      let _items = []
      let _menu = {items: this.menuitems}
      while(_menu.items) {
        const _activeMenu = _menu.items.filter(x => x.active)
        if (_activeMenu.length) {
          _menu = _activeMenu[0]
          _items.push(_menu)
        }
        else break
      }
      return _items
    }
  },
  created() {
    this.getMenuItems()
  },
  methods: {
    getMenuItems() {
      contentService.fetchSitemap().then(r => {
        this.menuitems = getMainNavigationStructure(r.data)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
  .breadcrumb-active {
    text-decoration: underline;
  }
</style>
