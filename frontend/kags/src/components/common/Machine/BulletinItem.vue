<template>
  <div class="mb-1">
    <div class="box p-2 position-relative overflow-hidden" :class="{'error' : isError}" data-test-id="download-doc" @click.prevent.stop="downloadFile">
      <b-row>
        <b-col cols="12" class="d-flex align-items-center">
          <b-badge v-if="item.status" class="d-inline small me-2" :variant="statusTable[item.status]">{{ item.status.toUpperCase() }}</b-badge>
          <span class="font-condensed">{{ item.bulletin_type }}</span>
          <span v-if="item.inactive_date" class="ms-2 small">{{ translate('expiresOnLabel', 'CommonDateTime') }} {{ formatDate(new Date(item.inactive_date)) }}</span>
        </b-col>
        <b-col cols="12" class="d-flex align-items-center justify-content-between">
          <span class="font-condensed">{{ item.description }}</span>
          <b-link :class="{'text-gray' : isError}" :disabled="isError">
            <b-icon icon="download" />
          </b-link>
        </b-col>
      </b-row>
    </div>
    <b-tooltip v-if="rootEl"
               :class="isError ? 'tooltip-danger' : 'tooltip-light'"
               noninteractive
               :target="rootEl"
               :title="isError ? translate('downloadError', 'CommonErrors') : translate('downloadDetails', 'Bulletins')"
    />
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {STATUS_TABLE} from './TechnicalServiceBulletins'
import {bdService} from '@/services'

export default {
  conceptName: ['Bulletins', 'CommonDateTime', 'CommonErrors'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    item: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      rootEl: null,
      statusTable: STATUS_TABLE,
      isError: false
    }
  },
  mounted() {
    // The tooltip targets this component's root element, so render it only after mount
    this.rootEl = this.$el
  },
  methods: {
    async downloadFile() {
      bdService.fetchDocumentDownloadLink(this.item.document_id).then(r => {
        const link = document.createElement('a')
        link.href = r
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
      }).catch(() => this.isError = true)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/_colors.scss';

.box {
  transition: background-color .15s ease-in-out;
  cursor: pointer;
  background-color: $white !important;

  &.error {
    cursor: not-allowed;
  }

  &:hover:not(.error) {
    background-color: $gray-300 !important;
  }
}
</style>
