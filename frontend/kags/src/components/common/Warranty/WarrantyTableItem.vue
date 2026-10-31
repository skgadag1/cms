<template>
  <div class="grid-table-expandable" :class="{ 'grayed': disabled}">
    <div class="grid-table-row position-relative overflow-hidden"
         :class="{ 'disabled': disabled }"
         @click="toggle"
    >
      <div class="item__status"><b-badge :variant="disabled ? '' : 'success'" class="text-uppercase">{{ item.status.toUpperCase() }}</b-badge></div>
      <div class="item__description"><span class="font-condensed">{{ item.policy_name.replace(/-/g, ' - ') }}</span></div>
      <div class="item__type"><span class="d-md-none me-2 font-condensed label">{{ translate('typeHeader') }}</span> {{ capitalize(item.type.toLowerCase()) }}</div>
      <div class="item__date"><span class="d-md-none me-2 font-condensed label">{{ translate('startDateHeader') }}</span> {{ new Date(item.start_date).toLocaleDateString() }}</div>
      <div class="item__date"><span class="d-md-none me-2 font-condensed label">{{ translate('endDateHeader') }}</span> {{ new Date(item.end_date).toLocaleDateString() }}</div>
      <div class="item__hours text-md-end"><span class="d-md-none me-2 font-condensed label">{{ translate('hoursHeader') }}</span> {{ item.hours_covered }}</div>
      <div class="dropdown-toggle" :aria-expanded="expanded" />
    </div>
    <hr v-if="expanded" class="m-0">
    <b-collapse v-model="expanded">
      <b-error v-if="error.present" :code="error.code" class="m-0">
        {{ translate('defaultError', 'CommonErrors') }}
      </b-error>
      <b-notification v-if="!loading && inclusions && inclusions.length === 0" class="m-0">
        {{ translate('noWarrantyInclusions') }}
      </b-notification>
      <overlay-wrapper :show="loading">
        <div v-if="loading" class="p-5" />
      </overlay-wrapper>
      <b-tree v-if="visible && inclusions && inclusions.length" :items="inclusions" class="py-2 px-3" />
    </b-collapse>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import BTree from '../BTree'
import BError from '../BError'
import BNotification from '../BNotification'
import {bdService} from '@/services'

export default {
  conceptName: ['CommonErrors'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    BNotification,
    BTree,
    BError
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      expanded: false,
      visible: false,
      inclusions: null,
      error: {present: false},
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  methods: {
    toggle() {
      if (!this.inclusions && !this.expanded) {
        this.loading = true
        this.expanded = true
        bdService.fetchWarrantyInclusions(this.getCurrentSerial, this.item.policy_code).then(response => {
          this.inclusions = response.data.inclusions
          this.visible = true
        }).catch(error => {
          this.error = {present: true, code: error.code, text: {ck: 'defaultError', cp: 'shared_CommonErrors'}}
        }).finally(() => {
          this.loading = false
        })
      } else {
        if (!this.expanded) {
          this.visible = true
          setTimeout(() => {
            this.expanded = true
          }, 10)
        } else {
          this.error = {present: false}
          this.expanded = false
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/_transitions.scss';
@import '../../../scss/_colors.scss';
@import '../../../scss/grid';

.dropdown-toggle {
  transition: $transition-base;
  position: absolute;
  width:.5rem;
  height: 1.2rem;
  bottom: 1rem;
  right: 1rem;

  &[aria-expanded="true"] {
    transform: rotate(180deg);
  }
}

.grid-table-expandable {
  background: $white;

  .grid-table-row {
    background-color: $white;
  }
}
@include media-breakpoint-up('md') {
  .dropdown-toggle {
    bottom: calc(50% - .5rem)
  }
}
</style>
