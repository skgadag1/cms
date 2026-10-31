<template>
  <LazyHydrate when-visible>
    <section data-test-id="accordion-group" class="container py-4">
      <div class="row">
        <div class="col-12">
          <div class="w-100">
            <div v-if="title" class="border-bottom mb-5 mt-3 w-100">
              <component :is="htag" :class="htag" data-test-id="_sItems-title">{{ title }}</component>
            </div>
          </div>
          <div class="col-12 col-lg-10 px-0 mx-auto d-flex flex-column align-items-center justify-content-center w-100">
            <div class="d-flex justify-content-end w-100">
              <button
                role="button"
                class="d-flex justify-content-center align-items-center mb-3 px-3 bg-transparent border-0 font-weight-bold text-primary"
                data-test-id="expand-button"
                title="Toogle accordion"
                :aria-expanded="isExpandedAll"
                @click.prevent="(e) => {ga4click(e); expandAll(e)}"
              >
                {{ isExpandedAll ? (labels.collapseAll || collapseLabel) : (labels.expandAll || expandLabel) }}  <span class="d-flex align-items-center justify-conent-center m-0 ml-2 transition-base" style="font-size: 6px" :style="isExpandedAll ? 'transform: rotate(180deg)' : null"><b-icon icon="sort-down" /></span>
              </button>
            </div>
            <template v-if="group && group.length">
              <div v-for="(accordion) in group" :key="accordion['@id']" class="p-0 bg-white transition-base mb-3 w-100" data-test-id="accordion-standalone">
                <Accordion v-if="accordion && accordion.children && accordion.children.length"
                           :id="accordion['@id']"
                           :ref="(el) => accordion.element = el"
                           :toggle-cb="toggleAccordion"
                           :is-expanded="accordion.isExpanded"
                           :color="accordion.color"
                           is-primary-accordion
                           :title="accordion.title"
                           :counter="accordion.children.length"
                           shadow
                           :htag="primaryAccordionHtag"
                           @expanded="checkIsExpandedAll"
                           @click="ga4click"
                >
                  <template v-for="(child) in accordion.children">
                    <div v-if="accordion && accordion.children && accordion.children.length" :key="child['@id']" :class="getVariant(child.color)" class="d-flex flex-column">
                      <SingleAccordion :ref="(el) => child.element = el"
                                       :htag="secondaryAccordionHtag"
                                       :color="child.color"
                                       nested
                                       :accordion="child"
                                       @expanded="checkIsExpandedAll"
                                       @click="ga4click"
                      />
                    </div>
                  </template>
                </Accordion>
                <SingleAccordion v-else-if="accordion"
                                 :ref="(el) => accordion.element = el"
                                 is-primary-accordion
                                 :color="accordion.color"
                                 shadow
                                 :htag="primaryAccordionHtag"
                                 :accordion="accordion"
                                 :is-expanded="accordion.isExpanded"
                                 @expanded="checkIsExpandedAll"
                                 @click="ga4click"
                />
              </div>
            </template>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {ref, useContext, useFetch, onServerPrefetch} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '../constants/variants/accordionGroupVariants'
import {COLORS} from '~/constants/variants/accordionGroupVariants'
import useMounted from '~/helpers/composables/useMounted'
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
    htag: {
      type: String,
      default: 'h2'
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
  setup(props, context) {
    const {accordions, isExpanded} = props
    const {$utils, $regions, $ga4, $services, $config} = useContext()
    const group = ref([])
    const isExpandedAll = ref(isExpanded === 'true')
    const labels = ref({})
    useMounted(props.nodeId)

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
            link: $utils.getLinkField(details?.link, $regions.current.language.name),
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
      $ga4.events.product_features_click(event, {})
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

    const getVariant = (color) => {
      return VARIANTS_CLASSES[color] || VARIANTS_CLASSES[COLORS.WHITE].accordionBackground
    }

    return {
      group,
      isExpandedAll,
      toggleAccordion,
      getVariant,
      checkIsExpandedAll,
      expandAll,
      ga4click,
      labels
    }
  }
}
</script>
