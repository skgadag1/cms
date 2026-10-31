<template>
  <div>
    <div class="d-flex align-items-end">
      <div>
        <label :for="`input-min-${facet.id}`" class="p-0" :title="generateConceptHint('min')">{{ labels.min || 'Min' }}</label>
        <b-form-input :id="`input-min-${facet.id}`"
                      v-model="rangeInput[0]"
                      lazy
                      :label="labels.min || 'Min'"
                      @focus="(e) => toggleRangeInputUnits(false, FIELD.MIN, e)"
                      @blur="(e) => toggleRangeInputUnits(true, FIELD.MIN, e)"
                      @change="(val) => changeValue(FIELD.MIN, val)"
        />
      </div>
      <div class="p-2" :title="generateConceptHint('to')">{{ labels.to || 'to' }}</div>
      <div>
        <label :for="`input-max-${facet.id}`" class="p-0" :title="generateConceptHint('max')">{{ labels.max || 'Max' }}</label>
        <b-form-input :id="`input-max-${facet.id}`"
                      v-model="rangeInput[1]"
                      lazy
                      :label="labels.max || 'Max'"
                      @focus="(e) => toggleRangeInputUnits(false, FIELD.MAX, e)"
                      @blur="(e) => toggleRangeInputUnits(true, FIELD.MAX, e)"
                      @change="(val) => changeValue(FIELD.MAX, val)"
        />
      </div>
    </div>
    <client-only>
      <VueSlider v-if="renderSlider"
                 v-model="range"
                 :min="min"
                 :max="max"
                 :interval="1"
                 :tooltip="'none'"
                 dot-size="20"
                 class="p-1 pt-4"
                 @drag-start="sliderDragStart"
                 @drag-end="sliderDragEnd"
                 @change="changeValue(FIELD.ALL)"
      >
        <template #dot="{ focus }">
          <div :class="['custom-dot', { focus }]" />
        </template>
      </VueSlider>
    </client-only>
  </div>
</template>

<script>
import {ref, watch, onBeforeMount, onMounted} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  components: {
    VueSlider: () => import('vue-slider-component/dist/vue-slider-component.common')
  },
  props: {
    facet: {
      type: Object,
      required: true
    },
    value: {
      type: Array,
      required: true
    },
    labels: Object,
    includeFacetLabel: Boolean
  },
  setup(props, {emit}) {
    const facetOpt = ref(props.facet)
    const min = Math.floor(facetOpt.value.min)
    const max = Math.ceil(facetOpt.value.max)
    const range = ref([min, max])
    const rangeInput = ref([...range.value])
    const {generateConceptHint} = useConcepts()
    const unit = props.facet.unit
    const renderSlider = ref(false)

    const facetFilterPart = (props.includeFacetLabel && facetOpt.value.label) ? `~${facetOpt.value.label}` : ''
    const facetFilteredName = facetOpt.value.filterField + facetFilterPart + ':'

    const FIELD = {
      MIN: 0,
      MAX: 1,
      ALL: 2
    }

    const sliderDragging = ref(false)

    const sliderDragStart = () => {
      sliderDragging.value = true
      clearDebounceEmit()
    }

    const sliderDragEnd = (e, v) => {
      sliderDragging.value = false
      changeValue(FIELD.ALL)
    }

    const validateRange = (_range) => {
      if (_range[0] > _range[1]) { _range = [_range[1], _range[0]] }
      if (isNaN(_range[0]) || _range[0] < min) { _range[0] = min }
      if (isNaN(_range[1]) || _range[1] > max) { _range[1] = max }
      return _range
    }

    const extractValues = (str) => {
      try {
        str = str.split(':').pop()
        const values = str.split('!')
        return values.map(x => Number(x))
      } catch (e) {
        return [min, max]
      }
    }

    const emitInputEvent = () => {
      const newValue = !(range.value[0] === min && range.value[1] === max) ? `${facetFilteredName}${range.value[0]}!${range.value[1]}` : []
      emit('input', facetOpt.value.filterField, newValue)
    }

    let timer
    const debounceEmit = (timeout = 800) => {
      clearTimeout(timer)
      timer = setTimeout(() => { emitInputEvent() }, timeout)
    }

    const clearDebounceEmit = () => {
      clearTimeout(timer)
    }

    const changeValue = (field = FIELD.ALL, val = null) => {
      const valueToValidate = range.value
      val && field === FIELD.MIN && (valueToValidate[0] = Number(val))
      val && field === FIELD.MAX && (valueToValidate[1] = Number(val))
      range.value = validateRange(valueToValidate)
      JSON.stringify(range.value) !== JSON.stringify(valueToValidate) && toggleRangeInputUnits(true)
      sliderDragging.value ? toggleRangeInputUnits(true) : debounceEmit()
      val && emitInputEvent()
    }

    const toggleRangeInputUnits = (mode = true, field = FIELD.ALL, e = null) => {
      e && e.preventDefault()
      if (!unit) { mode = false }
      const suffix = mode ? ` ${unit}` : ''
      field !== FIELD.MAX && (rangeInput.value = [`${range.value[0]}${suffix}`, rangeInput.value[1]])
      field !== FIELD.MIN && (rangeInput.value = [rangeInput.value[0], `${range.value[1]}${suffix}`])
    }

    const valuesChanged = (s) => {
      if (s) {
        const relatedItem = s?.find(x => x?.startsWith(facetFilteredName))
        range.value = relatedItem ? extractValues(relatedItem) : [min, max]
        toggleRangeInputUnits(true)
      }
    }

    onBeforeMount(() => valuesChanged(props.value))
    onMounted(() => { renderSlider.value = typeof window !== 'undefined' && window.document })
    watch(() => props.value, s => valuesChanged(s))

    toggleRangeInputUnits(true)

    return {
      renderSlider,
      range,
      rangeInput,
      changeValue,
      sliderDragStart,
      sliderDragEnd,
      clearDebounceEmit,
      min,
      max,
      FIELD,
      toggleRangeInputUnits,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/slider_theme';
</style>
