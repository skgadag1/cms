<template>
  <div class="position-absolute compare-models-dropdown small" :style="{'right':right ? 0 : 'auto'}" data-test-id="model-selector-dropdown">
    <div class="compare-models-dropdown-wrapper">
      <b-dropdown ref="ddown"
                  class="border border-light shadow-sm"
                  menu-class="border border-light shadow-sm"
                  block
                  :right="right"
                  @hide="$emit('hide')"
                  @show="$emit('show')"
      >
        <template #button-content>
          <span class="mr-3"
                data-test-id="model-selector-dropdown__select-label"
                :title="generateConceptHint('selectModel')"
          >
            {{ selectModelLabel }}
          </span>
        </template>
        <b-dropdown-item v-for="(o, i) in options"
                         :key="`model-${o.id}__${o['@id'] || o.name || o.title}_${i}`"
                         :data-test-id="`model-selector-dropdown__option-${i}`"
                         @click="$emit('add', o)"
        >
          {{ o.title || o['@name'] }}
        </b-dropdown-item>
      </b-dropdown>
    </div>
  </div>
</template>

<script>
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  props: {
    options: {
      type: Array,
      required: true
    },
    right: {
      type: Boolean,
      required: false
    },
    selectModelLabel: {
      type: String,
      required: false,
      default: 'Select a model'
    }
  },
  setup() {
    const id = Math.random().toString(12).substr(2, 10)
    const {generateConceptHint} = useConcepts()

    return {
      id,
      generateConceptHint
    }
  },
  created() {
    this.$emit('hide') // hack for hiding dropdown when creating item
  },
  methods: {
    open() {
      this.$refs.ddown.$refs.toggle.click()
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.compare-models-dropdown {
  z-index: $zindex-modal;

  .btn {
    font-family: $font-family-base;
    font-size: 1rem;
  }

  .dropdown{
    &-item {
      padding: .5rem 1rem;
    }

    &-menu.show {
      max-height: min(90vh, 410px); // 410px ~= 10*itemHeight
      overflow: auto;
    }
  }

  &-wrapper {
    max-height: 50vh;
    overflow-: auto;
  }
}
</style>
