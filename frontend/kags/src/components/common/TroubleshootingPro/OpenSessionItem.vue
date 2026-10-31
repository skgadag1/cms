<template>
  <div class="grid-table-row">
    <div class="item__title font-condensed">
      {{ item.title }}
    </div>
    <div class="item__description">
      {{ item.description }}
    </div>
    <div class="item__date_opened d-flex d-md-block justify-content-between">
      <label class="d-block d-md-none me-auto">{{ translate('dateOpened', 'TroubleshootingProcedures') }}</label>
      <div class="text-end text-md-start">
        <div class="item__date_value">{{ formatDate(item.createdAt) }}</div>
        <div class="item__days_ago d-block d-md-none">
          ({{ daysSince(item.createdAt) }} {{ translate('daysAgo', 'TroubleshootingProcedures') }})
        </div>
      </div>
    </div>
    <div class="item__session_actions d-flex justify-content-end">
      <b-button variant="outline-primary"
             size="sm"
             class="font-condensed text-nowrap"
             :data-test-id="'open-session-item__review__' + item.session_id"
             @click.stop.prevent="$emit('review', item.session_id)"
      >
        {{ translate('review', 'TroubleshootingProcedures') }}
      </b-button>
      <b-button v-if="item.isResumingAllowed"
             variant="primary"
             size="sm"
             class="font-condensed text-nowrap"
             :disabled="resuming"
             :data-test-id="'open-session-item__resume__' + item.session_id"
             @click.stop.prevent="$emit('resume', item.session_id)"
      >
        {{ translate('resume', 'TroubleshootingProcedures') }}
      </b-button>
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {daysSince} from '@/helpers/dateTimeUtils'

export default {
  conceptName: ['TroubleshootingProcedures', 'HistoryWidget'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  props: {
    item: {
      type: Object,
      required: true
    },
    resuming: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  methods: {
    daysSince
  }
}
</script>
