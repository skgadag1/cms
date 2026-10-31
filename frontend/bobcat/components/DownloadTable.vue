<template>
  <section data-test-id="download-table-wrapper" class="container my-4">
    <LazyHydrate never>
      <div v-if="title" class="row">
        <div class="col-12">
          <div class="border-bottom mb-4">
            <component :is="htag" :class="htag" data-test-id="download-table-title">{{ title }}</component>
          </div>
        </div>
      </div>
    </LazyHydrate>

    <div class="row no-gutters">
      <LazyHydrate never>
        <div v-if="headerLabel" data-test-id="download-table-header" class="col-12">
          <div class="rounded-top px-3 bg-dark text-white py-3">
            <h5 class="m-0">{{ headerLabel }}</h5>
          </div>
        </div>
      </LazyHydrate>

      <div class="col-12" data-test-id="download-table">
        <b-table v-if="mappedRows && mappedRows.length"
                 thead-tr-class="bg-gray-lighter"
                 :items="mappedRows"
                 :fields="fields"
                 responsive="sm"
        >
          <template #head(fileName)="data">
            <p class="m-0">{{ data.label }}</p>
          </template>
          <template #head(version)="data">
            <p class="m-0 d-none d-md-block">{{ data.label }}</p>
          </template>
          <template #head(filefileName)="data">
            <p class="m-0 d-none d-md-block">{{ data.label }}</p>
          </template>

          <template #cell(fileName)="data">
            <p class="m-0" data-test-id="download-table-label">{{ data.value }}</p>
          </template> <template #cell(version)="data">
            <p class="m-0">{{ data.value }}</p>
          </template>
          <template #cell(filefileName)="data">
            <a :href="$utils.getCloudinaryDocument(data.item.filePath)"
               :title="downloadLabel"
               target="_blank"
               rel="nofollow"
               role="button"
               class="d-flex justify-content-end justify-content-md-start align-items-center p-0 bg-transparent border-0 font-weight-bold text-primary"
               data-test-id="download-table-download-button"
               @click.prevent="download(data.item.filePath, data.item.fileName)"
            >
              <span class="d-flex align-items-center justify-conent-center m-0 mr-2 transition-base"><b-icon icon="download" /></span><span class="d-none d-md-block">
                {{ downloadLabel }}
              </span>
            </a>
          </template>
        </b-table>
        <WarningAlert v-else-if="inEditorEdit" data-test-id="download-table-alert">No rows configured in the component.</WarningAlert>
      </div>
    </div>
  </section>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    rows: {
      type: Object,
      required: true
    },
    title: {
      type: String
    },
    fileNameLabel: {
      type: String,
      default: 'File Name'
    },
    headerLabel: {
      type: String
    },
    versionLabel: {
      type: String,
      default: 'Version'
    },
    downloadLinkLabel: {
      type: String,
      default: 'Download Link'
    },
    downloadLabel: {
      type: String,
      default: 'Download'
    },
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const fields = [{key: 'fileName', label: props.fileNameLabel}, {key: 'version', label: props.versionLabel}, {key: 'filefileName', label: props.downloadLinkLabel}]
    const {$utils} = useContext()
    const rows = props.rows
      ? props.rows['@nodes'].map(x => ({
        fileName: props.rows[x]?.fileName,
        filePath: props.rows[x]?.asset?.filePath,
        version: props.rows[x]?.version,
        file: props.rows[x]?.file
      }))
      : []
    const mappedRows = ref(rows)
    useMounted(props.nodeId)

    const download = (url, fileName) => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    return {
      fields,
      download,
      mappedRows,
      inEditorEdit
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/tables';
</style>
