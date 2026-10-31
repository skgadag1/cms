<template>
  <div>
    {{ durationText }}
  </div>
</template>

<script>

export default {
  props: {
    duration: {
      type: Object
    }
  },
  setup(props) {
    const {duration} = props
    const durationText = duration?.text || ''

    return {
      durationText
    }
  }
}
</script>

<style scoped>
.time-to-destination {
  font-family: Arial, sans-serif;
}
</style>
