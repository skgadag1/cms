<template>
  <div class="feedback-container">
    <div class="feedback-title text-center" data-test-id="feedback-form__title">
      <slot name="title" />
      <b-icon :icon="['far', 'window-minimize']" class="float-end cursor-pointer" @click="close" />
    </div>
    <transition name="fade" :duration="{ enter: 350, leave: 10 }" appear>
      <div v-if="!loading" class="feedback-body align-items-center justify-content-center" data-test-id="feedback-form__body">
        <transition name="slide-down" :duration="{ enter: 350, leave: 20 }">
          <slot name="step1" />
        </transition>
        <transition name="slide-down" :duration="{ enter: 350, leave: 20 }">
          <slot name="step2" />
        </transition>
        <transition name="slide-down" :duration="{ enter: 350, leave: 20 }">
          <slot name="step3" />
        </transition>
      </div>
      <div v-else class="feedback-body d-flex align-items-center justify-content-center py-5" data-test-id="feedback-form__body">
        <div class="my-5 py-5" data-test-id="feedback-form__loading">
          <b-spinner variant="primary" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  emits: ['close'],
  props: {
    loading: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  methods: {
    close() {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';

.feedback-body {
  .btn {
    &-light {
      font-weight: 700;
      color: $gray-700;
      border-bottom: 2px solid $gray-400;
    }

    &-secondary {
      font-weight: 700;
      color: $white;
      border-bottom: 2px solid $gray-300;
    }
  }
}
</style>
