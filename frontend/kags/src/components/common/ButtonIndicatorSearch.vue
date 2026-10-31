<template>
  <a :href="href" class="position-absolute btn-indicator d-flex align-items-center" :class="{'hover': hover}">
    <div class="btn-indicator__label font-condensed text-white px-4 rounded bg-primary text-nowrap d-flex align-items-center justify-content-center">{{ label }}</div>
    <div class="icon-right h-100 d-flex align-items-center justify-content-center" @click.prevent.stop>
      <b-icon icon="caret-right" />
    </div>
  </a>
</template>

<script>
export default {
  props: {
    label: {
      type: String,
      required: true
    },
    href: {
      type: String,
      required: false,
      default: '#'
    },
    hover: {
      type: Boolean,
      required: false
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';
@import '../../scss/grid';

.btn-indicator {
  top: 0;
  right: 0;
  bottom: 0;
  width: 2rem;
  text-decoration: none !important;

  @include media-breakpoint-up('md') {
    width: 1rem;
  }

  &__label {
    position: relative;
    height: 100%;
    transform: translateX(1.5rem);
    transition: .2s all ease-in-out;
  }

  .icon-right {
    position: absolute;
    top: 0;
    right: 0;
    width: 2rem;
    background: $white;
    color: $primary;
    border-left: 1px solid $gray-200;
    transition: .1s all ease-in-out;

    @include media-breakpoint-up('md') {
      width: 1rem;
      border-left: none;
    }
  }

  &.hover {
    .btn-indicator__label {
      transform: translateX(-99%);
    }

    .icon-right {
      border-left-color: $primary;
      background: $primary;
      color: $white;
    }
  }
}
</style>
