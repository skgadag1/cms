<template>
  <section v-if="show" class="mb-4">
    <h2>
      {{ capitalize(title) }}
    </h2>

    <div v-for="itm in items"
         :key="'schematic_'+itm.schematic_id"
         class="box py-2 mb-2 cursor-pointer"
         @click="downloadSchematicsFile(itm)"
    >
      <b-row>
        <b-col cols="auto" class="font-condensed d-flex align-items-center">
          {{ itm.schematic_name }}
        </b-col>
        <b-col col class="text-end d-flex align-items-center justify-content-end ps-0">
          <b-tooltip v-if="itm.downloadError"
                     class="tooltip-danger"
                     noninteractive
                     :target="'download_' + itm.schematic_id"
                     :title="translate('downloadError', 'CommonErrors')"
          />
          <b-tooltip v-else
                     class="d-none d-sm-block tooltip-light"
                     noninteractive
                     :target="'download_' + itm.schematic_id"
                     :title="translate('downloadDetails', 'SchematicsTableComponent')"
          />
          <b-icon :id="'download_' + itm.schematic_id" icon="download" class="ms-3" :class="(itm.downloadError) ? 'text-gray-600' : 'text-primary'" />
        </b-col>
      </b-row>
    </div>
  </section>

  <div v-else-if="disabled" class="text-gray">
    <h2 class="font-condensed">{{ translate("electricSchematics") }}</h2>
    <ListEmptyWidget />
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'

export default {
  conceptName: ['SchematicsTableComponent', 'CommonErrors'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    ListEmptyWidget
  },
  props: {
    show: {
      type: Boolean,
      required: false,
      default: true
    },
    items: {
      type: Array,
      required: true
    },
    title: {
      type: String,
      required: false,
      default: 'Download Schematics'
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  methods: {
    downloadSchematicsFile(item) {
      if (!item.downloadError) {
        bdService.fetchSchematicDetails(this.getCurrentSerial, item.schematic_id).then(r => {
          const url = r.data.download_link
          if (url) {
            const link = document.createElement('a')
            link.href = url
            link.target = '_blank'
            document.body.appendChild(link)
            link.click()
          } else {
            this.$set(item, 'downloadError', true)
          }
        }).catch(e => {
          if (!e.isCancelled) {
            this.$set(item, 'downloadError', true)
          }
        })
      }
    }
  }
}
</script>
