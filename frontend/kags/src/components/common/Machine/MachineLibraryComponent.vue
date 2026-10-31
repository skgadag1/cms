<template>
  <b-col cols="12" tag="main">
    <slot name="header" />
    <template v-if="!error">
      <slot name="top-content">
        <BasicBreadcrumb />
      </slot>

      <div class="box mb-4">
        <b-icon icon="info-circle" class="position-absolute h2 me-4 mb-0" />
        <div class="font-condensed ms-4 ps-3">{{ translate('safetyDocsNotice', 'MachineLibraryComponent') }}</div>
      </div>

      <overlay-wrapper :show="loading && !disabled">
        <div v-if="disabled" class="text-gray">
          <h2>{{ translate("serviceManuals", 'MachineLibraryComponent') }}</h2>
        </div>

        <GroupedManuals v-for="(g, i) of mData"
                        :key="'manuals-group_'+i"
                        :title="g[0].manual_type"
                        :items="g"
        />
        <TechnicalServiceBulletins :sn="getCurrentSerial" />

        <template v-if="disabled || (loading && !Object.keys(mData).length)">
          <ListEmptyWidget />
        </template>
      </overlay-wrapper>
    </template>

    <template v-else-if="error && !loading">
      <h1 class="py-5 pt-md-0 mb-md-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row" data-test-id="machine-library-noitems">
        <b-icon icon="book" class="text-primary me-3" />
        {{ translate("error", 'MachineLibraryComponent') }} {{ error.code }}
      </h1>
      <b-alert variant="danger" data-test-id="machine-library-error" show>
        {{ error.message || error }}
      </b-alert>
    </template>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import langs from 'langs'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'
import GroupedManuals from '@/components/common/GroupedManuals'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import TechnicalServiceBulletins from './TechnicalServiceBulletins.vue'

export default {
  conceptName: ['MachineLibraryComponent'],
  setup() { return useLabels() },
  components: {
    BasicBreadcrumb,
    ListEmptyWidget,
    GroupedManuals,
    TechnicalServiceBulletins
  },
  props: {
    disabled: {
      type: Boolean,
      required: true,
      default: false
    }
  },
  data() {
    return {
      loading: true,
      error: false,
      mData: {}
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchManuals()
      else this.mData = {}
    }
  },
  created() {
    this.fetchManuals()
  },
  methods: {
    fetchManuals() {
      this.loading = true
      this.error = false

      if (!this.disabled && this.getCurrentSerial) {
        bdService.fetchManuals(this.getCurrentSerial).then(r => {
          this.mData = {}
          for (const itm of r.data.items) {
            if (typeof this.mData[itm.manual_type] !== 'object') this.mData[itm.manual_type] = []
            itm.language = langs.where('1', itm.language_code).local
            this.mData[itm.manual_type].push(itm)
          }
        }).catch(r => {
          if (!r.isCancelled) this.error = r.response ? r.response.data.error : r
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    downloadPdfFile(document_id) {
      bdService.fetchDocumentDownloadLink(document_id).then(download_link => {
        const link = document.createElement('a')
        link.href = download_link
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
      }).catch(r => {
        this.error = r.response ? r.response.data.error : r
      })
    },
    updateFilters(event) {
      this.queryParams = event.value
      this.fetchManuals()
    }
  }
}
</script>
