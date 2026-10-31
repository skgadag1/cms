<template>
  <BasicGridTable :disabled="disabled" header-class="d-none d-md-grid" grid-name="fieldmods">
    <template #header>
      <BasicGridTableHeader v-for="f of fields"
                            :key="'item__' + f.key"
                            :sortable="f.sortable"
                            :name="f.key"
      >
        {{ f.label }}
      </BasicGridTableHeader>
    </template>

    <template #rows>
      <FieldModsTableItem v-for="(m, i) of items"
                          :key="'fieldmods-table__' + m.code + '_' + i"
                          :mod="m"
      />
    </template>
  </BasicGridTable>
</template>

<script>
import BasicGridTable from '@/components/common/BasicGridTable'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'
import FieldModsTableItem from '@/components/common/FieldMods/FieldModsTableItem'

export default {
  components: {
    BasicGridTable,
    BasicGridTableHeader,
    FieldModsTableItem
  },
  props: {
    fields: {
      type: Array,
      required: true
    },
    items: {
      type: Array,
      required: true
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  }
}
</script>
