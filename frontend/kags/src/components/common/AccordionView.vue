<template>
  <div v-if="items && items.length" class="accordion rounded">
    <div class="accordion-header d-flex align-items-center p-3 rounded-1 w-100" role="button" @click="expanded = !expanded">
      <div class="header-content d-flex align-items-center w-100">
        <b-icon :icon="'caret-up'" class="header-chevron text-white d-inline-flex align-items-center justify-content-center" :aria-expanded="!expanded" />
        <span class="header-title h5 fw-bold text-white flex-grow-1 mb-0">{{ title }}</span>
      </div>
    </div>

    <b-collapse v-model="expanded" class="accordion-content-wrapper">
      <div class="accordion-content d-flex flex-wrap w-100 bg-light">
        <a v-for="item in items"
           :key="item.id"
           :href="item.url"
           target="_blank"
           rel="noopener noreferrer"
           class="article-link font-condensed d-block text-primary"
        >
          {{ item.title || item.name }}
        </a>
      </div>
    </b-collapse>
  </div>
</template>

<script>
export default {
  name: 'AccordionView',
  props: {
    title: {
      type: String,
      required: true
    },
    items: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      expanded: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/scss/colors';
@import '@/scss/grid';

.accordion {
  .accordion-header {
    background: $black;
    cursor: pointer;

    .header-content {
      gap: 6px;
    }

    .header-chevron {
      width: 24px;
      height: 24px;

      &[aria-expanded="true"] {
        transform: rotate(180deg);
      }
    }
  }

  .accordion-content {
    gap: 1rem;
    padding: 24px;
    border-radius: 0 0 2px 2px;

    @include media-breakpoint-up(md) {
      padding: 32px;
    }
  }

  .article-link {
    flex-basis: 100%;
    font-size: 1.125rem;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  @include media-breakpoint-up(sm) {
    .article-link {
      flex-basis: calc(48% - 0.5rem);
    }
  }

  @include media-breakpoint-up(xl) {
    .article-link {
      flex-basis: calc(25% - 0.75rem);
    }
  }
}
</style>
