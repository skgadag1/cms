<template>
  <div class="shadow-sm z-index-1" :class="[dark ? 'bg-dark' : 'bg-white', fixedToBottom && 'fixed-bottom z-index-5']">
    <div class="container">
      <div class="row">
        <div class="col-12 py-3 transition-base">
          <div class="row">
            <div class="col-12 d-flex justify-content-around justify-content-sm-center align-items-center" data-test-id="compare-stripe-items">
              <CompareItem v-for="m in models"
                           :key="`specification-models-to-compare__${m['@id']}`"
                           :dark="dark"
                           :compare="false"
                           :model="m"
                           :show-remove="!model || m['@id'] !== model['@id']"
                           :compare-label="labels.compare || 'Compare'"
                           @remove="$emit('remove', m['@id'])"
              />
              <CompareItem v-for="i in (maxToCompare - models.length)"
                           :key="`specification-models-to-compare-empty__${i}`"
                           :dark="dark"
                           :compare="false"
                           :model="{}"
                           :disabled="disabled"
                           data-test-id="compare-stripe-empty-item"
                           @activate="$emit('activate')"
                           @empty-trigger="emptyClickHandler(i)"
              >
                <CompareModelsDropdown v-show="compareModelsDropdown === i"
                                       :ref="`compare-models-dropdown__${i}`"
                                       :options="options"
                                       :select-model-label="labels.selectModel"
                                       :right="isMobile && i === maxToCompare - models.length"
                                       @hide="compareModelsDropdown === i ? compareModelsDropdown = null : false"
                                       @add="$emit('add', $event)"
                                       @show="$emit('full-fill-models', $event)"
                />
              </CompareItem>
              <transition name="fade-zoom">
                <div v-if="models.length > 1">
                  <b-button
                    v-if="showCompareButton"
                    variant="primary"
                    size="lg"
                    class="ml-3 d-none d-sm-inline-flex btn-narrow"
                    data-test-id="compare-stripe-button"
                    :disabled="disabled"
                    @click.prevent="$emit('compare')"
                  >
                    {{ labels.compare || 'Compare' }}
                  </b-button>
                  <b-button
                    v-if="showClearAllButton"
                    variant="outline-primary"
                    size="lg"
                    class="ml-3 d-none d-sm-inline-flex justify-content-center"
                    data-test-id="compare-stripe-button-clear"
                    :disabled="disabled"
                    @click.prevent="$emit('clearCompare')"
                  >
                    {{ labels.clearAll || 'Clear All' }}
                  </b-button>
                </div>
              </transition>
            </div>
          </div>
          <transition name="fade-zoom">
            <div v-if="models.length > 1" class="d-sm-none d-flex">
              <b-button
                v-if="showCompareButton"
                variant="primary"
                class="col-12 mt-3"
                size="lg"
                data-test-id="compare-stripe-button"
                :disabled="disabled"
                @click="$emit('compare')"
              >
                {{ labels.compare || 'Compare' }}
              </b-button>
              <b-button
                v-if="showClearAllButton"
                variant="outline-primary"
                size="lg"
                class="col-12 mt-3 justify-content-center"
                data-test-id="compare-stripe-button-clear"
                @click.prevent="$emit('clearCompare')"
              >
                {{ labels.clearAll || 'Clear All' }}
              </b-button>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {onMounted, onBeforeUnmount, ref, useContext} from '@nuxtjs/composition-api'

export default {
  inject: ['model'],
  props: {
    models: {
      type: Array,
      required: true
    },
    maxToCompare: {
      type: Number,
      default: 3
    },
    showCompareButton: {
      type: Boolean,
      default: true
    },
    showClearAllButton: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      required: false,
      default() { return {} }
    },
    options: {
      type: Array,
      required: false,
      default() { return [] }
    },
    disabled: { // means that products are picked and component should be blocked
      type: Boolean,
      default: false
    },
    fixedToBottom: {
      type: Boolean,
      default: false
    },
    dark: {
      type: Boolean
    }
  },
  setup() {
    const isMobile = ref(false)

    const {$utils} = useContext()
    if (process.client) {
      const checkSize = () => {
        isMobile.value = $utils.isMobile()
      }

      onMounted(() => {
        checkSize()
        window.addEventListener('resize', checkSize)
        setTimeout(() => { window.dispatchEvent(new Event('resize')) }, 200)
      })

      onBeforeUnmount(() => window.removeEventListener('resize', checkSize))
    }

    return {
      isMobile
    }
  },
  data() {
    return {
      compareModelsDropdown: null
    }
  },
  methods: {
    emptyClickHandler(i) {
      this.compareModelsDropdown = this.compareModelsDropdown === i ? null : i
      if (this.$refs && this.$refs[`compare-models-dropdown__${i}`]) {
        this.$refs[`compare-models-dropdown__${i}`][0].open()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.compare-models-dropdown {
  bottom: -2.5rem;
}

.fixed-bottom {
  .compare-models-dropdown {
    bottom: 0.5rem;
  }
}
</style>
