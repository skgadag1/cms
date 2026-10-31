<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <b-modal v-model="modalShow"
             header-bg-variant="danger"
             header-text-variant="light"
             body-bg-variant="danger"
             body-text-variant="light"
             content-class="notification-bar"
             centered
             :no-close-on-backdrop="true"
             :no-close-on-esc="true"
             :no-footer="true"
             :no-header="true"
    >
      <div class="d-flex align-items-center p-2">
        <div class="flex-grow-0 pe-3"><b-icon class="h2 m-0" icon="info-circle" /></div>
        <div class="flex-grow-1 text-center">{{ translate('sessionExpired', 'CommonErrors') }}</div>
      </div>
    </b-modal>
    <MachineHeader :banner-url="translatedConfig('bannerUrl')" />

    <b-container tag="section">
      <b-row>
        <InPageNavigation :disabled="isEmpty" />

        <b-col cols="12" lg="9" class="px-0 px-sm-3">
          <MachineDetails v-if="detailsReady && !isNewDashboardEnabled"
                          data-test-id="mike-machinedetails"
                          :serial="getCurrentSerial"
                          :disabled="isEmpty"
                          :asset-server="assetServer"
                          :config="componentConfig"
          />
          
          <MachineDetailsNewDashboard v-if="detailsReady && isNewDashboardEnabled"
                          data-test-id="mike-machinedetails-newDashboard"
                          :serial="getCurrentSerial"
                          :disabled="isEmpty"
                          :asset-server="assetServer"
                          :config="componentConfig"
          />

          <b-row v-if="componentConfig.showFaultCodes === 'true' || componentConfig.showFieldMods === 'true'" class="mb-3 px-3 px-sm-0">
            <FaultCodesWidget v-if="componentConfig.showFaultCodes === 'true'"
                              :disabled="isEmpty"
                              :fault-codes-page-url="translatedConfig('faultCodesPageUrl')"
                              :error-input-url="translatedConfig('errorInputUrl')"
                              :full-width="componentConfig.showFieldMods === 'false'"
                              :config="componentConfig"
            />
            <FieldModsWidget v-if="componentConfig.showFieldMods === 'true'"
                             class="mt-3 mt-md-0"
                             :disabled="isEmpty"
                             :field-mods-page-url="translatedConfig('fieldModsPageUrl')"
                             :error-input-url="translatedConfig('errorInputUrl')"
                             :full-width="componentConfig.showFaultCodes === 'false'"
            />
          </b-row>

          <b-row v-if="componentConfig.showBulletins === 'true'" class="mb-5 px-3 px-sm-0">
            <BulletinsWidget class="w-100 ms-auto"
                             :bulletins-page-url="translatedConfig('bulletinsPageUrl')"
            />
          </b-row>

          <b-row>
            <b-col>
              <div class="bg-white p-3">
                <h2 class="m-0">
                  {{ translate("title", 'HistoryWidget') }}

                  <b-link class="float-end small"
                          data-test-id="show-all-link"
                          :href="translatedConfig('historyPageUrl')"
                          :disabled="isEmpty"
                          :class="{ 'text-gray' : isEmpty }"
                  >
                    <span class="small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
                  </b-link>
                </h2>
              </div>
            </b-col>
          </b-row>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import MachineHeader from '@/components/common/Machine/MachineHeader'
import MachineDetails from '@/components/common/Machine/MachineDetails'
import MachineDetailsNewDashboard from '@/components/common/Machine/MachineDetailsNewDashboard'
import FaultCodesWidget from '@/components/common/FaultCodes/FaultCodesWidget'
import FieldModsWidget from '@/components/common/FieldMods/FieldModsWidget'
import InPageNavigation from '@/components/common/Navigation/InPageNavigation'
import BulletinsWidget from '../common/Machine/BulletinsWidget'
import { mapGetters } from 'vuex'

export default {
  conceptName: ['CommonErrors', 'CommonFunctional', 'HistoryWidget'],
  setup() { return useLabels() },
  components: {
    MachineDetails,
    MachineDetailsNewDashboard,
    MachineHeader,
    InPageNavigation,
    FaultCodesWidget,
    FieldModsWidget,
    BulletinsWidget
  },
  data() {
    return {
      modalShow: false,
      componentConfig: this.config
    }
  },
  computed: {
    ...mapGetters([
      'isEmpty',
      'getCurrentSerial',
      'isCurrentMachineAuthorized',
      'getFeatureFlag',
      'areFeatureFlagsResolved'
    ]),
    isNewDashboardEnabled() {
      return this.getFeatureFlag('switchToNewDashboard')
    },
    // Render a details view only once we know which one to show:
    // - no serial selected -> show the disabled/no-serial state immediately
    // - serial selected -> wait until feature flags resolve for that serial,
    //   so the legacy view doesn't flash and details aren't fetched twice
    detailsReady() {
      return this.isEmpty || this.areFeatureFlagsResolved
    },
  },
  watch: {
    getCurrentSerial(s) {
      if (s) {
        this.$store.dispatch('fetchMachine')
        this.$store.dispatch('fetchMachineAuth')
        this.$store.dispatch('fetchFeatureFlags')
      }
    }
  },
  created() {
    if (this.getCurrentSerial) {
      this.$store.dispatch('fetchMachine')
      this.$store.dispatch('fetchMachineAuth')
      this.$store.dispatch('fetchFeatureFlags')
    }
  },
  mounted() {
    window.addEventListener('forceLogout', () => {
      this.modalShow = true
      setTimeout(() => window.location.reload(), 5000)
    })
  }
}
</script>

<style lang="scss">
  .notification-bar {
    width: auto;
    margin: auto;
    background: transparent;
    border-radius: 8px!important;

    .modal-body {
      border-radius: 8px!important;
    }
  }
</style>