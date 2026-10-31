<template>
  <b-row class="mt-3">
    <b-col cols="auto" data-test-id="feedback-form__footer-extra">
      <slot />
    </b-col>
    <b-col cols />
    <b-col cols="auto">
      <b-button :variant="disabled ? 'dark' : 'primary'"
             class="font-condensed"
             data-test-id="feedback-form__submit"
             :disabled="disabled"
             @click="submit"
      >
        {{ submitLabel }}
      </b-button>
    </b-col>
  </b-row>
</template>

<script>
export default {
  emits: ['submit'],
  props: {
    submitLabel: {
      type: String,
      required: false,
      default: 'Submit'
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  methods: {
    submit() {
      this.$emit('submit')
    }
  }
}
</script>
