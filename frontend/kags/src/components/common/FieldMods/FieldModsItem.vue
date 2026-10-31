<template>
  <b-col cols="12" class="mb-1">
    <div class="box p-2 position-relative overflow-hidden" :class="{'error' : isError}" @click.prevent.stop="downloadFile">
      <b-row>
        <b-col cols="12" class="d-flex align-items-center">
          <b-badge class="d-inline small" variant="danger">{{ mod.status.toUpperCase() }}</b-badge>
          <span class="ms-2 font-condensed">{{ mod.campaign_code }}</span>
          <span class="ms-2 font-condensed">{{ mod.field_modification_code.toUpperCase() }}</span>
          <span class="ms-2 small">{{ translate('expiresOnLabel', 'CommonDateTime') }} {{ endDateFormatted }}</span>
        </b-col>
        <b-col cols="12" class="d-flex align-items-center justify-content-between">
          <span class="font-condensed">{{ mod.description }}</span>
          <b-link :class="{'text-gray' : isError}" :disabled="isError">
            <b-icon icon="download" />
          </b-link>
        </b-col>
      </b-row>
    </div>
    <b-tooltip v-if="rootEl && isError"
               class="d-none d-sm-block tooltip-danger"
               noninteractive
               :target="rootEl"
               :title="translate('downloadError', 'CommonErrors')"
    />
    <b-tooltip v-else-if="rootEl"
               class="d-none d-sm-block tooltip-light"
               noninteractive
               :target="rootEl"
               :title="translate('downloadDetails', 'FieldMods')"
    />
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'

export default {
  conceptName: ['FieldMods', 'CommonErrors', 'CommonDateTime'],
  setup() { return useLabels() },
  props: {
    mod: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      rootEl: null,
      isError: false
    }
  },
  mounted() {
    // The tooltip targets this component's root element, so render it only after mount
    this.rootEl = this.$el
  },
  computed: {
    endDate() {
      return (new Date(this.mod.end_date)).toLocaleString()
    },
    endDateFormatted() {
      return this.endDate.split(',')[0]
    }
  },
  methods: {
    downloadFile() {
      bdService.fetchServiceLetters(this.mod.field_modification_code).then(download_link => {
        const win = window.open(download_link, '_blank')
        win.focus()
      }).catch(() => {
        this.isError = true
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';

.box {
  transition: background-color .15s ease-in-out;
  cursor: pointer;
  background-color: $gray-100 !important;

  &.error {
    cursor: not-allowed;
  }

  &:hover:not(.error) {
    background-color: $gray-300 !important;
  }
}
</style>
