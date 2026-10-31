<template>
  <b-modal id="retrievePasswordModal" ref="modal" :title="translate(software + 'passwordRetrievalModalTitle', 'SoftwareRegistration')" size="xl" @hide="resetForm">
    <template v-slot:default>
      <b-row class="p-3">
        <b-col cols="12" lg="6">
          <h2>{{ translate('instructionsLabel', 'SoftwareRegistration') }}</h2>
          <div class="mt-3" v-html="translate(software + 'passwordRetrievalInstructions', 'SoftwareRegistration')" />
          <div v-if="software === 'v1'" class="d-block d-sm-flex  mb-3 mb-sm-0">
            <div class="flex-grow-1 pe-0 pe-sm-3">
              <b-form-group
                id="usernameGroup"
                class="w-100"
                :label="translate('usernameLabel', 'SoftwareRegistration')"
                label-for="username"
              >
                <b-form-input id="username"
                         v-model.trim="username"
                         size="sm"
                         class="form-control bg-gray-100 text-black"
                         name="username"
                />
              </b-form-group>
            </div>
            <div class="flex-grow-0">
              <div class="d-none d-sm-block mb-2">&nbsp;</div>
              <b-button variant="primary"
                     class="font-condensed mt-1 mt-sm-0 w-100"
                     :disabled="username === ''"
                     data-test-id="retrieve-password-button"
                     @click="retrievePassword"
              >
                <b-spinner v-if="loading" small />
                {{ translate("retrievePasswordButton", 'SoftwareRegistration') }}
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
            <div v-html="translate('retrievePasswordError', 'SoftwareRegistration')" />
          </b-error>
        </b-col>
        <b-col cols="12" lg="6" class="border-start py-3 py-lg-0">
          <h2>{{ translate('referenceImageLabel', 'SoftwareRegistration') }}</h2>
          <b-img fluid
                 :src="referenceImage"
                 :alt="translate('passwordRetrievalReferenceImageAlt', 'SoftwareRegistration')"
                 @error="replaceWithUndefinedImage"
          />
        </b-col>
      </b-row>
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
  props: ['software'],
  data() {
    return {
      username: '',
      id: '',
      password: '',
      isError: false,
      loading: false
    }
  },
  computed: {
    referenceImage() {
      let imageConcept = this.labels['SoftwareRegistration'][this.software + 'passwordRetrievalReferenceImage'] || ''
      const link = /(https?:\/\/[^" ]*)/i
      if (imageConcept) {
        const imageSrc = imageConcept.match(link)
        if (Array.isArray(imageSrc)) return imageSrc[0]
      }

      return this.assetServer + '/assets/img/examples/undefined.png'
    }
  },
  mounted() {
    if (this.software === 'v2') this.retrievePassword()
  },
  methods: {
    resetForm() {
      this.isError = false
      this.username = ''
      this.password = ''
    },
    retrievePassword() {
      this.loading = true
      this.isError = false
      let v2userName = ''
      if (this.apiToken) {
        const accessToken = parseJwt(this.apiToken)
        v2userName = accessToken.employeeNumber || accessToken.sub.split('@')[0]
      }
      const passwordPostParams = {
        userName: this.software === 'v1' ? this.username : v2userName
      }
      if (this.software === 'v1') {
        passwordPostParams.secretName = 'BobcatEngineV1'
      }
      bdService.retrievePassword(passwordPostParams).then(r => {
        if (r.status === 200) {
          this.id = v2userName
          this.password = r.data.password
        }
      }).catch(() => {
        this.isError = true
      }).finally(() => {
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
