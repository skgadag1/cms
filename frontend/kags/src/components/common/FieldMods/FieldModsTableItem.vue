<template>
  <div class="grid-table-row overflow-hidden" :class="{ 'grayed': isGrayedOut, 'error' : isError }" @click.prevent.stop="downloadFile">
    <div class="item__status"><b-badge class="text-uppercase d-inline small" :variant="modsTable[mod.status]">{{ mod.status.toUpperCase() }}</b-badge></div>
    <div class="item__class"><span class="font-condensed">{{ mod.campaign_code }}</span></div>
    <div class="item__code"><span class="font-condensed">{{ mod.field_modification_code.toUpperCase() }}</span></div>
    <div class="item__description"><span class="font-condensed">{{ mod.description }}</span></div>
    <div class="item__date"><span class="d-inline d-md-none me-1">{{ translate('expiresOnLabel', 'CommonDateTime') }}</span>{{ formatDate(new Date(mod.end_date)) }}</div>
    <div class="item__download">
      <b-link :class="{'text-gray' : isError}" :disabled="isError">
        <b-icon icon="download" />
      </b-link>
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
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {MODS_TABLE} from '@/components/common/FieldMods/FieldModsComponent'
import {bdService} from '@/services'

export default {
  conceptName: ['FieldMods', 'CommonErrors', 'CommonDateTime'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    mod: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      rootEl: null,
      modsTable: MODS_TABLE,
      isError: false
    }
  },
  mounted() {
    // The tooltip targets this component's root element, so render it only after mount
    this.rootEl = this.$el
  },
  computed: {
    isGrayedOut() {
      return this.mod.status.toUpperCase() !== 'PENDING'
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
