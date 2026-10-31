<template>
  <section class="mb-5">
    <overlay-wrapper :show="loading && !disabled">
      <div v-if="!disabled && machineData" class="arrow-down ps-3 pe-3">
        <div class="row">
          <div class="col-12 col-lg-6 mb-3 mb-lg-0 d-flex flex-column align-items-stretch bg-white border rounded-1 p-3 machine-summary-card summary-card--left">
            <div class="row">
              <div class="col-12 col-md-12">
                <p class="machine-title mb-0 text-uppercase">{{ serial }}-{{ machineData.model_id }}</p>
              </div>
              <div class="machine-media-row d-flex flex-row align-items-center bg-white ps-3 pe-3 w-100">
                <div class="machine-image-col ps-0 pe-0">
                    <b-img fluid
                       class="max-detail-height"
                       :src="productImage"
                       :alt="translate('machineImage', 'MachineDetails')"
                       @error="replaceWithUndefinedImage"
                />
                </div>

                <div class="flex-grow-1 flex-shrink-1 d-flex flex-column justify-content-center machine-bubbles-col">
                  <div v-if="!isCurrentMachineConnected" key="notConnected" class=" text-center text-md-start">
                    <b-badge size="sm" class="text-white small ms-md-2" variant="danger">{{ translate('errorNotConnected', 'MachineDetails') }}</b-badge>
                  </div>
                  <div v-else-if="error && !getCurrentMachineLoading" key="error" class=" text-center text-md-start">
                    <b-badge text-uppercase-badge size="sm" class="text-white small ms-md-2" variant="danger">{{ translate(error.text.ck, error.text.cp) }}</b-badge>
                  </div>
                   <div v-else-if="!getCurrentMachineLoading && !lastSeen" key="noData" class="text-center text-md-start">
                     <b-badge size="sm" class="text-gray-700 fw-lighter small ms-md-2" variant="light">{{ translate('noDataError', 'MachineDetails') }}</b-badge>
                   </div>
                  <MachineBubbles :asset-server="assetServer" :isCurrentMachineConnected="isCurrentMachineConnected"/>
                </div>
                
              </div>
              <div class="col-12">
                <div
                  v-if="!getCurrentMachineLoading"
                  key="lastSeen"
                  class="iq-row d-flex align-items-center justify-content-between flex-nowrap"
                >
                  <!-- IQ icon + coordinates / timestamp -->
                  <div class="d-flex align-items-center gap-2 flex-shrink-1">
                    <div class="flex-shrink-0">
                      <img
                        class="iq-icon"
                        :src="assetServer + '/assets/img/icons/iq.svg'"
                        alt=""
                        aria-hidden="true"
                      >
                    </div>
                    <div class="flex-shrink-1">
                      <div v-if="coordinates" class="iq-coords text-nowrap">
                        {{ coordinates }}
                      </div>
                      <div v-if="lastSeen" class="iq-timestamp text-nowrap">
                        {{ lastSeen }},
                      </div>
                      <div v-if="lastSeenTime" class="iq-timestamp text-nowrap">
                        {{ lastSeenTime }}
                      </div>
                    </div>
                  </div>
                  <!-- View on Map -->
                  <div class="flex-shrink-0 ms-2">
                    <b-link
                      class="view-map-link text-orange font-condensed"
                      :class="{ 'view-map-link--disabled': !isCurrentMachineConnected }"
                      :disabled="!isCurrentMachineConnected"
                      :href="isCurrentMachineConnected ? $attrs.config.locationHistoryPageUrl : null"
                      @click.native="!isCurrentMachineConnected && $event.preventDefault()"
                    >
                      {{ translate('viewMapLink', 'MachineDetails') }}
                      <img
                        class="iq-icon"
                        :src="assetServer + '/assets/img/icons/upword_arraw.svg'"
                        alt=""
                      >
                    </b-link>
                  </div>
                </div>
                <div v-if="isValidMachine" class="col-12 px-0 password"><machine-password :key="serial" :asset-server="assetServer" /></div>
              </div>
              
            </div>
          </div>

          <div class="col-12 col-lg-6 information-summary-card bg-white border rounded-1 p-3 summary-card--right">
            <div class="row">
              <!-- Machine Address -->
              <div :class="['col-12', { 'col-md-6': isContactSectionEnabled }]" data-test-id="bubble-location">
                <span class="font-condensed">{{ translate('machineAddress', 'MachineDetails') }}</span>
                <div class="machine-address-divider"></div>
                <template v-if="location && location.address">
                  <div v-if="location.address.municipality" class="text-small">{{ location.address.municipality }}</div>
                  <div v-if="location.address.subRegion" class="text-small">{{ location.address.subRegion }}</div>
                  <div v-if="location.address.region" class="text-small">{{ location.address.region }}</div>
                  <div v-if="location.address.country" class="text-small">{{ location.address.country }}</div>
                </template>
                <span v-else class="text-gray-700 text-small">
                  {{ translate('noMachineAddressDesc', 'MachineDetails') }}
                </span>
              </div>

              <!-- Contact -->
              <div v-if="isContactSectionEnabled" class="col-12 col-md-6 mt-3 mt-md-0" data-test-id="machine-contact">
                <span class="font-condensed">{{ translate('contact', 'MachineDetails') }}</span>
                <div class="machine-address-divider"></div>
                <template v-if="contactInfo && (contactInfo.accountName || contactInfo.phone || contactInfo.email)">
                  <div class="text-small">{{ contactName }}</div>
                  <div v-if="contactInfo.phone" class="contact-phone">
                    <span class="d-none d-md-block text-small">{{ contactInfo.phone }}</span>
                    <a :href="'tel:' + contactInfo.phone" class="contact-phone__link d-md-none">{{ contactInfo.phone }}</a>
                  </div>
                  <b-link
                    v-if="contactInfo.email"
                    :href="'mailto:' + contactInfo.email"
                    class="contact-email text-orange font-condensed d-inline text-break w-100"
                  >
                    <span class="contact-email__text">{{ contactInfo.email }}</span><img
                        class="iq-icon"
                        :src="assetServer + '/assets/img/icons/upword_arraw.svg'"
                        alt=""
                      ></b-link>
                </template>
                <span v-else class="text-gray-700 text-small">
                  {{ translate('noContactData', 'MachineDetails') }}
                </span>
              </div>
            </div>

            <div v-if="$attrs.config.softwareForKeyAccounts === 'true'" class="software-section">
              <span class="font-condensed">{{ translate('latestVersionsOfSoftware', 'MachineDetails') }}</span>
              <div class="machine-address-divider"></div>
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
            <p class="h1 text-uppercase" data-test-id="error-no-serial">{{ translate('noSerialNumber', 'MachineDetails') }}</p>
            <div class="text-center mt-3">
              <b-img fluid
                     alt="Unknown machine"
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
import ErrorSearch from './ErrorSearch'
import MachineSoftware from './MachineSoftware'
import MachinePassword from './MachinePassword'
import MachineBubbles from './MachineBubbles'
import RetrievePasswordModal from './RetrievePasswordModal.vue'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'
import {formatLastSeenDate, formatLastSeenTime} from '@/helpers/dateTimeUtils'
import {useLabels} from '@/composables/useLabels'

