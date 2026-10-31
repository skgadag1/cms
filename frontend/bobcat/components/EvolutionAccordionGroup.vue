<template>
  <LazyHydrate :on-interaction="['click', 'touchstart']">
    <section data-test-id="evolution-accordion-group" class="evolution-accordion-group py-4" :class="selectedColorVariant.background">
      <div class="container">
        <div class="row" :class="selectedAlignmentVariant">
          <div class="col-12">
            <div class="w-100 accordion-group-text">
              <div v-if="evolutionTitle" class="mb-3 mt-3 w-100" :class="selectedColorVariant.text" v-html="evolutionTitle" />
              <div v-else-if="title" class="mb-3 mt-3 w-100">
                <component :is="htag" :class="htag" data-test-id="accordion-group-title">{{ title }}</component>
              </div>
            </div>
            <div v-if="ctas.length" class="col-12 d-flex flex-column flex-md-row accordion-group-text">
              <DynamicButton
                v-for="(button,index) in ctas"
                :key="`evolution-accordion-group-button-${index}`"
                :variant="button.variant"
                :color="button.color"
                :href="button.link"
                :target="button.target === '_blank_follow' ? '_blank' : button.target"
                class="mr-0 mr-md-3 mt-3 mt-lg-0 text-uppercase"
                :data-test-id="`cta-button-${index}`"
              >
                {{ button.title }}
                <b-icon v-if="button.variant === BUTTON_NAMES.LINEAR" icon="chevron-right" class="mt-1" />
              </DynamicButton>
            </div>
            <div class="col-12 px-0 mx-auto d-flex flex-column align-items-center justify-content-center w-100">
              <div class="d-flex justify-content-end w-100">
                <button
                  role="button"
                  class="d-flex justify-content-center align-items-center mb-3 px-3 bg-transparent border-0 font-weight-bold"
                  :class="selectedColorVariant.expandAllText"
                  data-test-id="expand-button"
                  title="Toogle accordion"
                  :aria-expanded="isExpandedAll"
                  @click.prevent="(e) => {ga4click(e); expandAll(e)}"
                >
                  {{ isExpandedAll ? (labels.collapseAll || collapseLabel) : (labels.expandAll || expandLabel) }}  <span class="d-flex align-items-center justify-conent-center m-0 ml-2 transition-base" style="font-size: 6px" :style="isExpandedAll ? 'transform: rotate(180deg)' : null"><b-icon icon="sort-down" /></span>
                </button>
              </div>
              <template v-if="group && group.length">
                <div v-for="(accordion) in group" :key="accordion['@id']" class="p-0 transition-base mb-2 w-100" data-test-id="accordion-standalone">
                  <Accordion v-if="accordion && accordion.children && accordion.children.length"
                             :id="accordion['@id']"
                             :ref="(el) => accordion.element = el"
                             :toggle-cb="toggleAccordion"
                             :is-expanded="accordion.isExpanded"
                             :color="variant"
                             is-primary-accordion
                             :icon="accordion.icon"
                             :title="accordion.title"
                             :counter="accordion.children.length"
                             :htag="primaryAccordionHtag"
                             :animation-enabled="animationEnabled === 'true'"
                             @expanded="checkIsExpandedAll"
                             @click="ga4click"
                  >
                    <template v-for="(child) in accordion.children">
                      <div v-if="accordion && accordion.children && accordion.children.length" :key="child['@id']" :class="selectedColorVariant.accordionBackground" class="d-flex flex-column">
                        <SingleAccordion :ref="(el) => child.element = el"
                                         :htag="secondaryAccordionHtag"
                                         :color="variant"
                                         border
                                         nested
                                         :accordion="child"
                                         :animation-enabled="animationEnabled === 'true'"
                                         @expanded="checkIsExpandedAll"
                                         @click="ga4click"
                        />
                      </div>
                    </template>
                  </Accordion>
                  <SingleAccordion v-else-if="accordion"
                                   :ref="(el) => accordion.element = el"
                                   is-primary-accordion
                                   :htag="primaryAccordionHtag"
                                   :color="variant"
                                   :accordion="accordion"
                                   :animation-enabled="animationEnabled === 'true'"
                                   :is-expanded="accordion.isExpanded"
                                   @expanded="checkIsExpandedAll"
                                   @click="ga4click"
                  />
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {inject, ref, useContext, useFetch, onServerPrefetch, computed} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '../constants/variants/accordionGroupVariants'
import {COLORS} from '~/constants/variants/accordionGroupVariants'
import {ALIGNMENT_CLASSES} from '~/constants/variants/sectionTextVariants'
import {BUTTON_NAMES} from '~/constants/buttons'
import useMounted from '~/helpers/composables/useMounted'
import useSchema from '~/helpers/composables/useSchema'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    accordions: {
      type: Object
    },
    title: {
      type: String
    },
    htag: {
      type: String
    },
    evolutionTitle: {
      type: String
    },
    isExpanded: {
      type: String
    },
    expandLabel: {
      type: String,
      default: 'Expand all'
    },
    collapseLabel: {
      type: String,
      default: 'Collapse all'
    },
    primaryAccordionHtag: {
      type: String,
      default: 'h3'
    },
    secondaryAccordionHtag: {
      type: String,
      default: 'h4'
    },
    variant: {
      type: String,
      default: 'evolutionWhite'
    },
    animationEnabled: {
      type: String,
      default: 'false'
    },
    cta: {
      type: Object,
      default: () => {
        return {}
      }
    },
    sectionAlignment: {
      type: String,
      default: 'fullWidth'
    },
    generateSeoSchema: {
      type: String,
      default: 'false'
    }
  },
  setup(props, context) {
    const {accordions, isExpanded, cta, generateSeoSchema} = props
    const {$utils, $regions, $ga4, $services, $config} = useContext()
    const group = ref([])
    const isExpandedAll = ref(isExpanded === 'true')
    const selectedColorVariant = ref(VARIANTS_CLASSES[props.variant])
    const selectedAlignmentVariant = ref(ALIGNMENT_CLASSES[props.sectionAlignment])
    const labels = ref({})
    useMounted(props.nodeId)
    const model = inject('model')

    const {fetch: fetchLabels} = useFetch(async () => {
      if (!Object.keys(labels.value || {}).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      }
    })

    onServerPrefetch(async () => {
      await fetchLabels()
    })

    const accordionsSource = resolveLegacyListSource(
      accordions, context.attrs, 'accordions', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )

    const ctas = computed(() => {
      if (!cta?.link) { return [] }
      const linkField = $utils.getLinkField(cta.link, $regions.current.language.name)

      return [{
        variant: cta?.field,
        color: cta?.color,
        link: linkField?.url || '',
        title: linkField?.label || '',
        target: linkField?.target || '_self',
        config: {
          class: cta?.variant || 'bg-primary text-white'
        }
      }]
    })

    const initAccordions = (acc) => {
      return acc
        ? acc['@nodes'].map((x) => {
          const sItem = acc[x]
          const details = sItem?.details || sItem
          const assets = details?.assets && details?.assets?.['@nodes']
            ? details?.assets?.['@nodes'].filter(y =>
              (details?.assets?.[y]?.asset?.field === 'img' && details?.assets?.[y]?.asset?.img?.src) ||
              (details?.assets?.[y]?.asset?.field === 'video' && details?.assets?.[y]?.asset?.video?.src) ||
              (details?.assets?.[y]?.asset?.field === 'spinset' && details?.assets?.[y]?.asset?.tag) ||
              null
            ).map(x =>
              (details?.assets?.[x]?.asset?.field === 'img' && details?.assets?.[x]?.asset?.img) ||
              (details?.assets?.[x]?.asset?.field === 'video' && details?.assets?.[x]?.asset?.video) ||
              (details?.assets?.[x]?.asset?.field === 'spinset' && {field: 'spin', tag: details?.assets?.[x]?.asset?.tag}) ||
              null
            ).filter(z => z) || []
            : []

          const youtubeVideos = details?.assets && details?.assets?.['@nodes']
            ? details?.assets?.['@nodes'].filter(y => details?.assets?.[y]?.asset?.field === 'youtube' && details?.assets?.[y]?.asset?.youtube).map((youtube) => {
              const id = details?.assets?.[youtube]?.asset?.youtube?.split('watch?v=').pop()
              if (!id) { return null }
              return 'https://www.youtube.com/embed/' + id // template literals are not working here
            })
            : []

          const accordion = {
            '@id': sItem['@id'],
            assets,
            brochure: details?.brochure?.field === 'none' ? null : details?.brochure,
            color: sItem?.colors?.field || COLORS.BLACK,
            isExpanded: isExpanded === 'true' || details?.isExpanded === 'true',
            link: details?.link,
            icon: sItem?.icon,
            title: sItem?.title,
            description: details?.description,
            chips: details?.chips?.['@nodes'].map(name => details?.chips[name]),
            youtubeVideos,
            children: initAccordions(details?.nested)
          }

          return accordion
        })
        : []
    }

    group.value = initAccordions(accordionsSource)

    const expandAll = () => {
      group.value.forEach((accordion) => {
        if (accordion.children.length) {
          if (accordion.element) { accordion.element?.setVisible(!isExpandedAll.value) }
          accordion.children.forEach((child) => {
            if (child.element) { child.element.toggleCallback(!isExpandedAll.value) }
          })
        } else if (accordion.element) { accordion.element.toggleCallback(!isExpandedAll.value) }
      })
    }

    const checkIsExpandedAll = () => {
      let expandedAll = true
      let collapsedAll = true
      group.value.forEach((accordion) => {
        if (!expandedAll && !collapsedAll) { return } // cancel check if both false
        expandedAll = expandedAll && accordion.element?.isVisible
        collapsedAll = collapsedAll && !accordion.element?.isVisible
        if (accordion.children?.length && accordion.element?.isVisible) {
          accordion?.children.forEach((child) => {
            expandedAll = expandedAll && child.element?.isVisible
            collapsedAll = collapsedAll && !child.element?.isVisible
          })
        }
      })
      if (expandedAll) { isExpandedAll.value = true }
      if (collapsedAll) { isExpandedAll.value = false }
    }

    const ga4click = (event) => {
      $ga4.events.accordion_group_click(event, model, props.evolutionTitle)
    }

    const toggleAccordion = (isExpanded, id) => {
      group.value.forEach(async (accordion) => {
        if (accordion['@id'] === id) {
          if (accordion.children.length) {
            if (isExpanded === false) { // close childs on closing
              await accordion.children.forEach((child) => {
                if (child.element) { child.element.toggleCallback(isExpanded) }
              })
            }
          }
          if (accordion.element) { accordion.element.setVisible(isExpanded) }
        }
      })
    }

    const {faqPage: faqSchema} = generateSeoSchema === 'true' ? useSchema({faq: group.value}, $utils, $regions, $config) : {}

    return {
      group,
      isExpandedAll,
      toggleAccordion,
      selectedColorVariant,
      selectedAlignmentVariant,
      checkIsExpandedAll,
      expandAll,
      ga4click,
      ctas,
      BUTTON_NAMES,
      faqSchema,
      labels
    }
  },
  jsonld() {
    const id = `${this.$config.PUBLIC_HOST}${this.$router.currentRoute.path}`
    if (typeof this.faqSchema !== 'function') {
      return null
    }

    return this.faqSchema?.(id)
  }
}
</script>

<style lang="scss">
.evolution-accordion-group {
  .accordion-group-text {
    h1, h2, h3, h4, h5, h6, p {
      margin-bottom: 0;
      line-height: 1.1;
    }
  }

  .constrained .accordion-group-text {
    max-width: 41.875rem;
  }

  .constrained.justify-content-center .accordion-group-text {
    margin-left: auto;
    margin-right: auto;
    justify-content: center;
  }
}
</style>
