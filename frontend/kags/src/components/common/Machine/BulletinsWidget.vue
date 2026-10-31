<template>
  <b-col :md="6" sm="12" data-test-id="bulletins-widget" class="px-0 px-sm-3">
    <overlay-wrapper :show="loading && !disabled">
      <div v-if="!disabled"
           key="bulletins-disabled"
           class="rounded p-3 bg-gray-400"
           :class="[error.present ? 'bg-gray-400' : 'bg-white']"
      >
        <b-row v-if="!error.present">
          <b-col cols="12" class="mb-2">
            <h2 class="justify-content-between d-flex">
              <span class="text-dark">{{ translate("qualityAlertsLabel", 'Bulletins') }}</span>
              <b-link class="small" :href="bulletinsPageUrl">
                <span class="small" data-test-id="view-all-bulletins">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
              </b-link>
            </h2>
            <transition-group name="fade" class="w-100">
              <BulletinItem v-for="(item, n) in displayedDocuments"
                            :key="'bulletin-item__' + item.document_id"
                            :data-test-id="'bulletin-item__' + n"
                            :item="item"
              />
            </transition-group>
          </b-col>


          <b-col v-if="documents.length > 3"
                 cols="12"
                 class="text-gray-800 font-condensed text-uppercase mt-3 d-flex align-items-center justify-content-center cursor-pointer"
                 :data-test-id="isExpanded ? 'show-less-bulletins' : 'show-more-bulletins'"
                 @click.prevent="isExpanded = !isExpanded"
          >
            <template v-if="!isExpanded">
              <CounterRounded variant="danger"
                              class="me-2"
              >
                +{{ documents.length - 3 }}
              </CounterRounded>
              <!--              Show more Bulletins-->
              {{ translate("showMore", 'CommonFunctional') }}
              <b-icon icon="sort-down" class="ms-2 pb-2" />
            </template>
            <template v-else>
              <!--            Show less Bulletins-->
              {{ translate("showLess", 'CommonFunctional') }}
              <b-icon icon="sort-up" class="ms-2 mt-1 pt-1" />
            </template>
          </b-col>

          <b-col v-if="!documents.length && !isExpiredDocumentExists"
                 cols="12"
                 :class="{ 'py-5' : loading && !documents.length }"
          >
            <div v-if="!loading" class="px-4 mt-4 mb-5 mx-5 font-condensed text-center text-gray-800" data-test-id="no-bulletins">
              <b-icon icon="check" class="text-green h2" /><br>
              <div>{{ translate("noQualityAlerts", 'Bulletins') }}</div>
            </div>
          </b-col>
        </b-row>

        <b-row v-else-if="error.present">
          <b-col cols="6">
            <h2 class="d-flex justify-content-between mb-0">
              {{ translate("qualityAlertsLabel", 'Bulletins') }}
              <b-icon icon="lock-alt" class="small align-self-end text-gray-600" />
            </h2>
          </b-col>

          <template>
            <hr class="title-divider w-100 mt-3 border-gray-600">
            <div class="px-4 my-4 mx-5 font-condensed text-center">
              <template v-if="error.text">{{ translate(error.text.ck, error.text.cp) }}</template>
              <template v-else>{{ translate('defaultError', 'CommonErrors') }}</template>
            </div>
          </template>
        </b-row>
        <b-notification v-if="isExpiredDocumentExists" class="mt-1 expiration-alert-box">
          {{ translate("expiredQANotification", 'Bulletins') }}
        </b-notification>
      </div>

      <div v-else key="bulletins-available">
        <h2 class="text-gray">
          {{ translate("qualityAlertsLabel", 'Bulletins') }}
          <small>
            <span class="float-end small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
          </small>
        </h2>
        <ListEmptyWidget :range="3" />
        <b-notification v-if="isExpiredDocumentExists" class="mt-1 expiration-alert-box">
          {{ translate("expiredQANotification", 'Bulletins') }}
        </b-notification>
      </div>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import BulletinItem from './BulletinItem.vue'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import CounterRounded from '@/components/common/CounterRounded'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'
import BNotification from '@/components/common/BNotification'

export default {
  conceptName: ['Bulletins', 'CommonErrors', 'CommonFunctional'],
  setup() { return useLabels() },
  components: {
    BulletinItem,
    ListEmptyWidget,
    CounterRounded,
    BNotification
  },
  props: {
    bulletinsPageUrl: {
      type: String,
      required: false,
      default: null
    }
  },
  data() {
    return {
      loading: true,
      documents: [],
      isExpanded: false,
      isExpiredDocumentExists: false,
      error: {
        present: true,
        text: null
      }
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ]),
    ...mapGetters(['getCurrentSerial']),
    disabled() {
      return !this.getCurrentSerial
    },
    displayedDocuments() {
      return this.isExpanded ? this.documents : this.documents.slice(0, 3)
    }
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchBulletins()
    }
  },
  created() {
    this.fetchBulletins()
  },
  methods: {
    fetchBulletins() {
      if (this.getCurrentSerial) {
        this.loading = true
        bdService.fetchBulletins(this.getCurrentSerial, {bulletin_type: 'Quality Alert'})
          .then(r => {
            this.documents = r.data.items
            this.error.present = false
            this.error.text = null
            this.loading = false
            this.isExpiredDocumentExists = this.documents.some(doc => doc.status.toLowerCase() === 'expired')
            this.documents = this.documents.filter(doc => doc.status.toLowerCase() === 'active')
          })
          .catch(e => {
            if (!e.isCancelled) {
              this.error.present = true
              this.error.text = e.message || ''
            }
          })
          .finally(r => {
            if (r && !r.isCancelled || !r) this.loading = false
          })
      }
    },
    determineErrorText(e) {
      return e.code === 403 ? {ck: 'unauthorizedError', cp: 'CommonErrors'} : {ck: 'defaultError', cp: 'CommonErrors'}
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/_colors.scss';

.title-divider {
  border-top: 2px solid $black;
}

.expiration-alert-box {
    background-color: $gray-100 !important;
    border-color: $light-gray !important;
}

.alert {
  margin-bottom: 0rem !important;
}
</style>
