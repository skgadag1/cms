<template>
  <BasicFilterItem class="filter-item__checkboxes"
                   :title="title"
                   :options="options"
                   @updated="$emit('updated')"
                   @hide="$emit('hide')"
  >
    <slot />
    <b-dropdown-form v-for="o in options"
                     :key="'filter-options-'+title.trim()+'_'+o.label.trim()"
    >
      <b-form-checkbox v-model="o.value">{{ capitalize(o.label) }}</b-form-checkbox>
    </b-dropdown-form>
  </BasicFilterItem>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import BasicFilterItem from '@/components/common/BasicFilterItem'

export default {
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    BasicFilterItem
  },
  props: {
    title: {
      type: String,
      required: true
    },
    options: {
      type: Array,
      required: false,
      default() { return [] }
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/grid';
@import '../../scss/utils';

.filter-item {
  &__checkboxes {
    *,
    *:focus,
    *:active {
      outline: none !important;
    }

    .form-check-label {
      white-space: nowrap;
      width: 100%;
      background: no-repeat 50%/65% 65%;
    }

    li {
      &:hover {
        background: $gray-100;
      }
    }
  }
}
</style>
