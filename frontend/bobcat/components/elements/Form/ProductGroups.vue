<template>
  <div v-if="field" data-test-id="product-groups" :class="{'font-weight-bold': bold}" class="w-100">
    <label v-if="field.label">{{ required ? `${field.label} *` : field.label }}</label>
    <div class="card-columns">
      <div v-for="group in groups" :key="group['@name']" class="mb-3 card bg-transparent border-0">
        <label v-if="group.label" class="font-weight-bold">{{ group.label }}</label>
        <b-form-checkbox-group
          v-model="selected"
          stacked
          :name="group['@name']"
          @change="handleChange"
        >
          <b-form-checkbox
            v-for="proudct in group.products"
            :key="`check-option__${proudct['@id']}`"
            :value="proudct.value"
            class="ml-4 custom-checkbox font-weight-normal z-index-0"
            button-variant="secondary"
          >
            {{ proudct.label }}
          </b-form-checkbox>
        </b-form-checkbox-group>
      </div>
    </div>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'

export default {
  props: {
    field: {
      type: Object
    }
  },
  setup(props, {emit}) {
    const {field} = props
    const required = ref(field?.required === 'true')
    const bold = ref(field?.bold === 'true')
    const modifiedGroups = field?.groups?.['@nodes'].map(g => field?.groups[g])?.map(g => ({...g, products: g.products?.['@nodes'].map(p => g.products[p])}))
    const groups = ref(modifiedGroups || [])
    const selectedProducts = []
    groups.value.forEach(g => g.products.forEach((p) => { if (p?.selected === 'true') { selectedProducts.push(p.value) } }))
    const selected = ref(selectedProducts || [])

    if (selected.value.length > 0) {
      emit('change', selected.value, field?.name, true)
    }

    const handleChange = (val) => {
      const isValid = required.value ? val.length > 0 : true
      emit('change', val, field?.name, isValid)
    }

    return {
      required,
      bold,
      groups,
      selected,
      handleChange
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

div[data-test-id="product-groups"] {
    .card-columns {
      column-count: 1;

      .card {
        box-shadow: none !important;
      }

      @include media-breakpoint-up(md) {
        column-count: 2;
        width: 75% !important;
      }
    }
}
</style>
