<template>
  <LazyHydrate never>
    <section class="container py-4 product-overview" data-test-id="product-overview">
      <WarningAlert v-if="(!model || !model['@path']) && inEditorEdit" data-test-id="product-overview-model-alert">
        No model defined for page. Please check page template configuration.
      </WarningAlert>

      <div v-else-if="model" class="row">
        <div v-if="oTitle" class="col-12">
          <div class="border-bottom mb-5">
            <component :is="htag" :class="htag" data-test-id="product-overview-title">{{ oTitle }}</component>
          </div>
        </div>
        <div v-if="model.useNewOverview === 'true'">
          <template v-for="(overviewRow) in overviewRows">
            <div :key="overviewRow['@id']" class="d-flex flex-sm-row flex-column w-100 mb-2">
              <div v-if="overviewRow.img && overviewRow.img.src"
                   class="col-12 col-sm-3"
                   data-test-id="product-overview-row-image-wrapper"
              >
                <ImageSet class="w-100 h-auto mb-sm-0 mb-2"
                          :src="overviewRow.img.src"
                          :poster="overviewRow.img.poster"
                          :alt="overviewRow.img.alt"
                          :title="overviewRow.img.title"
                          :img-width="overviewRow.img.width"
                          :img-height="overviewRow.img.height"
                          data-test-id="product-overview-row-image"
                />
              </div>
              <div v-if="overviewRow.description"
                   :class="{'col-sm-9': overviewRow.img && overviewRow.img.src}"
                   class="col-12"
                   data-test-id="product-overview-row-html"
                   v-html="overviewRow.description"
              />
            </div>
          </template>
        </div>
        <template v-else>
          <div v-if="model.overview" class="col-12 col-md-10 offset-md-1 col-lg-6 offset-lg-3">
            <div data-test-id="product-overview-html" class="product-overview-html" v-html="model.overview" />
          </div>
          <div v-else class="col-12">
            <WarningAlert v-if="inEditorEdit" data-test-id="product-overview-empty-alert">
              Model has no overview.
            </WarningAlert>
          </div>
        </template>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {inject, ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  inject: ['model'],
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String,
      required: true
    },
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const currentModel = inject('model')
    const overviewRows = ref([])
    const {$utils, $regions} = useContext()

    overviewRows.value = currentModel?.overviewRows?.['@nodes']?.map(rowName => currentModel?.overviewRows?.[rowName])?.map((row) => {
      if (row?.img?.src) {
        const {metadata} = $utils.getCloudinaryAssetMetadata(row.img.src)
        return ({
          ...row,
          img: {
            src: row.img.src,
            poster: row.img.src,
            title: row.img?.imageTitle || metadata?.context?.caption || metadata?.title,
            alt: row.img?.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
            width: row.img?.imageWidth || null,
            height: row.img?.imageHeight || null
          }
        })
      }
      return row
    })

    const oTitle = props.title?.replace('%ID%', currentModel?.id).replace('%NAME%', currentModel?.['@name']).replace('%TITLE%', currentModel?.title)
    useMounted(props.nodeId)

    return {
      oTitle,
      inEditorEdit,
      overviewRows
    }
  }
}
</script>

<style lang="scss">
.product-overview {
  ul {
    padding-left: 1.1rem;
  }
}
.product-overview-html {
  iframe {
    max-width: 100%;
  }
}
</style>
