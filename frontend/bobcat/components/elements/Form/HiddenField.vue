<template>
  <b-form-group data-test-id="hidden-field" class="mb-0">
    <label v-if="label" class="d-none">{{ label }}</label>
    <input :id="id"
           :name="name"
           class="d-none"
           type="hidden"
           :value="value"
    >
  </b-form-group>
</template>

<script>

export default {
  props: {
    name: {
      type: String
    },
    id: {
      type: String
    },
    onChange: {
      type: Function
    },
    value: {
      type: String
    },
    label: {
      type: String
    }
  },
  setup(props) {
    const {value, name, onChange} = props

    if (onChange && value) {
      onChange(value, name, true)
    }
  }
}
</script>
