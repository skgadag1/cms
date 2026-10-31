<template>
  <div
    class="grid-table-row overflow-hidden"
    :class="{ 'error': isError }"
    @click.prevent.stop="downloadFile"
  >
    <div class="item__letter_type">
      <span class="font-condensed">{{ item.bulletin_type }}</span>
    </div>

    <div v-if="showStatus" class="item__status">
      <b-badge
        v-if="item.status"
        class="text-uppercase d-inline small"
        :variant="statusTable[item.status]"
      >
        {{ item.status.toUpperCase() }}
      </b-badge>
    </div>

    <div class="item__description">
      <span class="font-condensed">{{ item.description }}</span>
    </div>

    <div class="item__date">
      <span v-if="item.active_date" class="font-condensed">
        {{ formatDate(item.active_date) }}
      </span>
    </div>

    <div class="item__language d-block w-100">{{ item.language }}</div>

    <div v-if="showStatus" class="item__date d-block w-100">
      <span class="d-inline d-md-none me-1">
        {{ translate('expiresOnLabel', 'CommonDateTime') }}
      </span>
      <span v-if="item.inactive_date">
        {{ formatDate(new Date(item.inactive_date)) }}
      </span>
    </div>

    <div class="item__download text-center">
      <b-link
        :id="`tooltip-${item.document_id}`"
        :class="{ 'text-gray': isError }"
        :disabled="isError"
      >
        <b-icon icon="download" />
      </b-link>

      <b-tooltip
        :class="isError ? 'tooltip-danger' : 'tooltip-light'"
        noninteractive
        :target="`tooltip-${item.document_id}`"
        :title="isError ? translate('downloadError', 'CommonErrors') : translate('downloadDetails', 'Bulletins')"
      />
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {STATUS_TABLE} from './TechnicalServiceBulletins'
import {bdService} from '@/services'

export default {
  conceptName: ['Bulletins', 'CommonErrors', 'CommonDateTime'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    item: {
      type: Object,
      required: true
    },
    showStatus: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      statusTable: STATUS_TABLE,
      isError: false
    }
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
@import '@/scss/colors';
.badge.text-bg-light-grey {
  background-color: $dark-grey;
  color: $black;
}
</style>