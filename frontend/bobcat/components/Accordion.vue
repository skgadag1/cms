<template>
  <section data-test-id="accordion"
           class="accordion w-100 transition-base"
           :class="[selectedColorVariant.accordionBackground, {'primary-accordion': isPrimaryAccordion}, {'opacity-3 disabled':disabled}, {'shadow': shadow}, {shown: isShown}]"
  >
    <div
      :disabled="disabled"
      :class="[isVisible ? null : 'collapsed', selectedColorVariant.text]"
      class="accordion-wrapper d-flex align-items-center justify-content-start"
      :aria-expanded="isVisible ? 'true' : 'false'"
      role="button"
      @click.stop="toggle"
    >
      <span class="d-flex align-items-center justify-content-center m-0 transition-base" :style="isVisible ? 'transform: rotate(180deg)' : null"><b-icon icon="sort-down" class="dropdown" /></span>
      <div class="d-flex align-items-center accordion-title-wrapper">
        <b-icon v-if="icon" :icon="icon" />
        <component :is="htag" class="mb-0 accordion-title" :class="isPrimaryAccordion ? 'h4' : 'h5'">{{ title }}</component>
        <p v-if="counter" class="font-weight-normal mb-0 ml-2 pl-1 small" data-test-id="counter" :class="selectedColorVariant.counterText">
          {{ counter }}
        </p>
      </div>
    </div>
    <b-collapse v-model="isVisible"
                :class="border ? 'accordion-border' : null"
                @show="$emit('expanded', isVisible)"
                @hide="$emit('expanded', isVisible); setShown(false)"
                @shown="setShown(true)"
    >
      <transition :name="animationEnabled ? 'fade-slide-right' : ''">
        <div v-show="isVisible || !animationEnabled">
          <slot />
        </div>
      </transition>
    </b-collapse>
  </section>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '../constants/variants/accordionGroupVariants'
import {COLORS} from '~/constants/variants/accordionGroupVariants'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    color: {
      type: String,
      default: COLORS.WHITE,
      validator(value) {
        return Object.values(COLORS).includes(value)
      }
    },
    title: {
      type: String,
      default: ''
    },
    icon: {
      type: String
    },
    border: {
      type: Boolean,
      default: false
    },
    shadow: {
      type: Boolean,
      default: false
    },
    counter: {
      type: Number
    },
    isExpanded: {
      type: Boolean,
      default: false
    },
    isPrimaryAccordion: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    toggleCb: {
      type: Function
    },
    id: {
      type: String,
      default: ''
    },
    htag: {
      type: String,
      default: 'h3'
    },
    animationEnabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props, {emit}) {
    const {isExpanded, disabled, toggleCb, id} = props
    const isVisible = ref(isExpanded)
    const isShown = ref(isExpanded)
    const selectedColorVariant = ref(VARIANTS_CLASSES[props.color])
    useMounted(props.nodeId)

    watch(() => props.isExpanded, (newVal) => {
      if (!disabled) { isVisible.value = newVal }
    })

    const toggle = async (e) => {
      emit('click', e)
      if (!disabled) {
        if (toggleCb) {
          await toggleCb(!isVisible.value, id)
        } else {
          isVisible.value = !isVisible.value
        }
      }
    }

    const setShown = (bool) => {
      isShown.value = bool
    }

    const setVisible = (bool) => {
      isVisible.value = bool
    }

    return {
      isVisible,
      isShown,
      selectedColorVariant,
      toggle,
      setShown,
      setVisible
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.accordion-wrapper {
  min-height: 40px; // to unify accordions with and without counter
  border-left: 4px solid transparent;
  padding: 1rem; // 16px all around per Figma
  gap: 0.375rem; // 6px gap between icon and title per Figma
}

.accordion-title-wrapper {
  gap: 0.375rem; // 6px gap between icon and title text
}

.accordion .accordion .accordion-wrapper {
  border-left-color: $gray-200;
}

.accordion-border {
  border-left: 4px solid $gray-200;
}

.accordion.rounded-4 {
    @include media-breakpoint-down(xs) {
      border-radius: 0 !important;
    }
}

.accordion-title {
  &.h3,
  &.h4 {
    font-size: $h4-font-size !important;
  }

  &.h5 {
    @include media-breakpoint-down(md) {
      font-size: $font-size-base !important;
    }
  }
}

.primary-accordion {
    border-bottom: 3px solid transparent;
    border-top: 3px solid transparent;

    .accordion-wrapper {
      &[disabled] {
        cursor: auto;
      }
    }

    &.disabled:hover {
      border-bottom-color: transparent;
    }

    &:hover {
        border-bottom-color: $primary;
    }
}
</style>
