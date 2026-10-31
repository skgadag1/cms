<template>
  <section class="mb-5">
    <overlay-wrapper :show="loading && !disabled">
      <div v-if="!disabled && machineData" class="box arrow-down">
        <div class="row">
          <div class="col-12 col-md-3 col-lg-4">
            <div class="row">
              <div class="col-7 col-md-12">
                <p class="h1 mb-0 text-uppercase">{{ serial }}</p>
                <p class="h1 text-uppercase">{{ machineData.model_id }}</p>
                <machine-password v-if="isValidMachine" :key="serial" />
              </div>
              <div class="col-5 col-md-12 text-center mt-md-3">
                <b-img fluid
                       class="max-detail-height"
                       :src="productImage"
                       :alt="translate('machineImage', 'MachineDetails')"
                       @error="replaceWithUndefinedImage"
                />
              </div>
            </div>
          </div>

          <div class="col-12 col-md-9 col-lg-8 mt-3 mt-md-0">
            <div class="d-flex align-items-center mb-2 justify-content-between justify-content-md-start">
              <h2 class="m-0 me-2">
                {{ translate("machineIQ", 'MachineDetails') }}
              </h2>
              <div v-if="!isCurrentMachineConnected" key="notConnected">
                <b-badge size="sm" class="text-white small ms-md-2" variant="danger">{{ translate("errorNotConnected", 'MachineDetails') }}</b-badge>
              </div>
              <div v-else-if="lastSeen && !getCurrentMachineLoading" key="lastSeen">
                <b-badge size="sm" class="text-gray-700 fw-lighter small ms-md-2" variant="light">{{ lastSeen }}</b-badge>
              </div>
              <div v-else-if="error && !getCurrentMachineLoading" key="error">
                <b-badge text-uppercase-badge size="sm" class="text-white small ms-md-2" variant="danger">{{ translate(error.text.ck, error.text.cp) }}</b-badge>
              </div>
              <div v-else key="noData">
                <b-badge size="sm" class="text-gray-700 fw-lighter small ms-md-2" variant="light">
                  {{ translate("noDataError", 'MachineDetails') }}
                </b-badge>
              </div>
            </div>
            <div class="d-flex justify-content-around justify-content-md-between">
              <CircleInfo class="one-fourth"
                          data-test-id="bubble-engineHours"
                          color="gauge"
                          :value="typeof engineHours === 'number' ? engineHours : 0"
                          :disabled="typeof engineHours !== 'number'"
                          :label="translate('operatingHoursLabel', 'MachineDetails')"
                          :precision="1"
              >
                -
              </CircleInfo>
              <CircleInfo v-if="electric"
                          class="bubble"
                          :class="{'withDef':defLevel, 'no-animation': true}"
                          data-test-id="bubble-electric"
                          :disabled="false"
                          :value="100"
                          color="blue"
                          :display-value="false"
                          :label="translate('electric', 'MachineDetails')"
              >
                <b-icon class="text-blue text-extralarge" icon="bolt" />
                <template #symbol><b-icon class="text-blue text-extralarge" icon="bolt" /></template>
              </CircleInfo>
              <CircleInfo v-else
                          class="one-fourth"
                          data-test-id="bubble-fuelLevel"
                          :value="typeof fuelLevel === 'number' ? fuelLevel : 0"
                          :disabled="typeof fuelLevel !== 'number'"
                          :color="fuelLevel <= 25 ? 'yellow' : 'green'"
                          :label="translate('fuelLevelLabel', 'MachineDetails')"
              >
                %
                <template #symbol>%</template>
              </CircleInfo>
              <CircleInfo v-if="typeof defLevel === 'number' && defLevel > 0"
                          class="one-fourth"
                          data-test-id="bubble-defLevel"
                          :value="defLevel"
                          :disabled="false"
                          :color="defLevel <= 25 ? 'yellow' : 'secondary'"
                          :label="translate('defLevelLabel', 'MachineDetails')"
              >
                %
                <template #symbol>%</template>
              </CircleInfo>

              <div v-if="location && location.address"
                   data-test-id="bubble-location"
                   class="d-none d-md-block"
                   :class="defLevel ? 'one-fourth' : 'half'"
              >
                <div><b-icon class="text-primary me-1 text-large" icon="map-marker-alt" /> <span class="font-condensed">{{ translate("locationLabel", 'MachineDetails') }}</span></div>
                <div v-if="location.address.municipality" class="small">{{ location.address.municipality }}</div>
                <div v-if="location.address.subRegion" class="small">{{ location.address.subRegion }}</div>
                <div v-if="location.address.region" class="small">{{ location.address.region }}</div>
                <div v-if="location.address.country" class="small">{{ location.address.country }}</div>
                <div><b-link class="font-condensed" :href="$attrs.config.locationHistoryPageUrl">{{ translate("viewMapLink", 'MachineDetails') }}</b-link></div>
              </div>

              <template v-else>
                <CircleInfo class="one-fourth d-none d-md-block"
                            :label="translate('locationLabel', 'MachineDetails')"
                >
                  <b-icon icon="map-marker-alt" />
                </CircleInfo>
                <div class="one-fourth d-none d-md-block" />
              </template>
            </div>

            <div v-if="location.address" class="d-block d-md-none">
              <hr>
              <div class="d-flex align-items-center">
                <div class="flex-grow-1">
                  <h4>{{ translate("locationLabel", 'MachineDetails') }}</h4>
                  <div>
                    {{ location.address.municipality }}
                    {{ location.address.subRegion }}
                    {{ location.address.region }}
                    {{ location.address.country }}
                  </div>
                  <div><b-link class="font-condensed" :href="$attrs.config.locationHistoryPageUrl">{{ translate("viewMapLink", 'MachineDetails') }}</b-link></div>
                </div>
                <b-icon class="text-primary h1" size="xl" icon="map-marker-alt" />
              </div>
            </div>

            <div v-if="$attrs.config.softwareForKeyAccounts === 'true'">
              <h2 class="m-0 mt-2">{{ translate('latestVersionsOfSoftware', 'MachineDetails') }}</h2>
              <MachineSoftware :serial="serial" :config="$attrs.config" :asset-server="assetServer" />
            </div>
          </div>
        </div>
      </div>

      <div v-if="!disabled && machineData">
        <ErrorSearch class="p-3"
                     style="margin-top: -2px"
                     clear-on-init
                     :action="$attrs.config.errorInputUrl"
        />
      </div>

      <div v-else class="box position-relative">
        <div class="row">
          <div class="col-4">
            <p class="h1 text-uppercase" data-test-id="error-no-serial">{{ translate("noSerialNumber", 'MachineDetails') }}</p>
            <div class="text-center mt-3">
              <b-img fluid
                     alt="Undefined image"
                     class="max-detail-height"
                     :src="assetServer + '/assets/img/examples/undefined.png'"
              />
            </div>
          </div>
        </div>
        <div class="mask disabled" />
      </div>
    </overlay-wrapper>
    <retrieve-password-modal :key="serial" />
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ErrorSearch from './ErrorSearch'
import MachineSoftware from './MachineSoftware'
import MachinePassword from './MachinePassword'
import RetrievePasswordModal from './RetrievePasswordModal.vue'
import { bdService } from '@/services'
import CircleInfo from '../CircleInfo'
import { mapGetters } from 'vuex'
import { getNavigatorLanguage } from '@/composables/useFormatters'

