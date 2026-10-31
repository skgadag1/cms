<template>
  <BasicGridTable :disabled="disabled" header-class="d-none d-md-grid" grid-name="faultcodes">
    <template #header>
      <BasicGridTableHeader v-for="f of fields"
                            :key="'item__' + f.key"
                            :sortable="f.sortable"
                            :sorting-active="sortingKey === f.key"
                            :sorting-dir="sortingDir"
                            :name="f.key"
                            @click="f.sortable ? $emit('sorting', f.key, sortingKey === f.key && sortingDir === '-' ? '+' : '-') : null"
      >
        {{ f.label }}
      </BasicGridTableHeader>
    </template>

    <template #rows>
      <FaultCodeTableItem v-for="(c, i) of codes"
                          :key="'faultcodes-table__' + c.faultCode + '_' + i"
                          :code="c"
                          :url="config.troubleshootingUrl"
                          :search-label="translatedConfig('searchLabel')"
      />
    </template>
  </BasicGridTable>
</template>

<script>
import BasicGridTable from '@/components/common/BasicGridTable'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'
import FaultCodeTableItem from '@/components/common/FaultCodes/FaultCodeTableItem'
import {useLabels} from '@/composables/useLabels'

export default {
  setup() {
    return useLabels()
  },
  components: {
    BasicGridTable,
    BasicGridTableHeader,
    FaultCodeTableItem
  },
  props: {
    fields: {
      type: Array,
      required: true
    },
    sortingDir: {
      type: String,
      default: null,
      required: false
    },
    sortingKey: {
      type: String,
      default: null,
      required: false
    },
    codes: {
      type: Array,
      required: true
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: true
    }
  }
}
</script>
