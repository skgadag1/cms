<template>
  <b-col tag="main" cols="12" lg="9">
    <h1 class="py-4 mt-4 mb-5 mb-sm-3 mt-sm-1 pt-lg-0 mb-lg-5 text-white d-flex align-items-center justify-content-center flex-column flex-sm-row"
        data-test-id="component-title"
    >
      {{ translate("schematics", 'SchematicsTableComponent') }}
    </h1>

    <slot name="top-content">
      <BasicBreadcrumb />
    </slot>

    <overlay-wrapper :show="loading">
      <SchematicsTable v-if="electricData.length"
                       class="mb-3"
                       :title="translate('electricSchematics', 'SchematicsTableComponent')"
                       :items="electricData"
                       :show="!!(electricData && electricData.length)"
      />

      <SchematicsTable v-if="hydraulicData.length"
                       class="mb-3"
                       :title="translate('hydraulicSchematics', 'SchematicsTableComponent')"
                       :items="hydraulicData"
                       :show="!!(hydraulicData && hydraulicData.length)"
      />

      <SchematicsTable v-if="disabled || !tdata.length"
                       class="mb-3"
                       disabled
                       :show="false"
                       :title="translate('electricSchematics', 'SchematicsTableComponent')"
                       :items="tdata"
      />
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import SchematicsTable from '@/components/common/SchematicsTable'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'

export default {
  conceptName: ['SchematicsTableComponent'],
  setup() { return useLabels() },
  components: {
    BasicBreadcrumb,
    SchematicsTable
  },
  props: {
    tdata: {
      required: true
    },
    loading: {
      type: Boolean,
      required: false,
      default: false
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  computed: {
    electricData() {
      return this.tdata.filter(x => x.type === 'ELECTRIC')
    },
    hydraulicData() {
      return this.tdata.filter(x => x.type === 'HYDRAULIC')
    }
  }
}
</script>
