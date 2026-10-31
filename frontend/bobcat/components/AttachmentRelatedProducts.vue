<template>
  <section data-test-id="attachment-related-products" class="container">
    <div class="row">
      <div class="col-12">
        <div v-if="title" class="border-bottom mb-5 mt-3 w-100">
          <component :is="htag" :class="htag" data-test-id="attachment-related-products-title">{{ title }}</component>
        </div>

        <div v-if="categories && categories.length && !isFetching" class="d-flex flex-column">
          <div data-test-id="attachment-related-products-groups-container" class="w-100">
            <div v-for="(category) in categories" :key="category['name']" class="mb-3">
              <AttachmentCategory
                :accordion-htag="accordionHtag"
                :category="category"
                :attachments="attachments"
              />
            </div>
          </div>
        </div>
        <div v-else-if="isFetching" class="text-center">
          <b-spinner variant="primary" />
        </div>
        <WarningAlert v-else data-test-id="attachment-related-products-alert">{{ error }}</WarningAlert>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, onMounted, onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

const TYPES = {
  ATTACHMENT: 'dxp:attachment',
  GROUP: 'dxp:attachmentGroup'
}

export default {
  inject: ['model'],
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    accordionHtag: {
      type: String,
      default: 'h3'
    }
  },
  setup(props) {
    const categories = ref([])
    const isFetching = ref(true)
    const error = ref('No attachments configured in the component.')
    const {$services, $regions, $ga4} = useContext()
    const attachmentsCategories = inject('model')
    const currentLanguage = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE
    useMounted(props.nodeId)

    const attachments = computed({
      set: () => {},
      get: () => {
        let attachments = []
        switch (attachmentsCategories['@nodeType']) {
        case TYPES.GROUP: attachments = attachmentsCategories['@nodes'].reduce((acc, node) => attachmentsCategories[node]['@nodeType'] === TYPES.ATTACHMENT ? [...acc, attachmentsCategories[node]] : acc, []); break
        case TYPES.ATTACHMENT: attachments = [attachmentsCategories]; break
        default: attachments = []
        }
        return attachments.sort((a, b) => a?.title?.toLowerCase()?.localeCompare(b?.title?.toLowerCase(), currentLanguage))
      }
    })

    const attachmentIds = computed(() => attachments.value?.map(a => a['@id']))

    const {fetch} = useFetch(async () => {
      isFetching.value = true
      if (attachmentsCategories) {
        try {
          const result = await $services.content.getAttachmentsProductsAssociations(attachmentIds.value)
          categories.value = [...result?.selectorResult?.categories, ...result?.selectorResult?.subcategories].sort((a, b) => a?.name?.toLowerCase()?.localeCompare(b?.name?.toLowerCase(), currentLanguage))
        } catch (e) {
          error.value = '[AttachmentRelatedProducts] Unable to load data'
          // eslint-disable-next-line no-console
          console.error(error.value, e)
          $ga4?.events?.error_general({
            error_details: error.value
          }, 'data error')
        }

        isFetching.value = false
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    onMounted(async () => {
      if (!isFetching.value && !categories.value.length) { await fetch() }
    })

    return {
      attachments,
      categories,
      isFetching,
      error
    }
  }
}
</script>
