<template>
  <b-col tag="section" cols="12" :class="{ 'text-gray disabled' : disabled }" class="warranty-component" vcloak>
    <slot name="header" />

    <slot name="top-content" />

    <div v-if="loadingRequirementArticles || requirementArticles.length" class="warranty-requirements mb-5">
      <overlay-wrapper :show="loadingRequirementArticles">
        <h2 data-test-id="articles-component-title">
          {{ translate("requirementArticlesTitle", 'WarrantyComponent') }}
        </h2>
        <AccordionView :title="translate('viewAllArticles', 'WarrantyComponent')"
                       :items="requirementArticles"
        />
      </overlay-wrapper>
    </div>

    <div class="warranty-plans">
      <h2 class="mb-4" data-test-id="warranty-plans-title">
        {{ translate("warrantyPlansTitle", 'WarrantyComponent') }}
      </h2>
    </div>

    <overlay-wrapper :show="loading && !disabled">
      <b-row v-if="!disabled">
        <b-col v-if="items.length" cols="12">
          <BasicGridTable grid-name="warranty" header-class="d-none d-md-grid">
            <template #header>
              <BasicGridTableHeader v-for="f of fields"
                                    :key="'item__' + f.key"
                                    :sortable="f.sortable"
                                    :name="f.key"
                                    @click="f.sortable ? $emit('sorting', f.key) : null"
              >
                {{ f.label }}
              </BasicGridTableHeader>
            </template>

            <template #rows>
              <WarrantyTableItem v-for="(itm, i) in items"
                                 :key="'warranty-item__'+itm.policy_code+'_'+i"
                                 :item="itm"
                                 :disabled="disabledStatus.includes(itm.status.toUpperCase())"
                                 :config="config"
              />
            </template>
          </BasicGridTable>
        </b-col>

        <b-col v-else-if="error.present" class="tests-error">
          <div v-if="error.text" class="box">{{ error.text }}</div>
          <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
        </b-col>

        <b-col v-else-if="!error.present">
          <div v-if="!loading" class="box">{{ translate('noResults') }}</div>
          <ListEmptyWidget v-else />
        </b-col>
      </b-row>

      <BasicGridTable v-else :disabled="disabled" />
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BasicGridTable from '@/components/common/BasicGridTable'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'
import WarrantyTableItem from '@/components/common/Warranty/WarrantyTableItem'
import AccordionView from '@/components/common/AccordionView'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import ListEmptyWidget from '../ListEmptyWidget'

const MAX_REQUIREMENT_ARTICLES = 20

export default {
  conceptName: ['CommonErrors', 'WarrantyComponent'],
  setup() { return useLabels() },
  components: {
    ListEmptyWidget,
    BasicGridTable,
    BasicGridTableHeader,
    WarrantyTableItem,
    AccordionView
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: false,
      default() { return {} }
    }
  },
  data() {
    return {
      items: [],
      loading: true,
      warrantyStatuses: ['Active', 'Inactive', 'Suspended', 'Terminated', 'Inprogress', 'Expired'],
      error: {
        present: false,
        text: null,
        statusCounter: 0
      },
      disabledStatus: ['INACTIVE', 'SUSPENDED', 'TERMINATED', 'EXPIRED'],
      requirementArticles: [],
      loadingRequirementArticles: false
    }
  },
  computed: {
    fields() {
      return [
        {key: 'status', label: this.translatedConfig('statusHeader'), sortable: false},
        {key: 'description', label: this.translatedConfig('descriptionHeader'), sortable: false},
        {key: 'type', label: this.translatedConfig('typeHeader'), sortable: false},
        {key: 'engineHoursStart', label: this.translatedConfig('startDateHeader'), sortable: false},
        {key: 'engineHoursEnd', label: this.translatedConfig('endDateHeader'), sortable: false},
        {key: 'hours', label: this.translatedConfig('hoursHeader'), sortable: false}
      ]
    },
    ...mapGetters([
      'getCurrentSerial',
      'isCurrentMachineValidSubscription',
      'getCurrentUser'
    ]),
    getLanguage() {
      if (this.getCurrentUser && this.getCurrentUser.language) {
        return this.getCurrentUser.language.split('-')[0]
      }
      return 'en'
    }
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchWarranties()
    },
    getCurrentUser() {
      this.fetchRequirementArticles()
    }
  },
  created() {
    this.fetchWarranties()
    this.fetchRequirementArticles()
  },
  methods: {
    fetchWarranties() {
      if (this.getCurrentSerial) {
        const _s = '' + this.getCurrentSerial
        this.loading = true
        this.error.present = false
        this.error.statusCounter = 0
        this.error.text = null
        Promise.all(this.warrantyStatuses.map(_status => {
          return bdService.getWarrantyPlans(this.getCurrentSerial, {
            warranty_status: _status
          }).catch(e => {
            this.items = []
            if (!e.isCancelled) {
              this.error.statusCounter += 1
              if (e.message) this.error.text = e.message && !e.message.includes('Machine with key ') ? e.message : null
            }
          })
        })
        ).then(r => {
          this.items = []
          if (this.getCurrentSerial === _s) r.map(i => { if (Array.isArray(i)) this.items = this.items.concat(i) })
        }).finally(r => {
          if (r && !r.isCancelled || !r) {
            this.items.sort(this.compare)
            this.loading = false
            this.error.present = this.error.statusCounter === this.warrantyStatuses.length
          }
        })
      }
    },
    compare(a, b) {
      const weightA = this.disabledStatus.includes(a.status.toUpperCase()) ? 0 : 1
      const weightB = this.disabledStatus.includes(b.status.toUpperCase()) ? 0 : 1

      if (weightA < weightB) return 1
      else if (weightA > weightB) return -1
      else return new Date(b.end_date) - new Date(a.end_date)
    },
    async fetchRequirementArticles() {
      this.loadingRequirementArticles = true
      try {
        const language = this.getLanguage
        const response = await bdService.fetchWarrantyArticles(language)

        this.requirementArticles = (response.data.items || [])
          .slice(0, MAX_REQUIREMENT_ARTICLES)
          .map((item, idx) => ({
            ...item,
            id: idx,
            title: item.name
          }))
      } catch (e) {
        this.requirementArticles = []
      } finally {
        this.loadingRequirementArticles = false
      }
    }
  }
}
</script>
