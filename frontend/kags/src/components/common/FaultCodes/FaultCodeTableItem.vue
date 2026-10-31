<template>
  <div class="grid-table-row position-relative overflow-hidden"
       :class="{ 'disabled': isDisabled }"
  >
    <div class="item__status">{{ capitalize(code.status) }}</div>
    <div class="item__severity"><b-badge :variant="severityTable[code.severity]" class="text-uppercase">{{ code.severity }}</b-badge></div>
    <div class="item__code clearfix"><span class="font-condensed">{{ code.faultCode }}</span></div>
    <div class="item__description"><span class="font-condensed">{{ code.name }}</span></div>
    <div class="item__date">
      <span class="d-none d-md-inline">{{ code.startTimeLocalized }}</span>
      <span class="d-md-none">{{ dateStart }} | {{ timeStart }}</span>
    </div>
    <div class="item__hours text-center"><span class="d-md-none">| </span>{{ code.engineHoursStart }}h</div>
    <div class="button-indicator-wrapper"
         @mouseenter.prevent="showIndicator = true"
         @mouseleave="showIndicator = false"
    >
      <ButtonIndicatorSearch :href="url + '?keyword=' + code.faultCode"
                             :hover="showIndicator"
                             :label="searchLabel"
      />
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import ButtonIndicatorSearch from '@/components/common/ButtonIndicatorSearch'
import {SEVERITY_TABLE} from '@/components/common/FaultCodes/FaultCodesComponent'

export default {
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    ButtonIndicatorSearch
  },
  props: {
    url: {
      type: String,
      required: true
    },
    code: {
      type: Object,
      required: true
    },
    searchLabel: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      showIndicator: false,
      severityTable: SEVERITY_TABLE
    }
  },
  computed: {
    isDisabled() {
      return this.code.status.toUpperCase() !== 'ACTIVE'
    },
    startDate() {
      return (new Date(this.code.startTime)).toLocaleString()
    },
    dateStart() {
      return this.startDate.split(',')[0]
    },
    timeStart() {
      return this.startDate.split(',')[1]
    },
    hourDiff() {
      return this.code.engineHoursEnd - this.code.engineHoursStart
    }
  }
}
</script>
