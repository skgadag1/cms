<template>
  <BasicGridTable :disabled="disabled" header-class="d-none d-md-grid" grid-name="builtoptions">
    <template #header>
      <BasicGridTableHeader v-for="f of fields"
                            :key="'item__' + f.key"
                            :name="f.key"
      >
        {{ f.label }}
      </BasicGridTableHeader>
    </template>

    <template #rows>
      <div v-for="(itm, i) of items"
           :key="'item__' + itm.item_number"
           :data-test-id="'built-options-item__'+i"
           class="grid-table-row position-relative overflow-hidden inactive"
      >
        <div class="item__number font-condensed">{{ itm.item_number }}</div>
        <div class="item__description">{{ itm.description }}</div>
      </div>
    </template>
  </BasicGridTable>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BasicGridTable from '@/components/common/BasicGridTable'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'

export default {
  setup() { return useLabels() },
  components: {
    BasicGridTable,
    BasicGridTableHeader
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    items: {
      type: Array,
      required: true
    }
  },
  computed: {
    fields() {
      return [
        {key: 'number', label: this.translatedConfig('itemNumberLabel'), sortable: false},
        {key: 'description', label: this.translatedConfig('descriptionLabel'), sortable: false}
      ]
    }
  }
}
</script>
