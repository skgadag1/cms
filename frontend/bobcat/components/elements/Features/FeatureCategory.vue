<template>
  <div v-if="counter !== 0" class="p-0 bg-white" data-test-id="features-categories">
    <Accordion
      :counter="counter"
      is-primary-accordion
      shadow
      :color="colors.DARK"
      :title="category.title"
      :is-expanded="isExpanded"
      :toggle-cb="toggleAccordion"
      :htag="primaryAccordionHtag"
      @click="ga4click"
    >
      <div v-if="category.features && category.features.length" class="d-flex flex-column">
        <Feature v-for="(feature, idx) in category.features"
                 :key="`feature-${feature['@id']}__${idx}`"
                 :is-expanded="isExpanded"
                 :feature="feature"
                 :secondary-accordion-htag="secondaryAccordionHtag"
                 @toggle="toggleAccordion"
        />
      </div>
    </Accordion>
  </div>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import {COLORS} from '~/constants/accordion'
import useModels from '@/helpers/composables/useModels'

export default {
  props: {
    category: {
      type: Object,
      required: true
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    primaryAccordionHtag: {
      type: String,
      default: 'h3'
    },
    secondaryAccordionHtag: {
      type: String,
      default: 'h4'
    }
  },
  setup(props, {emit}) {
    const {model} = useModels()
    const {$ga4} = useContext()
    const {category: {features, isExpanded}} = props
    const colors = ref(COLORS)
    const counter = ref(features.length)
    const isVisible = ref(isExpanded)

    const toggleAccordion = (state) => {
      emit('accordion-expanded', state, props.category['@id'])
    }

    const ga4click = (event) => {
      $ga4.events.product_features_click(event, model)
    }

    const setVisible = (bool) => {
      isVisible.value = bool
    }

    return {
      colors,
      counter,
      toggleAccordion,
      setVisible,
      ga4click
    }
  }
}
</script>
