<template>
  <div class="d-flex flex-wrap gap-2">
    <div v-for="field in fields" :key="field['@id']" :class="field.width && field.width.field === 'halfWidth' ? 'flex-shrink-0 flex-auto pr-2' : 'w-100'">
      <span v-if="[
              FORM_FIELD_TYPES.PRODUCT_GROUPS,
              FORM_FIELD_TYPES.CHECKBOX_GROUP,
              FORM_FIELD_TYPES.SELECT,
              FORM_FIELD_TYPES.STATES,
              FORM_FIELD_TYPES.RADIO,
              FORM_FIELD_TYPES.INPUT,
              FORM_FIELD_TYPES.AREA,
              FORM_FIELD_TYPES.NUMERIC,
              FORM_FIELD_TYPES.PHONE].includes(field['@name'])"
            v-html="getCurrentFieldValue(field.name)"
      />
      <span
        v-else-if="field['@name'] === FORM_FIELD_TYPES.CUSTOM_TEXT && field.text"
        data-test-id="custom-text"
        v-html="field.text"
      />
    </div>
  </div>
</template>

<script>
import config from '~/constants/props/form.config'
import FORM_FIELD_TYPES from '~/constants/formFieldTypes'

const {
  fields,
  formFields
} = config

export default {
  props: {
    fields,
    formFields
  },
  setup(props) {
    const getCurrentFieldValue = (fieldName) => {
      if (props.formFields?.[fieldName]?.value instanceof Array) {
        return props.formFields?.[fieldName]?.value.join(', ')
      } else {
        return props.formFields?.[fieldName]?.value || ''
      }
    }

    return {
      FORM_FIELD_TYPES,
      getCurrentFieldValue
    }
  }
}
</script>