export default {
  conceptName: ['MachineDetails'],
  setup() { return useLabels() },
  components: {
    ErrorSearch,
    MachineSoftware,
    MachinePassword,
    MachineBubbles,
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
    // used by the machine-address block to size its column
    defLevel() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.defLevel && Math.round(this.getCurrentMachine.defLevel)
    },
    location() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.location || false
    },

    contactInfo() {
      if (!this.isCurrentMachineConnected) return false
      return  this.getCurrentMachine && this.getCurrentMachine.customer || {}
    },

    // display name for the contact: prefer account/company name, else first + last
    contactName() {
      const c = this.contactInfo
      if (!c) return ''
      const parts = [c.firstName, c.lastName, c.accountName].filter(Boolean)  
      return parts.join(' ')
    },

    // date portion only with trailing comma, e.g. "September 8, 2026,"
    lastSeen () {
      return formatLastSeenDate(this.getCurrentMachine && this.getCurrentMachine.lastReported)
    },
    // time portion of lastSeen, e.g. "12:09 PM"
    lastSeenTime () {
      return formatLastSeenTime(this.getCurrentMachine && this.getCurrentMachine.lastReported)
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
      'getCurrentAbbreviation',
      'getFeatureFlag'
    ]),
    isContactSectionEnabled() {
      return this.getFeatureFlag('switchToNewDashboardContact')
    },
    coordinates () {
      if (!this.isCurrentMachineConnected)  return null
      const loc = this.getCurrentMachine?.location
      if (loc == null || typeof loc.latitude !== 'number' || typeof loc.longitude !== 'number') {
        return null
      }
      const latHemi = loc.latitude >= 0 ? 'N' : 'S'
      const lonHemi = loc.longitude >= 0 ? 'E' : 'W'
      return `${Math.abs(loc.latitude).toFixed(2)}\u00B0${latHemi}, ${Math.abs(loc.longitude).toFixed(2)}\u00B0${lonHemi}`
    },
  },
  watch: {
    serial(s) {
      this.machineData = null
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
        const request = {}
        this._latestDetailsRequest = request
        this.loading = true
        bdService.fetchDetails(_s).then(r => {
          if (request === this._latestDetailsRequest && _s === this.serial) this.machineData = r.data
        }).catch(error => {
          if (!error.isCancelled && request === this._latestDetailsRequest && _s === this.serial) this.machineData = {}
        }).finally(() => {
          if (request === this._latestDetailsRequest && _s === this.serial) this.loading = false
        })
      }
    },
    replaceWithUndefinedImage(event) {
      event.target.src = this.assetServer + '/assets/img/examples/undefined.png'
    }
  }
}
</script>

