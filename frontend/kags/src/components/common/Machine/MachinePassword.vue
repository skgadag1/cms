<template>
  <b-spinner v-if="loading" variant="orange" small class="me-2" />
  <div v-else-if="machinePassword && machinePassword.password" v-b-modal.retrieve-password-modal class="machine-password cursor-pointer">
    <b-badge pill variant="dark" class="font-condensed fw-bold d-flex align-items-center justify-content-center">
      <b-icon icon="key" class="me-1" />
      {{ translate('password', 'MachinePassword') }}: {{ machinePassword.password }}
    </b-badge>
    <b-icon icon="undo" class="ms-1 text-orange machine-password-refresh" />
  </div>

  <b-button v-else-if="isNewDashboardEnabled" v-b-modal.retrieve-password-modal variant="link" class="retrieve-password text-orange cursor-pointer p-0">
    <span class="retrieve-password__text font-condensed">{{ translate('retrievePassword', 'MachinePassword') }}</span>
    <img
      class="iq-icon"
      :src="resolvedAssetServer + '/assets/img/icons/password_icon.svg'"
    >
  </b-button>

  <!-- Legacy dashboard -->
  <div v-else v-b-modal.retrieve-password-modal class="text-orange cursor-pointer">
    <b-icon icon="key" class="me-1" />
    <span class="font-condensed"> {{ translate('retrievePassword', 'MachinePassword') }}</span>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
import { mapGetters } from 'vuex';

export default {
  conceptName: ['MachinePassword'],
  setup() { return useLabels() },
  props: {
    assetServer: {
      required: false,
      default: null
    }
  },
  data: () => ({
    loading: true
  }),
  emits: ['open-modal'],
  computed: {
    isNewDashboardEnabled() {
      return this.getFeatureFlag('switchToNewDashboard')
    },
    // Prefer the passed prop; fall back to the global asset server
    // (Vue.prototype.assetServer, set from <body data-asset-server>).
    resolvedAssetServer() {
      return this.assetServer != null ? this.assetServer : (this.$root.assetServer || '')
    },
    ...mapGetters({
      machinePassword: 'getMachinePassword',
      getFeatureFlag: 'getFeatureFlag'
    })
  },
  async mounted() {
    await this.$store.dispatch('fetchMachinePassword')
    this.loading = false
  }
}
</script>

<style lang="scss" scoped>
@use 'sass:color';
@import '../../../scss/transitions.scss';
@import '../../../scss/colors.scss';
@import "../../../scss/grid";

.machine-password {
  display: flex;
  &:hover {
    .machine-password-refresh {
      transform: rotate(-90deg);
    }
  }
  &-refresh {
    transition: $transition-base;
  }
}

// New-dashboard "Retrieve Password" link state
.retrieve-password {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  // b-button variant="link" adds border/outline/focus ring — strip them so it
  // reads as a plain text link per the design
  border: 0 !important;
  box-shadow: none !important;
  outline: none !important;

  &:focus,
  &:active,
  &.focus,
  &:focus-visible {
    border: 0 !important;
    box-shadow: none !important;
    outline: none !important;
  }

  &__text {
    text-decoration: underline;
  }

  &__icon {
    width: 1rem;
    flex-shrink: 0;
  }

  // the key icon is an SVG <img>, so darken it via filter on hover to match
  // the darkened text color
  img {
    transition: filter 0.15s ease;
  }

  &:hover img,
  &:focus img {
    filter: brightness(0.85);
  }
}

.text-orange.cursor-pointer {
  transition: $transition-base;

  &:hover {
    color: color.adjust($orange, $lightness: -15%) !important;
    text-decoration: underline;
  }
}
</style>
