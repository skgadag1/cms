<template>
  <a :href="href" class="card-icon text-gray-600" :style="cssStyle">
    <slot name="icon" />
    <p class="text-center m-0">
      <slot name="label" />
    </p>
  </a>
</template>

<script>
export default {
  name: 'CardIcon',
  props: {
    styles: {
      type: Object,
      required: false
    },
    href: {
      type: String,
      required: false,
      default: '#'
    }
  },
  data() {
    return {
      cssStyle: {
        width: '6.25rem',
        'min-height': '7.8125rem'
      }
    }
  },
  created() {
    this.cssStyle = Object.assign(this.cssStyle, this.styles)
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';
@import '../../scss/utils';

.card-icon {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  border-radius: var(--bs-border-radius) !important;

  &:hover {
    color: $orange;
  }
}
</style>
