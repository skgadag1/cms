<template>
  <i :class="`bobcat-icon-${iconObject}`" />
</template>

<script>
export default {
  props: {
    icon: {
      required: true
    }
  },
  computed: {
    iconObject() {
      try {
        return JSON.parse(this.icon)
      } catch (e) {
        return this.icon
      }
    }
  }
}
</script>

<style lang="css" scoped>
@import '../../public/BobcatIcons.css';

i {
    display: inline-flex;
    justify-content: center;
    align-items: center;
}
</style>
