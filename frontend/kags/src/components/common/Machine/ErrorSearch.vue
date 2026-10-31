<template>
  <div>
    <BasicSearch
      :action="action"
      :disabled="disabled"
      :initial-keyword="getTroubleshootingKeyword"
      :initial-search-type="getTroubleshootingSearchType"
      :ajax="ajax"
      :message="!isTroubleshootingSearch && isAiServiceEnabled ? translate('ServiceAiTitle', 'TroubleshootingProcedures'): translate('iNeedToTroubleshootErrorCode', 'MachineDetails')"
      :placeholder="translate('enterYourErrorCode', 'MachineDetails')"
      :is-troubleshooting-search="isTroubleshootingSearch"
      @search="saveKeyword"
    />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BasicSearch from '@/components/common/BasicSearch';
import {TROUBLESHOOING_KEYWORD_SAVE, TROUBLESHOOTING_SEARCH_TYPE_SAVE} from '@/store/mutations';
import { mapGetters } from 'vuex';

export default {
  conceptName: ['MachineDetails', 'TroubleshootingProcedures'],
  setup() { return useLabels() },
  components: {
    BasicSearch
  },
  props: {
    action: {
      type: String,
      required: false
    },
    ajax: {
      type: Boolean,
      required: false,
      default: false
    },
    clearOnInit: {
      type: Boolean,
      required: false,
      default: false
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    isTroubleshootingSearch: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  computed: {
    ...mapGetters([
      'getTroubleshootingKeyword',
      'getTroubleshootingSearchType',
      'getCurrentSerial',
      'getFeatureFlag'
    ]),
    isAiServiceEnabled() {
      return this.getFeatureFlag('serviceAIAccess')
    }
  },
  created() {
    if (this.clearOnInit) this.saveKeyword()
  },
  methods: {
    saveKeyword(k = '', s) {
      this.$store.commit(TROUBLESHOOING_KEYWORD_SAVE, k)
      if (this.isTroubleshootingSearch) {
        this.$store.commit(TROUBLESHOOTING_SEARCH_TYPE_SAVE, s)
      }
    }
  }
}
</script>
