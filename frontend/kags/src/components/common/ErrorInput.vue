<template>
  <div class="error-input">
    <transition v-if="!isMagnoliaEditMode" name="fade">
      <div v-show="opened"
           class="modal-backdrop"
           @click="setOpened(false)"
      />
    </transition>

    <b-input-group :class="{ 'exposed' : opened }" :disabled="disabled">
      <b-input-group-text>
        <b-icon icon="search"
                class="fw-bold text-black cursor-pointer"
                role="button"
                tabindex="0"
                :aria-label="searchLabel"
                @click="emitSubmit"
                @keydown.enter.prevent="emitSubmit"
                @keydown.space.prevent="emitSubmit"
        />
      </b-input-group-text>
      <b-form-input ref="input"
                    v-model="keyword"
                    data-test-id="troubleshooting-input"
                    name="keyword"
                    :disabled="disabled"
                    :placeholder="placeholder"
                    @focus="setOpened(true)"
                    @keydown.enter="emitSubmit"
      />
      <template v-if="opened || keepButton">
        <b-icon v-if="keyword.length"
                class="text-gray-800 d-flex align-items-center input-icon cursor-pointer"
                :icon="['fal', 'times']"
                @click.prevent="resetKeyword"
        />
        <b-button variant="primary"
                  class="font-condensed"
                  data-test-id="troubleshooting-btn"
                  :disabled="disabled"
                  :class="buttonExtraClass"
                  @click="emitSubmit"
        >
          {{ capitalize(buttonLabel) }}
        </b-button>
      </template>
    </b-input-group>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
export default {
  conceptName: ['CommonFunctional'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  emits: ['change', 'submit'],
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    searchLabel: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      required: false
    },
    ajax: {
      type: Boolean,
      required: false,
      default: false
    },
    emptyLabel: {
      type: String,
      required: true
    },
    keepButton: {
      type: Boolean,
      required: false,
      default: false
    },
    initialKeyword: {
      type: String,
      required: false,
      default: ''
    },
    submitOnClose: {
      type: Boolean,
      required: false,
      default: false
    },
    buttonExtraClass: {
      type: String,
      required: false,
      default: ''
    }
  },
  data() {
    return {
      opened: false,
      keyword: ''
    }
  },
  computed: {
    buttonLabel() {
      return this.keyword.length === 0 ? this.emptyLabel : this.searchLabel
    }
  },
  watch: {
    initialKeyword(n) {
      if (typeof n === 'string') this.keyword = n
    }
  },
  created() {
    this.keyword = this.initialKeyword
  },
  methods: {
    setOpened(state, submitted = false) {
      this.opened = state
      if (!state) {
        this.$refs['input'].$el.blur()
        if (this.submitOnClose && !submitted && this.keyword !== this.initialKeyword) this.emitSubmit()
      }
      this.$emit('change', state)
    },
    emitSubmit() {
      if (this.ajax && this.opened) this.setOpened(false, true)
      this.$emit('submit', this.keyword)
    },
    resetKeyword() {
      this.keyword = ''
      this.$refs['input'].$el.focus()
      this.emitSubmit()
    }
  }
}
</script>

<style scoped lang="scss">
@import '../../scss/colors';
@import '../../scss/_z-index';

.col-12 .input-group {
  position: relative;
}

.exposed {
  position: relative;
  z-index: $zindex-modal-backdrop + 1;
}

input,
.error-input :deep(.input-group-text) {
  border-width: 0;
  border-color: $white !important;
}

.input-icon {
  padding: .675rem;
  background-color: $white  !important;
}

input::-ms-clear {
  display: none;
}
</style>
