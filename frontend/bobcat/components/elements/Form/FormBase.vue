<template>
  <div v-if="!formSent">
    <div v-if="stickyProgressBar === 'true' && fieldsAmount > 0 && (!ctaExists ? true : ctaDisabled)"
         data-test-id="form-sticky-progress-bar"
    >
      <StickyProgressBar :progress-label="progressLabel"
                         :fields-amount="fieldsAmount"
                         :filled-fields-amount="filledFieldsAmount"
      />
    </div>
    <form ref="formEl" class="container ngb-form pb-4 pt-2">
      <div class="row justify-content-center">
        <div class="form-column col-12 col-sm-10 d-flex justify-content-center pt-3">
          <div :class="grayBackground === 'true' ? 'bg-gray-lighter' : 'bg-white'"
               class="form-thin-wrapper p-sm-6"
          >
            <component :is="htag" v-if="title" data-test-id="form-title" :class="htag">{{ title }}</component>
            <h4 v-if="subtitle" data-test-id="form-subtitle">{{ subtitle }}</h4>
            <div class="mb-4" v-html="description" />
            <b-collapse v-model="ctaDisabled" class="mt-2">
              <Fields v-if="ctaDisabled"
                      :picked-country="pickedCountry"
                      :init-related-fields="initRelatedFields"
                      :remove-related-fields="removeRelatedFields"
                      :handle-form-change="handleFormChange"
                      :init-dynamic-forms="initDynamicForms"
                      :fields="fields"
              />
            </b-collapse>
            <b-button
              v-if="cta === 'true' && ctaLabel && !ctaDisabled"
              aria-expanded="false"
              data-test-id="cta-button"
              variant="primary"
              size="lg"
              type="button"
              class="btn btn-wide submit-button"
              @click.prevent="ctaDisabled = !ctaDisabled"
            >
              {{ ctaLabel }}
            </b-button>
            <b-button
              v-if="cta === 'true' && cancelLabel && ctaDisabled"
              data-test-id="cancel-button"
              variant="primary"
              size="lg"
              type="submit"
              class="btn-outline-primary btn-secondary w-100"
              @click.prevent="ctaDisabled = !ctaDisabled"
            >
              {{ cancelLabel }}
            </b-button>
            <b-button
              v-if="submitLabel && ctaDisabled"
              data-test-id="submit-button"
              variant="primary"
              size="lg"
              type="submit"
              :disabled="!isFormValid || isFetching"
              :class="{'mt-2': cancelLabel && cta === 'true'}"
              class="btn btn-wide submit-button w-100"
              :title="generateConceptHint('submit')"
              @click.prevent="onFormSubmit"
            >
              <span class="d-flex align-items-center justify-content-center">
                <b-spinner v-if="isFetching" class="mr-2" small />
                {{ submitLabel }}
              </span>
            </b-button>
            <WarningAlert v-if="errorMessage" class="mt-4">
              {{ errorMessage }}.
              <button class="bg-transparent border-0 text-danger p-0"
                      :title="generateConceptHint('refreshForm')"
                      @click.prevent="resetForm"
              >
                {{ labels.refreshForm || 'Refresh the form.' }}
              </button>
            </WarningAlert>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
import useConcepts from '~/helpers/composables/useConcepts'

const {
  title,
  htag,
  subtitle,
  submitLabel,
  ctaLabel,
  description,
  cancelLabel,
  cta,
  fields,
  labels,
  fieldsAmount,
  stickyProgressBar,
  filledFieldsAmount,
  progressLabel,
  handleFormChange,
  isFormValid,
  isFetching,
  handleSubmit,
  errorMessage,
  resetForm,
  formSent,
  initRelatedFields,
  removeRelatedFields,
  pickedCountry,
  initDynamicForms,
  formType,
  formName,
  grayBackground
} = config

export default {
  props: {
    title,
    htag,
    subtitle,
    submitLabel,
    ctaLabel,
    description,
    cancelLabel,
    cta,
    fields,
    labels,
    fieldsAmount,
    stickyProgressBar,
    filledFieldsAmount,
    progressLabel,
    handleFormChange,
    isFormValid,
    isFetching,
    handleSubmit,
    errorMessage,
    resetForm,
    formSent,
    initRelatedFields,
    removeRelatedFields,
    pickedCountry,
    initDynamicForms,
    formType,
    formName,
    grayBackground
  },
  setup(props) {
    const {cta, formType, formName, handleSubmit} = props
    const {$ga4} = useContext()
    const ctaExists = ref(Boolean(cta === 'true'))
    const ctaDisabled = ref(!ctaExists.value)
    const formEl = ref()
    const {generateConceptHint} = useConcepts()

    const ga4click = (event) => {
      if (formType) {
        $ga4.events.form_submit(event, formType, formName)
      }
    }

    const getFormSnapshot = () => {
      if (formEl.value) {
        return new FormData(formEl.value)
      }
    }

    const onFormSubmit = (e) => {
      ga4click(e)
      handleSubmit(e, getFormSnapshot())
    }

    return {
      ctaDisabled,
      ctaExists,
      formEl,
      generateConceptHint,
      onFormSubmit
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.form-column {
  @include media-breakpoint-down(sm) {
    padding: 0 !important;

    .form-thin-wrapper {
      padding: 15px !important; // to adjust for col-12 default padding
    }
  }

  .form-thin-wrapper {
    max-width: 47.25rem;
  }
}
</style>