export default {
  conceptName: ['MachineDetails'],
  setup() { return useLabels() },
  components: {
    CircleInfo,
    ErrorSearch,
    MachineSoftware,
    MachinePassword,
    RetrievePasswordModal
  },
  props: {
    serial: {
      required: true
    },
    assetServer: {
      required: true
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      loading: true,
      machineData: null
    }
  },
  computed: {
    isValidMachine() {
      return !['ATT', 'LC'].includes(this.getCurrentAbbreviation) && ![
        'A33P',
        'A4BP',
        'B4K9',
        'B4PC',
        'B4PD',
        'B55N',
        'B4KL'
      ].includes(this.serial.substr(0, 4))
    },
    electric() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.equipmentType === 'electric' || false
    },
    fuelLevel() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.fuelLevel && Math.round(this.getCurrentMachine.fuelLevel)
    },
    defLevel() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.defLevel && Math.round(this.getCurrentMachine.defLevel)
    },
    engineHours() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.engineHours
    },
    location() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.location || false
    },
    lastSeen() {
      if (this.getCurrentMachine && this.getCurrentMachine.lastReported) {
        const ts = new Date(this.getCurrentMachine.lastReported)
        const options = {hour: '2-digit', minute: '2-digit'}
        return ts.toLocaleDateString() + ' ' + ts.toLocaleTimeString(getNavigatorLanguage(), options)
      } else return false
    },
    lastModified() {
      const d = new Date(this.machineData.last_modified)
      return `${d.getUTCMonth()}/${d.getDay()}/${d.getFullYear()}`
    },
    error() {
      if (this.getCurrentMachineError && this.getCurrentMachineError.message && (this.getCurrentMachineError.message.startsWith('Machine with key') || this.getCurrentMachineError.message.startsWith('Equipment data not found'))) {
        return {text: {ck: 'errorNotConnected', cp: 'MachineDetails'}}
      } else if (this.getCurrentMachineError && this.getCurrentMachineError.status && this.getCurrentMachineError.status === 403) {
        return {text: {ck: 'unauthorizedError', cp: 'MachineDetails'}}
      } else if (this.getCurrentMachineError) {
        return {text: {ck: 'defaultError', cp: 'MachineDetails'}}
      } return false
    },
    productImage() {
      let abbreviationImages = this.$store.state.concepts.en && this.$store.state.concepts.en['mymachine_AbbreviationImages'] || {}
      let prefixImages = this.$store.state.concepts.en && this.$store.state.concepts.en['mymachine_PrefixImages'] || {}
      const link = /(https?:\/\/[^" ]*)/i
      if (prefixImages && this.serial) {
        const match = Object.keys(prefixImages).find(prefix => this.serial.startsWith(prefix))
        if (match) {
          const imageSrc = prefixImages[match].match(link)
          if (Array.isArray(imageSrc)) return imageSrc[0]
        }
      }
      if (this.getCurrentAbbreviation && abbreviationImages[this.getCurrentAbbreviation]) {
        const imageSrc = abbreviationImages[this.getCurrentAbbreviation].match(link)
        if (Array.isArray(imageSrc)) return imageSrc[0]
      }

      return this.assetServer + '/assets/img/examples/undefined.png'
    },
    ...mapGetters([
      'getCurrentMachine',
      'getCurrentMachineError',
      'getCurrentMachineLoading',
      'isCurrentMachineConnected',
      'getCurrentAbbreviation'
    ])
  },
  watch: {
    serial(s) {
      if (s) this.getMachineData()
    }
  },
  created() {
    this.getMachineData()
    this.$store.dispatch('fetchMachineDetails')
    this.$store.dispatch('fetchConcepts', 'en')
  },
  methods: {
    getMachineData(_s = this.serial) {
      if (_s) {
        this.loading = true
        bdService.fetchDetails(_s).then(r => {
          this.machineData = r.data
        }).catch(() => {
          this.machineData = {}
        }).finally(() => this.loading = false)
      }
    },
    replaceWithUndefinedImage(event) {
      event.target.src = this.assetServer + '/assets/img/examples/undefined.png'
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';

.arrow-down {
  position: relative;
  z-index: 1;

  &:after,
  &:before {
    content: '';
    position: absolute;
    bottom: -.75rem;
    left: 50%;
    margin-left: -.75rem;
    width: 2rem;
    height: 2rem;
    background: $white;
    transform: rotate(55deg);
    z-index: -1;
  }

  &:after {
    transform: rotate(-55deg);
    margin-left: -1.25rem;
  }
}

.one-fourth {
  flex: 0 0 25%;
}

.half {
  flex: 0 0 50%;
}

.max-detail-height {
  max-height: 146px;
}
</style>
