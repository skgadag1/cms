<template>
  <div v-if="componentVisible"
       id="feedback"
       class="mb-2 mb-md-1 me-md-3"
       :class="'step-'+step"
  >
    <transition name="fade">
      <div v-if="step !== state.INITIAL" class="modal-backdrop" style="z-index: -1" @click="close" />
    </transition>

    <transition name="slide-down" :duration="{ enter: 350, leave: 50 }" mode="out-in" appear>
      <template v-if="step === state.INITIAL">
        <b-button variant="primary"
               class="font-condensed box-shadow me-3 me-md-0"
               data-test-id="feedback-button"
               @click="nextStep(state.YESNO)"
        >
          <b-icon icon="paper-plane" class="me-2" />
          {{ translate('feedbackButton') }}
        </b-button>
      </template>

      <FeedbackForms v-else-if="survey" :loading="loading" @close="close">
        <template #title>{{ translate('feedbackTitle') }}</template>

        <template v-if="step === state.ERROR" #step1>
          <h3 class="text-red" data-test-id="feedback-form__error">{{ translate('defaultError', 'CommonErrors') }}</h3>
        </template>

        <template v-else-if="step !== state.FINAL" #step1>
          <div class="text-center my-3" data-test-id="feedback-form__step1">
            <h3>{{ survey.questions[0].title.English }}</h3>
            <FeedbackYesNoWidget class="mt-2"
                                 :options="survey.questions[0].options"
                                 @selected="checkStatus"
            />
          </div>
        </template>

        <template #step2>
          <div v-if="step === state.POSITIVE" class="mt-4 text-center w-100" data-test-id="feedback-form__step2">
            <div class="mb-2">
              <h3>{{ survey.questions[1].title.English }}</h3>
              <b-button v-for="q in survey.questions[1].options"
                     :key="'feedback-question_1_'+q.id"
                     size="sm"
                     class="m-1"
                     :variant="response[1] && response[1].includes(q.id) ? 'secondary' : 'light'"
                     @click="handleBoolOption(1, q.id)"
              >
                {{ q.title.English }}
              </b-button>
            </div>

            <transition name="slide-down" mode="out-in">
              <div v-if="comment.visible" class="mt-4 text-start position-relative" data-test-id="feedback-form__comment">
                <h3>{{ survey.questions[3].title.English }}</h3>
                <b-form-textarea v-model="comment.text"
                                 max-rows="4"
                                 :formatter="formatTextareaLength"
                                 :placeholder="survey.questions[3].title.English"
                                 @update:model-value="response[3] = $event"
                />
                <span class="position-absolute text-counter"
                      :class="[comment.text.length === comment.maxLength ? 'text-red' : 'text-gray']"
                >({{ comment.text.length }}/{{ comment.maxLength }})</span>
              </div>
            </transition>

            <FeedbackWidgetFooter :submit-label="translatedConfig('feedbackSubmit')" @submit="submitServey">
              <b-button variant="outline-primary"
                     class="font-condensed"
                     data-test-id="feedback-form__submit-button"
                     :class="{'invisible': comment.visible}"
                     @click="comment.visible = true"
              >
                <b-icon icon="pen" />
                {{ survey.questions[3].title.English }}
              </b-button>
            </FeedbackWidgetFooter>
          </div>

          <div v-else-if="step === state.NEGATIVE" class="mt-4 text-center w-100" data-test-id="feedback-form__step2">
            <h3>{{ survey.questions[2].title.English }}</h3>
            <b-button v-for="q in survey.questions[2].options"
                   :key="'feedback-question_2_'+q.id"
                   size="sm"
                   class="m-1"
                   :data-test-id="'feedback-form__question-option__'+q.id"
                   :variant="response[2] && response[2].includes(q.id) ? 'secondary' : 'light'"
                   @click="handleBoolOption(2, q.id)"
            >
              {{ q.title.English }}
            </b-button>

            <transition name="slide-down" mode="out-in">
              <div v-if="comment.visible" class="mt-4 text-start position-relative" data-test-id="feedback-form__comment">
                <h3>{{ survey.questions[3].title.English }}</h3>
                <b-form-textarea v-model="comment.text"
                                 max-rows="4"
                                 :formatter="formatTextareaLength"
                                 :placeholder="survey.questions[3].title.English"
                                 @update:model-value="response[3] = $event"
                />
                <span class="position-absolute text-counter"
                      :class="[comment.text.length === comment.maxLength ? 'text-red' : 'text-gray']"
                >({{ comment.text.length }}/{{ comment.maxLength }})</span>
              </div>
            </transition>

            <FeedbackWidgetFooter :submit-label="translatedConfig('feedbackSubmit')"
                                  :disabled="!response[2].length"
                                  @submit="submitServey"
            >
              <b-button variant="outline-primary"
                     class="font-condensed"
                     data-test-id="feedback-form__submit-button"
                     :class="{'invisible': comment.visible}"
                     @click="comment.visible = true"
              >
                <b-icon icon="pen" />
                {{ survey.questions[3].title.English }}
              </b-button>
            </FeedbackWidgetFooter>
          </div>
        </template>

        <template v-if="step === state.FINAL" #step3>
          <div class="w-100" data-test-id="feedback-form__step3">
            <b-row class="h2 my-4" data-test-id="feedback-form__thankyou">
              <b-col>{{ translate('feedbackThankyou') }}</b-col>
              <b-col cols="auto" offset="1"><b-icon icon="check" class="text-green display-2" /></b-col>
            </b-row>
            <div class="mt-4">
              {{ survey.questions[4].title.English }}
              <b-form-input v-model="response[4]"
                            type="email"
                            placeholder="your.email@here.com"
                            data-test-id="feedback-form__email"
              />
            </div>
            <FeedbackWidgetFooter :submit-label="translatedConfig('feedbackSubmit')" @submit="updateServey" />
          </div>
        </template>
      </FeedbackForms>
    </transition>
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import FeedbackForms from '@/components/common/Feedback/FeedbackForms'
import FeedbackWidgetFooter from '@/components/common/Feedback/FeedbackWidgetFooter'
import FeedbackYesNoWidget from '@/components/common/Feedback/FeedbackYesNoWidget'
import { mapGetters } from 'vuex'
import { bdService } from '@/services'

