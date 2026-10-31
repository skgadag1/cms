<template>
  <div>
    <div v-for="i in range" :key="'empty-box_'+i" class="box py-4 mb-2 position-relative" :data-test-id="'empty-widget__disabled_'+i">
      <div class="mask disabled rounded" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    range: {
      type: Number,
      required: false,
      default: 5
    }
  }
}
</script>
