<template>
  <div data-test-id="sticky-cta-buttons"
       class="d-none d-md-flex justify-content-center justify-content-lg-end align-items-center w-100"
  >
    <ul class="d-flex list-unstyled m-0">
      <li v-for="(item, index) in buttonActions" :key="index" class="mr-4 font-weight-bold">
        <b-spinner v-if="item.isLoading" small variant="primary" class="mr-2" />
        <a href="#" @click.prevent="$emit('handle-button-click', item)">{{ item.label }}</a>
      </li>
    </ul>
    <div class="d-flex justify-content-center align-items-center">
      <button
        v-if="primaryButton"
        class="d-flex justify-content-center btn btn-primary d-flex align-items-center font-weight-bold"
        @click.prevent="$emit('handle-button-click', primaryButton)"
      >
        {{ primaryButton.label }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    buttonActions: {
      type: Array,
      required: true
    },
    primaryButton: {
      type: Object,
      required: false
    }
  }
}
</script>
