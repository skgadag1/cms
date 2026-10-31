<template>
  <div class="mb-5 flex-grow-1 flex-shrink-0">
    <MachineHeader :banner-url="translatedConfig('bannerUrl')"
                   :empty="isEmpty"
    />

    <b-container tag="section">
      <b-row>
        <InPageNavigation :disabled="isEmpty" />

        <b-col cols="12" lg="9" class="px-0">
          <MachineLibraryComponent :disabled="isEmpty">
            <template v-slot:header>
              <h1 class="py-4 mt-4 mb-5 mb-sm-3 mt-sm-1 pt-lg-0 mb-lg-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
                  data-test-id="component-title"
              >
                {{ componentConfig.componentTitle }}
              </h1>
            </template>
          </MachineLibraryComponent>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>

<script>
import MachineHeader from '@/components/common/Machine/MachineHeader'
import InPageNavigation from '@/components/common/Navigation/InPageNavigation'
import MachineLibraryComponent from '@/components/common/Machine/MachineLibraryComponent'
import {mapGetters} from 'vuex'
import {useLabels} from '@/composables/useLabels'

export default {
  setup() {
    return useLabels()
  },
  components: {
    MachineHeader,
    InPageNavigation,
    MachineLibraryComponent
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
