<template>
  <section :id="anchorId" ref="form" class="position-relative d-flex flex-column bg-gray-lighter" data-test-id="model-specific-lead">
    <div class="container order-lg-0 order-1">
      <div class="row align-items-center">
        <div class="col-12 col-lg-6">
          <div class="pr-lg-4 form-container py-6">
            <div class="model-specific-lead-content">
              <div class="mb-4">
                <div v-if="subtitle && !formSent" data-test-id="model-specific-lead-subtitle" v-html="subtitle" />
                <h1 v-else-if="dynamicSubtitle && !formSent" data-test-id="model-specific-lead-subtitle" class="h4" v-html="dynamicSubtitle" />
                <div v-if="title && !formSent" data-test-id="model-specific-lead-title" v-html="title" />
                <div v-if="description && !formSent" data-test-id="model-specific-lead-description" v-html="description" />
              </div>
              <aside
                v-if="(!thankYouPage || (thankYouPage.field !== 'message' && thankYouPage.field !== 'page')) && fireSuccessMsg"
                class="px-4 pt-4"
              >
                <WarningAlert type="success" :title="generateConceptHint('formSentSuccess')">
                  {{ labels.formSentSuccess || 'Form sent successfully.' }}
                </WarningAlert>
              </aside>
              <form v-else-if="!formSent" class="ngb-form">
                <ul v-if="!formSent" class="list-unstyled step-indicator mb-4">
                  <li v-for="(step,index) in steps" :key="`step-` + (index + 1)" :class="{'active': index === currentStep, 'completed': index < currentStep}">
                    <div class="d-flex w-100 justify-content-between mb-4">
                      <div class="d-flex">
                        <span v-if="currentStep <= index" class="d-flex justify-content-center align-items-center step-badge mr-2" :class="index === currentStep ? 'badge-primary' : 'badge-primary-disabled'">{{ index + 1 }}</span>
                        <span v-else class="d-flex justify-content-center align-items-center step-badge mr-2 bg-green">
                          <b-icon icon="success" class="align-middle text-white" />
                        </span>
                        <span class="font-weight-bold text-large mb-0">{{ step.label }}</span>
                      </div>
                      <div v-if="index < currentStep">
                        <a class="text-primary cursor-pointer font-weight-bold ml-auto" @click.prevent="editStep(index)">
                          <strong>{{ editLabel }}</strong>
                        </a>
                      </div>
                    </div>
                    <div v-show="index === currentStep">
                      <Fields
                        :picked-country="pickedCountry"
                        :init-related-fields="initRelatedFields"
                        :remove-related-fields="removeRelatedFields"
                        :handle-form-change="handleFormChange"
                        :init-dynamic-forms="initDynamicForms"
                        :form-fields="formFields"
                        :fields="step.fields"
                      />
                      <b-button
                        v-if="currentStep < steps.length - 1"
                        variant="primary"
                        type="button"
                        size="lg"
                        class="btn btn-wide d-inline-flex align-items-center justify-content-center mb-4"
                        :disabled="!isValid"
                        @click.prevent="handleNext"
                      >
                        <span class="align-middle">{{ ctaLabel }}</span>
                        <b-icon icon="arrow-right" class="ml-2 align-middle" />
                      </b-button>
                      <b-button
                        v-else
                        type="submit"
                        variant="primary"
                        size="lg"
                        class="btn btn-wide d-inline-flex align-items-center justify-content-center"
                        :disabled="!isValid || isFetching"
                        @click.prevent="submit"
                      >
                        <span class="align-middle">{{ submitLabel }}</span>
                      </b-button>
                    </div>
                    <div v-if="index < currentStep">
                      <div class="mb-4">
                        <FieldsReadOnly
                          :fields="step.fields"
                          :form-fields="formFields"
                        />
                      </div>
                    </div>
                  </li>
                </ul>
                <template v-for="hiddenField in hiddenFieldsFromConfig">
                  <DynamicHiddenField
                    v-if="hiddenField['@name'] === FORM_FIELD_TYPES.DYNAMIC_HIDDEN_FIELD"
                    :id="hiddenField.id || `hidden-${hiddenField.name}`"
                    :key="hiddenField?.['@id'] || hiddenField.id || hiddenField.name"
                    :name="hiddenField.name"
                    :field="hiddenField"
                    :on-change="handleHiddenChangeWithMapping"
                  />
                </template>
              </form>
              <FormCaptcha v-if="captcha.isEnabled"
                           ref="captchaComponent"
                           :labels="labels"
                           :site-key-v3="captcha.sitekeyv3"
                           :site-key-v2="captcha.sitekeyv2"
                           :language="captcha.language"
              />
            </div>
            <WarningAlert v-if="errorMessage" data-test-id="model-specific-lead-alert">
              {{ errorMessage }}
            </WarningAlert>
            <div v-else-if="formSent && isThankYouMessage">
              <div class="d-flex mb-3">
                <b-icon icon="success-state" class="h4 m-0 text-success" />
                <h3 class="m-0 ml-2">{{ successStateTitle }}</h3>
              </div>
              <p v-if="successStateSubtitle">{{ successStateSubtitle }}</p>
              <b-button
                v-if="submitAnotherLabel"
                data-test-id="submit-another-button"
                variant="primary"
                size="lg"
                type="submit"
                class="btn btn-wide submit-button"
                @click.prevent="resetForm"
              >
                {{ submitAnotherLabel }}
              </b-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="order-0 order-lg-1 d-flex align-content-center align-items-center justify-content-center model-specific-lead-image bg-white overflow-hidden">
      <ImageSet
        v-if="productImageSrc"
        :src="productImageUrl || productImageSrc"
        :title="productImageTitle"
        :alt="productImageAlt"
        :img-width="productImageWidth"
        :img-height="productImageHeight"
      />
    </div>
  </section>
