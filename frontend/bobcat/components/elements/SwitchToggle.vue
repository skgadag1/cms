<template>
  <div class="font-weight-bold custom-switch custom-switch-toggle text-nowrap">
    <span v-if="label" class="pr-2">{{ label }}</span>
    <span class="mr-2" :class="{ 'text-muted' : selected === options[1].value}" :data-test-id="`toggle-switch__${options[0].label}`" @click.prevent="setValue(options[0].value)">{{ options[0].label }}</span>
    <b-form-checkbox ref="switchControl"
                     v-model="selected"
                     :value="options[1].value"
                     :unchecked-value="options[0].value"
                     switch-variant="info"
                     class="d-inline-block"
                     switch
    >
      <span class="cursor-pointer" :class="{ 'text-muted' : selected === options[0].value}" :data-test-id="`toggle-switch__${options[1].label}`">{{ options[1].label }}</span>
    </b-form-checkbox>
  </div>
</template>

<script>
import {watch, ref} from '@nuxtjs/composition-api'

export default {
  props: {
    label: String,
    value: String,
    options: {type: Array, required: true}
  },
  setup(props, ctx) {
    const selected = ref(props.value)

    watch(selected, () => {
      ctx.emit('input', selected.value)
    })

    return {
      selected
    }
  },
  methods: {
    setValue(v) {
      this.selected = v
    }
  }
}
</script>
