<template>
  <div
    class="bubble-grid"
    :class="{ 'bubble-grid--two-col': bubbleCount === 4 }"
  >
    <!-- Engine Hours -->
    <MachineBubble
      type="icon"
      :asset-server="assetServer"
      icon="/assets/img/icons/battery_temp.svg"
      :value="engineHours"
      unit=" h"
      :aria-label="translate('operatingHoursLabel', 'MachineDetails')"
    />

    <!-- Battery Voltage -->
    <div
      class="bubble-item d-flex flex-column align-items-center justify-content-end"
      role="img"
      :aria-label="`${translate('batteryAriaLabel', 'MachineDetails')}: ${batteryVoltageDisplay}`"
    >
      <span class="bubble-icon voltage-icon-wrapper d-inline-flex align-items-center justify-content-center position-relative">
        <img
          :class="{ 'bubble-disabled': typeof machineBatteryVoltage !== 'number' }"
          :src="batteryIcon"
          alt=""
          aria-hidden="true"
        >
        <img
          v-if="typeof machineBatteryVoltage === 'number'"
          :src="assetServer + '/assets/img/icons/vector_2.svg'"
          class="voltage-needle position-absolute z-2"
          :style="voltageNeedleStyle"
          alt=""
          aria-hidden="true"
        >
      </span>
      <span class="bubble-label fw-bold text-black text-center text-wrap" aria-hidden="true">{{ batteryVoltageDisplay }}</span>
    </div>

    <!-- Fuel Level -->
    <MachineBubble
      v-if="!electric"
      type="ring"
      :asset-server="assetServer"
      icon="/assets/img/icons/icon_fuel.svg"
      :value="fuelLevel"
      unit=" %"
      :aria-label="translate('fuelLevelLabel', 'MachineDetails')"
    />

    <!-- Electric -->
    <MachineBubble
      v-if="electric"
      type="electric"
      :asset-server="assetServer"
      :aria-label="translate('electric', 'MachineDetails')"
    />

    <!-- Def Level -->
    <MachineBubble
     v-if="showDef || !isCurrentMachineConnected"
      type="ring"
      :asset-server="assetServer"
      icon="/assets/img/icons/def_level.svg"
      :value="defLevel"
      unit=" %"
      :aria-label="translate('defLevelLabel', 'MachineDetails')"
    />
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import MachineBubble from './MachineBubble';
import { useLabels } from '@/composables/useLabels';

export default {
  conceptName: ['MachineDetails'],
  setup() { return useLabels() },
  components: {
    MachineBubble
  },
  props: {
    assetServer: {
      required: true
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentMachine',
      'isCurrentMachineConnected'
    ]),
    electric() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.equipmentType === 'electric' || false
    },
    fuelLevel() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && typeof this.getCurrentMachine.fuelLevel === 'number' ? Math.round(this.getCurrentMachine.fuelLevel) : null
    },
    machineBatteryVoltage() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && typeof this.getCurrentMachine.machineBatteryVoltage === 'number' ? this.getCurrentMachine.machineBatteryVoltage : null
    },
    // formatted battery voltage label, e.g. "13.9 V" or "- V" when unavailable
    batteryVoltageDisplay() {
      // use a non-breaking space so the value and unit stay on one line
      return typeof this.machineBatteryVoltage === 'number' ? this.machineBatteryVoltage.toFixed(1) + '\u00A0V' : '-\u00A0V'
    },
    defLevel() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.defLevel && Math.round(this.getCurrentMachine.defLevel)
    },
    // DEF bubble shows only for a real, positive reading (matches legacy CircleInfo logic)
    showDef() {
      return typeof this.defLevel === 'number' && this.defLevel >= 0
    },
    engineHours() {
      if (!this.isCurrentMachineConnected) return false
      return this.getCurrentMachine && this.getCurrentMachine.engineHours
    },
    // number of bubbles actually rendered, drives the grid layout
    bubbleCount() {
      if (!this.isCurrentMachineConnected) return 4
      let count = 3
      if (this.showDef) count += 1
      return count
    },
    
    batteryIcon() {
      const iconBase = this.assetServer + '/assets/img/icons/'
      if (typeof this.machineBatteryVoltage !== 'number') return iconBase + 'battery_null.svg'
      if (this.machineBatteryVoltage <= 10.8) return iconBase + 'battery_red.svg'
      if (this.machineBatteryVoltage <= 11.7) return iconBase + 'battery_yellow.svg'
      return iconBase + 'battery_green.svg'
    },
    voltageNeedleStyle() {
      if (typeof this.machineBatteryVoltage !== 'number') return {display: 'none'}
      if (this.machineBatteryVoltage <= 10.8) {
        // Red zone
        return {
          top: '0.238475rem',
          left: '-0.323319rem',
          transform: 'rotate(224.5deg)'
        }
      } else if (this.machineBatteryVoltage <= 11.7) {
        // Yellow zone
        return {
          top: '-0.5rem',
          left: '0.875rem',
          transform: 'rotate(288deg)'
        }
      } else {
        // Green zone
        return {
          top: '0.238475rem',
          left: '2.19831875rem',
          transform: 'rotate(357.5deg)'
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';
@import '../../../scss/grid';

.bubble-disabled {
  opacity: 0.4;
  filter: grayscale(1);

  .bubble-label {
    color: $gray-600 !important;
  }
}

.bubble-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: flex-start;
  gap: 0.75rem 0.5rem;

  // default: 3 items per row (works for 3 and 5 bubbles — 5 wraps as 3+2)
  .bubble-item {
    flex: 1 1 30%;
    max-width: 33.333%;
    min-width: 3rem;
    padding: 0.25rem;
    gap: 0.375rem;
  }

  // 4 bubbles: switch to 2 per row (2×2 grid)
  &.bubble-grid--two-col .bubble-item {
    flex: 1 1 45%;
    max-width: 50%;
  }

  // tablet: card is full-width. Keep the bubble group narrow and grouped
  // next to the (now larger) image instead of stretching wide
  @include media-breakpoint-only('md') {
    max-width: 14rem;
    margin: 0 auto;
    gap: 0.75rem 0.5rem;

    .bubble-item {
      min-width: 3.5rem;
    }
  }

  // mobile: keep the bubble group narrow and centered below the image
  // instead of stretching the three bubbles across the full card width
  @include media-breakpoint-down('md') {
    max-width: 18rem;
    margin: 0 auto;
    gap: 0.75rem 0.5rem;

    .bubble-item {
      min-width: 3rem;
      padding: 0.25rem;
      gap: 0.375rem;
    }
  }
}

.voltage-needle {
  transition: top 0.3s ease, left 0.3s ease;
  width: 0.375rem;
  height: 0.375rem;
}

.bubble-label {
  font-size: 0.875rem;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  letter-spacing: 0.025rem;
}
</style>