<template>
  <b-modal id="retrieve-password-modal"
           ref="modal"
           centered
           lazy
           size="lg"
           title-class="flex-grow-1"
           @shown="openModal"
  >
    <template #title>
      {{ translate('titlePasswordPanel', 'MachinePassword') }}
    </template>

    <p class="text-black ms-4 ms-md-5">
      {{ translate('selectPasswordPanel', 'MachinePassword') }}
    </p>

    <p class="mt-1 text-black mb-2 ms-4 ms-md-5">
      {{ translate('panelConfiguration', 'MachinePassword') }}
    </p>
    <BasicFilterItem ref="screenLabel"
                     class="ms-md-5 ms-4 smallest dropdown m-0"
                     :class="{'is-placeholder': screenLabel === ''}"
                     :title="selectedScreenLabel"
    >
      <div v-for="(itm, i) in screenTypes"
           :key="'screen_item_' + i"
           class="nav-item text-nowrap"
           :class="{'disabled': itm.disabled}"
           @click="hideScreenDropdown"
      >
        <b-dropdown-item @click.prevent="selectScreenType(itm)">{{ itm.label }}</b-dropdown-item>
      </div>
    </BasicFilterItem>
    <div v-show="showNotification" class="ms-md-5 ms-4 mt-3">
      <b-notification class="fixed-width-wider border-gray-400">
        {{ translate('viewPanel', 'MachinePassword') }}
      </b-notification>
    </div>
    <b-form-group class="d-flex ms-md-5 mb-0 mt-2 password-panel-form">
      <div class="password-panel-group">
        <div v-for="passwordPanel in passwordPanels"
             :key="passwordPanel.type"
             class="password-panel-item me-2 mb-2"
             :class="{
               active: selectedPasswordPanel === passwordPanel.type,
               interactive: !loading && selectedPasswordPanel !== passwordPanel.type
             }"
             @click="setSelectedPasswordPanel(passwordPanel.type)"
        >
          <div>
            <div class="password-panel-image">
              <img :src="passwordPanel.image">
            </div>
            <p class="password-panel-image-name mb-0">{{ passwordPanel.imageName }}</p>
          </div>
          <div class="radio-wrapper mt-auto d-flex justify-content-center">
            <b-form-radio v-model="selectedPasswordPanel"
                          name="region-radio"
                          :value="passwordPanel.type"
                          :disabled="loading"
                          class="mb-0"
            />
          </div>
        </div>
      </div>
    </b-form-group>

    <template #footer>
      <div v-if="error" class="text-orange">{{ translate('defaultError', 'CommonErrors') }}</div>
      <div class="text-end d-flex flex-wrap d-md-block w-100">
        <b-button class="mt-1 ms-md-3 order-1 col-12 col-md-auto"
                  variant="outline-primary"
                  @click="closeModal('formModal')"
        >
          {{ translate('cancel', 'CommonFunctional') }}
        </b-button>
        <b-button type="submit"
                  variant="primary"
                  :disabled="loading || !selectedPasswordPanel"
                  class="mt-2 ms-md-3 order-0 col-12 col-md-auto d-inline-flex justify-content-center align-items-center"
                  @click="setMachinePassword"
        >
          <b-spinner v-if="loading" small class="me-2" />
          {{ translate('setPanelType', 'MachinePassword') }}
        </b-button>
      </div>
    </template>
  </b-modal>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import {bdService} from '../../../services'
import {MACHINE_SAVE} from '../../../store/mutations'
import BasicFilterItem from '@/components/common/BasicFilterItem'
import BNotification from '@/components/common/BNotification'

