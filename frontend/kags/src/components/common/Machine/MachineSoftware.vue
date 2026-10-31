<template>
  <overlay-wrapper :show="loadingSoftware">
    <b-row :no-gutters="isNewDashboardEnabled" :class="{ 'software-row software-divider': isNewDashboardEnabled }">
      <b-col v-if="mostRecentSA" cols="12" :md="6" class="d-flex align-items-start" :class="{ 'software p-2': isNewDashboardEnabled }">
        <div class="flex-fill order-0 order-md-1 small">
          <b-link v-if="config.serviceAnalyzerNationalUrl && mostRecentSA.brand !== 'Bobcat NA'" class="font-condensed mt-1" :href="config.serviceAnalyzerNationalUrl">{{ softwareLabels['nationalServiceAnalyzer'] }}</b-link>
          <b-link v-else-if="config.serviceAnalyzerBobcatUrl && mostRecentSA.brand === 'Bobcat NA'" class="font-condensed mt-1" :href="config.serviceAnalyzerBobcatUrl">{{ softwareLabels['serviceAnalyzer'] }}</b-link>
          <div v-else class="font-condensed mt-1">{{ softwareLabels['serviceAnalyzerLabel'] }}</div>
          <div>v.{{ mostRecentSA.version }}</div>
        </div>
        <div class="order-1 order-md-0 pe-md-3">
          <b-img class="mt-1" width="34" height="34" :src="softwareIcons[mostRecentSA.brand === 'Bobcat NA' ? 'serviceAnalyzer' : 'nationalServiceAnalyzer']" />
        </div>
      </b-col>
      <b-col v-if="mostRecentEADCU || mostRecentEAECU || mostRecentEAV2" cols="12" :md="6" class="d-flex align-items-start" :class="{ 'software p-2': isNewDashboardEnabled }">
        <div class="flex-fill order-0 order-md-1 small">
          <template v-if="mostRecentEADCU || mostRecentEAECU">
            <b-link v-if="config.engineAnalyzerUrl" class="font-condensed mt-1" :href="config.engineAnalyzerUrl">{{ softwareLabels['engineAnalyzer'] }}</b-link>
            <div v-else class="font-condensed mt-1">{{ softwareLabels['engineAnalyzer'] }}</div>
            <div v-if="mostRecentEAECU">ECU v.{{ mostRecentEAECU.version }}</div>
            <div v-if="mostRecentEADCU">DCU v.{{ mostRecentEADCU.version }}</div>
          </template>

          <template v-if="mostRecentEAV2">
            <b-link v-if="config.v2engineAnalyzerUrl" class="font-condensed mt-1" :href="config.v2engineAnalyzerUrl">{{ softwareLabels['v2engineAnalyzer'] }}</b-link>
            <div v-else class="font-condensed mt-1">{{ softwareLabels['v2engineAnalyzer'] }}</div>
            <div>ECU v.{{ mostRecentEAV2.version }}</div>
          </template>
        </div>
        <div class="order-1 order-md-0 pe-md-3">
          <b-img class="mt-1" width="34" height="34" :src="softwareIcons['engineAnalyzer']" />
        </div>
      </b-col>
      <b-col v-if="mostRecentOrionBMS" cols="12" :md="6" class="d-flex align-items-start" :class="{ 'software  p-2': isNewDashboardEnabled }">
        <div class="flex-fill order-0 order-md-1 small">
          <b-link class="font-condensed mt-1" :href="config.orionBMSUrl">{{ softwareLabels['orionBMS'] }}</b-link>
          <div>v.{{ mostRecentOrionBMS.version }}</div>
        </div>
        <div class="order-1 order-md-0 pe-md-3">
          <b-img class="mt-1" width="34" height="34" :src="softwareIcons['orionBMS']" />
        </div>
      </b-col>
      <b-col v-if="!mostRecentSA && !mostRecentEADCU && !mostRecentEAECU && !mostRecentEAV2 && !mostRecentOrionBMS" cols="12">
        {{ translate('noSoftwareError', 'MachineDetails') }}
      </b-col>
    </b-row>
  </overlay-wrapper>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['MachineDetails'],
  setup() { return useLabels() },
  props: {
    serial: {
      required: true
    },
    config: {
      type: Object,
      required: true
    },
    assetServer: {
      required: true
    }
  },
  data() {
    return {
      loadingSoftware: true,
      softwareData: null,
      mostRecentSA: false,
      mostRecentEADCU: false,
      mostRecentEAECU: false,
      mostRecentEAV2: false,
      mostRecentOrionBMS: false
    }
  },
  computed: {
    softwareLabels() {
      const labels = {}
      labels['nationalServiceAnalyzer'] = this.translate('serviceAnalyzerLabel', 'MachineDetails')
      labels['serviceAnalyzer'] = this.translate('serviceAnalyzerLabel', 'MachineDetails')
      labels['engineAnalyzer'] = this.translate('engineAnalyzerLabel', 'MachineDetails')
      labels['v2engineAnalyzer'] = this.translate('v2engineAnalyzerLabel', 'MachineDetails')
      labels['orionBMS'] = this.translate('orionBMSLabel', 'MachineDetails')

      let softwareConcepts = this.$store.state.concepts.en && this.$store.state.concepts.en['mymachine_Software'] || {}
      if (softwareConcepts) {
        for (const software of Object.keys(labels)) {
          const match = Object.keys(softwareConcepts).find(label => label === software + 'Label')
          if (match) {
            labels[software] = softwareConcepts[match]
          }
        }
      }
      return labels
    },
    softwareIcons() {
      const icons = {}
      icons['nationalServiceAnalyzer'] = this.assetServer + '/assets/img/icons/service-analyzer.svg'
      icons['serviceAnalyzer'] = this.assetServer + '/assets/img/icons/service-analyzer-red.svg'
      icons['engineAnalyzer'] = this.assetServer + '/assets/img/icons/engine-analyzer.svg'
      icons['orionBMS'] = this.assetServer + '/assets/img/icons/battery.svg'

      let softwareConcepts = this.$store.state.concepts.en && this.$store.state.concepts.en['mymachine_Software'] || {}
      if (softwareConcepts) {
        const link = /(https?:\/\/[^" ]*)/i
        for (const software of Object.keys(icons)) {
          const match = Object.keys(softwareConcepts).find(label => label === software + 'Icon')
          if (match) {
            const imageSrc = softwareConcepts[match].match(link)
            if (Array.isArray(imageSrc)) icons[software] = imageSrc[0]
          }
        }
      }
      return icons
    },
    isNewDashboardEnabled() {
      return this.getFeatureFlag('switchToNewDashboard')
    },
    ...mapGetters([
      'getCurrentMachine',
      'getCurrentMachineError',
      'getCurrentMachineLoading',
      'getCurrentAbbreviation',
      'getFeatureFlag'
    ])
  },
  watch: {
    serial(s) {
      if (s) this.getSoftware()
    }
  },
  created() {
    this.getSoftware()
  },
  methods: {
    compareVersion(v1, v2) {
      if (typeof v1 !== 'string') return false
      if (typeof v2 !== 'string') return false
      v1 = v1.split('.')
      v2 = v2.split('.')
      const k = Math.min(v1.length, v2.length)
      for (let i = 0; i < k; ++ i) {
        v1[i] = parseInt(v1[i], 10)
        v2[i] = parseInt(v2[i], 10)
        if (v1[i] > v2[i]) return 1
        if (v1[i] < v2[i]) return -1
      }
      return v1.length === v2.length ? 0 : (v1.length < v2.length ? -1 : 1)
    },
    getLatestVersion(_software_type) {
      let version = '0'
      let brand = false
      if (this.softwareData) {
        this.softwareData.forEach(e => {
          if (e.software_type === _software_type) {
            if (this.compareVersion(e.version, version) === 1) {
              version = e.version
              brand = e.brand
            }
          }
        })
      }
      if (version !== '0') return {version, brand}; else return false
    },
    resetSoftware() {
      this.softwareData = null
      this.mostRecentSA = false
      this.mostRecentEADCU = false
      this.mostRecentEAECU = false
      this.mostRecentEAV2 = false
      this.mostRecentOrionBMS = false
    },
    getSoftware(_s = this.serial) {
      if (_s) {
        // clear the previous machine's readings up front so stale versions never
        // linger while the new request is in flight (or if it fails)
        this.resetSoftware()
        this.loadingSoftware = true
        bdService.fetchProductSoftware(_s).then(r => {
          // ignore stale responses if the serial changed while in flight
          if (_s !== this.serial) return
          this.softwareData = r.data
          this.mostRecentSA = this.getLatestVersion('ServiceAnalyzer')
          this.mostRecentEADCU = this.getLatestVersion('EngineAnalyzerDCU')
          this.mostRecentEAECU = this.getLatestVersion('EngineAnalyzerECU')
          this.mostRecentEAV2 = this.getLatestVersion('EngineAnalyzerV2')
          this.mostRecentOrionBMS = this.getLatestVersion('OrionBMS')
        }).catch(() => {
          // on failure for the active serial, keep readings cleared (no stale versions)
          if (_s === this.serial) this.resetSoftware()
        }).finally(() => {
          // only clear the loading state for the request matching the current serial
          if (_s === this.serial) this.loadingSoftware = false
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
@import '../../../scss/colors';

// New-dashboard software layout (applied only when the feature flag is on
// via the .software / .software-row classes bound in the template)
.software {
  border: 0.0625rem solid $secondary;
  box-shadow: 0 0.125rem 0.25rem rgba($black, 0.1);
  border-radius: 0.25rem;
  overflow: hidden;
  align-items: center !important;
}

// even gap between the two cards using the row as a flex gap container
.software-row {
  gap: 0.5rem;

  .software {
    flex: 1 1 calc(50% - 0.25rem);
    max-width: calc(50% - 0.25rem);
  }

  @media (max-width: 767px) {
    .software {
      flex: 1 1 100%;
      max-width: 100%;
    }
  }
}
</style>