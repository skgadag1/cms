<template>
  <div class="row row-scrolled no-gutters align-items-stretch bg-white specification-table-compare-header">
    <div class="d-none d-md-block col col-sticky bg-white" />
    <div v-for="model in models"
         :key="`specification-table-compare-header__${model.id}`"
         :data-test-id="`specification-table-compare-header__${model.id}`"
         class="col"
    >
      <LazyHydrate never>
        <div class="compare-cell px-2 pb-4 pb-md-5 text-primary d-flex align-items-center font-weight-bold box-shadow-inset">
          {{ model.title || model.id || model['@name'] }}
          <span v-if="model.lifecycle && model.lifecycle.toLowerCase() === 'historical' && model.yearEnd"
                class="ml-1"
          >({{ model.yearEnd }})</span>
        </div>
      </LazyHydrate>
    </div>
    <div class="d-none scroll-space col bg-gray-200" />
  </div>
</template>

<script>
export default {
  props: {
    models: {
      type: Array,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/_colors';

.box-shadow-inset {
  box-shadow: inset -1px 0px 0px $tertiary;
}
</style>
