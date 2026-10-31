<template>
  <section v-if="getSerials.current" class="py-md-4 position-relative">
    <div class="banner-main" :style="'background-image: url(' + bannerUrl + ')'" />
    <b-container class="d-none d-md-flex">
      <b-button v-for="s in getSerials.current"
                :key="'tabs_serials_'+s.serial_number.toLowerCase()+'_'+s.active"
                v-b-tooltip="{ placement: 'bottom', title: s.model_id }"
                tag="div"
                class="me-2 box-shadow d-flex align-items-center cursor-pointer"
                :data-test-id="'serial-button-desktop_'+s.serial_number"
                :variant="s.active ? 'primary' : 'light'"
                @click.prevent="setActiveSerial(s)"
      >
        <b>{{ s.serial_number }}</b>
        <b-icon v-if="s.active"
                icon="times"
                class="ms-2"
                :data-test-id="'serial-button-desktop__remove_'+s.serial_number"
                @click.stop="removeSerial(s)"
        />
      </b-button>
      <AddSerialControl :disabled="getSerials.current.length === maxSerials || disabled"
                        :empty="isEmpty"
                        @add="e => updateUrlParam('serial', e.serial_number)"
      />
    </b-container>

    <div class="d-flex d-md-none">
      <MobileSelect ref="mobileSelect"
                    data-test-id="serial-button-mobile"
                    :title="activeSerial || '+ ' + translate('addASerialNumber', 'AddSerialControl')"
                    :empty="isEmpty"
      >
        <template #dropdown>
          <b-list-group>
            <b-list-group-item v-for="s in getSerials.current"
                               :key="'mobile_serial_' + s.serial_number.toLowerCase() + '_' + s.active"
                               class="px-3"
                               :data-test-id="'serial-button-mobile_'+s.serial_number"
                               :active="!!s.active"
                               @click.prevent="setActiveSerial(s)"
            >
              <b>{{ s.serial_number }}</b> <span class="ms-1 small">({{ s.model_id }})</span>
              <b-icon icon="trash"
                      class="ms-2 float-end text-gray-500"
                      :data-test-id="'serial-button-mobile__remove_' + s.serial_number"
                      @click.prevent="removeSerial(s)"
              />
            </b-list-group-item>
            <b-list-group-item v-for="i in (maxSerials - getSerials.current.length)"
                               v-show="getSerials.current.length < maxSerials"
                               :key="'mobile_adding_label' + i"
                               class="px-3"
            >
              <AddSerialControl :disabled="i !== 1"
                                :empty="false"
                                @add="e => updateUrlParam('serial', e.serial_number)"
              />
            </b-list-group-item>
          </b-list-group>
        </template>
      </MobileSelect>
    </div>

    <b-modal id="serial-wrong-modal"
             ref="serialWrongModal"
             centered
             lazy
             ok-only
             no-header-close
             no-close-on-backdrop
             ok-title="Close"
             footer-bg-variant="white"
    >
      <template #title>Error</template>
      <div class="box pb-0">
        <p>S/N: <span class="font-condensed">{{ querySerial }}</span></p>
        <span>{{ serialErrorMessage }}</span>
      </div>
    </b-modal>
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import MobileSelect from '@/components/common/MobileSelect'
import AddSerialControl from '@/components/common/Machine/AddSerialControl'
import {SERIAL_ADD, SERIAL_REMOVE, SERIAL_ACTIVATE} from '@/store/mutations'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'

export default {
  conceptName: ['AddSerialControl', 'CommonErrors'],
  setup() { return useLabels() },
  components: {
    MobileSelect,
    AddSerialControl
  },
  props: {
    bannerUrl: {
      required: false
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      maxSerials: 4,
      querySerial: '',
      serialErrorMessage: ''
    }
  },
  computed: {
    activeSerial() {
      let _as = this.getCurrentSerial

      if (!_as && this.getSerials.current.length) {
        const _last = this.getSerials.current[this.getSerials.current.length - 1]
        this.setActiveSerial(_last)
        _as = _last.serial_number
      }

      return _as
    },
    ...mapGetters([
      'isEmpty',
      'getSerials',
      'getCurrentSerial'
    ])
  },
  watch: {
    activeSerial(_s) {
      if (_s) this.$store.dispatch('fetchMachineAuth')
    }
  },
  created() {
    // here we handle the ?serial queryparam
    if (location.search.indexOf('serial=') > 0) {
      try {
        const serial = location.search.substring(1).split('&').filter(x => x.startsWith('serial='))[0].split('=')[1]
        if (serial.length) this.handleQuerySerial(serial.toUpperCase())
      } catch (e) {
        return
      }
    } else {
      this.setActiveSerial()
    }
  },
  methods: {
    removeSerial(s) {
      this.$store.commit(SERIAL_REMOVE, s)
      this.updateUrlParam('serial', this.getCurrentSerial)
    },
    setActiveSerial(s = this.getCurrentSerial) {
      if (s) {
        this.$store.commit(SERIAL_ACTIVATE, s)
        if (this.$refs.mobileSelect) this.$refs.mobileSelect.close()
        this.updateUrlParam('serial', s && s.serial_number ? s.serial_number : s)
      }
    },
    handleQuerySerial(_s) {
      if (_s && _s.length) {
        // if the serial is already in the user's list, just activate it —
        // don't re-validate (a failing/404 validate call would wrongly show the
        // error modal for a serial that is already known)
        const existing = this.getSerials && this.getSerials.current
          ? this.getSerials.current.find(x => x.serial_number === _s.toUpperCase())
          : null
        if (existing) {
          this.setActiveSerial(existing)
          return
        }

        bdService.validateSerial(_s.toUpperCase()).then(({data}) => {
          if (data.serial_number === _s) {
            this.$store.commit(SERIAL_ADD, data)
            this.setActiveSerial(data)
          }
          else {
            this.serialErrorMessage = this.getSerialErrorMessage({code: 404}, _s)
            this.wrongSerialModal(_s)
          }
        }).catch((e) => {
          this.serialErrorMessage = this.getSerialErrorMessage(e, _s)
          this.wrongSerialModal(_s)
        })
      }
    },
    wrongSerialModal(_s) {
      this.querySerial = _s
      this.$refs.serialWrongModal.show()
      this.setActiveSerial()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';

.close {
  position: relative;
  display: inline-flex;
  align-self: center;
  width: 1rem;
  height: 1rem;
  opacity: .9;

  &:after,
  &:before {
    content: '';
    position: absolute;
    right: 0;
    top: 60%;
    width: .75rem;
    transform: rotate(-45deg);
    border-bottom: 2px solid $white;
  }

  &:after {
    transform: rotate(45deg);
  }
}
</style>

<style lang="scss">
@import '../../../scss/grid';

@include media-breakpoint-up('sm') {
  #serial-wrong-modal .modal-dialog {
    max-width: 75%;
  }
}

@include media-breakpoint-up('md') {
  #serial-wrong-modal .modal-dialog {
    max-width: 50%;
  }
}
</style>