</template>

<script>
import {computed, onBeforeMount, onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'

import useCaptcha from '../helpers/composables/useCaptcha'
import useConcepts from '../helpers/composables/useConcepts'
import FormCaptcha from './FormCaptcha.vue'
import FORM_FIELD_TYPES from '~/constants/formFieldTypes'
import DynamicHiddenField from '~/components/elements/Form/DynamicHiddenField'
import ImageSet from '~/components/elements/ImageSet'
import Fields from '~/components/elements/Form/Fields.vue'
import useModels from '~/helpers/composables/useModels'
import useForms from '~/helpers/composables/useForms'
import {HIDDEN_FIELDS_NAMES} from '~/constants/hiddenFields'
import {SUPPORTED_FORM_SENDER_TYPES} from '~/constants/formSenders'

export default {
  components: {FormCaptcha, DynamicHiddenField, ImageSet, Fields},
  props: {
    title: String,
    subtitle: String,
    htag: {
      type: String,
      default: 'h1'
    },
    description: String,
    overrideImage: {
      type: Object,
      default: null
    },
    configuration: {
      type: Object,
      default: null
    },
    editLabel: {type: String, default: 'Edit'},
    thankYouPage: {type: Object, default: null},
    submitLabel: {type: String, default: 'Submit'},
    formType: {type: String, default: ''},
    formSelectionsForGA4: {type: String, default: ''},
    anchorId: {
      type: String,
      default: 'model-specific-lead-form'
    },
    ctaLabel: {
      type: String,
      default: 'Next'
    }
  },
  setup(props) {
    const {$utils, $services, $regions} = useContext()
    const {submitWithCaptcha, captcha, initCaptchaField} = useCaptcha()
    const {generateConceptHint} = useConcepts()
    const {initFormFields, spreadConfig, handleFormChanges, handleMapFormValues, extractSteps, extractHiddenFields} = useForms()
    const {model} = useModels()

    // refs from DOM Elements
    const form = ref(null)
    const captchaComponent = ref(null)

    // form state
    const formSent = ref(false)
    const formSentError = ref(false)
    const errorMessage = ref(null)
    const isFetching = ref(false)
    const fireSuccessMsg = ref(false)
    const config = ref(null)
    const hiddenFields = ref([])
    const fields = ref([])
    const formFields = ref({})
    const savedDynamicFormFields = ref({})
    const hiddenFieldsMapping = ref({})
    const pickedCountry = ref(null)
    const isFormValid = ref(false)
    const isValid = ref(false)
    const formSender = ref(null)
    const securityToken = ref(null)
    const labels = ref({})

    const steps = ref([])
    const currentStep = ref(0)

    const firstModelImage = computed(() => $utils.products.getFirstModelImage(model))
    const productImageSrc = computed(() => (props.overrideImage?.src) || firstModelImage.value?.img?.src || '')
    const productImageUrl = computed(() => productImageSrc.value ? $utils.getCloudinaryAsset(productImageSrc.value, true, {}) : '')
    const productImageAlt = computed(() => (props.overrideImage?.imageAlt) || firstModelImage.value?.img?.imageAlt || model?.title || model?.name || '')
    const productImageTitle = computed(() => (props.overrideImage?.imageTitle) || firstModelImage.value?.img?.imageTitle || model?.title || model?.name || '')
    const productImageWidth = computed(() => (props.overrideImage?.imageWidth) || firstModelImage.value?.img?.imageWidth || null)
    const productImageHeight = computed(() => (props.overrideImage?.imageHeight) || firstModelImage.value?.img?.imageHeight || null)
    const isThankYouMessage = computed(() => props.thankYouPage?.field === 'message')
    const successStateTitle = computed(() => isThankYouMessage.value ? props.thankYouPage?.successStateTitle : 'Thank you!')
    const successStateSubtitle = computed(() => isThankYouMessage.value ? props.thankYouPage?.successStateSubtitle : 'We will get back to you as soon as possible.')
    const submitAnotherLabel = computed(() => isThankYouMessage.value ? props.thankYouPage?.submitAnother : 'Submit Another')

    const dynamicSubtitle = computed(() => {
      const modelName = model?.title || model?.id || model?.['@name'] || ''
      const category = model?.parent?.parentTitle || ''
      return (category || modelName).trim()
    })

    onBeforeMount(async () => {
      if (props.configuration && props.configuration?.['@id']) {
        config.value = {...await $services.bobcatapi.getFormConfig(props.configuration['@id']), formType: props.formType, formSelectionsForGA4: props.formSelectionsForGA4}
        formSender.value = config.value?.['form-sender-selector']
        fields.value = spreadConfig(config.value)
        hiddenFields.value = extractHiddenFields(fields.value)
        steps.value = extractSteps(config.value)
        const formData = initFormFields(fields.value)
        formFields.value = formData.formFields
        isFormValid.value = formData.isFormValid
        initCaptchaField(fields.value)
      }
    })

    onMounted(async () => {
      try {
        const {data} = await $services.content.getToken()
        securityToken.value = data?.securityToken
        labels.value = await $services.bobcatapi.getConcepts()
      } catch (e) {

      }
    })

    const hiddenFieldsFromConfig = computed(() => {
      return hiddenFields.value
    })

    const editStep = (index) => {
      if (index < currentStep.value) { currentStep.value = index }
    }

    const handleNext = () => {
      if (!isValid.value) { return }
      currentStep.value = currentStep.value + 1
    }

    const handleHiddenChangeWithMapping = (value, name) => {
      const fieldConfig = hiddenFieldsFromConfig.value.find(f => f.name === name)

      hiddenFieldsMapping.value[name] = {
        name,
        value,
        mappingId: fieldConfig?.id || name
      }
    }

    watch([currentStep, steps, formFields], () => {
      const step = steps.value[currentStep.value]
      if (step?.fields) {
        isValid.value = step.fields.every((field) => {
          const fieldState = formFields.value?.[field.name]
          return !fieldState || fieldState.isValid
        })
      } else {
        isValid.value = false
      }
    }, {deep: true, immediate: true})

    const setPickedCountry = (val) => { pickedCountry.value = val }
    const setFilledFieldsAmount = () => {}
    const setFormFields = (val) => { formFields.value = val }
    const setIsFormValid = (val) => { isFormValid.value = val }
    const handleFormChange = (value, fieldName, isValid, id) => handleFormChanges(formFields, setPickedCountry, setFilledFieldsAmount, setFormFields, setIsFormValid, value, fieldName, isValid, id)

    const submit = async (_event, formSnapshot) => {
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
          const data = await submitWithCaptcha(captchaComponent, captcha, makeRequest, props.thankYouPage, form, formSentError, formSent, errorMessage, config.value)
          formSentError.value = data.formSentError
          errorMessage.value = data.errorMessage
          formSent.value = data.formSent
          if (props.thankYouPage?.field !== 'message' && props.thankYouPage?.field !== 'page') {
            resetForm()
            fireSuccessMsg.value = true
          }
        } else {
          // eslint-disable-next-line no-console
          console.debug('[form] The sender email or Salesforce URL is missing')
        }
      } else {
        errorMessage.value = 'There was an error submitting the form. Please try again later.'
        // eslint-disable-next-line no-console
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
    }

    const initDynamicForms = (dynamicFormFields, isValid) => {
      for (const [key] of Object.entries(savedDynamicFormFields.value)) {
        delete formFields.value[key]
      }
      formFields.value = {...formFields.value, ...dynamicFormFields}
      isFormValid.value = isValid
      savedDynamicFormFields.value = dynamicFormFields
    }

    const removeRelatedFields = (inputs) => {
      inputs.forEach((input) => {
        delete formFields.value[input.name]
      })
    }

    const resetForm = () => {
      currentStep.value = 0
      formSent.value = false
      formSentError.value = false
      formFields.value = {}
      errorMessage.value = null
      const data = initFormFields(fields.value)
      formFields.value = data.formFields
      isFormValid.value = data.isFormValid
    }

    return {
      errorMessage,
      fireSuccessMsg,
      labels,
      captcha,
      captchaComponent,
      isValid,
      pickedCountry,
      hiddenFieldsFromConfig,
      initDynamicForms,
      initRelatedFields,
      removeRelatedFields,
      handleFormChange,
      handleNext,
      submit,
      generateConceptHint,
      handleHiddenChangeWithMapping,
      hiddenFieldTypes: HIDDEN_FIELDS_NAMES,
      FORM_FIELD_TYPES,
      productImageSrc,
      productImageUrl,
      productImageAlt,
      productImageTitle,
      productImageWidth,
      productImageHeight,
      steps,
      currentStep,
      editStep,
      formSent,
      isFetching,
      resetForm,
      formFields,
      isThankYouMessage,
      successStateTitle,
      successStateSubtitle,
      submitAnotherLabel,
      form,
      model,
      dynamicSubtitle
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

:deep(.model-specific-lead-content) {
  h1, h2, h3, h4, h5, h6, p {
    margin-bottom: 0;
  }
}

.btn-wide {
  min-width: 10rem;
  transition: background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}

.model-specific-lead-image {
  @include media-breakpoint-up(lg) {
    height: 100%;
    top: 0;
    right: 0;
    width: 50vw;
    position: absolute;

    img {
      max-width: 720px;
      object-position: center; // center crop
    }
  }
}

.step-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.form-container {
  min-height: 30rem;
}
</style>
