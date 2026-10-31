<template>
  <div data-test-id="error-page">
    <header ref="header" class="shadow w-100 bg-primary fixed-top ">
      <b-col cols="12">
        <b-row>
          <b-navbar type="dark" toggleable="md" class="text-white w-100 container">
            <div v-if="logoUrl" class="d-flex justify-content-center col-md-2 col px-0" data-test-id="main-header-logo">
              <b-navbar-brand class="text-white" href="/">
                <img :src="logoUrl"
                     alt="Bobcat logo"
                     title="Bobcat logo"
                     height="38"
                     width="143"
                     style="margin-top: -0.5rem; width: auto !important; max-width: none;"
                     class="img-fluid"
                     loading="lazy"
                     decoding="async"
                >
              </b-navbar-brand>
            </div>
          </b-navbar>
        </b-row>
      </b-col>
    </header>
    <div class="min-vh-100 min-vw-100 d-flex align-items-center justify-content-center flex-column">
      <SectionTextRegionalized
        v-if="errorPageConfig.sections"
        :sections="errorPageConfig.sections"
        class="error-page-section"
      />
      <div v-if="isDev" class="container">
        <div class="row">
          <WarningAlert class="col-12 mt-2 py-2">Development Environment Only Info: <br> <strong>{{ error.statusCode }}</strong>: {{ error.message }}</WarningAlert>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {useContext, useFetch, ref, onMounted} from '@nuxtjs/composition-api'

export default {
  name: 'Error',
  props: {
    error: {
      type: Object
    }
  },
  setup() {
    const {$utils, $ga4} = useContext()
    const errorPageConfig = ref({})
    const logoUrl = ref()
    const isDev = process.env.IS_DEV === true || process.env.IS_DEV === 'true'

    useFetch(async () => {
      errorPageConfig.value = (await (() => import('~/static/errorPageConfig.json'))()).default

      logoUrl.value = $utils.getCloudinaryAsset(errorPageConfig.value.logo, true, {height: 38, crop: 'scale'})
    })

    if (process.client) {
      onMounted(() => {
        const qparams = new URLSearchParams(window?.location?.search)
        $ga4.events.error_general({
          error_details: `Missing page: ${qparams.get('path')}, referring from: ${qparams.get('ref')?.length ? qparams.get('ref') : 'unknown'}`
        })
      })
    }

    return {errorPageConfig, logoUrl, isDev}
  }
}
</script>

<style scoped>
.error-page-section ::v-deep .section-text header {
  text-align: center;
}
</style>
