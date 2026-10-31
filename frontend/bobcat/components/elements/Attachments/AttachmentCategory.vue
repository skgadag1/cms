<template>
  <div
    v-if="counter !== 0"
    class="p-0 bg-white"
    data-test-id="attachment-related-products-category-group"
  >
    <Accordion
      :counter="counter"
      is-primary-accordion
      shadow
      :color="colors.BLACK"
      :title="category.name"
      :is-expanded="isExpanded"
      :htag="accordionHtag"
    >
      <div
        v-if="category.products && category.products.length"
        class="d-flex flex-column bg-white related-products-table-wrapper"
      >
        <b-table
          thead-tr-class="bg-gray-lighter related-products-thead"
          tbody-tr-class="related-products-tbody"
          responsive
          :items="rows"
          :fields="fields"
        >
          <template
            v-for="(field, index) in fields"
            #[`cell(${field.key})`]="data"
          >
            <div :key="index" class="d-flex justify-content-center align-items-center">
              <b-icon
                v-if="field.key !== 'model' && data.value"
                icon="success"
              />
              <b-icon
                v-else-if="field.key !== 'model' && !data.value"
                icon="minus"
                class="text-gray-light"
              />
              <p v-else :key="index" class="m-0">
                <a v-if="data && data.item && data.item.url"
                   class="font-weight-bold cursor-pointer"
                   :href="data.item.url"
                   target="_blank"
                >{{ data.value }}</a>
                <template v-else>{{ data.value }}</template>
              </p>
            </div>
          </template>
        </b-table>
      </div>
    </Accordion>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import {COLORS} from '~/constants/accordion'

export default {
  props: {
    category: {
      type: Object,
      required: true
    },
    attachments: {
      type: Array,
      required: true
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    accordionHtag: {
      type: String,
      default: 'h3'
    }
  },
  setup(props) {
    const {category: {products}, attachments} = props
    const colors = ref(COLORS)
    const counter = ref(products.length)
    const fields = ref([{key: 'model', label: ''}, ...attachments.map(el => ({key: el['@id'], label: el.title || el['@name']}))])
    const options = ref([])
    const rows = ref([])

    products.forEach((product) => {
      const row = {}
      attachments.forEach((attachment) => {
        row[attachment['@id']] = !!product.attachments.find(attachmentId => attachmentId === attachment['@id'])
      })
      rows.value.push({
        model: product.name || product.identifier,
        url: product.url || '',
        ...row
      })
    })

    return {
      colors,
      counter,
      fields,
      rows: rows.value.sort((a, b) => a?.model?.toLowerCase()?.localeCompare(b?.model?.toLowerCase())),
      options
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/tables';

.related-products-table-wrapper {
  .table-responsive {
    margin-bottom: 0;

    .related-products-thead {
      th {
        width: 12rem;
        background: $gray-800;
        vertical-align: middle;

        div {
          text-overflow: ellipsis;
        }

        &:first-child {
          background: $black;
        }

        &:last-child {
          border-right: none;
        }

        color: $white;
        border-right: 1px solid $black;
        border-top: 1px solid $black;
      }
    }

    .related-products-tbody {
      td {
        text-align: center;
        width: 12rem;
        vertical-align: middle;

        div {
          text-overflow: ellipsis;
        }

        &:first-child {
          text-align: left;
          position: sticky;
          left: 0;
          background: $gray-100;
        }
      }
    }
  }
}
</style>
