<template>
  <div class="content-limiter" :class="{'opened' : isOpened || disabled}">
    <slot />
    <div v-if="!disabled" class="show-more d-flex align-items-end justify-content-center mb-n3 pb-4">
      <DynamicButton v-if="!isOpened"
                     :variant="evolutionButton ? BUTTON_NAMES.HEXAGONAL : BUTTON_NAMES.CLASSIC"
                     color="white-red"
                     class="mb-3"
                     :title="generateConceptHint('showMore')"
                     @click.prevent.stop="isOpened = true"
      >
        {{ showMore }}
      </DynamicButton>
      <DynamicButton v-else
                     class="mb-3"
                     :variant="evolutionButton ? BUTTON_NAMES.HEXAGONAL : BUTTON_NAMES.CLASSIC"
                     color="white-red"
                     :title="generateConceptHint('showLess')"
                     @click.prevent.stop="isOpened = false"
      >
        {{ showLess }}
      </DynamicButton>
    </div>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '../../constants/buttons'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    evolutionButton: {
      type: Boolean,
      default: false
    },
    showLess: {
      type: String,
      default: 'Show less'
    },
    showMore: {
      type: String,
      default: 'Show more'
    }
  },
  setup(props) {
    const {generateConceptHint} = useConcepts()
    const isOpened = ref(false)
    isOpened.value = props.disabled

    return {
      BUTTON_NAMES,
      isOpened,
      generateConceptHint
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~/assets/sass/transitions';
@import '{.position-relative} from ~bootstrap/scss/utilities/_position.scss';
@import '{.transition-base} from ~bootstrap/scss/mixins/_transition.scss';
@import '{.overflow-hidden} from ~bootstrap/scss/utilities/_overflow.scss';

.content-limiter {
  @extend .position-relative;
  @extend .transition-base;

  &:not(.opened) {
    @extend .overflow-hidden;
    min-height: 9rem;
    max-height: 45vh;
  }

  .show-more {
    @include absolute-bottom;
    height: 7rem;
    background: linear-gradient(180deg, rgba($white, 0) 0%, rgba($white, .5) 35%, $white 80%);
    @extend .transition-base;
    @extend .z-index-2;

    .bg-gray-lighter & {
      background: linear-gradient(180deg, rgba($gray-100, 0) 0%, rgba($gray-100, .5) 35%, $gray-100 80%);
    }
  }

  &:before {
    content: '';
    pointer-events: none;
    @include position-top;
    @include absolute-bottom;
    @extend .transition-base;
    @extend .z-index-1;
  }

  &.opened {
    max-height: 100%;

    .show-more {
      height: auto;
      @extend .position-relative;
    }

    &:before {
      display: none;
    }
  }
}
</style>
