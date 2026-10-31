<template>
  <div class="grid-table-row position-relative overflow-hidden"
       :class="{'error' : isError}"
       @click.prevent.stop="downloadPdfFile(item.document_id)"
  >
    <div class="item__title"><span class="font-condensed">{{ item.title }}</span></div>
    <div class="item__description"><span class="font-condensed">{{ item.description }}</span></div>
    <div class="item__category"><span>{{ item.category }}</span></div>
    <div class="item__language">
      <span>{{ translateLocale(item.locale) }}</span>
    </div>
    <div class="item__download">
      <b-link class="float-end" :class="{'text-gray' : isError}" :disabled="isError">
        <b-icon icon="download" />
      </b-link>
    </div>

    <b-tooltip v-if="rootEl"
               :class="isError ? 'tooltip-danger' : 'tooltip-light'"
               noninteractive
               :target="rootEl"
               :title="isError ? translate('downloadError', 'CommonFunctional') : translate('downloadDetails', 'config')"
    />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'
import langs from 'langs'

export default {
  conceptName: ['CommonErrors', 'LanguagesISO'],
  setup() { return useLabels() },
  props: {
    item: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      rootEl: null,
      langs: langs,
      isError: false
    }
  },
  mounted() {
    // The tooltip targets this component's root element, so render it only after mount
    this.rootEl = this.$el
  },
  methods: {
    downloadPdfFile() {
      bdService.fetchDocumentDownloadLink(this.item.document_id).then(r => {
        const link = document.createElement('a')
        link.href = r
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
      }).catch(() => this.isError = true)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';

.box {
  transition: background-color .15s ease-in-out;
  cursor: pointer;

  &.error {
    cursor: not-allowed;
  }

  &:hover:not(.error) {
    background-color: $gray-300 !important;
  }
}
</style>
