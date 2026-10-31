<template>
  <div class="specifications specifications-spec pb-3 d-flex flex-column flex-wrap align-content-start">
    <template v-if="keyTableData && keyTableData.sections">
      <div class="specification-row key-specifications">
        <div class="px-3 print-specification-items">
          <template v-for="(section, sectionIndex) in keyTableData.sections">
            <PrintSpecItem v-for="(property, propertyIndex) in section.properties"
                           :key="`key-specification-${sectionIndex}-${propertyIndex}`"
                           :model="property[keyTableData.models[0].id]"
                           :label="property.label || ''"
                           :unit="property.unit || ''"
            />
          </template>
        </div>
      </div>
    </template>
    <template v-if="allTableData && allTableData.sections">
      <template v-for="(section, sectionIndex) in allTableData.sections">
        <div v-if="Object.keys(section.properties).length > 0" :key="`specification-${sectionIndex}`" class="specification-row mt-3 font-weight-bold size-17">
          <b-icon v-if="showIcon && sectionIconDisplay(section.title).type === 'icon'"
                  :icon="sectionIconDisplay(section.title).name"
                  class="mr-2"
                  aria-hidden="true"
          />
          <img v-else-if="showIcon && sectionIconDisplay(section.title).type === 'image'"
               :src="extractUrlFromConceptValue(sectionIconDisplay(section.title).src)"
               alt=""
               aria-hidden="true"
               decoding="async"
               class="mr-2 print-spec-section-icon"
          >
          {{ section.title }}
        </div>
        <PrintSpecItem v-for="(property, propertyIndex) in section.properties"
                       :key="`specification-${sectionIndex}-${propertyIndex}`"
                       :model="property[allTableData.models[0].id]"
                       :label="property.label || ''"
                       :unit="property.unit || ''"
                       :is-feature-section="isFeaturesSection(section.title)"
        >
          <template v-if="isFeaturesSection(section.title) && showIcon" #suffix>
            <template v-if="getCheckmarkIcon(section.title, property[allTableData.models[0].id])">
              <img v-if="getCheckmarkIcon(section.title, property[allTableData.models[0].id]).type === 'image'"
                   :src="extractUrlFromConceptValue(getCheckmarkIcon(section.title, property[allTableData.models[0].id]).src)"
                   alt="checkmark"
                   decoding="async"
                   class="print-check-icon"
              >
              <b-icon v-else
                      :icon="getCheckmarkIcon(section.title, property[allTableData.models[0].id]).name"
                      class="print-check-icon"
              />
            </template>
          </template>
        </PrintSpecItem>
      </template>
    </template>
  </div>
</template>

<script>
import {useFetch, watch, useContext, ref} from '@nuxtjs/composition-api'
import PrintSpecItem from './elements/PrintSpecItem.vue'
import {MODEL_TYPE, PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import useSpecificationData from '@/helpers/composables/useSpecificationData'
import {fetchEvolutionCompareSpecSectionIconConcepts, resolveSectionIconDisplay, extractUrlFromConceptValue} from '~/helpers/evolutionCompareSpecsFilter'

export default {
  components: {PrintSpecItem},
  props: {
    models: {
      type: Array,
      required: true
    },
    uom: {
      type: Object
    },
    modelType: {
      type: String,
      default: MODEL_TYPE.PRODUCT
    },
    specificationLevel: {
      type: [Number, String],
      default: 3
    },
    tableSpecificationData: {
      type: Object
    },
    showIcon: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  emits: ['data-update', 'updateScrollPosition'],
  setup(props) {
    const {modelType, labels, showIcon} = props
    const {$services, $regions} = useContext()
    const sectionIconConcepts = ref({})
    const options = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      showCategoryNames: true
    }

    const keySpecs = useSpecificationData(props, options)
    const allSpecs = useSpecificationData(props, options)

    const {fetch} = useFetch(async () => {
      await keySpecs.specificationFetch(2, modelType, labels)
      await allSpecs.specificationFetch(3, modelType, labels)
      if (showIcon) {
        await loadSectionIconConcepts()
      }
    })

    const loadSectionIconConcepts = async () => {
      const loaded = await fetchEvolutionCompareSpecSectionIconConcepts(
        $services.bobcatapi,
        $regions.current.name,
        process.env.VUE_APP_SITE_BASENAME || ''
      )
      if (loaded && Object.keys(loaded).length) {
        sectionIconConcepts.value = loaded
      }
    }

    const sectionIconDisplay = (sectionTitle) => {
      return resolveSectionIconDisplay(sectionTitle, {
        conceptImageMap: sectionIconConcepts.value
      })
    }

    const isFeaturesSection = (sectionTitle) => {
      return String(sectionTitle || '').toLowerCase().includes('feature')
    }

    const getCheckmarkIcon = (sectionTitle, model) => {
      if (!showIcon || !isFeaturesSection(sectionTitle)) {
        return null
      }
      const value = typeof model === 'object' ? model?.value : model
      const val = String(value || '').toLowerCase().trim()
      const includedValues = new Set(['std', 'standard', 'yes', 'included', 'true'])
      return includedValues.has(val) ? sectionIconDisplay('success-check') : null
    }

    watch(() => props.uom, () => {
      fetch()
    })

    return {
      keyTableData: keySpecs.tableData,
      allTableData: allSpecs.tableData,
      sectionIconDisplay,
      extractUrlFromConceptValue,
      getCheckmarkIcon,
      isFeaturesSection
    }
  }
}
</script>
