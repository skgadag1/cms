<template>
  <BasicGridTable grid-name="service-library" header-class="d-none d-md-grid">
    <template #header>
      <BasicGridTableHeader v-for="f of fields"
                            :key="'item__' + f.key"
                            :sortable="f.sortable"
                            :sorting-active="sortingKey === f.key"
                            :sorting-dir="sortingDir"
                            :name="f.key"
                            @click.native="f.sortable ? $emit('sorting', f.key, sortingKey === f.key && sortingDir === '-' ? '+' : '-') : null"
      >
        {{ f.label }}
      </BasicGridTableHeader>
    </template>

    <template #rows>
      <div class="px-3 px-sm-0">
        <ServiceLibraryItem v-for="(item,i) of items" :key="'item' + i" :item="item" />

        <div v-if="!items.length">
          <div class="box" data-test-id="no-results-message">
            {{ translate('noResults', 'CommonErrors') }}
          </div>
        </div>
      </div>
    </template>
  </BasicGridTable>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BasicGridTable from '@/components/common/BasicGridTable'
import ServiceLibraryItem from '@/components/common/ServiceLibrary/ServiceLibraryItem'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {
    BasicGridTable,
    BasicGridTableHeader,
    ServiceLibraryItem
  },
  props: {
    items: {
      required: true
    },
    sortingDir: {
      type: String,
      default: null,
      required: false
    },
    sortingKey: {
      type: String,
      default: null,
      required: false
    }
  },
  computed: {
    fields() {
      return [
        {key: 'title', label: this.translatedConfig('titleHeader'), sortable: true},
        {key: 'description', label: this.translatedConfig('descriptionHeader'), sortable: true},
        {key: 'category', label: this.translatedConfig('categoryHeader'), sortable: true},
        {key: 'language_code', label: this.translatedConfig('languageHeader')}
      ]
    }
  }
}
</script>
