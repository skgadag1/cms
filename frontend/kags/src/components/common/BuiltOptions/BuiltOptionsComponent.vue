<template>
  <b-col tag="section" cols="12" data-test-id="built-options-component">
    <slot name="header" />

    <slot name="top-content" />

    <overlay-wrapper :show="loading">
      <ListEmptyWidget v-if="disabled || (loading && !items.length)" />

      <b-row v-else-if="error.present">
        <b-col data-test-id="built-options-error">
          <div v-if="error.text" class="box">{{ error.text }}</div>
          <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
        </b-col>
      </b-row>

      <b-row v-else-if="items.length">
        <b-col cols="12">
          <BuiltOptionsTable :disabled="disabled" :items="items" data-test-id="built-options-table" />
        </b-col>
      </b-row>

      <div v-else-if="!loading" class="box" data-test-id="built-options-noitems">{{ translate('noResults', 'CommonErrors') }}</div>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import BuiltOptionsTable from '@/components/common/BuiltOptions/BuiltOptionsTable'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {
    ListEmptyWidget,
    BuiltOptionsTable
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      items: [],
      loading: true,
      error: {
        present: false,
        text: null
      }
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchOptions()
    }
  },
  created() {
    this.fetchOptions()
  },
  methods: {
    fetchOptions() {
      if (!this.disabled && this.getCurrentSerial) {
        this.loading = true
        this.error.present = false

        bdService.getBuiltOptions(this.getCurrentSerial).then(r => {
          this.items = r.data || r
        }).catch(e => {
          if (!e.isCancelled) {
            this.error.present = true
            if (e.message) this.error.text = e.message && !e.message.includes('Machine with key ') ? e.message : null
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      } else {
        this.loading = false
        this.error.present = true
      }
    }
  }
}
</script>
