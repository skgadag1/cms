<template>
  <div
    class="bubble-item d-flex flex-column align-items-center justify-content-end"
    role="img"
    :aria-label="type === 'electric' ? ariaLabel : `${ariaLabel}: ${displayValue}`"
  >
    <!-- Ring variant: eclipse bg + progress ring + centered icon (fuel, DEF, ...) -->
    <div
      v-if="type === 'ring'"
      class="fuel-bubble d-flex align-items-center justify-content-center position-relative"
    >
      <img
        class="fuel-bg position-absolute"
        :class="{ 'bubble-disabled': !hasValue }"
        :src="assetServer + '/assets/img/icons/eclipse_32.svg'"
        alt=""
        aria-hidden="true"
      >
      <svg
        class="fuel-ring position-absolute"
        viewBox="0 0 39 39"
        aria-hidden="true"
        focusable="false"
      >
        <circle
          cx="19.5"
          cy="19.5"
          r="18"
          fill="none"
          :class="ringColor"
          stroke-width="3"
          stroke-linecap="round"
          :stroke-dasharray="ringCircumference"
          :stroke-dashoffset="ringOffset"
          transform="rotate(-90 19.5 19.5)"
        />
      </svg>
      <img
        class="fuel-icon position-relative z-1"
        :class="{ 'bubble-disabled': !hasValue }"
        :src="assetServer + icon"
        alt=""
        aria-hidden="true"
      >
    </div>

    <!-- Electric variant: eclipse bg + bolt icon, no ring / no label -->
    <div
      v-else-if="type === 'electric'"
      class="fuel-bubble d-flex align-items-center justify-content-center position-relative rounded-circle"
    >
      <img
        class="fuel-bg position-absolute"
        :src="assetServer + '/assets/img/icons/eclipse_32.svg'"
        alt=""
        aria-hidden="true"
      >
      <b-icon class="text-blue position-relative z-1" icon="bolt" aria-hidden="true" />
    </div>

    <!-- Icon variant: single icon (engine hours, ...) -->
    <span v-else class="bubble-icon">
      <img
        :class="{ 'bubble-disabled': !hasValue }"
        :src="assetServer + icon"
        alt=""
        aria-hidden="true"
      >
    </span>

    <!-- Label (all variants except electric) -->
    <span
      v-if="type !== 'electric'"
      class="bubble-label fw-bold text-black text-center text-wrap"
      aria-hidden="true"
    >{{ displayValue }}</span>
  </div>
</template>

<script>
const ringCircumference = 2 * Math.PI * 18

export default {
  props: {
    assetServer: {
      required: true
    },
    // 'ring' | 'icon' | 'electric'
    type: {
      type: String,
      default: 'ring'
    },
    // relative icon path, e.g. '/assets/img/icons/icon_fuel.svg'
    icon: {
      type: String,
      default: ''
    },
    // numeric reading (or false/null when unavailable)
    value: {
      default: null
    },
    // unit appended to the value, e.g. '%', ' h'
    unit: {
      type: String,
      default: ''
    },
    // accessible name (translated label)
    ariaLabel: {
      type: String,
      default: ''
    }
  },
  computed: {
    hasValue() {
      return typeof this.value === 'number' && !isNaN(this.value)
    },
    ringCircumference() {
      return ringCircumference
    },
    displayValue() {
      // join value and unit with a non-breaking space so the unit never wraps
      // to its own line (e.g. "1905.45 h" stays together)
      const unit = this.unit.replace(/^\s+/, '')
      const sep = unit ? '\u00A0' : ''
      return this.hasValue ? `${this.value}${sep}${unit}` : `-${sep}${unit}`
    },
    ringColor() {
      // no/invalid reading -> neutral grey
      if (!this.hasValue) return 'class_gray' // $gray-400
      if (this.value <= 25) return 'class_red' // $red / danger
      if (this.value <= 50) return 'class_yellow' // $yellow / warning
      return 'class_green' // $green / success
    },
    ringOffset() {
      if (!this.hasValue) return ringCircumference
      const percent = Math.min(Math.max(this.value, 0), 100)
      if (percent === 100) return 0
      const maxFill = 0.95
      const fillPercent = Math.min(percent / 100, maxFill)
      return ringCircumference - fillPercent * ringCircumference
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';

.bubble-disabled {
  opacity: 0.4;
  filter: grayscale(1);
}

.fuel-bubble {
  width: 2.4375rem;
  height: 2.4375rem;

  .fuel-bg,
  .fuel-ring {
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  // keep the grey background disc concentric with (and just inside) the ring
  .fuel-bg {
    top: 50%;
    left: 50%;
    width: 90%;
    height: 90%;
    transform: translate(-50%, -50%);
  }

  .fuel-icon {
    width: 0.875rem;
    height: 0.875rem;
  }
}

.bubble-label {
  font-size: 0.875rem;
  font-style: normal;
  font-weight: 700;
  line-height: normal;
  letter-spacing: 0.025rem;
}
.class_green {
  stroke: $green;
}
.class_yellow {
  stroke: $yellow;
}
.class_red {
  stroke: $red;
}
.class_gray {
  stroke: $gray-400;
}
</style>
