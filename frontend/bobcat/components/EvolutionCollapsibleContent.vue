<template>
  <div
    ref="contentWrapper"
    data-test-id="evolution-collapsible-content"
    :class="['evolution-collapsible-content position-relative', selectedColorVariant.headline, `gradient-${variant}`, {'show-full': showFullContent}]"
  >
    <div v-if="headerHtml" class="container">
      <div class="collapsible-inner" v-html="headerHtml" />
    </div>

    <div class="content-wrapper">
      <div v-if="bodyHtml" class="container">
        <div class="collapsible-inner" v-html="bodyHtml" />
      </div>

      <slot />
    </div>

    <div v-if="isTruncated && !showFullContent" class="show-more-block">
      <DynamicButton
        :variant="BUTTON_NAMES.HEXAGONAL"
        color="white-red"
        @click.prevent="toggleContent"
      >
        {{ labels.showMore || 'Show More' }}
      </DynamicButton>
    </div>

    <div v-if="isTruncated && showFullContent" class="show-less-block text-center">
      <DynamicButton
        :variant="BUTTON_NAMES.HEXAGONAL"
        color="white-red"
        @click.prevent="toggleContent"
      >
        {{ labels.showLess || 'Show Less' }}
      </DynamicButton>
    </div>
  </div>
</template>

<script>
import {ref, computed} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '~/constants/buttons'
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

    const LEADING_HEADINGS_RE = /^(?:\s*<h[1-6][^>]*>[\s\S]*?<\/h[1-6]>)+\s*/i

    const splitContent = computed(() => {
      if (!props.content) {
        return {header: '', body: ''}
      }
      const match = props.content.match(LEADING_HEADINGS_RE)
      if (!match) {
        return {header: '', body: props.content.trim()}
      }
      return {
        header: match[0].trim(),
        body: props.content.slice(match[0].length).trim()
      }
    })

    const headerHtml = computed(() => splitContent.value.header)
    const bodyHtml = computed(() => splitContent.value.body)

    const toggleContent = () => {
      showFullContent.value = !showFullContent.value
    }

    return {
      showFullContent,
      selectedColorVariant,
      headerHtml,
      bodyHtml,
      toggleContent,
      BUTTON_NAMES
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

$collapsed-max-height: 8.75rem;
$overlay-height: 5rem;

.evolution-collapsible-content {
  &.show-full .content-wrapper {
    max-height: none;
    overflow: visible;
  }
}

.content-wrapper {
  position: relative;
  overflow: hidden;
  max-height: $collapsed-max-height;
}

.show-more-block {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  height: $overlay-height;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: .5rem;
  pointer-events: none;

  > * {
    pointer-events: auto;
  }

  .gradient-white & {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $gray-100);
  }

  .gradient-light &,
  .gradient-darkMedium & {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $gray-200);
  }

  .gradient-dark & {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $black);
  }

  .gradient-black &,
  .gradient-gradientBlack & {
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0), $gray-800);
  }
}

.show-less-block {
  margin-top: 1.5rem;
}

</style>
