<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <MachineHeader :banner-url="translatedConfig('bannerUrl')"
                   :empty="isEmpty"
    />

    <b-container tag="section">
      <b-row>
        <InPageNavigation :disabled="isEmpty" />

        <b-col cols="12" lg="9" class="px-0">
          <BuiltOptionsComponent :disabled="isEmpty">
            <template v-slot:header>
              <h1 class="py-4 mt-4 mb-5 mb-sm-3 mt-sm-1 pt-lg-0 mb-lg-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row">
                {{ translate("title") }}
              </h1>
            </template>

            <template v-slot:top-content>
              <BasicBreadcrumb />
            </template>
          </BuiltOptionsComponent>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import MachineHeader from '@/components/common/Machine/MachineHeader'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'
import BuiltOptionsComponent from '@/components/common/BuiltOptions/BuiltOptionsComponent'
import InPageNavigation from '@/components/common/Navigation/InPageNavigation'

export default {
  setup() { return useLabels() },
  components: {
    MachineHeader,
    BasicBreadcrumb,
    BuiltOptionsComponent,
    InPageNavigation
  },
  data() {
    return {
      componentConfig: this.config
    }
  },
  computed: {
    ...mapGetters([
      'isEmpty'
    ])
  }
}
</script>
