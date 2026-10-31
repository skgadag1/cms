<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <MachineHeader :banner-url="translatedConfig('bannerUrl')" />

    <div class="container">
      <div class="row">
        <div class="col-12 col-lg-12 pe-0">
          <InPageNavigationDesktop />
        </div>

        <template v-if="!error">
          <BatsTroubleshootingComponent v-for="(tdata, idx) in errorData"
                                        :key="'bats_troubleshooting_data_'+idx"
                                        :tdata="tdata"
          />
        </template>

        <main v-else class="col-9 mb-4">
          <div class="row py-3 d-flex align-items-center mb-4">
            <a href="#" class="col-auto text-white font-condensed">
              <div class="d-inline-flex rounded-circle bg-secondary justify-content-center align-items-center" style="width: 24px; height: 24px">&lt;</div>
              Back
            </a>
            <h1 class="col border-start border-2 border-gray-700 text-white">Error {{ error.code }}</h1>
          </div>
          <b-alert variant="danger" show>
            {{ error.message || error }}
          </b-alert>
        </main>
      </div>
    </div>
  </div>
</template>

<script>
import MachineHeader from '@/components/common/Machine/MachineHeader'
import InPageNavigationDesktop from '@/components/common/Navigation/InPageNavigationDesktop'
import BatsTroubleshootingComponent from '@/components/common/Machine/BatsTroubleshootingComponent'
import {useLabels} from '@/composables/useLabels'

export default {
  name: 'Bats',
  setup() {
    return useLabels()
  },
  components: {
    MachineHeader,
    InPageNavigationDesktop,
    BatsTroubleshootingComponent
  },
  data() {
    return {
      errorData: null,
      error: false,
      componentConfig: this.config
    }
  },
  mounted() {
    this.$http.get(this.$api.BATS_TROUBLESHOOTING.replace('{serial}', this.config.serial).replace('{code}', this.config.code)).then(r => {
      if (r.data.error) this.error = r.data.error
      this.errorData = r.data
    }).catch(r => {
      this.error = r.response ? r.response.data.error : {'code': '', 'message': r}
    })
  }
}
</script>