<style lang="scss" scoped>
@use 'sass:color';
@import '../../../scss/colors';
@import '../../../scss/grid';

.arrow-down {
  position: relative;
  z-index: 1;

  &:after,
  &:before {
    content: '';
    position: absolute;
    bottom: -1.30rem;
    left: 50%;
    margin-left: -.75rem;
    width: 2rem;
    height: 2rem;
    background: $gray-100;
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
  max-height: 9.125rem;
}
.machine-summary-card {
  min-width: 0;
  min-height: 23.75rem;
}

.machine-title {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-weight: 700;
  color: $black;
  font-size: 1.5rem;
  line-height: 2.25rem;

  @include media-breakpoint-up('lg') {
    font-size: 2.25rem;
    line-height: 3.375rem;
  }
}
.information-summary-card {
  min-height: 23.75rem;
}

@include media-breakpoint-up('lg') {
  .summary-card--left {
    flex: 0 0 calc(50% - 0.375rem);
    max-width: calc(50% - 0.375rem);
    margin-right: 0.375rem;
  }

  .summary-card--right {
    flex: 0 0 calc(50% - 0.375rem);
    max-width: calc(50% - 0.375rem);
    margin-left: 0.375rem;
  }
}

.machine-address-divider  {
  border-top: 0.1875rem dashed $tertiary;
  margin: 0.5rem 0;
  width: 100%;
}
.password {
  border-top: 0.1875rem dashed $tertiary; 
  border-bottom: 0.1875rem dashed $tertiary;
  padding-top: 1rem;
  padding-bottom: 1rem;
}

.software-section {
  margin-top: 1rem;
}

.machine-media-row {
  // border-bottom: 3px dashed $tertiary; // #d5dee3
  gap: 0.75rem;
  padding-bottom: 1rem;

  // tablet (card is full-width here): keep image + bubbles grouped and
  // evenly spaced instead of stretched to opposite edges
  @include media-breakpoint-only('md') {
    justify-content: center;
    gap: 1.5rem;
  }

  // mobile: stack the three bubbles below the image with a tight gap
  @include media-breakpoint-down('md') {
    flex-direction: column !important;
    align-items: center;
    gap: 0.25rem;
    padding-left: 0.5rem !important;
    padding-right: 0.5rem !important;
  }
}

.machine-image-col {
  flex: 0 0 auto;
  width: 45%;
  max-width: 12rem;

  // tablet: roomy card, give the image a larger fixed size
  @include media-breakpoint-only('md') {
    width: auto;
    max-width: 24rem;
    flex: 0 1 60%;
  }

  // mobile: image sits on its own row above the bubbles, filling the card width
  @include media-breakpoint-down('md') {
    width: 100%;
    max-width: 22rem;
  }

  .max-detail-height {
    width: 100%;
    object-fit: contain;
  }
}

.machine-bubbles-col {
  // mobile: once stacked below the image, span the full width so the three
  // bubbles spread evenly across the row
  @include media-breakpoint-down('md') {
    width: 100%;
    flex: 0 0 100%;
  }
}

// IQ location row: dashed divider top + bottom, icon left, text middle, link right
.iq-row {
  gap: 0.75rem;
  padding: 1rem 0;
  border-top: 0.1875rem dashed $tertiary; // #d5dee3 — dashed divider above IQ section
}

.iq-icon {
  width: 1.5rem;
  height: 1.5rem;
  margin-right: 0.625rem; // 10px
}

// "View on Map" link: on hover darken the text and the arrow icon together.
// The arrow is an SVG <img>, so it's darkened via filter to match the text.
.view-map-link {
  .iq-icon {
    transition: filter 0.15s ease;
  }

  &:hover,
  &:focus {
    color: color.adjust($orange, $lightness: -15%) !important;
    text-decoration: underline;

    .iq-icon {
      filter: brightness(0.85);
    }
  }
}

// Contact email link: orange, underlined, arrow icon; darken both on hover
.contact-email {
  overflow-wrap: anywhere;
  text-decoration: underline;

  .iq-icon {
    width: 1.5rem;
    height: 1.5rem;
    vertical-align: middle;
    transition: filter 0.15s ease;
  }

  &:hover,
  &:focus {
    color: color.adjust($orange, $lightness: -15%) !important;
    text-decoration: underline;

    .iq-icon {
      filter: brightness(0.85);
    }
  }

  // mobile: no underline on the email link
  @include media-breakpoint-down('md') {
    text-decoration: none;

    &:hover,
    &:focus {
      text-decoration: none;
    }
  }
}

// disabled "View on Map" link when the machine is not connected:
// greyed out (incl. the arrow icon) and non-clickable
.view-map-link--disabled {
  opacity: 0.4;
  filter: grayscale(1);
  pointer-events: none;
  cursor: default;

  &,
  &:hover,
  &:focus {
    color: $gray-600 !important;
    text-decoration: none !important;
  }

  .iq-icon {
    filter: grayscale(1);
  }
}

.iq-coords {
  color: $dark-grey;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 0.75rem;
  letter-spacing: 0.0625rem;
}

.iq-timestamp {
  color: $gray-800;
  font-size: 0.875rem;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-weight: 500;
  line-height: 1rem;
  letter-spacing: 0.0625rem;
}
.contact-phone {
  line-height: 1.5rem;
  color: $gray-800;

  &__link {
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    font-weight: 700;
    text-decoration: none;

    // mobile: 16px orange tappable tel link
    font-size: 1rem;
    color: $orange;

    &:hover {
      text-decoration: underline;
    }

    // desktop: 14px plain dark text (non-clickable)
    @include media-breakpoint-up('md') {
      font-size: 0.875rem;
      color: $gray-800;
      pointer-events: none;
      cursor: default;
      font-weight: 400;
    }
  }
}

</style>
