<template>
  <div>
    {{ formattedDistance }} {{ selectedUnit }}
  </div>
</template>

<script>
import {computed} from '@nuxtjs/composition-api'

export default {
  props: {
    distance: {
      type: Object
    },
    miLabel: {
      type: String,
      required: true
    },
    kmLabel: {
      type: String,
      required: true
    },
    unit: {
      type: String,
      default: 'mi'
    }
  },
  setup(props) {
    const {distance} = props
    const distanceValue = distance?.value ? distance.value / 1000 : null

    const formattedDistance = computed(() => {
      return distanceValue ? (props.unit === 'km' ? (distanceValue * 1.60934).toFixed(1) : distanceValue.toFixed(1)) : null
    })

    const selectedUnit = computed(() => {
      if (props.unit === 'km') {
        return props.kmLabel || 'km' // Default to km if no label is provided
      } else if (props.unit === 'mi') {
        return props.miLabel || 'mi' // Default to mi if no label is provided
      }
      return 'mi' // Default to mi if no unit is provided
    })

    return {
      formattedDistance,
      selectedUnit
    }
  }
}
</script>

  <style scoped>
  .time-to-destination {
    font-family: Arial, sans-serif;
  }
  </style>
