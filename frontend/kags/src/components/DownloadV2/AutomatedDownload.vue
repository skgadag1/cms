<template>
  <section class="container mb-4">
    <div class="box">
      <div class="row">
        <div class="col-12 col-md-7">
          <h2>{{ translate('title') }}</h2>
          <p v-html="subtitle" />
        </div>
        <div class="col-12 col-md-5 text-md-end font-condensed">
          <template v-for="(document, i) in componentConfig.documents" :key="'document' + i">
            <div>
              <a :href="document.assetLink" target="_blank">
                <i class=" text-primary me-2" :class="'fas fa-' + document.icon" />{{ document.documentDownloadsLabel }}
              </a>
            </div>
          </template>

          <template v-for="(link, i) in releaseNotes" :key="'releaseNotes_' + i">
            <div>
              <b-link class="font-condensed" :href="link.url" target="_blank"><b-icon icon="file" class="me-1 text-large" />{{ link.label }}</b-link>
            </div>
          </template>
        </div>
      </div>


      <template v-for="(link, i) in componentConfig.softwares" :key="'software' + i">
        <SoftwareLink :link="link" @error="errorSoftware = true" @releaseNotes="addReleaseNotes" />
      </template>

      <div v-if="inMgnl" class="box">
        Notice: These are mocked data for rendering purposes only
      </div>

      <div v-if="errorSoftware" class="box"><b-icon icon="exclamation-triangle" class="small align-self-end text-gray-600 me-2" /> {{ translate('defaultError', 'CommonErrors') }}</div>
    </div>
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels';
import { inMgnl } from '@/helpers/magnolia-helper';
import SoftwareLink from './SoftwareLink';

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {SoftwareLink},
  data() {
    return {
      componentConfig: this.config,
      errorSoftware: false,
      releaseNotes: [],
      inMgnl: inMgnl
    }
  },
  computed: {
    subtitle() {
      return this.componentConfig.subtitle ? this.translatedConfig('subtitle') : ''
    }
  },
  created() {
    this.$store.dispatch('fetchConcepts', 'en')
  },
  methods: {
    addReleaseNotes(link) {
      this.releaseNotes.push(link)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
