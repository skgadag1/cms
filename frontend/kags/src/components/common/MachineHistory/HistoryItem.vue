<template>
  <article class="d-flex flex-wrap d-md-grid grid-table-row" @click.prevent.stop="toggleDetailsModal">
    <div class="p-0 col-6 col-md-12 order-2 order-md-1 d-flex align-items-center justify-content-end justify-content-md-start status">
      <b-badge class="d-inline small" :variant="$_variant">{{ $_state.toUpperCase() }}</b-badge>
    </div>

    <div class="p-0 col-6 col-md-12 order-1 order-md-2 font-condensed text-break pe-1">
      {{ item.title }}
    </div>

    <div class="p-0 col-12 order-3 mt-2 mb-2 mt-md-0 mb-md-0">{{ $_created }}</div>

    <div class="p-0 order-4" :class="showInternals ? 'col-12 mb-2' : 'col-6 col-md-12'">
      <div class="d-none d-md-block">
        {{ trunc(item.description) }}
      </div>
      <div class="d-md-none">
        {{ item.description }}
      </div>
    </div>

    <template v-if="showInternals">
      <div class="p-0 col-6 col-md-12 order-5">
        <span class="d-none d-md-inline">{{ translate((item.isInternal ? 'internal' : 'external'), 'HistoryWidget') }}</span>
        <span class="d-md-none text-small font-condensed">{{ translate((item.isInternal ? 'internal' : 'external'), 'HistoryWidget') }}</span>
      </div>
    </template>
    <div v-else class="d-none d-md-block order-5" />

    <div class="p-0 col-6 col-md-12 order-last d-flex justify-content-end">
      <div class="border-start ps-2" :class="{'invisible' : $_state !== 'in progress' && $_state !== 'paused'}">
        <div v-b-tooltip="{ placement: 'top', titleClass: 'tooltip-light', title: item.isResumingAllowed ? translate('resumeSession', 'HistoryWidget') : translate('resumingNotAllowed', 'HistoryWidget') }">
          <b-link href="/resume"
                  target="_blank"
                  class="d-flex align-items-center"
                  :disabled="session.resuming !== null || !item.isResumingAllowed"
                  :data-test-id="'history-item-desktop__resume__' + item.session_id"
                  @click.prevent.stop="() => { if (item.isResumingAllowed) resumeSession(item.session_id) }"
          >
            <b-icon icon="redo" class="me-1" />
          </b-link>
        </div>
      </div>
      <div class="ms-2 ps-2 border-start">
        <b-link v-if="item.session_id"
                v-b-tooltip="{ placement: 'top', titleClass: 'tooltip-light', title: translate('downloadDetails', 'HistoryWidget') }"
                :data-test-id="'history-item-desktop__download__' + item.session_id"
                @click.prevent.stop="downloadFile"
        >
          <b-icon icon="download" class="me-1" />
        </b-link>
        <b-icon v-else icon="download" class="me-1 text-gray-300" />
      </div>
    </div>
  </article>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['HistoryWidget'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    item: {
      type: Object,
      required: true
    },
    showInternals: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      session: {
        resuming: null
      }
    }
  },
  computed: {
    $_state() {
      return this.item.state ? this.item.state.toLowerCase() : ''
    },
    $_variant() {
      let _v

      switch (this.$_state) {
      case 'in progress':
        _v = 'warning'
        break
      case 'paused':
        _v = 'warning'
        break
      case 'canceled':
        _v = 'light'
        break
      case 'not started':
        _v = 'warning'
        break
      default:
        _v = 'success'
      }

      return _v
    },
    $_created() {
      return new Date(this.item.createdAt).toLocaleDateString()
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  methods: {
    toggleDetailsModal() {
      this.$emit('details', this.item.session_id)
    },
    downloadFile() {
      bdService.troubleshootingSessionSummaryPDF(this.item.session_id).then(r => {
        if (window.navigator && window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveOrOpenBlob(new Blob([r.data], {type: 'application/pdf'}))
          return
        }
        let link = document.createElement('a')
        link.href = window.URL.createObjectURL(new Blob([r.data], {type: 'application/pdf'}))
        link.download = 'session_' + this.item.session_id + '.pdf'
        link.click()
      })
    },
    resumeSession(id) {
      this.session.resuming = id
      bdService.troubleshootingSessionResume(id).then(r => {
        window.open(r.data.url)
        this.session.resuming = null
      }).catch(() => {
        this.session.resuming = null
      })
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';
@import '../../../scss/grid';

.history-item {
  border: 2px solid $white;

  &:hover,
  &:active,
  &:focus,
  &.active {
    border: 2px solid $secondary;
    background-color: $hover-default !important;
    cursor: pointer;
  }
}
</style>
