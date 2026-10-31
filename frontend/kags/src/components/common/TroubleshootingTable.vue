<template>
  <div v-if="show" class="row">
    <div class="col-12 text-center">
      <h2>{{ translate("troubleshootingProcedure", 'TroubleshootingTable') }}</h2>
      <p v-for="(n, i) in notes" :key="'troubleshooting_procedure_note_'+i" class="fst-italic"><strong>Note</strong>: {{ n }}</p>
    </div>
    <div class="col-12">
      <b-table borderless
               small
               class="table-fancy"
               head-variant="dark"
               thead-tr-class="small"
               :items="items"
               :fields="fields"
      >
        <template #cell(index)="data">
          <TableCounter :title="data.index + 1" />
        </template>

        <template #cell(action)="data">
          <b>{{ data.value }}</b>
        </template>
      </b-table>
    </div>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import TableCounter from '@/components/common/TableCounter'

export default {
  conceptName: ['TroubleshootingTable'],
  setup() { return useLabels() },
  components: {TableCounter},
  props: {
    show: {
      type: Boolean,
      required: false,
      default: true
    },
    items: {
      type: Array,
      required: true
    },
    notes: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      fields: [
        {key: 'index', label: ''},
        'action',
        {key: 'yes', class: 'label-yes'},
        {key: 'no', class: 'label-no'}
      ]
    }
  }

}
</script>
