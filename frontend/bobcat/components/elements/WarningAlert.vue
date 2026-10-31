<template>
  <div class="alert" :class="'alert-' + type">
    <div class="d-flex align-items-start justify-content-start h5 mb-0"
         :title="title"
    >
      <b-icon :icon="type" class="mr-3 mt-1" />
      <div><slot /></div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String
    },
    type: {
      type: String,
      default: 'error'
    }
  }
}
</script>
