<template>
  <b-form-group v-if="renderCondition">
    <b-form-checkbox
      v-model="status"
      class="custom-checkbox z-index-0 py-2"
      button-variant="secondary"
      value="1"
      unchecked-value=""
      :class="{'font-weight-bold': bold}"
    >
      {{ field.checkboxLabel }}
    </b-form-checkbox>
    <b-collapse :visible="status === '1'" class="mt-2">
      <b-row class="mb-2">
        <b-col v-for="input in inputs" :key="input['@id']" :md="input.width && input.width.field === 'halfWidth' ? 6 : 12" cols="12">
          <InputText
            v-if="status === '1'"
            :key="input['@id']"
            :name="input.name"
            :icon="(input.icon && input.icon !== 'none') ? input.icon : null"
            :required="input.required === 'true'"
            :minlength="input.minLength"
            :maxlength="input.maxLength"
            :label="input.label"
            :placeholder="input.placeholder"
            :validation-message="input.validationMessage"
            :validation="input.validation"
            :on-change="onChange"
            :bold="input.bold === 'true'"
          />
        </b-col>
      </b-row>
    </b-collapse>
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'

export default {
  props: {
    field: {
      type: Object,
      required: true
    },
    onChange: {
      type: Function,
      required: true
    },
    initRelatedFields: {
      type: Function,
      required: true
    },
    removeRelatedFields: {
      type: Function,
      required: true
    },
    bold: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const {field, removeRelatedFields, initRelatedFields, onChange} = props
    const status = ref('0')
    const inputs = ref([])
    const renderCondition = ref(field && field.checkboxLabel && field?.inputs?.['@nodes']?.length)

    inputs.value = field?.inputs?.['@nodes'].map(inputName => field?.inputs?.[inputName])

    watch(status, (newVal) => {
      onChange(newVal, field.name, true)
      if (newVal === '1') {
        initRelatedFields(inputs.value)
      } else {
        removeRelatedFields(inputs.value)
      }
    })

    return {
      status,
      inputs,
      renderCondition
    }
  }
}
</script>
