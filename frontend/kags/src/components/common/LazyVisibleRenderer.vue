<template>
  <div ref="rootEl" />
</template>

<script>
const ROOT_MARGIN = '500px'

export default {
  data() {
    return {
      isVisible: false
    }
  },
  mounted() {
    if (typeof IntersectionObserver === 'undefined') return
    this.observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting !== this.isVisible) this.handler(entry.isIntersecting)
    }, {rootMargin: ROOT_MARGIN})
    this.observer.observe(this.$refs.rootEl)
  },
  beforeUnmount() {
    if (this.observer) this.observer.disconnect()
  },
  methods: {
    handler(_v) {
      this.isVisible = _v
      this.$emit('visible', _v)
    }
  }
}
</script>
