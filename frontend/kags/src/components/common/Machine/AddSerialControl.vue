<template>
  <div v-if="editMode" class="add-serial-control d-flex align-items-center">
    <transition v-if="!isMagnoliaEditMode" name="fade">
      <div class="modal-backdrop" @click.prevent="() => { if (!loading) editMode = false }" />
    </transition>

    <div class="exposed w-100">
      <b-input-group>
        <b-input-group-text class="fw-bold"
                            :class="{ 'cursor-pointer form-valid' : !input.invalidSerial && !loading }"
                            @click.prevent="addSerial"
        >
          <b-icon v-if="!loading" icon="plus" class="small" />
          <b-spinner v-else variant="primary" />
        </b-input-group-text>
        <b-form-input id="serial-tooltip-target"
                      ref="serialInput"
                      v-model="input.internalSerial"
                      data-test-id="add-serial-input"
                      autofocus
                      :formatter="serialFormatter"
                      :placeholder="translate('enterSerialHere', 'AddSerialControl')"
                      @keyup.enter.prevent="addSerial"
                      @update:model-value="validateSerial"
        />
      </b-input-group>
      <b-tooltip v-model="tooltipVisible"
                 target="serial-tooltip-target"
                 manual
                 placement="bottom"
                 class="tooltip-danger"
      >
        {{ errorMessage }}
      </b-tooltip>
      <ul v-if="getRecentSerials.length"
          class="position-absolute list-unstyled bg-white w-100 rounded text-dark"
          style="padding-left: 2.75rem"
      >
        <li v-for="s in getRecentSerials"
            :key="'recent_serial_'+s.serial_number"
            class="fw-bold p-1"
            @click.prevent="pasteSerial(s)"
        >
          {{ s.serial_number }}
        </li>
      </ul>
    </div>
  </div>

  <div v-else class="add-serial-control d-flex align-items-center" :class="{ 'disabled' : disabled }">
    <transition v-if="!isMagnoliaEditMode && !disableEmpty" name="fade">
      <div v-if="isEmpty" class="modal-backdrop modal-backdrop__partial" />
    </transition>

    <template v-if="!inputMode">
      <b-badge size="lg"
               class="d-inline-flex align-items-center justify-content-center cursor-pointer"
               :class="{ 'exposed' : isEmpty }"
               :variant="disabled ? 'light' : 'primary'"
               @click.prevent="enableEditMode"
      >
        <b-icon icon="plus" class="small" />
      </b-badge>
      <b-link class="d-none d-md-inline text-white font-condensed ms-2"
              :class="{ 'exposed' : isEmpty }"
              data-test-id="add-serial-link__desktop"
              @click.prevent="enableEditMode"
      >
        {{ translate('addASerialNumber', 'AddSerialControl') }}
      </b-link>
      <b-link class="d-inline-flex d-md-none font-condensed ms-2"
              data-test-id="add-serial-link__mobile"
              :class="{ 'exposed' : isEmpty }"
              @click.prevent="enableEditMode"
      >
        {{ translate("addASerialNumber", 'AddSerialControl') }}
      </b-link>
    </template>

    <template v-else>
      <b-input-group>
        <b-input-group-text class="fw-bold form-valid">
          <b-icon icon="plus" class="small" />
        </b-input-group-text>
        <b-form-input v-model="input.internalSerial"
                      data-test-id="add-serial-input__inactive"
                      :formatter="serialFormatter"
                      :placeholder="translate('enterSerialHere', 'AddSerialControl')"
                      @click.prevent="enableEditMode"
        />
      </b-input-group>
    </template>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {SERIAL_ADD} from '@/store/mutations'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'

export default {
  conceptName: ['CommonErrors', 'AddSerialControl'],
  setup() { return useLabels() },
  props: {
    disabled: {
      required: false,
      type: Boolean
    },
    inputMode: {
      required: false,
      type: Boolean,
      default: false
    },
    disableEmpty: {
      required: false,
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      editMode: false,
      input: {
        invalidSerial: true,
        internalSerial: '',
        serialData: null,
        errorMessageLabel: 'Wrong serial number format'
      },
      loading: false,
      tooltipVisible: false
    }
  },
  computed: {
    errorMessage() {
      return this.input.internalSerial && this.input.internalSerial.length && this.input.invalidSerial ? this.input.errorMessageLabel : null
    },
    ...mapGetters([
      'isEmpty',
      'getRecentSerials'
    ])
  },
  methods: {
    addSerial() {
      this.validateSerial().then(() => {
        if (!this.input.invalidSerial) {
          this.$store.commit(SERIAL_ADD, this.input.serialData)
          this.$emit('add', this.input.serialData)
          this.clearField()
        }
        this.loading = false
      })
    },
    validateSerial(_s = this.input.internalSerial) {
      if (_s.length !== 9 && _s.length !== 12) {
        this.input.invalidSerial = true
        this.input.errorMessageLabel = this.translate('wrongSerialNumberFormat', 'AddSerialControl')
        setTimeout(() => { this.tooltipVisible = true }, 200)
        return new Promise(() => {})
      }

      this.loading = true
      this.input.invalidSerial = false
      this.tooltipVisible = false

      return bdService.validateSerial(_s).then(({data}) => {
        if (data.serial_number.toUpperCase() !== _s) {
          this.input.invalidSerial = true
        } else {
          this.input.invalidSerial = false
          this.input.errorMessageLabel = ''
          this.input.serialData = data
        }
      }).catch(error => {
        this.input.invalidSerial = true
        this.input.errorMessageLabel = this.getSerialErrorMessage(error, _s)
        setTimeout(() => { this.tooltipVisible = true }, 200)
      }).finally(() => this.loading = false)
    },
    pasteSerial(s) {
      this.input.internalSerial = s.serial_number
      this.addSerial()
    },
    clearField() {
      this.editMode = false
      this.input = {
        invalidSerial: true,
        internalSerial: '',
        serialData: null,
        errorMessageLabel: ''
      }
    },
    enableEditMode() {
      if (!this.disabled) this.editMode = true
    },
    serialFormatter(val) {
      return val.trim().toUpperCase()
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';
@import '../../../scss/z-index';

.add-serial-control {
  line-height: 2.25rem;

  .badge {
    width: 1.5rem;
    height: 1.5rem;
    padding: 0;
    font-size: 1.2rem;
    box-sizing: content-box;
  }

  &.disabled {
    opacity: .5;

    * {
      cursor: default !important;
    }

    a {
      color: $gray-400 !important;
    }
  }

  .input-group {
    margin: -3px 0;

    .input-group-text {
      padding: 0;
      font-size: 22px;
      width: 2.7rem;
      align-items: center;
      justify-content: center;
      border: transparent;

      .spinner-border {
        font-size: 1rem;
      }
    }

    .input-group-text.form-valid {
      background: $orange;
      color: $white;
      border-color: $orange;
    }

    input,
    input:focus {
      border: transparent;
    }
  }

  .is-mobile & {
    line-height: inherit;
  }
}

.exposed {
  position: relative;
  z-index: $zindex-modal-backdrop + 1;
}

.modal-backdrop__partial {
  z-index: 100 !important;

  & ~ .exposed {
    z-index: 101;
  }
}
</style>
