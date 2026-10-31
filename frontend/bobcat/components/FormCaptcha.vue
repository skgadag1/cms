<template>
  <section v-if="scriptsLoaded && captcha && captcha.apiLinkTemplate" data-test-id="formCaptcha">
    <BModal title="Action Required"
            :visible="showCaptchaModal"
            :hide-footer="true"
            :static="true"
            @close="handleModalClose"
    >
      <h4 class="container pt-4 pb-2">{{ labels.captchaVerificationFailedError }}</h4>
      <div id="humanCaptcha" class="justify-content-center" />
    </BModal>
  </section>
</template>

<script>
/* eslint-disable no-console */
import {ref, onMounted} from '@nuxtjs/composition-api'
import {BModal} from 'bootstrap-vue/src/components/modal'

export default {
  components: {BModal},
  props: {
    siteKeyV3: {
      type: String
    },
    siteKeyV2: {
      type: String
    },
    language: {
      type: String
    },
    humanActionCallback: {
      type: Function
    },
    labels: {
      type: Object,
      default: () => { return {} }
    }
  },
  setup(props) {
    const {siteKeyV3, siteKeyV2, language} = props
    const showCaptchaModal = ref(false)
    const scriptsLoaded = ref(false)

    const captcha = {
      apiLinkTemplate: `https://www.google.com/recaptcha/api.js?hl=${language}&render=${siteKeyV3}`,
      getRecaptchaToken: async () => await window.grecaptcha.execute(siteKeyV3, {action: 'submit'}).then(token => ({action: 'submit', token})),
      renderHumanCaptcha(onActionCallback) {
        try {
          window.grecaptcha.render('humanCaptcha', {
            sitekey: siteKeyV2,
            callback: (token) => {
              showCaptchaModal.value = false
              onActionCallback(token)
            }
          })
        } catch (e) {
          console.debug('Captcha already rendered')
        }
      }
    }

    onMounted(() => {
      if (process.client && captcha?.apiLinkTemplate) {
        const scriptElement = document.createElement('script')
        scriptElement.setAttribute('src', captcha.apiLinkTemplate)
        scriptElement.setAttribute('defer', '')
        scriptElement.setAttribute('async', '')
        document.body.appendChild(scriptElement)
        setTimeout(() => { scriptsLoaded.value = true }, 200)
      }
    })

    return {
      captcha,
      showCaptchaModal,
      renderHumanCaptcha(callback) {
        captcha.renderHumanCaptcha(callback)
        showCaptchaModal.value = true
      },
      async getRecaptchaToken() {
        return await this.captcha.getRecaptchaToken()
      },
      handleModalClose() {
        showCaptchaModal.value = false
      },
      scriptsLoaded
    }
  }
}
</script>

<style lang="scss">
.grecaptcha-badge { visibility: hidden; }
</style>
