<template>
  <b-form-checkbox-group v-model="selected"
                         stacked
                         button-variant="secondary"
                         @change="emitInput"
  >
    <b-form-checkbox v-for="(option, i) in itemsVisible"
                     :key="`${option.id}-${i}`"
                     :value="option.id"
                     :disabled="!option.count"
    >
      <span v-html="option.name" /> ({{ option.count || 0 }})
    </b-form-checkbox>

    <template v-if="itemsHidden.length">
      <b-collapse :id="`facets-${facet.filterField}-${MAX_ITEMS}`">
        <b-form-checkbox v-for="(option,i) in itemsHidden"
                         :key="`${option.id}-${i}`"
                         :value="option.id"
                         :disabled="!option.count"
        >
          <span v-html="option.name" /> ({{ option.count || 0 }})
        </b-form-checkbox>
      </b-collapse>
      <div v-b-toggle="`facets-${facet.filterField}-${MAX_ITEMS}`" class="cursor-pointer mt-1 font-weight-bold text-primary">
        <div class="when-open">
          <div class="d-flex align-items-center">
            <b-icon icon="minus" class="mr-2" /><span :title="generateConceptHint('showLess')">{{ labels.showLess || 'Show less' }}</span>
          </div>
        </div>
        <div class="when-closed">
          <div class="d-flex align-items-center">
            <b-icon icon="plus" class="mr-2" /><span :title="generateConceptHint('showMore')">{{ labels.showMore || 'Show more' }}</span>
          </div>
        </div>
      </div>
    </template>
  </b-form-checkbox-group>
</template>

<script>
import {ref, watch, useFetch, useContext, computed} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'

const Combinators = {
  OR: 'OR',
  AND: 'AND'
}

export default {
  props: {
    facet: {
      type: Object,
      required: true
    },
    value: {
      type: Array,
      required: true
    },
    expandable: {
      type: Boolean,
      default: true
    },
    showEmptyFacets: Boolean,
    includeFacetLabel: Boolean
  },
  setup(props, {emit}) {
    const {facet, expandable, showEmptyFacets, includeFacetLabel} = props
    const {$services} = useContext()
    const facetFilterPart = (includeFacetLabel && facet.label) ? `~${facet.label}` : ''
    const facetFilteredName = facet.filterField + facetFilterPart + ':'
    const selected = ref([])
    const MAX_ITEMS = 5
    const labels = ref({})
    const {generateConceptHint} = useConcepts()
    const itemsWithValues = computed(() => props.facet.values?.filter(x => showEmptyFacets || x.count > 0) || [])
    const shouldExpand = expandable && itemsWithValues.value.length > MAX_ITEMS
    const combinator = facet.combineAs || Combinators.AND
    // Each CheckboxGroupFacet receives all selections for a particular filterField. ex: tags.value
    // and its expected to re-emit all those values again (including those not available here) if
    // selection changes here
    const unknownSelections = ref([])

    const itemsVisible = computed(() => shouldExpand ? itemsWithValues.value.slice(0, MAX_ITEMS) : itemsWithValues.value)
    const itemsHidden = computed(() => shouldExpand ? itemsWithValues.value.slice(MAX_ITEMS) : [])

    const hasKnownValues = filteredFacet => filteredFacet.replace(facetFilteredName, '')
      .split(';')
      .every(value => itemsWithValues.value.some(v => v.id === value))

    const getValues = filteredFacet => filteredFacet.replace(facetFilteredName, '').split(';')

    const serialize = (values) => {
      if (!values || values.length === 0) {
        return unknownSelections.value
      }

      switch (combinator) {
      case Combinators.OR:
        return [...unknownSelections.value, facetFilteredName + selected.value.join(';')]
      default:
        return unknownSelections.value.concat(values.map(v => facetFilteredName + v))
      }
    }

    const deserialize = (values) => {
      if (!values || values.length === 0) {
        unknownSelections.value = []
        return []
      }

      values = values.filter(x => x.startsWith(`${facet.filterField}:`) || x.startsWith(`${facet.filterField}~`))
      unknownSelections.value = values.filter(x => !x.startsWith(facetFilteredName) || !hasKnownValues(x))
      return values.filter(x => x.startsWith(facetFilteredName)).filter(hasKnownValues).flatMap(getValues)
    }

    const emitInput = () => {
      emit('input', facet.filterField, serialize(selected.value))
    }

    if (shouldExpand) {
      useFetch(async () => {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      })
    }

    watch(() => props.value, (s) => {
      selected.value = deserialize(s)
    }, {immediate: true})

    return {
      MAX_ITEMS,
      itemsVisible,
      generateConceptHint,
      itemsHidden,
      selected,
      labels,
      emitInput
    }
  }
}
</script>

<style lang="scss" scoped>
.collapsed > .when-open,
.not-collapsed > .when-closed {
  display: none;
}
</style>
