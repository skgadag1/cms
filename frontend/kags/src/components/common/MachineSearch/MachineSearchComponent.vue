<template>
  <b-container class="my-5 py-4 text-white">
    <b-row class="mb-4">
      <b-col md="6" lg="4">
        <h1>{{ translate('title') }}</h1>
        <div class="display-4">{{ translate('subtitle') }}</div>
      </b-col>
    </b-row>

    <b-row class="mb-4">
      <b-col md="6" lg="4">
        <AddSerialControl input-mode disable-empty @add="handleAddSerial" />
      </b-col>
    </b-row>

    <b-row>
      <b-col md="7" lg="4">
        <div v-show="getRecentSerials.length" class="fw-bold mb-2">{{ translate('recentLabel') }}</div>
        <b-badge v-for="s in getRecentSerials"
                 :key="'recent-serials__'+s.serial_number"
                 variant="primary"
                 class="me-2 mb-2"
                 :href="translatedConfig('machineUrl') + '?serial=' + s.serial_number"
        >
          {{ s.serial_number }}
        </b-badge>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import AddSerialControl from '@/components/common/Machine/AddSerialControl'
import {mapGetters} from 'vuex'

export default {
  setup() { return useLabels() },
  components: {
    AddSerialControl
  },
  computed: {
    ...mapGetters([
      'getRecentSerials'
    ])
  },
  methods: {
    handleAddSerial(s) {
      if (s) window.location.href = this.translatedConfig('machineUrl') + '?serial=' + s.serial_number
    }
  }
}
</script>

<style lang="scss" scoped>
.display-4 {
  line-height: 1.5rem;
}

.badge {
  font-size: 100%;
  min-width: 6.75rem;
}
</style>
