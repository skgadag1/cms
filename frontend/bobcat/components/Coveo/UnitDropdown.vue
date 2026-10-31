<template>
  <div class="unit-selector">
    <label v-if="unitsLabel" class="unit-select-label" for="unit-select">{{ unitsLabel }}</label>
    <div class="custom-dropdown" @click="toggleDropdown">
      <div v-if="selectedUnitText" class="dropdown-selected">{{ selectedUnitText }}</div>
      <div class="arrow" :class="{ open: isOpen }" />
      <ul v-if="isOpen" class="dropdown-options">
        <li
          v-for="unit in units"
          :key="unit.value"
          class="dropdown-option"
          @click.stop="selectUnit(unit.value)"
        >
          <span v-if="unit.text">{{ unit.text }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import {loadGenericAnalyticsActions} from '@coveo/headless'
import {computed, ref, useContext} from '@nuxtjs/composition-api'

export default {
  props: {
    unitsLabel: {
      type: String
    },
    kilometerLabel: {
      type: String
    },
    milesLabel: {
      type: String
    },
    engine: {
      type: Object
    }
  },
  emits: ['change-engine-context'],
  setup(props, {emit}) {
    const {$ga4, $regions} = useContext()
    const {kilometerLabel, milesLabel, engine} = props
    const isOpen = ref(false)
    const selectedUnit = $regions?.current?.name === 'na' ? ref('mi') : ref('km')
    const units = ref([
      {value: 'km', text: kilometerLabel},
      {value: 'mi', text: milesLabel}
    ])
    const coveoEventPayload = ref(null)
    const eventType = 'dealer_locator_event'
    const gtmEventMapping = {
      unitSwitch: 'unit_switch'
    }
    const regionInfo = {
      name: $regions?.current?.name
    }

    const selectedUnitText = computed(() => {
      const unit = units.value.find(u => u.value === selectedUnit.value)
      return unit?.text
    })

    const toggleDropdown = () => {
      isOpen.value = !isOpen.value
    }

    const logGtmEvents = (eventPayload) => {
      $ga4.events.coveo_analytics_events(eventPayload)
    }

    const setEngineAdditionalContext = (additionalContext) => {
      emit('change-engine-context', undefined, false, additionalContext)
    }

    // Logs Analytic Events to Coveo and GTM
    const logAnalytics = (cause) => {
      const {logCustomEvent} = loadGenericAnalyticsActions(engine)
      // Set engine context so that selected unit can be logged in event
      const additionalContext = {unit: selectedUnitText.value?.toLowerCase()}
      setEngineAdditionalContext(additionalContext)

      // Log event to Coveo
      engine.dispatch(logCustomEvent(coveoEventPayload.value))

      // Log event to GTM
      const gtmEventPayload = {
        cause: gtmEventMapping[cause],
        unit: selectedUnitText.value?.toLowerCase()
      }
      logGtmEvents(gtmEventPayload)
    }

    const setCoveoEventPayload = (cause) => {
      coveoEventPayload.value = {
        evt: cause,
        type: eventType
      }
    }

    const selectUnit = (value) => {
      selectedUnit.value = value
      isOpen.value = false
      emitUnit()
      // Log Analytics for unit selection but only for non-NA regions
      if (regionInfo.name !== 'na') {
        const cause = 'unitSwitch'
        setCoveoEventPayload(cause)
        logAnalytics(cause)
      }
    }

    const emitUnit = () => {
      emit('unit-selected', selectedUnit.value)
    }

    return {
      isOpen,
      selectedUnit,
      units,
      toggleDropdown,
      selectUnit,
      selectedUnitText
    }
  }
}
</script>

  <style scoped lang="scss">
  .unit-selector {
    display: flex;
    align-items: center;
    margin-bottom: 0px;
  }

  .unit-selector label {
    margin-right: 10px;
  }

  .unit-select-label {
    margin-bottom: 0px;
    font-size: 1rem;
  }

  .custom-dropdown {
    position: relative;
    display: inline-block;
    cursor: pointer;
    border: 1px solid #777;
    border-radius: 4px;
    padding: 10px 15px;
    min-width: 150px;
    background-color: #ffffff;
    font-size: 1rem;
    }

  .arrow {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%) rotate(0deg);
    border: solid transparent;
    border-width: 6px 6px 0 6px;
    border-top-color: #333;
    transition: transform 0.2s ease;
  }

  .arrow.open {
    transform: translateY(-50%) rotate(180deg);
  }

  .dropdown-options {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: #ffffff;
    font-size: 1rem;
    border: 1px solid #777;
    border-radius: 4px;
    z-index: 100;
    max-height: 150px;
    overflow-y: auto;
    list-style-type: none;
    padding: 0;
    margin: 0;
  }

  .dropdown-option {
    padding: 10px 15px;
    cursor: pointer;
  }

  .dropdown-option:hover {
    background-color: #f0f0f0;
  }
  </style>
