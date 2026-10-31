<template>
  <b-modal id="registrationModal" ref="modal" :title="translate(software + 'registrationModalTitle', 'SoftwareRegistration')" size="xl" @hide="resetForm">
    <template v-slot:default>
      <b-row class="p-3">
        <b-col cols="12" lg="6">
          <h2>{{ translate('instructionsLabel', 'SoftwareRegistration') }}</h2>
          <div class="mt-3" v-html="translate( software + 'softwareRegistrationInstructionsTop', 'SoftwareRegistration')" />
          <div class="d-sm-flex d-block mb-3 mb-sm-0">
            <div class="flex-grow-1" :class="software === 'v2' ? 'pe-sm-3' : ''">
              <b-form-group
                id="originalNumberGroup"
                class="w-100"
                :label="translate('originalNumberLabel', 'SoftwareRegistration')"
                label-for="originalNumber"
              >
                <b-form-input id="originalNumber"
                         v-model.trim="form.originalNumber"
                         size="sm"
                         class="form-control bg-gray-100 text-black"
                         name="originalNumber"
                />
              </b-form-group>
            </div>
            <div v-if="software === 'v2'" class="flex-grow-0">
              <div class="d-none d-sm-block mt-2">&nbsp;</div>
              <b-button variant="primary"
                     class="font-condensed mt-1 mt-sm-0 w-100"
                     :disabled="form.originalNumber === '' || form.secretName === ''"
                     data-test-id="get-authentication-number-button"
                     @click="getAuthenticationNumber"
              >
                <b-spinner v-if="loading" small />
                {{ translate("getAuthenticationNumberButton", 'SoftwareRegistration') }}
              </b-button>
            </div>
          </div>
          <div v-if="software === 'v1'" class="d-sm-flex d-block mb-3 mb-sm-0">
            <div class="flex-grow-1 pe-sm-3">
              <b-form-group
                id="createUsernameGroup"
                class=""
                :label="translate('createUsernameLabel', 'SoftwareRegistration')"
                label-for="createUsername"
              >
                <b-form-input id="createUsername"
                         v-model.trim="form.userName"
                         size="sm"
                         class="form-control bg-gray-100 text-black"
                         name="createUsername"
                />
                <b-form-invalid-feedback id="createUsername-feedback" :state="form.userName.length <= 16"><b-icon icon="exclamation-triangle" class="me-1" />{{ translate('userNameInvalidFeedback', 'SoftwareRegistration') }}</b-form-invalid-feedback>
              </b-form-group>
            </div>
            <div class="flex-grow-0">
              <div class="d-none d-sm-block mt-2">&nbsp;</div>
              <b-button variant="primary"
                     class="font-condensed mt-1 mt-sm-0 w-100"
                     :disabled="form.originalNumber === '' || form.userName === '' || form.userName.length > 16"
                     data-test-id="get-authentication-number-button"
                     @click="getAuthenticationNumber"
              >
                <b-spinner v-if="loading" small />
                {{ translate("getAuthenticationNumberButton", 'SoftwareRegistration') }}
              </b-button>
            </div>
          </div>
          <div class="d-flex">
            <div class="flex-grow-1 pe-3">
              <b-form-group
                id="authenticationNumberGroup"
                label-class="mb-0"
                :label="translate('authenticationNumberLabel', 'SoftwareRegistration')"
                label-for="authenticationNumber"
              >
                <b-form-input id="authenticationNumber"
                         v-model.trim="authenticationNumber"
                         size="sm"
                         readonly
                         class="form-control dashed bg-white text-black"
                         name="authenticationNumber"
                />
              </b-form-group>
            </div>
            <div class="flex-grow-0">
              <div class="mb-2">&nbsp;</div>
              <b-button variant="outline-dark"
                     class="font-condensed"
                     :disabled="authenticationNumber === '' || loading"
                     data-test-id="copy-button"
                     @click="copyTextToClipboard(authenticationNumber)"
              >
                <b-icon :icon="copyClicked(authenticationNumber) ? 'check' : 'clone' " class="me-2" />
                {{ translate("copyButton", 'SoftwareRegistration') }}
              </b-button>
            </div>
          </div>
          <div v-if="software === 'v2'" class="d-flex">
            <div class="flex-grow-1 pe-3">
              <b-form-group
                id="idGroup"
                label-class="mb-0"
                :label="translate('idLabel', 'SoftwareRegistration')"
                label-for="id"
              >
                <b-form-input id="id"
                         v-model.trim="id"
                         size="sm"
                         readonly
                         class="form-control dashed bg-white text-black"
                         name="id"
                />
              </b-form-group>
            </div>
            <div class="flex-grow-0">
              <div class="mb-2">&nbsp;</div>
              <b-button variant="outline-dark"
                     class="font-condensed"
                     :disabled="id === '' || loading"
                     data-test-id="copy-button"
                     @click="copyTextToClipboard(id)"
              >
                <b-icon :icon="copyClicked(id) ? 'check' : 'clone' " class="me-2" />
                {{ translate("copyButton", 'SoftwareRegistration') }}
              </b-button>
            </div>
          </div>
          <div class="d-flex">
            <div class="flex-grow-1 pe-3">
              <b-form-group
                id="passwordGroup"
                label-class="mb-0"
                :label="translate('passwordLabel', 'SoftwareRegistration')"
                label-for="password"
              >
                <b-form-input id="password"
                         v-model.trim="password"
                         size="sm"
                         readonly
                         class="form-control dashed bg-white text-black"
                         name="password"
                />
              </b-form-group>
            </div>
            <div class="flex-grow-0">
              <div class="mb-2">&nbsp;</div>
              <b-button variant="outline-dark"
                     class="font-condensed"
                     :disabled="password === '' || loading"
                     data-test-id="copy-button"
                     @click="copyTextToClipboard(password)"
              >
                <b-icon :icon="copyClicked(password) ? 'check' : 'clone' " class="me-2" />
                {{ translate("copyButton", 'SoftwareRegistration') }}
              </b-button>
            </div>
          </div>
          <b-error v-if="isError">
            <div v-html="translate( software + 'registerSoftwareError', 'SoftwareRegistration')" />
          </b-error>
          <div v-html="translate(software + 'softwareRegistrationInstructionsBottom', 'SoftwareRegistration')" />
        </b-col>
        <b-col cols="12" lg="6" class="border-start py-3 py-lg-0">
          <h2>{{ translate('referenceImageLabel', 'SoftwareRegistration') }}</h2>
          <b-img fluid
                 :src="referenceImage"
                 :alt="translate('registrationReferenceImageAlt', 'SoftwareRegistration')"
                 @error="replaceWithUndefinedImage"
          />
        </b-col>
      </b-row>

      <div class="p-3" v-html="footer" />
    </template>

    <template #footer>
      <div class="d-flex w-100">
        <div class="mb-3 d-flex flex-column align-items-end w-100">
          <b-button variant="primary"
                 size="lg"
                 class="font-condensed text-nowrap"
                 @click="closeModal"
          >
            {{ translate('closeButton', 'SoftwareRegistration') }}
          </b-button>
        </div>
      </div>
    </template>
  </b-modal>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import { useCopy } from '@/composables/useCopy'
