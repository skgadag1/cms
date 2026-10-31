<template>
  <b-col cols="12" :md="componentConfig.halfWidth === 'true' ? '6' : '12'" class="mb-5">
    <b-container class="box d-flex h-100 align-items-center justify-content-center">
      <b-row align-content="stretch" class="w-100" align-v="center">
        <b-col cols="12"
               :md="componentConfig.halfWidth === 'true' ? '12' : '8'"
               :lg="componentConfig.halfWidth === 'true' ? '12' : '6'"
               class="d-flex align-items-center justify-content-center flex-column mb-3"
        >
          <h2>{{ translate('title') }}</h2>
          <div class="w-100" v-html="translatedConfig('text')" />
        </b-col>

        <b-col v-if="componentConfig.imageUrl"
               cols="12"
               :md="componentConfig.halfWidth === 'true' ? '12' : '4'"
               :lg="componentConfig.halfWidth === 'true' ? '12' : '6'"
               class="d-flex align-items-center justify-content-center d-block-ie-only"
        >
          <b-img fluid
                 placement="center"
                 :src="translatedConfig('imageUrl')"
          />
        </b-col>
      </b-row>
    </b-container>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
export default {
  setup() { return useLabels() },
  data() {
    return {
      componentConfig: this.config
    }
  }
}
</script>
