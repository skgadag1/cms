<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <MachineHeader :banner-url="translatedConfig('bannerUrl')" />

    <b-container>
      <b-row>
        <InPageNavigation :disabled="isEmpty" />

        <template v-if="!error">
          <SchematicsTableComponent :tdata="schematicsData"
                                    :loading="loading"
                                    :disabled="isEmpty"
          />
        </template>

        <b-col v-else tag="main" cols="12" lg="9" class="mb-4">
          <h1 class="py-5 pt-md-0 mb-md-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
              data-test-id="component-title"
          >
            Error {{ error.code }}
          </h1>

          <b-alert variant="danger" show>
            {{ error.message || error }}
          </b-alert>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
import MachineHeader from '@/components/common/Machine/MachineHeader'
import InPageNavigation from '@/components/common/Navigation/InPageNavigation'
import SchematicsTableComponent from '@/components/common/Machine/SchematicsTableComponent'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import {useLabels} from '@/composables/useLabels'

export default {
  setup() {
    return useLabels()
  },
  components: {
    MachineHeader,
    InPageNavigation,
    SchematicsTableComponent
  },
  data() {
    return {
      schematicsData: [],
      errorData: null,
      error: false,
      loading: false
    }
  },
  computed: {
    componentConfig() { return this.config },
    ...mapGetters([
      'isEmpty',
      'getCurrentSerial'
    ])
  },
  watch: {
    getCurrentSerial() {
      this.getSchematicsData()
    }
  },
  created() {
    this.getSchematicsData()
  },
  methods: {
    getSchematicsData() {
      if (!this.isEmpty && this.getCurrentSerial) {
        this.loading = true
        bdService.fetchSchematics(this.getCurrentSerial).then(r => {
          if (r.data.error) this.error = r.data.error
          this.schematicsData = r.data
        }).catch(e => {
          if (!e.isCancelled) {
            this.error = e.response ? e.response.data.error : e
            this.errorData = e.data
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      } else {
        this.schematicsData = []
      }
    }
  }
}
</script>
