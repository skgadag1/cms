<template>
  <div data-test-id="terms-and-conditions-field">
    <div class="accordion pb-2" role="tablist">
      <b-card>
        <b-card-header header-tag="header" class="p-1" role="tab">
          <div class="terms-and-conditions">
            <b-form-checkbox
              class="custom-checkbox z-index-0"
              button-variant="secondary"
              @change="handleChange"
            >
              <div v-html="title" />
            </b-form-checkbox>
            <span v-b-toggle.accordion-1 class="dropdown-toggle ml-2" />
          </div>
        </b-card-header>
        <b-collapse
          id="accordion-1"
          accordion="my-accordion"
          role="tabpanel"
        >
          <b-card-body>
            <b-card-text v-html="text" />
          </b-card-body>
        </b-collapse>
      </b-card>
    </div>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValid">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import {title, text, name, onChange, required, validationMessage} from '~/constants/props/form.config'

export default {
  props: {
    name,
    onChange,
    required,
    validationMessage,
    title,
    text
  },
  setup(props) {
    const {onChange, name, required} = props
    const isValid = ref(true)

    const handleChange = (value) => {
      const paredValue = value ? 1 : 0
      if (required) { isValid.value = paredValue === 1 }
      onChange(paredValue, name, isValid.value)
    }

    return {
      handleChange,
      isValid
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.card-body {
  p {
    max-height: 28rem;
    overflow-y: auto;
    border: 0.063rem solid $gray-200;
    background: $white;
    gap: $font-size-base;
    padding: $h4-font-size;
    flex-direction: column;
    align-items: flex-start;
  }
  display: flex;
  padding: 0;
  flex-direction: column;
}

.card, .card:hover {
  border: none;
  box-shadow: none
}

.card-header {
  padding: 1rem 1.25rem;
  margin-bottom: 0;
  background-color: none;
  border: none;
  background: none;
}

.terms-and-conditions {
  display: flex;
  align-items: center;

  .collapsed {
    transform: rotate(0);
    &:hover {
      color: $red;
    }
  }

  .not-collapsed {
    transform: rotateX(180deg);
    color: $red;
  }
}

.dropdown-toggle {
  margin-bottom: 0.9rem;
}

.card-body {
  word-break: break-all;
}

</style>
