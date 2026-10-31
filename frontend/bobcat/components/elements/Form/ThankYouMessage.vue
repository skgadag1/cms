<template>
  <div data-test-id="form-success-state" class="container">
    <div class="row">
      <div class="col-12">
        <div class="d-flex flex-column justify-content-center align-items-center py-6">
          <div class="d-flex align-items-center justify-content-center mb-2">
            <b-icon icon="success-state" class="h4 m-0 text-success" />
            <h4 class="m-0 ml-2">{{ successStateTitle }}</h4>
          </div>
          <p v-if="successStateSubtitle">{{ successStateSubtitle }}</p>
          <b-button
            v-if="submitAnother"
            data-test-id="submit-another-button"
            variant="primary"
            size="lg"
            type="submit"
            class="btn btn-wide submit-button mt-4"
            @click.prevent="resetForm"
          >
            {{ submitAnother }}
          </b-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import config from '~/constants/props/form.config'

const {
  successStateTitle,
  successStateSubtitle,
  submitAnother,
  resetForm
} = config

export default {
  props: {
    successStateTitle,
    successStateSubtitle,
    submitAnother,
    resetForm
  }
}
</script>
