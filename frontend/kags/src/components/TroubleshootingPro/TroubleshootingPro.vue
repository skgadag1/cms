<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <MachineHeader :banner-url="translatedConfig('bannerUrl')"
                   :empty="isEmpty"
    />

    <b-container tag="section">
      <b-row>
        <InPageNavigation :disabled="isEmpty" />

        <b-col cols="12" lg="9" class="px-0">
          <TroubleshootingProcedures :enable-tabs="hasAiEnabled" :disabled="isEmpty" :config="componentConfig">
            <template v-slot:header>
              <h1 class="py-4 mt-4 mb-5 mb-sm-3 mt-sm-1 pt-lg-0 mb-lg-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
                  data-test-id="component-title"
              >
                {{ translate('title') }}
              </h1>
            </template>

            <template v-slot:top-content>
              <BasicBreadcrumb />
            </template>
          </TroubleshootingProcedures>
        </b-col>
      </b-row>
    </b-container>

    <b-modal ref="troubleshootingModal" />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import MachineHeader from '@/components/common/Machine/MachineHeader'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'
import InPageNavigation from '@/components/common/Navigation/InPageNavigation'
import TroubleshootingProcedures from '@/components/common/TroubleshootingPro/TroubleshootingProcedures'
import {TROUBLESHOOING_KEYWORD_SAVE, TROUBLESHOOTING_SEARCH_TYPE_SAVE} from '@/store/mutations'

export default {
  setup() { return useLabels() },
  components: {
    MachineHeader,
    BasicBreadcrumb,
    InPageNavigation,
    TroubleshootingProcedures
  },
  data() {
    return {
      componentConfig: this.config
    }
  },
  computed: {
    ...mapGetters([
      'isEmpty',
      'getCurrentSerial',
      'getFeatureFlag'
    ]),
    hasAiEnabled() {
      return this.getFeatureFlag('serviceAIAccess')
    }
  },
  watch: {
    getCurrentSerial: {
      immediate: true,
      handler(s) {
        if (s) this.$store.dispatch('fetchFeatureFlags')
      }
    }
  },
  created() {
    const keyword = this.componentConfig.keyword || ''
    const searchType = this.componentConfig.searchType || 'true'
    this.$store.commit(TROUBLESHOOING_KEYWORD_SAVE, keyword)
    this.$store.commit(TROUBLESHOOTING_SEARCH_TYPE_SAVE, searchType)
  }
}
</script>
