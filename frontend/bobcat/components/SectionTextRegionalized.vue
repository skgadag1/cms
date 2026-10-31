<template>
  <section>
    <SectionText
      :title="regionalizedData.title"
      :content="regionalizedData.content"
      :font-size="regionalizedData.fontSize"
      :htag="regionalizedData.htag"
      class="na-region-text"
    />
    <SectionIntelligent v-if="isNARegion" />
    <SectionLinks
      :links="regionalizedData.links"
      :class="isNARegion ? 'na-section-link' : null"
    />
  </section>
</template>

<script>
import {ref, computed, useContext} from '@nuxtjs/composition-api'
import SectionIntelligent from './SectionIntelligent.vue'
import useMounted from '~/helpers/composables/useMounted'

export default {
  components: {
    SectionIntelligent
  },
  props: {
    nodeId: {
      type: String
    },
    sections: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const {$regions} = useContext()
    const regionalSection = props.sections?.[$regions.current.language.name]?.['@nodes']?.map(key => props.sections[$regions.current.language.name][key])
    const nativeSection = props.sections?.['@nodes']?.map(key => props.sections[key])
    const sectionsAll = ref(regionalSection || nativeSection)
    const defaultLanguageRegionSections = ref(props.sections?.[process.env.NUXT_DEFAULT_LANGUAGE]?.['@nodes']?.map(key => props.sections[process.env.NUXT_DEFAULT_LANGUAGE][key]))
    useMounted(props.nodeId)

    const regionalizedData = computed(() => {
      // returning regionalized error data - the best possible (defined)
      return sectionsAll.value?.find(s => s.region?.['@name'] === $regions.current.name) || // current language, current region
        sectionsAll.value?.find(s => s.region?.['@name'] === process.env.NUXT_DEFAULT_REGION) || // current language, default region
        defaultLanguageRegionSections.value?.find(s => s.region?.['@name'] === $regions.current.name) || // default language, current region
        defaultLanguageRegionSections.value?.find(s => s.region?.['@name'] === process.env.NUXT_DEFAULT_REGION) || // default language, default region
        sectionsAll.value?.[0] || // current language, first region
        defaultLanguageRegionSections.value?.[0] || // default language, first region
        {}
    })

    const isNARegion = $regions?.current?.name === process.env.NUXT_DEFAULT_REGION

    return {
      regionalizedData,
      isNARegion
    }
  }
}
</script>

<style scoped>
.na-region-text {
  margin-top: 10%;
  margin-left: 10%;
}
.na-section-link {
  margin-top: 2%;
}
</style>
