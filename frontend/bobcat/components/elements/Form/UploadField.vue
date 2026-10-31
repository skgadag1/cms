<template>
  <b-form-group v-if="field" data-test-id="upload-field">
    <label
      v-if="label"
      :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]"
    >
      {{ label }}<span v-if="required"> *</span>
    </label>
    <b-input-group class="position-relative">
      <div
        class="form-control px-3 py-4 bg-white border d-flex align-items-center"
        :class="isTouched ? (isValidated ? 'is-valid' : 'is-invalid') : ''"
      >
        <p class="m-0"> {{ files && files.length && field.selectedLabel ? `${field.selectedLabel} ${files.length}` : placeholder }}</p>
      </div>
      <input
        type="file"
        :accept="field.allowedMimeTypePattern"
        :name="name"
        multiple
        :required="required"
        class="position-absolute w-100 h-100 opacity-0 cursor-pointer"
        :placeholder="placeholder"
        @change="handleFilesChange"
      >
    </b-input-group>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
const {
  label,
  name,
  onChange,
  required,
  validationMessage,
  placeholder,
  validation,
  bold,
  field
} = config

export default {
  props: {
    label,
    name,
    onChange,
    required,
    validationMessage,
    placeholder,
    validation,
    bold,
    field
  },
  setup(props) {
    const {onChange, name, required, field} = props
    const files = ref(null)
    const isValidated = ref(true)
    const isTouched = ref(false)

    const handleFilesChange = (e) => {
      files.value = e.target?.files
      isTouched.value = true
    }

    watch(files, (newVal) => {
      let isValid = !required || (required && newVal?.length > 0)
      if (isValid && field.maxUploadSize) {
        const totalSize = Array.from(newVal)?.reduce((acc, val) => acc + val?.size, 0)
        isValid = totalSize <= parseInt(field.maxUploadSize)
      }

      isValidated.value = isValid
      if (onChange) {
        onChange(newVal, name, isValid, null, true)
      }
    })

    return {files, isValidated, handleFilesChange, isTouched}
  }
}
</script>
