<template>
  <form class="bg-dark text-white rounded position-relative"
        :action="action"
        :class="{ 'mobile-light' : mobileLight}"
        @submit="emitSubmit"
  >
    <b-row class="pt-3 py-md-3 px-3 px-md-4 align-items-center">
      <div class="col-12" :class="{'col-12 col-xl-auto mb-xl-0' : !isTroubleshootingSearch}">
        <h2 v-show="message"
            class="text-center text-sm-start"
            :class="{ 'd-xl-none' : isOpened}"
        >
          <b-icon v-if="isAiServiceEnabled && !isTroubleshootingSearch" icon="sparkles" class="me-1" />
          {{ message }}
        </h2>
      </div>
      <div :class="[isOpened ? 'col-12' : 'col']">
        <ErrorInput class="border-dark-sm rounded"
                    :submit-on-close="submitOnClose"
                    :initial-keyword="initialKeyword"
                    :ajax="ajax"
                    :placeholder="placeholder"
                    :search-label="translate('searchButton', 'CommonFunctional')"
                    :empty-label="translate('emptySearchButton', 'CommonFunctional')"
                    @change="isOpen"
                    @submit="submitForm"
        />
        <div v-if="isAiServiceEnabled && !isTroubleshootingSearch" class="disclaimer">
          <b-icon icon="info" />
          <div v-html="translate('Disclaimer', 'TroubleshootingProcedures')"></div>
        </div>
      </div>
     
      <div v-if="isTroubleshootingSearch && !isOpened" class="col-12 col-sm-auto mt-2 mt-sm-0">
        <div class="bg-gray-100 p-1 rounded">
          <b-form-radio-group
            v-model="selected"
            class="btn-troubleshooting-switch w-100"
            :options="translatedOptions"
            buttons
            @update:model-value="handleSwitchChange"
          />
        </div>
      </div>
    </b-row>
    <input ref="submitBtn" type="submit" class="d-none">
    <div v-if="disabled" class="mask disabled rounded" />
  </form>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ErrorInput from '@/components/common/ErrorInput'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['CommonFunctional', 'TroubleshootingProcedures'],
  setup() { return useLabels() },
  components: {
    ErrorInput
  },
  emits: ['search'],
  props: {
    action: {
      type: String,
      required: false
    },
    message: {
      type: String,
      required: false
    },
    initialKeyword: {
      type: String,
      required: false
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
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    submitOnClose: {
      type: Boolean,
      required: false,
      default: true
    },
    mobileLight: {
      type: Boolean,
      required: false,
      default: false
    },
    isTroubleshootingSearch: {
      type: Boolean,
      required: false,
      default: false
    },
    initialSearchType: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      isOpened: false,
      value: this.initialKeyword,
      selected: this.initialSearchType
    }
  },
  computed: {
    ...mapGetters(['getFeatureFlag']),
    isAiServiceEnabled() {
      return this.getFeatureFlag('serviceAIAccess')
    },
    translatedOptions() {
      return [
        {text: this.translate('exactMatches', 'TroubleshootingProcedures'), value: 'true'},
        {text: this.translate('partialMatches', 'TroubleshootingProcedures'), value: 'false'}
      ]
    }
  },
  methods: {
    isOpen(s) {
      this.isOpened = s
    },
    emitSubmit(e) {
      if (!this.value || !this.value.trim()) {
        if (e) e.preventDefault()
        // still emit on a cleared search so listeners (e.g. the troubleshooting
        // keyword in the store) reset back to the no-search state
        this.$emit('search', '', this.selected)
        return
      }
      this.$emit('search', this.value, this.selected)
      if (this.ajax && e) e.preventDefault()
    },
    submitForm(k) {
      this.value = k
      this.updateUrlParam('keyword', k)
      this.$refs['submitBtn'].click()
    },
    handleSwitchChange(s) {
      this.updateUrlParam('searchType', s)
      setTimeout(() => {
        this.emitSubmit()
      }, 500)
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/_colors';
@import '../../scss/grid';

.btn-troubleshooting-switch {
  label {
    background: $gray-100;
    color: $gray-800;
    font-weight: bold;
    border-color: transparent;
    cursor: pointer;

    &:hover {
      opacity: .8;
      background: transparent !important;
      border-color: transparent !important;
      color: $gray-800 !important;
    }
  }

  // Selected option
  .btn-check:checked + label {
    border-color: transparent !important;
    background: $white !important;
    color: $black !important;
    box-shadow: 0 .15rem .5rem rgba($black, .35);
    border-radius: 0.125rem !important;
  }
}

.mobile-light {
  @include media-breakpoint-down('md') {
    background: $white !important;
    color: $black !important;

    .border-dark-sm {
      border: 1px solid $gray-400;
    }
  }
}
.disclaimer {
  display: flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.805rem;
  color: $white;

  p, h1, h2, h3 {
    margin: 0;
  }

  bicon {
    font-size: 1.5rem;
  }
}
</style>
