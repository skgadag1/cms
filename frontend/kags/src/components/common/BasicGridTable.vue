<template>
  <section class="grid-table" :class="gridName ? 'grid-table__' + gridName : ''">
    <header v-if="!disabled" class="grid-table-header grid-table-header__dark mb-1" :class="headerClass">
      <slot name="header" />
    </header>

    <slot v-if="!disabled" name="rows" />
    <ListEmptyWidget v-else />
  </section>
</template>

<script>
import ListEmptyWidget from '@/components/common/ListEmptyWidget'

export default {
  components: {
    ListEmptyWidget
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    gridName: {
      type: String,
      required: false
    },
    headerClass: {
      type: String,
      required: false
    }
  }
}
</script>
