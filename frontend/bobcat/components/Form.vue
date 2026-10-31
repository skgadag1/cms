<template>
  <section v-if="config"
           :id="configuration?.anchorId || 'model-specific-lead-form'"
           ref="form"
           data-test-id="form"
           :class="grayBackground === 'true' ? 'bg-white' : 'bg-gray-lighter'"
  >
    <aside
      v-if="(!thankYouPage || (thankYouPage.field !== 'message' && thankYouPage.field !== 'page')) && fireSuccessMsg"
      class="px-4 pt-4"
    >
      <WarningAlert type="success" :title="generateConceptHint('formSentSuccess')">
        {{ labels.formSentSuccess || 'Form sent successfully.' }}
      </WarningAlert>
    </aside>
    <LazyHydrate never :trigger-hydration="onReadyForInput">
      <FormBase
        :title="title"
        :htag="htag"
        :subtitle="subtitle"
        :submit-label="submitLabel"
        :cta-label="ctaLabel"
        :description="description"
        :gray-background="grayBackground"
        :cancel-label="cancelLabel"
        :init-related-fields="initRelatedFields"
        :remove-related-fields="removeRelatedFields"
        :picked-country="pickedCountry"
        :init-dynamic-forms="initDynamicForms"
        :cta="cta"
        :fields="fields"
        :labels="labels"
        :fields-amount="fieldsAmount"
        :sticky-progress-bar="stickyProgressBar"
        :filled-fields-amount="filledFieldsAmount"
        :progress-label="progressLabel"
        :handle-form-change="handleFormChange"
        :is-form-valid="isFormValid"
        :is-fetching="isFetching"
        :handle-submit="handleSubmit"
        :error-message="errorMessage"
        :reset-form="resetForm"
        :form-sent="formSent"
        :form-type="formType"
        :form-name="formName"
      />
    </LazyHydrate>
    <ThankYouMessage
      v-if="formSent && isThankYouMessage"
      :success-state-title="successStateTitle"
      :success-state-subtitle="successStateSubtitle"
      :submit-another="submitAnother"
      :reset-form="resetForm"
    />
    <BModal title="Form Sending Error"
            header-bg-variant="danger"
            header-text-variant="light"
            :visible="formSentError"
            :ok-only="true"
            :static="true"
            lazy
    >
      <h4 class="container pt-4 pb-2">{{ labels.formSendingError }}</h4>
    </BModal>
    <FormCaptcha v-if="captcha.isEnabled"
                 ref="captchaComponent"
                 :labels="labels"
                 :site-key-v3="captcha.sitekeyv3"
                 :site-key-v2="captcha.sitekeyv2"
                 :language="captcha.language"
    />
  </section>
</template>

