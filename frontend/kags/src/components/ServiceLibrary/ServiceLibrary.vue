<template>
  <div class="mb-5 position-relative flex-grow-1 flex-shrink-0">
    <div class="banner-main no-navigation" :style="'background-image: url(' + translatedConfig('bannerUrl') + ')'" />

    <b-container tag="section" class="pt-2">
      <b-row>
        <b-col cols="12" class="pt-4">
          <h1 class="py-4 mt-4 mb-5 mb-lg-4 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
              data-test-id="component-title"
          >
            {{ translate("componentTitle") }}
          </h1>
        </b-col>
      </b-row>

      <b-row>
        <b-col cols="12" class="mt-md-1 mt-lg-4 pt-lg-1">
          <BasicBreadcrumb />
        </b-col>
      </b-row>

      <ServiceLibraryComponent />
    </b-container>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ServiceLibraryComponent from '@/components/common/ServiceLibrary/ServiceLibraryComponent'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'

export default {
  setup() { return useLabels() },
  components: {
    ServiceLibraryComponent,
    BasicBreadcrumb
  },
  data() {
    return {
      componentConfig: this.config
    }
  }
}
</script>
