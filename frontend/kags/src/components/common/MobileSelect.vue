<template>
  <div class="w-100 position-relative is-mobile">
    <transition name="fade">
      <div v-show="isOpen || empty"
           class="modal-backdrop"
           :class="{ 'modal-backdrop__partial' : empty }"
           @click="close"
      />
    </transition>

    <b-button
              size="lg"
              variant="primary"
              class="w-100 text-start fw-bold"
              :class="{ 'exposed' : isOpen || empty }"
              @click.prevent="isOpen = true"
    >
      <slot name="button-title">{{ title }}</slot><span class="float-end dropdown-toggle" />
    </b-button>

    <div v-show="isOpen"
         class="position-absolute exposed bg-white rounded w-100"
         style="top: 0"
    >
      <slot name="dropdown" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      required: false
    },
    empty: {
      type: Boolean,
      required: false
    }
  },
  data() {
    return {
      isOpen: false
    }
  },
  methods: {
    close() {
      this.isOpen = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../scss/colors';

.list-group{
  &-item.active {
    background-color: $white;
    border: 1px solid rgba(0, 0, 0, 0.125);
    color: $orange;
  }
}
</style>
