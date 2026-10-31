import {reactive, useContext} from '@nuxtjs/composition-api'
import useForms from '~/helpers/composables/useForms'

export default function useCaptcha() {
  const {handlerResponse, isCaptchaFieldType} = useForms()
  const {$regions} = useContext()

  // STATES
  const captcha = reactive({
    isEnabled: false,
    sitekeyv3: undefined,
    sitekeyv2: undefined,
    language: $regions.current.language.name || process.env.NUXT_DEFAULT_LANGUAGE
  })
  // END OF STATES

  // INIT FUNCTIONS
  const initCaptchaField = (fields) => {
    fields.value?.filter(f => isCaptchaFieldType(f['@name'])).forEach((f) => {
      if (f.captchaV3SiteKey && f.captchaV2SiteKey) {
        captcha.sitekeyv3 = f.captchaV3SiteKey
        captcha.sitekeyv2 = f.captchaV2SiteKey
        captcha.isEnabled = true
      }
    })
  }
  // END OF INIT FUNCTIONS

  // SUBMISSIONS
  const submitWithCaptcha = async (captchaComponent, captcha, makeRequest, thankYouPage, form, formSentError, formSent, errorMessage, config) => {
    const captchaData = captcha.isEnabled ? await captchaComponent.value.getRecaptchaToken() : undefined
    const response = await makeRequest(captchaData)
    const errMsg = (response.data?.errors?.[0] || '').toLowerCase()

    if (captcha.isEnabled && !response.success &&
      (errMsg.includes('threshold validation too low') || errMsg.includes('security token validation failed'))) {
      const captchaCallback = async (token) => {
        formSentError.value = false
        const data = handlerResponse(await makeRequest({action: 'resubmit', token}), config, () => {
          formSentError.value = true
        }, thankYouPage, formSent, form, errorMessage)
        formSent.value = data.formSent
        errorMessage.value = data.errorMessage
      }
      captchaComponent.value.renderHumanCaptcha(captchaCallback)
    } else {
      const data = handlerResponse(response, config, null, thankYouPage, formSent, form, errorMessage)
      formSent.value = data.formSent
      errorMessage.value = data.errorMessage
    }
    return {formSentError: formSentError.value, formSent: formSent.value, errorMessage: errorMessage.value}
  }
  // END OF SUBMISSIONS

  return {
    submitWithCaptcha,
    initCaptchaField,
    captcha
  }
}