<script>
/* eslint-disable no-console */
import {onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import {BModal} from 'bootstrap-vue/src/components/modal'
import FormCaptcha from './FormCaptcha'
import config from '~/constants/props/form.config'
import useConcepts from '~/helpers/composables/useConcepts'
import useForms from '~/helpers/composables/useForms'
import useCaptcha from '~/helpers/composables/useCaptcha'
import {SUPPORTED_FORM_SENDER_TYPES} from '~/constants/formSenders'

const {
  configuration,
  title,
  htag,
  subtitle,
  stickyProgressBar,
  thankYouPage,
  progressLabel,
  submitLabel,
  ctaLabel,
  description,
  cancelLabel,
  cta,
  grayBackground,
  formType,
  formSelectionsForGA4
} = config

export default {
  components: {BModal, FormCaptcha},
  props: {
    title,
    htag,
    subtitle,
    stickyProgressBar,
    thankYouPage,
    progressLabel,
    submitLabel,
    ctaLabel,
    description,
    cancelLabel,
    cta,
    grayBackground,
    configuration,
    formType,
    formSelectionsForGA4
  },
  setup(props) {
    const captchaComponent = ref(null)
    const {configuration, thankYouPage, formType, formSelectionsForGA4} = props
    const formSender = ref(null)
    const config = ref(null)
    const fields = ref([])
    const formFields = ref({})
    const {generateConceptHint} = useConcepts()
    const {submitWithCaptcha, captcha, initCaptchaField} = useCaptcha()
    const {initFormFields, spreadConfig, handleFormChanges, handleMapFormValues} = useForms()
    const isFormValid = ref(false)
    const formSent = ref(false)
    const form = ref(null)
    const labels = ref({})
    const filledFieldsAmount = ref(0)
    const isFetching = ref(false)
    const fieldsAmount = ref(0)
    const securityToken = ref(null)
    const {$services, $regions} = useContext()
    const isThankYouMessage = ref(thankYouPage?.field === 'message')
    const successStateTitle = ref(isThankYouMessage.value ? thankYouPage?.successStateTitle : '')
    const successStateSubtitle = ref(isThankYouMessage.value ? thankYouPage?.successStateSubtitle : '')
    const submitAnother = ref(isThankYouMessage.value ? thankYouPage?.submitAnother : '')
    const formSentError = ref(false)
    const errorMessage = ref(null)
    const pickedCountry = ref(null)
    const fireSuccessMsg = ref(false)
    const savedDynamicFormFields = ref({})
    const formName = ref('')
    const onReadyForInput = ref(false)

    const initAmounts = () => {
      filledFieldsAmount.value = Object.keys(formFields.value)
        .filter(el => formFields.value[el].isFilled && !formFields.value[el].isHidden)?.length
      fieldsAmount.value = Object.keys(formFields.value).filter(el => !formFields.value[el].isHidden).map(el => el)?.length
    }

    const {$fetch} = useFetch(async () => {
      config.value = {...await $services.bobcatapi.getFormConfig(configuration['@id']), formType, formSelectionsForGA4}
      formSender.value = config.value?.['form-sender-selector']
      fields.value = spreadConfig(config.value)
      formName.value = config.value?.['@name']

      const formData = initFormFields(fields.value)
      formFields.value = formData.formFields
      isFormValid.value = formData.isFormValid

      initCaptchaField(fields)
      initAmounts()

      if (typeof window !== 'undefined') {
        onReadyForInput.value = true
      }
    })

    onMounted(async () => {
      try {
        const [{data}, labelsData] = await Promise.all([
          $services.content.getToken(),
          $services.bobcatapi.getConcepts(),
          $fetch()
        ])
        securityToken.value = data?.securityToken
        labels.value = labelsData
      } catch (error) {
        console.debug('[forms] Error on fetching data', error)
      }
    })

    const setPickedCountry = (val) => {
      pickedCountry.value = val
    }
    const setFilledFieldsAmount = (val) => {
      filledFieldsAmount.value = val
    }
    const setFormFields = (val) => {
      formFields.value = val
    }
    const setIsFormValid = (val) => {
      isFormValid.value = val
    }

    const handleFormChange = (value, fieldName, isValid, id) => handleFormChanges(formFields, setPickedCountry, setFilledFieldsAmount, setFormFields, setIsFormValid, value, fieldName, isValid, id)

    const handleSubmit = async (_event, formSnapshot) => {
      isFetching.value = true
      formFields.value = handleMapFormValues(formFields, formSnapshot)

      const makeRequest = captchaData => $services.content.submitForm(
        `bobcat/${$regions.current.name}`,
        formFields.value,
        config.value,
        formSender.value,
        securityToken.value,
        captchaData
      )

      if (securityToken.value) {
        if (SUPPORTED_FORM_SENDER_TYPES.includes(formSender.value?.field)) {
          const data = await submitWithCaptcha(captchaComponent, captcha, makeRequest, thankYouPage, form, formSentError, formSent, errorMessage, config.value)
          formSentError.value = data.formSentError
          formSent.value = data.formSent
          errorMessage.value = data.errorMessage
          if (thankYouPage?.field !== 'message' && thankYouPage?.field !== 'page') {
            resetForm()
            fireSuccessMsg.value = true
          }
        } else {
          console.debug('[form] The sender email or Salesforce URL is missing')
        }
      } else {
        console.debug('[form] Security token is missing or it expired')
      }
      isFetching.value = false
    }

    const initRelatedFields = (inputs) => {
      inputs.forEach((input) => {
        formFields.value[input.name] = {
          value: '',
          name: input.name,
          fieldType: input['@name'],
          id: input?.id || null,
          isValid: true,
          isFilled: false
        }
      })
      initAmounts()
    }

    const initDynamicForms = (dynamicFormFields, isValid, parentFieldName) => {
      const removeNestedFields = (fieldsToRemove) => {
        for (const fieldName of Object.keys(fieldsToRemove)) {
          delete formFields.value[fieldName]

          if (savedDynamicFormFields.value[fieldName]) {
            removeNestedFields(savedDynamicFormFields.value[fieldName])
            delete savedDynamicFormFields.value[fieldName]
          }
        }
      }

      if (savedDynamicFormFields.value[parentFieldName]) {
        const fieldsToRemove = savedDynamicFormFields.value[parentFieldName]
        removeNestedFields(fieldsToRemove)
      }
      savedDynamicFormFields.value[parentFieldName] = dynamicFormFields

      formFields.value = {
        ...formFields.value,
        ...dynamicFormFields
      }

      initAmounts()
      isFormValid.value = isValid
    }

    const removeRelatedFields = (inputs) => {
      inputs.forEach((input) => {
        delete formFields.value[input.name]
      })
      initAmounts()
    }

    const resetForm = () => {
      formSent.value = false
      formSentError.value = false
      filledFieldsAmount.value = 0
      formFields.value = {}
      errorMessage.value = null
      const data = initFormFields(fields.value)
      formFields.value = data.formFields
      isFormValid.value = data.isFormValid
    }

    return {
      fields,
      errorMessage,
      handleFormChange,
      handleSubmit,
      isFormValid,
      filledFieldsAmount,
      fieldsAmount,
      formSent,
      form,
      generateConceptHint,
      labels,
      isFetching,
      formSentError,
      resetForm,
      successStateTitle,
      successStateSubtitle,
      submitAnother,
      isThankYouMessage,
      captcha,
      captchaComponent,
      formSender,
      removeRelatedFields,
      initRelatedFields,
      pickedCountry,
      initDynamicForms,
      fireSuccessMsg,
      config,
      formName,
      onReadyForInput
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/bootstrap_forms';
@import '~bootstrap/scss/progress';

section[data-test-id="form"] {
  button[data-test-id="cta-button"] {
    @include media-breakpoint-down(xs) {
      width: 100%;
    }
  }
}

section[data-test-id='form'] {
  iframe {
    width: 100%;
  }
}
</style>
