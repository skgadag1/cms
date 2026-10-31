<template>
  <section class="mb-4">
    <h2>
      {{ capitalize(title) }}
    </h2>

    <div v-for="itm in sortedItems"
         :key="'manual-document_'+itm.document_id"
         class="box py-2 mb-2 cursor-pointer"
         @click="downloadDocument(itm.document_id)"
    >
      <b-row>
        <b-col cols="auto"
               md="8"
               lg="9"
               class="d-flex align-items-center"
        >
          <span class="font-condensed">S/N {{ itm.serial_range_start }} - {{ itm.serial_range_end }}</span>
        </b-col>
        <b-col cols="auto"
               class="font-condensed d-flex align-items-center p-0"
        >
          {{ translateLocale(itm.locale) }}
        </b-col>
        <b-col col
               class="text-end d-flex align-items-center justify-content-end ps-0"
        >
          <b-icon icon="download" class="text-primary ms-3" />
        </b-col>
      </b-row>
    </div>
  </section>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'

export default {
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    title: {
      type: String,
      required: false,
      default: 'Manuals'
    },
    items: {
      type: Array,
      required: true
    }
  },
  computed: {
    sortedItems() {
      return this.items.slice(0).sort((item1, item2) => {
        if (item1.language_code === 'en' || (item2.language_code !== 'en' && item1.language_code < item2.language_code)) return -1
        if (item2.language_code === 'en' || item1.language_code > item2.language_code) return 1
        return 0
      })
    }
  },
  methods: {
    downloadDocument(document_id) {
      bdService.fetchDocumentDownloadLink(document_id).then(download_link => {
        const link = document.createElement('a')
        link.href = download_link
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
      }).catch(r => {
        this.error = r.response ? r.response.data.error : r
      })
    }
  }
}
</script>
