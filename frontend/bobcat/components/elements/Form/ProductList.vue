<template>
  <div class="w-100">
    <h6 :data-test-id="`product-selector-${name}-header`" class="px-3 mb-2">{{ label }}</h6>
    <ul :data-test-id="`product-selector-${name}`" class="d-flex flex-column list-unstyled" :class="searchArray.length ? 'mb-2' : 'mb-0'">
      <li v-for="(it, i) in searchArray" :key="it.identifier + i" class="px-4 transition-base cursor-pointer" @click.prevent="addToList(it, name)">{{ it.name }}</li>
    </ul>
    <div v-if="!searchArray.length" class="px-3 text-gray-dark small mb-2">{{ noResultsMessage }}</div>
  </div>
</template>

<script>
export default {
  props: {
    searchArray: {
      type: Array,
      required: true
    },
    noResultsMessage: {
      type: String
    },
    label: {
      type: String
    },
    name: {
      type: String,
      required: true
    },
    addToList: {
      type: Function,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

li {
  &:hover {
    background-color: $gray-100;
  }
}
</style>