export default {
  conceptName: ['MachinePassword', 'CommonFunctional', 'MachinePasswordImages', 'CommonErrors'],
  setup() { return useLabels() },
  name: 'RetrievePassword',
  components: {
    BasicFilterItem,
    BNotification
  },
  data: () => ({
    screenLabel: '',
    selectedPasswordPanel: '',
    showNotification: true,
    passwordPanels: [],
    loading: false,
    error: false
  }),
  computed: {
    ...mapGetters({
      serialNumber: 'getCurrentSerial',
      machinePassword: 'getMachinePassword'
    }),
    screenTypes() {
      return [
        {name: 'buttonscreen', label: this.$options.filters.translate('buttonscreen', this.labels, 'MachinePassword')},
        {name: 'button', label: this.$options.filters.translate('button', this.labels, 'MachinePassword')},
        {name: 'touchscreen', label: this.$options.filters.translate('touchscreen', this.labels, 'MachinePassword')}
      ]
    },
    allPasswordPanels() {
      const passwordPanelImages = this.labels['MachinePasswordImages']
      return Object.entries(passwordPanelImages).map(([type, image]) => ({
        type: type.split('-')[1],
        image: this.getPasswordPanelImage(image),
        imageName: this.getPasswordPanelText(image),
        name: type.split('-')[0]
      }))
    },
    selectedScreenLabel() {
      if (this.selectedPasswordPanel) {
        const result = this.allPasswordPanels.find(item => item.type === this.selectedPasswordPanel)
        const selectedScreen = this.screenTypes.find(item => item.name === result.name)
        return selectedScreen.label
      } else {
        return this.screenLabel || this.translate('select', 'MachinePassword')
      }
    }
  },
  methods: {
    openModal() {
      if (this.machinePassword) this.selectedPasswordPanel = this.machinePassword.panel
      if (this.selectedPasswordPanel) {
        const result = this.allPasswordPanels.find(item => item.type === this.selectedPasswordPanel)
        this.selectScreenType(result)
      }
    },
    closeModal() {
      this.$refs.modal.hide()
    },
    setSelectedPasswordPanel(passwordPanel) {
      if (this.loading) return
      this.selectedPasswordPanel = passwordPanel
    },
    hideScreenDropdown() {
      this.$refs.screenLabel.hideDropdown()
    },
    selectScreenType(itm) {
      if (itm.name && itm.label) {
        this.selectedPasswordPanel = ''
      }
      this.screenLabel = itm.label
      this.showNotification = false
      this.passwordPanels = this.filterPasswordPanels(itm.name)
    },
    filterPasswordPanels(screenType) {
      return this.allPasswordPanels.filter(item => item.name === screenType)
    },
    getPasswordPanelImage(image) {
      const link = /(https?:\/\/[^" ]*)/i
      const imageSrc = image && image.match(link)
      if (Array.isArray(imageSrc)) return imageSrc[0]
    },
    getPasswordPanelText(image) {
      if (!image) return ''
      return image
        .replace(/<[^>]*>/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
    },
    async setMachinePassword() {
      this.loading = true
      this.error = false
      const panelToSend = this.selectedPasswordPanel === '6' ? '5' : this.selectedPasswordPanel
      try {
        const {data} = await bdService.updateMachinePassword(this.serialNumber, panelToSend)
        await this.$store.commit(MACHINE_SAVE, {
          sn: this.serialNumber,
          password: data
        })
        this.closeModal()
      } catch (error) {
        this.error = true
      } finally {
        this.loading = false
      }
    }
  }
}
</script>


<style lang="scss" scoped>
@import "../../../scss/grid";
@import "../../../scss/colors";
@import "../../../scss/transitions";

.password-panel {
  &-group {
    display: flex;
    flex-wrap: wrap;
  }

  &-item {
    width: 14rem;
    height: 19rem;
    padding: 1.3rem;
    border: 0.1rem solid $black;
    transition: $transition-base;
    display: flex;
    flex-direction: column;
    align-items: center;

    @include media-breakpoint-down('md') {
      width: 100%;
      aspect-ratio: 0.9;
    }

    &.active {
      border-color: $orange;
    }

    &.interactive {
      cursor: pointer;

      &:hover {
        box-shadow: 0 2rem 2rem 0 $hover-default;
        border-color: $orange;
      }
    }

    :deep(.form-check-input) {
      border: 0.1rem solid $black;
    }
    &:hover {
      :deep(.form-check-input) {
        border-color: $orange;
        box-shadow: 0 2rem 2rem 0 $hover-default;
      }
    }
  }

  &-image {
    width: 100%;
    height: 12rem;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      max-width: 100%;
      max-height: 100%;
    }
  }

  &-image-name {
    font-size: 0.8rem;
    font-weight: 700;
    text-align: center;
    line-height: 1.2;
    margin-top: 0.5rem;
    max-height: 2.4rem;
  }
}

:deep(.smallest) {
  @include media-breakpoint-up('md') {
    width: 22rem;
  }
  @include media-breakpoint-down('md') {
    width: 19rem;
  }
}

:deep(.password-panel-form > div) {
  width: 100%;
  @include media-breakpoint-down('md') {
    padding: 1rem 1.5rem;
  }
}

:deep(.fixed-width-wider) {
  background: $gray-100;
  @include media-breakpoint-up('md') {
    width: 27rem;
  }

  @include media-breakpoint-down('lg') {
    width: 19rem;
  }
}
</style>