export const STATE = {
  INITIAL: 1,
  YESNO: 2,
  POSITIVE: 3,
  NEGATIVE: 4,
  FINAL: 5,
  ERROR: 6
}

export default {
  conceptName: ['CommonErrors'],
  setup() { return useLabels() },
  components: {
    FeedbackForms,
    FeedbackYesNoWidget,
    FeedbackWidgetFooter
  },
  data() {
    return {
      componentConfig: this.config,
      step: STATE.INITIAL,
      state: STATE,
      componentVisible: false,
      showFooter: false,
      latency: parseInt(this.config.latency) || 0,
      survey: null,
      comment: {
        visible: false,
        text: '',
        maxLength: 350
      },
      response: {},
      loading: false,
      error: null
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  created() {
    this.loadServeyData()
  },
  methods: {
    loadServeyData() {
      if (this.componentConfig && this.componentConfig.surveyId) {
        bdService.fetchFeedback(this.componentConfig.surveyId).then(r => {
          this.survey = r.data
          this.resetResponses()
          setTimeout(() => { this.componentVisible = true }, this.latency)
        }).catch(e => {
          this.surveyError(e)
        })
      }
    },
    nextStep(_step = STATE.INITIAL) {
      this.step = _step
    },
    close() {
      this.nextStep()
      this.resetResponses()
    },
    resetResponses() {
      this.response = {}
      for (const i in this.survey.questions) this.response[i] = this.survey.questions[i].type === 'TEXTBOX' ? '' : []
      this.comment.visible = false
      this.comment.text = ''
    },
    checkStatus(direction, id, value) {
      this.step = value ? this.state[direction.toUpperCase()] : this.state.YESNO
      this.response[0] = value ? id : value
      this.response[1] = []
      this.response[2] = []
    },
    handleBoolOption(question, id) {
      if (question && id) {
        const _q = this.response[question]
        if (_q.includes(id)) this.response[question] = _q.filter(x => x !== id)
        else _q.push(id)
        this.$forceUpdate()
      }
    },
    submitServey() {
      this.loading = true
      bdService.sendFeedback(this.survey.id, {
        'survey_responses': this.createPostParameters()
      }).then(r => {
        this.survey.response = r.data.data
        this.resetResponses()
        this.nextStep(STATE.FINAL)
        this.loading = false
      }).catch(e => {
        this.surveyError(e)
      })
    },
    updateServey() {
      this.loading = true
      bdService.updateFeedback(this.survey.id, this.survey.response.id, {
        'survey_responses': this.createPostParameters()
      }).catch(e => {
        this.surveyError(e)
      }).finally(() => {
        this.close()
        this.loading = false
      })
    },
    createPostParameters() {
      let params = 'status=Complete'
      for (const q in Object.keys(this.response)) {
        if (this.response[q] && this.response[q].length || typeof this.response[q] == 'number') {
          switch (this.survey.questions[q].type) {
          case 'RADIO':
            params += '&data[' + this.survey.questions[q].id + '][' + this.response[q] + ']=true'
            break
          case 'CHECKBOX':
            this.response[q].map(x => params += '&data[' + this.survey.questions[q].id + '][' + x + ']=true')
            break
          case 'TEXTBOX':
            if (this.response[q].length) params += '&data[' + this.survey.questions[q].id + '][value]=' + this.response[q]
            break
          }
        }
      }
      params += '&data[7][value]=' + window.location.pathname
      return params
    },
    surveyError(e) {
      this.nextStep(STATE.ERROR)
      this.error = e
      this.loading = false
      if (this.survey) this.resetResponses()
    },
    formatTextareaLength(value) {
      return value.substring(0, this.comment.maxLength)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/grid';

.text-counter {
  bottom: 0;
  right: .25rem;
  font-size: 75%;
  user-select: none;

  @include media-breakpoint-up('md') {
    right: 1.25rem;
  }
}
</style>
