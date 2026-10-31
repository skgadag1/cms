<template>
  <b-col tag="section" cols="12" :class="{ 'text-gray disabled' : disabled }" class="maintenance-component" vcloak>
    <slot name="header" />

    <slot name="top-content" />

    <overlay-wrapper :show="loading && !disabled">
      <b-row v-if="!disabled && !error.present">
        <b-col v-if="downloadLink" cols="12" class="mb-3" data-test-id="maintenance-download-section">
          <h2>{{ translate('maintenanceDownloadTitle') }}</h2>
          <div class="box py-2 mb-2 cursor-pointer" data-test-id="maintenance-download-link" @click.prevent="downloadServiceSchedulePdf">
            <b-row>
              <b-col cols="auto" class="font-condensed d-flex align-items-center">
                {{ getCurrentSerial.toUpperCase() }} | {{ translate('maintenanceInfoLabel') }}
              </b-col>
              <b-col col class="text-end d-flex align-items-center justify-content-end ps-0">
                <b-icon icon="download" class="text-primary ms-3" />
              </b-col>
            </b-row>
          </div>
        </b-col>

        <b-col v-if="items.length" cols="12">
          <BasicGridTable :disabled="disabled" grid-name="maintenance-schedule" data-test-id="maintenance-intervals-table">
            <template #header>
              <BasicGridTableHeader v-for="f of fields"
                                    :key="'item__' + f.key"
                                    :sortable="f.sortable"
                                    :name="f.key"
                                    @click="f.sortable ? $emit('sorting', f.key) : null"
              >
                {{ f.label }}
              </BasicGridTableHeader>
            </template>

            <template #rows>
              <div v-for="(itm, i) in items"
                   :key="'maintenance-row__'+i"
                   class="grid-table-row inactive font-condensed"
                   :data-test-id="'maintenance-intervals-table-item__'+i"
              >
                <div>{{ itm.name['EN'] }}</div>
                <div><template v-if="itm.maintenance_kits">{{ itm.maintenance_kits.join(',') }}</template></div>
              </div>
            </template>
          </BasicGridTable>
        </b-col>

        <b-col v-else data-test-id="maintenance-no-results">
          <div v-if="!loading" class="box">{{ translate('noResults', 'CommonErrors') }}</div>
          <ListEmptyWidget v-else />
        </b-col>
      </b-row>

      <BasicGridTable v-else-if="disabled" :disabled="disabled" data-test-id="maintenance-disabled" />

      <b-row v-else-if="error.present" data-test-id="maintenance-error">
        <b-col v-if="!loading" cols="12">
          <div v-if="error.text" class="box">{{ error.text }}</div>
          <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
        </b-col>
        <b-col v-else cols="12">
          <ListEmptyWidget />
        </b-col>
      </b-row>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import BasicGridTable from '@/components/common/BasicGridTable'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {
    BasicGridTable,
    ListEmptyWidget,
    BasicGridTableHeader
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
      loading: true,
      items: [],
      error: {
        present: false,
        text: null
      }
    }
  },
  computed: {
    fields() {
      return [
        {key: 'name', label: this.translatedConfig('intervalHeader'), sortable: false},
        {key: 'maintenance_kits', label: this.translatedConfig('kitHeader'), sortable: false}
      ]
    },
    downloadLink() {
      const _links = this.items.filter(i => i.service_schedule)
      return _links.length ? _links[0].service_schedule : false
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.loadData()
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      if (!this.disabled && this.getCurrentSerial) {
        this.loading = true
        this.error.present = false
        this.error.text = null
        this.items = []

        bdService.fetchMaintenanceSchedule(this.getCurrentSerial).then(r => {
          this.items = r.data
        }).catch(e => {
          if (!e.isCancelled) {
            this.error.present = true
            if (e.response) this.error.text = e.response.data.error_message && !e.response.data.error_message.includes('Machine with key ') ? e.response.data.error_message : null
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    downloadServiceSchedulePdf() {
      if (this.downloadLink) {
        const win = window.open(this.downloadLink, '_blank')
        win.focus()
      }
    }
  }
}
</script>
