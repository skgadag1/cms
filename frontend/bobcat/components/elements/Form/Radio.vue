<template>
  <b-form-group v-if="selected" data-test-id="radio-wrapper" :class="{'font-weight-bold': bold}" :label="label">
    <b-form-radio-group
      v-slot="{ ariaDescribedby }"
      v-model="selected.value"
      stacked
      :name="name"
      @change="handleChange"
    >
      <b-form-radio
        v-for="o in options"
        :key="`radio-option__${o['@id']}`"
        class="ml-4 z-index-0"
        :aria-describedby="ariaDescribedby"
        :value="o.value"
        :disabled="o.disabled === 'true'"
      >
        {{ o.label }}
      </b-form-radio>
    </b-form-radio-group>
  </b-form-group>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {label, name, options, onChange, bold} = config

export default {
  props: {
    label, name, options, onChange, bold
  },
  setup(props) {
    const {onChange, name, options} = props
    const selected = ref(options?.find(o => o.selected === 'true' && o.disabled === 'false') || options?.find(o => o.disabled === 'false') || null)

    if (onChange && selected.value) {
      onChange(selected.value.value, name, true)
    }

    const handleChange = (val) => {
      if (onChange) {
        onChange(val, name, true)
      }
    }

    return {
      selected,
      handleChange
    }
  }
}
</script>
