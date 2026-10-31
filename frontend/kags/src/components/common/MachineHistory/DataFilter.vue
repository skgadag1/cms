<template>
  <b-row class="mb-3">
    <b-col cols="12" class="position-relative">
      <div v-if="disabled" class="mask disabled zindex-1 rounded" />

      <b-form-checkbox-group v-model="selected"
                             @update:model-value="selectionChange"
      >
        <b-form-checkbox v-for="(f, i) in filters"
                         :key="'table_filter_'+i"
                         inline
                         :value="f"
        >
          <span class="font-condensed">{{ f }}</span>
        </b-form-checkbox>
      </b-form-checkbox-group>

      <b-form-checkbox-group v-if="showInternals"
                             v-model="selected"
                             @update:model-value="selectionChange"
      >
        <b-form-checkbox v-for="f in ['internal','external']"
                         :key="'table_filter_'+f"
                         inline
                         :value="f"
        >
          <span class="font-condensed">{{ translate(['internal', 'external'].find(e=>e === f), 'HistoryWidget') }}</span>
        </b-form-checkbox>
      </b-form-checkbox-group>
    </b-col>
  </b-row>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
export default {
  conceptName: ['HistoryWidget'],
  setup() { return useLabels() },
  props: {
    filters: {
      type: Array,
      required: true
    },
    disabled: {
      type: Boolean,
      default: false
    },
    showInternals: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      selected: []
    }
  },
  watch: {
    filters(n) {
      if (n.length) this.selected = [...this.filters, 'internal', 'external']
    }
  },
  methods: {
    selectionChange(s) {
      this.$emit('update', s)
    }
  }
}
</script>
