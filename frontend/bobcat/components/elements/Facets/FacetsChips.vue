<template>
  <div>
    <b-badge v-for="(chip, index) in chips"
             :key="`item-chips__filters__${index}`"
             pill
             :variant="variant"
             class="mr-2 cursor-pointer mb-2 d-inline-block mw-100"
             @click="$emit('removeFilteringSelection', chip.value)"
    >
      <div class="d-flex align-items-center">
        <div class="text-truncate" v-html="chip.label" />
        <b-icon icon="remove" class="ml-1" />
      </div>
    </b-badge>
  </div>
</template>

<script>
export default {
  props: {
    chips: Array,
    variant: {
      type: String,
      default: 'dark'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.badge {
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
  border-radius: 0.875rem;

  &:hover {
    box-shadow: 0 0.5rem 1rem 0 rgba(0, 0, 0, 0.20);
  }

  &.badge-dark:hover {
    background-color: $gray-800 !important;
  }

  &.badge-white:hover {
    background-color: $gray-100 !important;
  }
}
</style>
