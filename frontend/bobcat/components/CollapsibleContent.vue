<template>
  <div
    ref="contentWrapper"
    data-test-id="collapsible-content"
    class="col-12"
    :class="[{'small' : fontSize === 'small'}, selectedColorVariant.headline]"
  >
    <div v-if="content"
         class="content-wrapper"
         :class="[{'show-full': showFullContent}, `gradient-${variant}`]"
         v-html="content"
    />

    <slot v-if="showFullContent" />

    <b-button v-if="isTruncated"
              variant="outline-primary"
              :class="['show-content-btn', selectedColorVariant.buttonBg, { 'show-full': showFullContent }]"
              @click="toggleContent"
    >
      {{ showFullContent ? labels.showLess || 'Show Less' : labels.showMore || 'Show More' }}
    </b-button>
    <hr v-if="showFullContent && isTruncated" class="divider">
    <div v-if="showFullContent && isTruncated" class="expanded-content-spacing" />
  </div>
</template>

<script>
import {ref, computed} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES, WHITE_VARIANT} from '~/constants/variants/sectionTextVariants'

export default {
  props: {
    fontSize: {
      type: String
    },
    labels: {
      type: Object
    },
    variant: {
      type: String,
      default: WHITE_VARIANT
    },
    isTruncated: {
      type: Boolean,
      default: false
    },
    contentWrapper: {
      type: Boolean
    },
    content: {
      type: String
    }
  },
  setup(props) {
    const showFullContent = ref(!props.isTruncated)
    const selectedColorVariant = computed(() => (VARIANTS_CLASSES[props.variant]))

    const toggleContent = () => {
      showFullContent.value = !showFullContent.value
    }

    return {
      showFullContent,
      selectedColorVariant,
      toggleContent
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.content-wrapper {
  position: relative;
  overflow: hidden;
  max-height: calc(1.5rem * 6); // limit to 6 lines

  p:last-child {
    margin-bottom: 0;
  }

  &::after {
    content: '';
    display: block;
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;
    height: calc(1.5rem * 4);
    pointer-events: none;
  }

  &.gradient-white::after {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $white);
  }

  &.gradient-dark::after {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $dark);
  }

  &.gradient-light::after {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $light);
  }

  &.show-full {
    max-height: unset;
  }

  &.show-full::after {
    display: none; // hide the fade effect when content is expanded
  }
}

.expanded-content-spacing {
  height: 40px;
  width: 100%;
}

.divider {
  height: 1px;
  background-color: $tertiary;
}

.show-content-btn {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translate(-50%, -20%);

  &.show-full {
    transform: translate(-50%, 0)
  }

}
</style>
