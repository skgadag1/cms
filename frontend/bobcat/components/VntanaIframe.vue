<template>
  <section class="container py-4" data-test-id="vntana-iframe">
    <h4 v-if="title">{{ title }}</h4>
    <div v-if="paragraph" v-html="paragraph" />
    <div class="d-flex justify-content-center">
      <div class="vntana-responsive position-relative w-100">
        <iframe
          :src="src"
          class="position-absolute w-100 h-100 border-0"
          allow="fullscreen; xr-spatial-tracking"
        />
      </div>
    </div>
  </section>
</template>

<script>
export default {
  props: {
    title: {
      type: String
    },
    paragraph: {
      type: String
    },
    src: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.vntana-responsive {
  aspect-ratio: 620 / 500;

  @include media-breakpoint-down(sm) {
    aspect-ratio: 0.7;
  }

  iframe {
    border: none;
  }
}
</style>
