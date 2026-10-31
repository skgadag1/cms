<template>
  <div class="table-counter d-flex align-items-center justify-content-center align-self-center fw-bold rounded">
    {{ title }}
  </div>
</template>

<script>
export default {
  name: 'TableCounter',
  props: {
    title: {
      type: [String, Number],
      required: true
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';

.table-counter {
  position: relative;
  z-index: 0;
  background: $green;
  color: $black;
  width: 2rem;
  height: 1.5rem;
  margin-right: .5rem;
  padding-left: .25rem;

  &:after {
    content: '';
    position: absolute;
    top: .25rem;
    right: -.55rem;
    width: 1.025rem;
    height: 1.025rem;
    background: $green;
    transform: rotate(45deg);
    z-index: -1;
  }
}
</style>
