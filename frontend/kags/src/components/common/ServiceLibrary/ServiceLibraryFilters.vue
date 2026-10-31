<template>
  <div>
    <template v-for="(name, i) in Object.keys(aggregations)" :key="name">
      <div v-if="i === 0" class="px-3 pt-3">
        <div class="d-flex justify-content-between">
          <h3 v-html="translatedConfig('refineSearch')" />

          <b-link v-if="Object.keys(facetFilter).length > 0 && Object.values(facetFilter).reduce((r, k) => { return r + k.length }, 0)" class="flex-column small mb-2 font-condensed" @click.prevent="$emit('clearAll')">
            {{ translate('clearAll', 'CommonFunctional') }}
          </b-link>
        </div>
      </div>
      <hr v-else class="m-0">
      <div class="p-3">
        <div class="d-flex justify-content-between">
          <h4>{{ translate((name === 'locale_filter' ? 'language_filter' : name)) }} ({{ aggregations[name]._counter }})</h4>

          <b-link v-if="(name in facetFilter) && facetFilter[name].length > 0" class="flex-column small font-condensed" @click.prevent="$emit('clear', name)">
            {{ translate('clearLabel', 'CommonFunctional') }}
          </b-link>
        </div>
        <b-form-checkbox-group v-model="facetFilter[name]" stacked>
          <template v-for="(count, facet) in aggregations[name]" :key="'facet_' + facet">
            <b-form-checkbox v-if="facet !== '_counter'"
                             :value="facet"
                             :disabled="!facetFilter[name].includes(facet) && count === 0"
            >
              {{ name === 'locale_filter' ? translateLocale(facet) : facet }} ({{ count }})
            </b-form-checkbox>
          </template>
        </b-form-checkbox-group>
      </div>
    </template>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import langs from 'langs'

export default {
  conceptName: ['CommonFunctional'],
  setup() { return useLabels() },
  props: {
    aggregations: {
      required: true
    },
    facetFilter: {
      required: true
    }
  },
  data() {
    return {
      langs: langs
    }
  }
}
</script>