import { bdService } from '@/services'
import BError from '../common/BError.vue'
import parseJwt from '../../helpers/jwtToken'

export default {
  name: 'RegistrationModal',
  components: {BError},
  conceptName: ['SoftwareRegistration'],
  setup() { return {...useLabels(), ...useCopy()} },
  props: ['footer', 'software'],
  data() {
    return {
      form: {
        originalNumber: '',
        userName: ''
      },
      authenticationNumber: '',
      id: '',
      password: '',
      isError: false,
      loading: false
    }
  },
  computed: {
    referenceImage() {
      let imageConcept = this.labels['SoftwareRegistration'][this.software + 'registrationReferenceImage'] || ''
      const link = /(https?:\/\/[^" ]*)/i
      if (imageConcept) {
        const imageSrc = imageConcept.match(link)
        if (Array.isArray(imageSrc)) return imageSrc[0]
      }

      return this.assetServer + '/assets/img/examples/undefined.png'
    }
  },
  methods: {
    resetForm() {
      this.isError = false
      this.form = {
        originalNumber: '',
        userName: ''
      }
      this.authenticationNumber = ''
    },
    getAuthenticationNumber() {
      this.loading = true
      this.isError = false
      const authenticationPostParams = {originalNumber: this.form.originalNumber}
      let v2userName = ''
      if (this.apiToken) {
        const accessToken = parseJwt(this.apiToken)
        v2userName = (accessToken.employeeNumber || accessToken.email || accessToken.sub).substring(0, 16)
      }
      const passwordPostParams = {userName: this.software === 'v1' ? this.form.userName : v2userName}
      if (this.software === 'v1') {
        authenticationPostParams.secretName = 'BobcatEngineV1'
        passwordPostParams.secretName = 'BobcatEngineV1'
      }
      const authRequest = bdService.getAuthenticationNumber(authenticationPostParams).then(r => {
        if (r.status === 200) {
          this.authenticationNumber = r.data.authenticationKey
        }
      }).catch(() => {
        this.isError = true
      })
      const passwordRequest = bdService.retrievePassword(passwordPostParams).then(r => {
        if (r.status === 200) {
          this.id = v2userName
          this.password = r.data.password
        }
      }).catch(() => {
        this.isError = true
      })
      // Both requests run in parallel; the spinner stays until both have settled.
      Promise.allSettled([authRequest, passwordRequest]).then(() => {
        this.loading = false
      })
    },
    openModal() {
      this.$refs['modal'].show()
    },
    closeModal() {
      this.$refs['modal'].hide()
    },
    replaceWithUndefinedImage(event) {
      event.target.src = this.assetServer + '/assets/img/examples/undefined.png'
    }
  }
}
</script>
