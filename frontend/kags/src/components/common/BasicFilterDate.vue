<template>
  <BasicFilterItem ref="basicFilter"
                   class="filter-item__date"
                   :title="title"
                   @show="handleShow"
                   @hide="handleHide"
  >
    <div class="p-2 small">
      <b-form-radio v-model="options.value" :value="options.week">Last 7 Days</b-form-radio>
      <b-form-radio v-model="options.value" :value="options.month">Last 30 Days</b-form-radio>
      <b-form-radio v-model="options.value" value="custom" class="mb-2">Custom Date Range</b-form-radio>

      <date-picker v-model="options.custom[0]"
                   placeholder="Start date"
                   :text-input="true"
                   format="yyyy-MM-dd"
                   :enable-time-picker="false"
                   auto-apply
                   :disabled-dates="startDateValidation"
                   @click="options.value = 'custom'"
      />

      <div class="text-center" :class="{ 'text-gray': options.value !== 'custom' }">{{ translate('toLabel', 'CommonDateTime') }}</div>

      <date-picker v-model="options.custom[1]"
                   placeholder="End date"
                   :text-input="true"
                   format="yyyy-MM-dd"
                   :enable-time-picker="false"
                   auto-apply
                   :disabled-dates="endDateValidation"
                   @click="options.value = 'custom'"
      />
    </div>
  </BasicFilterItem>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BasicFilterItem from '@/components/common/BasicFilterItem'

export default {
  conceptName: ['CommonDateTime'],
  setup() { return useLabels() },
  components: {
    BasicFilterItem
  },
  props: {
    title: {
      type: String,
      required: true
    },
    options: {
      type: Object,
      required: true
    },
    allowFutureDates: {type: Boolean, default: false}
  },
  data() {
    return {
      internalOptions: null
    }
  },
  methods: {
    handleShow() {
      this.internalOptions = JSON.parse(JSON.stringify(this.options))
    },
    handleHide() {
      if (this.options.value === 'custom' && !this.options.custom[0] && !this.options.custom[1]) this.options.value = this.options.all
      if (this.options.custom[1]) this.options.custom[1].setHours(23, 59, 59, 999)
      if (this.options && this.internalOptions) {
        if (this.internalOptions.value !== this.options.value || this.internalOptions.custom[0] !== this.options.custom[0] || this.internalOptions.custom[1] !== this.options.custom[1]) {
          this.$emit('updated')
        }
      }
    },
    startDateValidation(d) {
      const endDate = new Date(this.options.custom[1])
      if (!this.allowFutureDates) {
        const today = new Date()
        return this.options.custom[1] ? d > today || d > endDate : d > today
      }
      return this.options.custom[1] ? d > endDate : false
    },

    endDateValidation(d) {
      const startDate = new Date(this.options.custom[0])
      if (!this.allowFutureDates) {
        const today = new Date()
        return this.options.custom[0] ? d > today || d < startDate : d > today
      }
      return this.options.custom[0] ? d < startDate : false
    }
  }
}
</script>
<style lang="scss">
@import '../../scss/z-index';

.dp__main {
  width: 11rem;

  .dp__menu {
    z-index: $zindex-modal-backdrop;
  }
}
</style>
