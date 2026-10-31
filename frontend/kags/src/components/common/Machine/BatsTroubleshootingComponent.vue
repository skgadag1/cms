<template>
  <main class="col-8">
    <template v-if="tdata">
      <div class="row py-3 d-flex align-items-center mb-5">
        <a href="#" class="col-auto text-white font-condensed">
          <div class="d-inline-flex rounded-circle bg-secondary justify-content-center align-items-center" style="width: 24px; height: 24px">&lt;</div>
          Back
        </a>
        <h1 class="col border-start border-2 border-gray-700 text-white">Code: {{ tdata.code }}</h1>
      </div>
      <div class="row mb-4">
        <div class="col-12 text-center mb-3" data-test-id="bats-description">
          <h2>{{ tdata.code_description }}</h2>
          <p v-for="(desc, i) in tdata.detailed_description"
             :key="'error_desc_'+i"
          >
            {{ desc.text }}
          </p>
        </div>
        <div v-if="tdata.component_links && tdata.component_links.length" class="col-12">
          <div class="box" data-test-id="bats-component_links">
            <h3>Components</h3>
            <b-link v-for="(c, i) in tdata.component_links"
                    :key="'troubleshooting_links_'+i"
                    :href="c.link"
                    target="_blank"
                    class="d-block"
            >
              {{ c.name }}
            </b-link>
          </div>
        </div>
        <div v-if="tdata.causes && tdata.causes.length" class="col-12">
          <div class="box" data-test-id="bats-causes">
            <h3>Possible cause</h3>
            <ul class="list-checked">
              <li v-for="(c, i) in tdata.causes" :key="'troubleshooting_cause_'+i">{{ c }}</li>
            </ul>
          </div>
        </div>
        <div v-if="tdata.conditions_for_alarm && tdata.conditions_for_alarm.length" class="col-12">
          <div class="box" data-test-id="bats-conditions_for_alarm">
            <h3>Conditions for Code</h3>
            <div v-for="(c, i) in tdata.conditions_for_alarm" :key="'troubleshooting_conditions_for_alarm_'+i">{{ c }}</div>
          </div>
        </div>
        <div v-if="tdata.conditions_for_clearing_alarm && tdata.conditions_for_clearing_alarm.length" class="col-12">
          <div class="box" data-test-id="bats-conditions_for_clearing_alarm">
            <h3>Conditions for Clearing Code</h3>
            <div v-for="(c, i) in tdata.conditions_for_clearing_alarm" :key="'troubleshooting_conditions_for_clearing_alarm_' + i">{{ c }}</div>
            <WarningAlert v-for="(w, i) in tdata.warning_data"
                          :key="'troubleshooting_warning_'+i"
                          :data-test-id="'bats-warning_'+i"
            >
              <template #title>Warning</template>
              <template #body>
                <div v-for="(par, y) in w.paragraphs" :key="'troubleshooting_warning_data_' + y">{{ par }}</div>
              </template>
            </WarningAlert>
          </div>
        </div>
      </div>

      <TroubleshootingTable data-test-id="bats-troubleshooting-table"
                            :notes="tdata.troubleshooting_chart.notes"
                            :items="troubleshootingTable"
                            :show="!!(tdata.troubleshooting_chart && tdata.troubleshooting_chart.steps.length)"
      />
    </template>
    <template v-else>
      <div class="d-flex w-100 align-items-center justify-content-center py-4">
        <b-spinner variant="primary" />
      </div>
      <div class="d-flex w-100 align-items-center justify-content-center py-5 my-5">
        <b-spinner variant="primary" />
      </div>
    </template>
  </main>
</template>

<script>
import WarningAlert from '@/components/common/WarningAlert'
import TroubleshootingTable from '@/components/common/TroubleshootingTable'

export default {
  name: 'BatsTroubleShootingComponent',
  components: {TroubleshootingTable, WarningAlert},
  props: {
    tdata: {
      required: true
    }
  },
  computed: {
    troubleshootingTable() {
      const td = []
      for (const r of this.tdata.troubleshooting_chart.steps) {
        td.push({
          'action': r.instructions,
          'yes': r.yes,
          'no': r.no,
          'reference': r.step
        })
      }
      return td
    }
  }
}
</script>
