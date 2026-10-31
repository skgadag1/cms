<template>
  <b-col tag="section" cols="12" class="field-mods-component">
    <slot name="header" />

    <slot name="top-content" />

    <overlay-wrapper :show="loading && !disabled">
      <b-row v-if="!disabled && !error.present">
        <b-col v-if="!pendingMods" cols="12" class="mb-4">
          <div class="box">
            <b-icon icon="check" class="position-absolute text-green h2 me-4 mb-0" />
            <div class="font-condensed ms-4 ps-3">{{ translate('noPendingFieldmods', 'CommonErrors') }}</div>
          </div>
        </b-col>

        <b-col cols="12">
          <FieldModsTable :disabled="disabled || (loading && !mods.length)"
                          :items="mods"
                          :fields="fields"
          />
        </b-col>

        <b-col v-if="!mods.length && !loading" cols="12">
          <div class="box">{{ translate('noResults', 'CommonErrors') }}</div>
        </b-col>
      </b-row>

      <template v-else-if="disabled">
        <ListEmptyWidget />
      </template>

      <b-row v-else-if="error.present">
        <b-col cols="12">
          <div v-if="error.text" class="box">{{ error.text }}</div>
          <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
        </b-col>
      </b-row>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import FieldModsTable from '@/components/common/FieldMods/FieldModsTable'
import {bdService} from '@/services'

export const MODS_TABLE = {
  'PENDING': 'danger',
  'COMPLETE': 'success',
  'IN_PROGRESS': 'dark'
}

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {
    FieldModsTable,
    ListEmptyWidget
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      mods: [],
      pendingMods: true,
      loading: true,
      error: {
        present: false,
        text: null
      }
    }
  },
  computed: {
    fields() {
      return [
        {key: 'status', label: this.translatedConfig('statusHeader'), sortable: false},
        {key: 'class', label: this.translatedConfig('classHeader'), sortable: false},
        {key: 'code', label: this.translatedConfig('codeHeader'), sortable: false},
        {key: 'description', label: this.translatedConfig('descriptionHeader'), sortable: false},
        {key: 'date', label: this.translatedConfig('expiresOnHeader'), sortable: false}
      ]
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial() {
      this.fetchFieldMods()
    }
  },
  created() {
    this.fetchFieldMods()
  },
  methods: {
    fetchFieldMods() {
      if (!this.disabled && this.getCurrentSerial) {
        this.loading = true
        bdService.fetchFieldMods(this.getCurrentSerial, 'AA,A').then(r => {
          this.mods = r.sort(this.compare)
          this.pendingMods = this.mods.find(i => i.status === 'PENDING')
          this.error.present = false
          this.error.text = null
        }).catch(e => {
          if (!e.isCancelled) {
            this.pendingMods = true
            this.error.present = true
            this.error.text = e.response && e.response.data && e.response.data.error_message || ''
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    compare(a, b) {
      const s = {
        'PENDING': 3,
        'COMPLETE': 2,
        'IN_PROGRESS': 1
      }
      // Sorting by status then code ('AA' on top) and then sort by end date (oldest on top).
      if (s[a.status] < s[b.status]) return 1
      else if (s[a.status] > s[b.status]) return -1
      else if (a.campaign_code.length < b.campaign_code.length) return 1
      else if (a.campaign_code.length > b.campaign_code.length) return -1
      else if (a.campaign_code > b.campaign_code) return 1
      else if (a.campaign_code < b.campaign_code) return -1
      else if (a.end_date > b.end_date) return 1
      else if (a.end_date < b.end_date) return -1
      else return 0
    }
  }
}
</script>
