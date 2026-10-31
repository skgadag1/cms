<template>
  <section v-if="modelsToCompare.length">
    <CompareStripe :models="modelsToCompare"
                   :labels="labels"
                   :options="options.filter(x => !modelsToCompare.some(z => x['@id'] === z['@id']))"
                   :disabled="disabled"
                   @activate="$emit('activate')"
                   @remove="(e) => { removeFromCompare(e); $emit('remove', e) }"
                   @add="(e) => { $ga4.events.product_compare_click(e); addToCompare(e)}"
                   @compare="$emit('compare', modelsToCompare)"
    />

    <div v-show="!disabled" class="container my-5">
      <div class="row">
        <div v-for="m in options"
             :key="`specification-compare__${m['@id']}`"
             class="col-6 col-sm-4 col-md-3 col-lg-2 mb-3"
        >
          <CompareItem :model="m"
                       :selected="modelsToCompare.some(x => x['@id'] === m['@id'])"
                       :disabled="modelsToCompare.length === possibleToCompare"
                       :is-removable="m['@id'] !== model['@id']"
                       :compare-label="labels.compare || 'Compare'"
                       @add="(e) => { $ga4.events.product_compare_click(e); addToCompare(e)}"
                       @remove="removeFromCompare"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import {inject, useContext} from '@nuxtjs/composition-api'
import useCompare from '@/helpers/composables/useCompare'

export default {
  inject: ['model'],
  provide: ['model'],
  props: {
    options: {
      type: Array,
      required: true
    },
    labels: {
      type: Object,
      required: false,
      default() {
        return {}
      }
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  setup() {
    const {$ga4} = useContext()
    const currentModel = inject('model')
    const possibleToCompare = 3

    const {
      addToCompare,
      removeFromCompare,
      modelsToCompare,
      clearAll
    } = useCompare([currentModel], 1)

    return {
      possibleToCompare,
      modelsToCompare,
      addToCompare,
      removeFromCompare,
      clearAll,
      $ga4
    }
  }
}
</script>
