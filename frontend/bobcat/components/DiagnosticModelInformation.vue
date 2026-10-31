<template>
  <div>
    <section class="py-6 bg-gray-lighter w-100 d-flex flex-column justify-content-center align-items-center"
             data-test-id="diagnostic-model-information"
    >
      <b-row class="container ngb-form p-0">
        <b-col offset-lg="2" lg="8">
          <LazyHydrate never>
            <component :is="htag" :class="htag">{{ title }}</component>
          </LazyHydrate>
          <b-row class="pt-2 pt-md-3 mb-4 mb-md-0">
            <b-col md="6">
              <label for="serialNumber" class="p-0" :title="serialNumberLabel">{{ serialNumberLabel }}</label>
              <div class="input-group">
                <b-form-input
                  id="serialNumber"
                  v-model="serialNumberInput"
                  name="serialNumber"
                  class="form-control px-3 py-4 bg-white border border-color-gray-600"
                  :class="{'border-color-danger' : serialNumberTouched && !serialNumberInput.length}"
                  @blur.prevent="serialNumberTouched = true"
                  @keydown.enter="checkCode"
                />
              </div>
              <div class="mt-2 mb-3">
                <a v-if="linkField && linkField.label"
                   class="font-weight-bold mt-2"
                   :href="linkField.url"
                   :title="linkField.label"
                   :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
                   data-test-id="where-to-find-link"
                >{{ linkField.label }}<b-icon class="ml-2" icon="external" /></a>
              </div>
            </b-col>
            <b-col md="6">
              <label for="diagnosticCode" class="p-0" :title="diagnosticCodeLabel">{{ diagnosticCodeLabel }}</label>
              <div class="input-group">
                <b-form-input
                  v-model="faultCodeInput"
                  name="diagnosticCode"
                  class="form-control px-3 py-4 bg-white border border-color-gray-600"
                  :class="{'border-color-danger' : faultCodeTouched && !faultCodeInput.length}"
                  @blur.prevent="faultCodeTouched = true"
                  @keydown.enter="checkCode"
                />
              </div>
            </b-col>
          </b-row>
          <div :class="{'my-4': error || inEditorEdit}">
            <WarningAlert v-if="error"><div v-html="error" /></WarningAlert>
            <WarningAlert v-else-if="inEditorEdit" type="warning">
              No data to display due to edit mode
            </WarningAlert>
          </div>
          <a
            class="w-100 d-lg-flex justify-content-center px-5 btn btn-lg btn-primary"
            data-test-id="check-code-button"
            :class="{'disabled': !faultCodeInput.length || !serialNumberInput.length || loading}"
            @click="checkCode"
          >
            {{ checkCodeButton }}<b-spinner v-if="loading" small class="mr-2" /></a>
        </b-col>
      </b-row>
    </section>
    <section class="py-3 w-100 d-flex flex-column justify-content-center align-items-center"
             data-test-id="diagnostic-model-information"
    >
      <div v-if="description || loading" class="container">
        <b-overlay class="row" :show="loading">
          <div class="d-block d-sm-none col-12 mb-2">
            <div class="bg-gray-lighter py-2 px-3 rounded-2">
              <DiagnosticCode :fault-code="faultCode" :code-label="codeLabel" :loading="loading" />
              <DiagnosticDescription :description="description" :description-label="descriptionLabel" :loading="loading" />
            </div>
          </div>
          <div class="col-12 col-sm-6">
            <div class="d-flex align-items-center justify-content-center w-100 h-100">
              <ImageSet :src="img" data-test-id="diagnostic-model-information-image" class="w-100 h-100" />
            </div>
          </div>
          <div class="col-12 col-sm-6 flex-column">
            <div class="d-none d-sm-block bg-gray-lighter py-2 px-3 rounded-2">
              <DiagnosticCode :fault-code="faultCode" :code-label="codeLabel" :loading="loading" />
              <DiagnosticDescription :description="description" :description-label="descriptionLabel" :loading="loading" />
            </div>
            <div v-if="!error && faultCode && serialNumber">
              <p v-if="modelID" class="h2 mb-0">{{ modelID }}</p>
              <b-skeleton v-else-if="loading" class="my-2" height="2.75rem" width="20%" />
              <p v-if="category" class="h3 mb-0">{{ category }}</p>
              <b-skeleton v-else-if="loading" class="my-2" height="2.25rem" width="70%" />
              <p v-if="serialNumber" class="h4 mb-0">{{ serialNumber }}</p>
            </div>
          </div>
        </b-overlay>
      </div>
    </section>
  </div>
</template>

