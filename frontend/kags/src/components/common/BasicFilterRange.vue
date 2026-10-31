<template>
  <BasicFilterItem ref="basicFilter"
                   class="filter-item__range"
                   :title="title"
                   @hide="handleHide"
  >
    <div class="d-flex align-items-end p-2">
      <div>
        {{ minLabel }}
        <b-form-input v-model="options.minValue" type="number" @update:model-value="updated = true" @keydown.enter.prevent="$refs['basicFilter'].hideDropdown()" />
      </div>
      <div class="p-2">-</div>
      <div>
        {{ maxLabel }}
        <b-form-input v-model="options.maxValue" type="number" @update:model-value="updated = true" @keydown.enter.prevent="$refs['basicFilter'].hideDropdown()" />
      </div>
    </div>
  </BasicFilterItem>
</template>

<script>
import BasicFilterItem from '@/components/common/BasicFilterItem'

export default {
  components: {
    BasicFilterItem
  },
  props: {
    title: {
      type: String,
      required: true
    },
    minLabel: {
      type: String,
      required: true
    },
    maxLabel: {
      type: String,
      required: true
    },
    options: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      updated: false
    }
  },
  methods: {
    handleHide() {
      if (this.updated) {
        this.$emit('updated')
        this.updated = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* Chrome, Safari, Edge, Opera */
input {
  width: 3.5rem !important;
  min-width: 3.5rem !important;
  padding: .5rem;

  &::-webkit-outer-spin-button,
  &::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }

  /* Firefox */
  &[type=number] {
    -moz-appearance: textfield;
  }
}
</style>
