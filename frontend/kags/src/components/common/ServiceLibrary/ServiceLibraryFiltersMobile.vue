<template>
  <div class="bg-white shadow">
    <div class="p-3">
      <b-button :disabled="disabled" class="w-100 font-condensed" :variant="isExpanded ? 'danger' : 'primary'" @click="isExpanded = !isExpanded">
        <b-icon icon="filter" class="float-start me-1" />{{ translate('selectFiltersLabel', 'CommonFunctional') }}
      </b-button>

      <b-collapse id="filters" v-model="isExpanded" class="mt-3">
        <b-button v-if="Object.keys(facetFilter).length > 0 && Object.values(facetFilter).reduce((r, k) => { return r + k.length }, 0)" variant="outline-primary" class="w-100 font-condensed" @click="$emit('clearAll')">
          {{ translate('clearAll', 'CommonFunctional') }}
        </b-button>

        <template v-for="(name, i) in Object.keys(aggregations)" :key="name">
          <hr v-if="i !== 0" class="m-0">

          <div class="p-3">
            <h4 v-b-toggle="'aggredation_'+ name">{{ translate((name === 'locale_filter' ? 'language_filter' : name)) }} ({{ aggregations[name]._counter }}) <b-icon class="float-end text-large" icon="caret-up" /></h4>

            <b-collapse :id="'aggredation_'+ name">
              <b-link v-if="(name in facetFilter) && facetFilter[name].length > 0"
                      class="d-block font-condensed"
                      :disabled="disabled"
                      @click.prevent="$emit('clear', name)"
              >
                {{ translate('clearLabel', 'CommonFunctional') }}
              </b-link>

              <b-form-checkbox-group v-model="facetFilter[name]" stacked>
                <template v-for="(count, facet) in aggregations[name]" :key="'facet_' + facet">
                  <b-form-checkbox v-if="facet !== '_counter'"
                                   :value="facet"
                                   :disabled="!facetFilter[name].includes(facet) && count === 0"
                  >
                    {{ name === 'locale_filter' ? translateLocale(facet) : facet }}
                    ({{ count }})
                  </b-form-checkbox>
                </template>
              </b-form-checkbox-group>
            </b-collapse>
          </div>
        </template>

        <slot />
      </b-collapse>
    </div>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import langs from 'langs'

export default {
  conceptName: ['CommonFunctional'],
  setup() { return useLabels() },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    aggregations: {
      required: true
    },
    facetFilter: {
      required: true
    }
  },
  data() {
    return {
      isExpanded: false,
      langs: langs
    }
  }
}
</script>

<style scoped lang="scss">
  h4 {
    span {
      transition: .2s transform;
    }
    &.collapsed {
      span {
        transform: rotate(180deg);
      }
    }
  }
</style>