<script>
import {onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useQueryParams from '~/helpers/composables/useQueryParams'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  props: {
    codeLabel: {
      type: String,
      default: 'Code'
    },
    descriptionLabel: {
      type: String,
      default: 'Description'
    },
    title: {
      type: String,
      default: 'Code Diagnostic'
    },
    htag: {
      type: String,
      default: 'h2'
    },
    serialNumberLabel: {
      type: String,
      default: 'Serial Number (Required)'
    },
    diagnosticCodeLabel: {
      type: String,
      default: 'Diagnostic Code (Required)'
    },
    checkCodeButton: {
      type: String,
      default: 'Check Code'
    },
    whereToFindLink: Object
  },
  setup(props) {
    const {queryParams} = useQueryParams()
    const {$utils, $regions, $services, $ga4} = useContext()
    const isValidated = ref(false)
    const serialNumberTouched = ref(false)
    const faultCodeTouched = ref(false)
    const abbreviation = ref('')
    const img = ref('')
    const error = ref('')
    const loading = ref(false)
    const description = ref('')
    const modelID = ref('')
    const faultCode = ref(queryParams?.value?.code)
    const faultCodeInput = ref(queryParams?.value?.code || '')
    const serialNumber = ref(queryParams?.value?.SN)
    const serialNumberInput = ref(queryParams?.value?.SN || '')
    const category = ref('')
    const linkRegex = /(https?:\/\/[^" ]*)/i
    const linkField = ref($utils.getLinkField(props.whereToFindLink, $regions.current.language.name))

    const getImageFromConcepts = (prefixImages, abbreviationImages) => {
      let imageSrc

      if (serialNumber.value) {
        const prefix = Object.keys(prefixImages).find(prefix => serialNumber.value.startsWith(prefix))
        imageSrc = prefix && prefixImages[prefix]?.match(linkRegex)
        if (imageSrc && Array.isArray(imageSrc)) {
          return imageSrc[0]
        }
      }

      imageSrc = abbreviation.value && abbreviationImages[abbreviation.value]?.match(linkRegex)
      if (imageSrc && Array.isArray(imageSrc)) {
        return imageSrc[0]
      }
    }

    const getSparrowImage = async (serialNumber) => {
      const data = await $services.bobcatapi.getSparrowImage(serialNumber)
      const imageSrc = $utils.getCloudinaryAsset(data.src)
      return imageSrc
    }

    const ga4Trigger = () => {
      if (queryParams?.value?.code && queryParams?.value?.SN) {
        $ga4.events.sparrow_qr_code(faultCode.value, description.value, modelID.value, category.value, serialNumber.value)
      }
    }

    const errorLabels = ref({})
    const abbreviationLabels = ref({})
    const prefixLabels = ref({})
    const prefixImages = ref({})
    const abbreviationImages = ref({})

    const {fetch} = useFetch(async () => {
      errorLabels.value = await $services.bobcatapi.getConcepts(['/%region%/diagnostic/errors'])
      abbreviationLabels.value = await $services.bobcatapi.getConcepts(['/%region%/diagnostic/abbreviation_category_labels'])
      prefixLabels.value = await $services.bobcatapi.getConcepts(['/%region%/diagnostic/prefix_model_labels'])
      prefixImages.value = await $services.bobcatapi.getConcepts(['/%region%/diagnostic/prefix_images'])
      abbreviationImages.value = await $services.bobcatapi.getConcepts(['/%region%/diagnostic/abbreviation_images'])

      if (serialNumber.value && faultCode.value) {
        const data = await $services.bobcatapi.getFaultCodeDetails(faultCode.value, serialNumber.value)
        await prepareData(data)
      }
    })

    const prepareData = async (data) => {
      const errorMsg = data?.errors?.[0]
      modelID.value = data?.modelId
      abbreviation.value = data?.abbreviation
      category.value = data?.familyId
      description.value = data?.description
      category.value = abbreviationLabels.value[abbreviation?.value] || category.value

      if (serialNumber.value) {
        const match = Object.keys(prefixLabels.value).find(prefix => serialNumber.value.startsWith(prefix))
        modelID.value = prefixLabels.value[match] || modelID.value
      }

      if (!faultCode.value || !serialNumber.value) {
        const missingParams = []
        if (!faultCode.value) {
          missingParams.push('code')
        }
        if (!serialNumber.value) {
          missingParams.push('SN')
        }
        error.value = `${errorLabels.value.paremeter_required || errorMsg} ${missingParams.join(' & ')}`
      } else if (errorMsg?.toLowerCase().includes('serial')) {
        error.value = errorLabels.value.noserial_number_found?.replace('%SN%', serialNumber.value) || errorMsg
      } else if (errorMsg?.toLowerCase().includes('fault code')) {
        error.value = errorLabels.value.nofault_codes_found?.replace('%code%', faultCode.value) || errorMsg
      } else if (errorMsg) {
        error.value = errorMsg
      }

      const sparrowImage = await getSparrowImage(serialNumber.value)
      if (sparrowImage) {
        img.value = sparrowImage
      } else {
        img.value = errorMsg ? errorLabels.value.error_image?.match(linkRegex)?.[0] : getImageFromConcepts(prefixImages.value, abbreviationImages.value)
      }
    }

    const clearResults = () => {
      error.value = ''
      img.value = ''
      description.value = ''
      modelID.value = ''
      abbreviation.value = ''
      category.value = ''
    }

    const checkCode = async () => {
      serialNumberTouched.value = true
      faultCodeTouched.value = true
      if (serialNumberInput.value && faultCodeInput.value) {
        serialNumber.value = serialNumberInput.value
        faultCode.value = faultCodeInput.value
        clearResults()
        loading.value = true
        const data = await $services.bobcatapi.getFaultCodeDetails(faultCode.value, serialNumber.value)
        loading.value = false
        await prepareData(data)
      }
    }

    onMounted(async () => {
      if (!abbreviationImages.value.length) {
        await fetch()
        ga4Trigger()
      }
    })

    return {
      category,
      modelID,
      serialNumber,
      faultCode,
      description,
      img,
      inEditorEdit,
      error,
      isValidated,
      serialNumberTouched,
      faultCodeTouched,
      faultCodeInput,
      serialNumberInput,
      checkCode,
      loading,
      linkField
    }
  }
}
</script>

<style lang="scss">
img[data-test-id="diagnostic-model-information-image"] {
  aspect-ratio: 3/2;
}
</style>
