<template>
  <b-col :md="fullWidth ? 12 : 6" sm="12" data-test-id="field-mods-widget" class="px-0 px-sm-3">
    <overlay-wrapper :show="loading && !disabled">
      <div v-if="!disabled"
           class="rounded p-3"
           :class="[error.present ? 'bg-gray-400' : 'bg-white']"
      >
        <b-row v-if="!error.present">
          <b-col cols="12" class="mb-2">
            <h2 class="justify-content-between d-flex">
              <span class="text-dark">{{ translate("title", 'FieldMods') }}</span>

              <b-link class="small" :href="fieldModsPageUrl">
                <span class="small" data-test-id="view-all-field-mods">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
              </b-link>
            </h2>
          </b-col>

          <transition-group name="fade" class="w-100">
            <FieldModsItem v-for="(item, n) in displayedMods"
                           :key="'field-mod-item__'+item.field_modification_code"
                           :data-test-id="'field-mod-item__' + n"
                           :mod="item"
            />
          </transition-group>

          <b-col v-if="mods.length > 3"
                 cols="12"
                 class="text-gray-800 font-condensed text-uppercase mt-3 d-flex align-items-center justify-content-center cursor-pointer"
                 :data-test-id="isExpanded?'show-less-field-mods':'show-more-field-mods'"
                 @click.prevent="isExpanded = !isExpanded"
          >
            <template v-if="!isExpanded">
              <CounterRounded variant="danger"
                              class="me-2"
              >
                +{{ mods.length - 3 }}
              </CounterRounded>
              <!--              Show more field mods-->
              {{ translate("showMore", 'CommonFunctional') }}
              <b-icon icon="sort-down" class="ms-2 pb-2" />
            </template>
            <template v-else>
              <!--            Show less field mods-->
              {{ translate("showLess", 'CommonFunctional') }}
              <b-icon icon="sort-up" class="ms-2 mt-1 pt-1" />
            </template>
          </b-col>

          <b-col v-if="!mods.length"
                 cols="12"
          >
            <div v-if="!loading" class="px-4 mt-4 mb-5 mx-5 font-condensed text-center text-gray-800">
              <b-icon icon="check" class="text-green h2" /><br>
              <div>{{ translate("noPendingFieldmods", 'CommonErrors') }}</div>
            </div>
            <ListEmptyWidget v-else :range="3" />
          </b-col>
        </b-row>

        <b-row v-else-if="error.present">
          <b-col cols="12">
            <h2 class="d-flex justify-content-between mb-0">
              {{ translate("title", 'FieldMods') }}
              <b-icon icon="lock-alt" class="small align-self-end text-gray-600" />
            </h2>
          </b-col>

          <template>
            <hr class="title-divider w-100 mt-3 border-gray-600">
            <div class="px-4 my-4 mx-5 font-condensed text-center">
              <template v-if="error.text">{{ error.text }}</template>
              <template v-else>{{ translate('defaultError', 'CommonErrors') }}</template>
            </div>
          </template>
        </b-row>
      </div>

      <div v-else>
        <h2 class="text-gray">
          {{ translate("title", 'FieldMods') }}
          <small>
            <span class="float-end small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
          </small>
        </h2>
        <ListEmptyWidget :range="3" />
      </div>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import FieldModsItem from '@/components/common/FieldMods/FieldModsItem'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import CounterRounded from '@/components/common/CounterRounded'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['FieldMods', 'CommonErrors', 'CommonFunctional'],
  setup() { return useLabels() },
  components: {
    FieldModsItem,
    ListEmptyWidget,
    CounterRounded
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    fieldModsPageUrl: {
      type: String,
      required: false,
      default: null
    },
    fullWidth: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      loading: true,
      mods: [],
      isExpanded: false,
      error: {
        present: false,
        text: null
      }
    }
  },
  computed: {
    displayedMods() {
      return this.isExpanded ? this.mods : this.mods.slice(0, 3)
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchFieldMods()
    }
  },
  created() {
    this.fetchFieldMods()
  },
  methods: {
    fetchFieldMods() {
      if (this.getCurrentSerial) {
        this.loading = true
        bdService.fetchFieldMods(this.getCurrentSerial, 'AA,A').then(r => {
          this.mods = r.filter(x => ['PENDING', 'IN_PROGRESS'].includes(x.status.toUpperCase()))
          this.mods.sort(this.compare)
          this.error.present = false
          this.error.text = null
        }).catch(e => {
          if (!e.isCancelled) {
            this.error.present = true
            this.error.text = e.message || ''
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    compare(a, b) {
      // Sorting by campaign code ('AA' on top) and then sort by end date (oldest on top).
      if (a.campaign_code < b.campaign_code) return 1
      else if (a.campaign_code > b.campaign_code) return -1
      else if (a.end_date > b.end_date) return 1
      else if (a.end_date < b.end_date) return -1
      else return 0
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/_colors.scss';

.title-divider {
  border-top: 2px solid $black;
}
</style>
