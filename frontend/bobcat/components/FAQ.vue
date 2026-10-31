<template>
  <section id="faq" data-test-id="faq">
    <div class="bg-gray-lighter">
      <div class="container">
        <div class="py-4">
          <LazyHydrate never>
            <b-row v-if="title">
              <b-col cols="12">
                <component :is="htag" :class="htag">{{ title }}</component>
              </b-col>
            </b-row>
          </LazyHydrate>

          <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
            <b-row align-v="center">
              <b-col cols="12" md lg="6">
                <b-input-group class="form-control-wrapper border border-gray rounded-2 transition-base">
                  <template #prepend>
                    <b-input-group-text class="bg-white border-0">
                      <b-icon icon="search" />
                    </b-input-group-text>
                  </template>
                  <b-form-input v-model="keyword"
                                :placeholder="placeholder"
                                :disabled="inEditorEdit"
                                class="border-0 pl-0"
                                data-test-id="faq-keyword-input"
                                @input="filterFaq"
                  />
                  <template v-if="keyword.length" #append>
                    <b-input-group-text class="bg-white border-0 cursor-pointer text-dark"
                                        data-test-id="faq-keyword-clear"
                                        @click.prevent="clearKeyword"
                    >
                      <b-icon icon="times" class="small text-black" />
                    </b-input-group-text>
                  </template>
                </b-input-group>
              </b-col>
            </b-row>
          </LazyHydrate>

          <LazyHydrate never>
            <b-row v-if="hint" class="mt-2">
              <b-col cols="12 small">
                {{ hint }}
              </b-col>
            </b-row>
          </LazyHydrate>
        </div>
      </div>
    </div>

    <div class="container py-6">
      <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
        <div v-if="sections.length" data-test-id="faq-sections">
          <template v-for="section in sections">
            <transition :key="`faq-section-${section['@id']}`" name="fade">
              <b-row v-show="!excludedSections.includes(section['@id'])"
                     :class="{'faq-section__hidden' : excludedSections.includes(section['@id']) || !section.questions.length}"
                     class="faq-section"
              >
                <b-col cols="12">
                  <h2>{{ section.title }}</h2>
                  <transition-group name="fade">
                    <Accordion
                      v-for="q in section.questions"
                      v-show="!excludedQuestions.includes(q['@id'])"
                      :key="`faq-question-${section['@id']}-${q['@id']}`"
                      :title="q.question"
                      :data-test-id="`faq-section-${section['@id']}-${q['@id']}`"
                      is-primary-accordion
                      shadow
                      :htag="accordionHtag"
                      class="clearfix mb-3"
                      @click="$ga4.events.search_result_selected($event, keyword, q.question, 'faq')"
                    >
                      <div class="col-12 mt-2" v-html="q.answer" />
                    </Accordion>
                  </transition-group>
                </b-col>
              </b-row>
            </transition>
          </template>

          <b-row v-show="noResults" data-test-id="faq-no-results">
            <b-col cols="12">
              <h3>{{ noResultsLabel }} &#171; {{ keyword }} &#187;</h3>
            </b-col>
          </b-row>
        </div>
        <WarningAlert v-else-if="inEditorEdit" data-test-id="faq-no-sections">No sections defined!</WarningAlert>
      </LazyHydrate>
    </div>
  </section>
</template>

<script>
import {ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useConcepts from '~/helpers/composables/useConcepts'
import useSchema from '~/helpers/composables/useSchema'
import useMounted from '~/helpers/composables/useMounted'

const STRIP_HTML_REGEX = /<\/?[^>]+(>|$)/g

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    accordionHtag: {
      type: String,
      default: 'h3'
    },
    placeholder: {
      type: String
    },
    hint: {
      type: String
    },
    noResultsLabel: {
      type: String,
      default: 'No results for'
    },
    faqSections: {
      type: Object
    }
  },
  setup(props) {
    const {$services, $utils, $regions, $ga4, $config} = useContext()
    const labels = {}
    const {generateConceptHint} = useConcepts()
    const {faqSections} = props
    const sections = faqSections && faqSections['@nodes']
      ? faqSections['@nodes'].map((x) => {
        if (!faqSections[x].faqSection) {
          return null
        }
        return {
          ...faqSections[x].faqSection,
          questions: faqSections[x].faqSection['@nodes']
            ? faqSections[x].faqSection['@nodes'].filter(name => faqSections[x]?.faqSection[name]?.['@nodeType'] === 'dxp:faq').map((name) => {
              const currentQuestion = faqSections[x].faqSection[name]
              return {
                ...currentQuestion,
                text: `${currentQuestion.question?.replace(STRIP_HTML_REGEX, '')?.toLowerCase()} ${currentQuestion.answer?.replace(STRIP_HTML_REGEX, '')?.toLowerCase()}`
              }
            })
            : []
        }
      }).filter(y => y)
      : []
    const {faqPage: faqSchema} = useSchema(sections, $utils, $regions, $config)
    const excludedSections = ref([])
    const excludedQuestions = ref([])
    const noResults = ref(sections.length === excludedSections.value.length)
    useMounted(props.nodeId)

    watch(excludedSections, () => {
      noResults.value = sections.length === excludedSections.value.length
    })

    useFetch(async () => {
      if (!Object.keys(labels).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      }
    })

    const keyword = ref('')
    const clearKeyword = () => {
      keyword.value = ''
      filterFaq()
    }

    const filterFaq = (_str = keyword.value) => {
      const str = _str.toLowerCase()
      excludedSections.value = []
      excludedQuestions.value = []

      let totalIncludedQuestions = 0

      if (str.length > 2) {
        // filter out sections/questions to hide
        sections.forEach((s) => {
          let sectionShow = false

          s.questions.forEach((q) => {
            if (q.text.includes(str)) {
              sectionShow = true
              totalIncludedQuestions++
            } else {
              excludedQuestions.value.push(q['@id'])
            }
          })

          if (!sectionShow) {
            excludedSections.value.push(s['@id'])
          }
        })
      }
      $ga4?.events?.view_search_results(event, str, totalIncludedQuestions, 'faq', 'faq')
      $ga4?.events?.search(event, str, 'faq', 'faq')
    }

    return {
      labels,
      keyword,
      filterFaq,
      clearKeyword,
      sections,
      noResults,
      inEditorEdit,
      excludedSections,
      excludedQuestions,
      generateConceptHint,
      faqSchema
    }
  },
  jsonld() {
    const id = `${this.$config.PUBLIC_HOST}${this.$router.currentRoute.path}#faq`
    return this.faqSchema(id)
  }
}
</script>

<style lang="scss" scoped>
.faq-section:not(.faq-section__hidden) + .faq-section {
  margin-top: 2rem;
}

// Limit width of long text blocks inside accordion answers per Figma (623px = 38.9375rem)
.faq-section :deep(.accordion) {
  .col-12 {
    max-width: 38.9375rem;
  }
}
</style>
