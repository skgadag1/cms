<template>
  <div>
    <div class="circle m-auto m-md-0" :class="[isReady ? 'p' + value : '', color, disabled ? 'disabled' : '']">
      <div class="circle-wrap">
        <div class="circle-guideline" />
        <div class="cover full">
          <div class="fill" />
        </div>
        <div class="cover half">
          <div class="fill" />
        </div>
        <div class="circle-overlay">
          <svg v-if="color === 'gauge'" viewBox="0 0 40 40">
            <g class="marks">
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
              <line x1="18" y1="0" x2="19" y2="0" />
            </g>
          </svg>
        </div>
        <div class="circle-inside">
          <div><div class="circle-value">{{ !disabled && displayValue ? display.toFixed(precision) : '' }}<slot v-if="disabled" /><slot v-else name="symbol" /></div></div>
          <div class="d-none d-md-block"><div class="circle-label">{{ label }}</div></div>
        </div>
      </div>
    </div>
    <div class="d-block d-md-none text-center mt-1 font-condensed">{{ label }}</div>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      required: false,
      type: Number,
      default: 0
    },
    color: {
      required: false,
      type: String,
      default: 'gray'
    },
    disabled: {
      required: false,
      type: Boolean,
      default: true
    },
    label: {
      required: true,
      type: String
    },
    precision: {
      required: false,
      type: Number,
      default: 0
    },
    displayValue: {
      required: false,
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      display: 0,
      isReady: false,
      interval: null
    }
  },
  watch: {
    value(val) {
      this.animate(val)
    }
  },
  mounted() {
    setTimeout(() => {this.isReady = true}, 100)
    this.animate(this.value)
  },
  methods: {
    easeInOut(t) {
      return t > 0.5 ? 4 * Math.pow((t - 1), 3) + 1 : 4 * Math.pow(t, 3)
    },
    animate(target) {
      clearInterval(this.interval)
      const start = this.display
      const interval = 50 //refresh rate in miliseconds
      const duration = 600 //miliseconds
      let progress = 0
      this.interval = setInterval(() => {
        progress += interval
        this.display = Math.round((start + (start * -1) + target) * this.easeInOut(progress / duration) * Math.pow(10, this.precision)) / Math.pow(10, this.precision)
        if (progress >= duration || this.display >= target) clearInterval(this.interval)
      }, interval)
    }
  }
}
</script>
