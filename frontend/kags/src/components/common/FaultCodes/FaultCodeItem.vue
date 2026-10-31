<template>
  <b-col cols="12"
         class="mb-1"
         @mouseenter="showIndicator = true"
         @mouseleave="showIndicator = false"
  >
    <div class="box p-2 position-relative overflow-hidden">
      <b-row>
        <b-col cols="12" class="d-flex align-items-center">
          <b-badge class="d-inline small" :variant="severityTable[code.severity]">{{ code.severity.toUpperCase() }}</b-badge>
          <span class="ms-2 font-condensed">{{ code.faultCode }}</span>
        </b-col>
        <b-col cols="12" class="font-condensed">
          {{ code.name }}
        </b-col>
        <b-col cols="12" class="small">
          {{ dateStart }} <span class="text-gray-400">|</span> {{ timeStart }} <span class="text-gray-400">|</span> {{ code.engineHoursStart }}h
        </b-col>
      </b-row>
      <ButtonIndicatorSearch :href="url + '?keyword=' + code.faultCode + '&serial=' + getCurrentSerial"
                             :hover="showIndicator"
                             :label="searchLabel"
      />
    </div>
  </b-col>
</template>

<script>
import ButtonIndicatorSearch from '@/components/common/ButtonIndicatorSearch'
import {SEVERITY_TABLE} from './FaultCodesComponent'
import {mapGetters} from 'vuex'

export default {
  components: {
    ButtonIndicatorSearch
  },
  props: {
    code: {
      type: Object,
      required: true
    },
    url: {
      type: String,
      required: true
    },
    searchLabel: {
      type: String,
      required: false,
      default: 'Search code'
    }
  },
  data() {
    return {
      showIndicator: false,
      severityTable: SEVERITY_TABLE
    }
  },
  computed: {
    startDate() {
      return new Date(this.code.startTime)
    },
    dateStart() {
      return this.startDate.toLocaleDateString()
    },
    timeStart() {
      return this.startDate.toLocaleTimeString()
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  }
}
</script>
