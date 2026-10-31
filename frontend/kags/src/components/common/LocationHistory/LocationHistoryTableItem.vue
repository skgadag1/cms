<template>
  <div class="grid-table-row overflow-hidden grid-table-row-dark py-0" :class="{'highlighted':highlighted}">
    <div class="item__dateTime">
      <span>{{ formatDate(item.startTs) }}</span><br>
      <span class="text-small">{{ formatTime(item.startTs) }}</span>
    </div>
    <div class="item__location">
      <div class="text-small">
        {{ item.reversedGeolocation !== true ? item.reversedGeolocation : '' }}
        {{ item.municipality }}
        {{ item.subRegion }}
        {{ item.region }}
        {{ item.country }}
      </div>
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
export default {
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    item: {
      type: Object,
      required: true
    },
    highlighted: {
      type: Boolean
    }
  }
}
</script>
