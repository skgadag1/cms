<template>
  <b-row class="display-3 mt-3">
    <b-col cols="12" class="d-flex justify-content-center">
      <a href="#"
         class="thumb thumb__positive text-dark me-3"
         data-test-id="feedback-form__thumb-up"
         :class="{ 'active': positive.selected }"
         @mouseover="positive.mouseover = true"
         @mouseleave="positive.mouseover = false"
      >
        <b-icon v-if="!positive.mouseover && !positive.selected" :icon="['fal', 'thumbs-up']" />
        <b-icon v-else :icon="['fas', 'thumbs-up']" @click="selectFeedback('positive')" />
      </a>
      <a href="#"
         class="thumb thumb__negative text-dark"
         data-test-id="feedback-form__thumb-down"
         :class="{ 'active': negative.selected }"
         @mouseover="negative.mouseover = true"
         @mouseleave="negative.mouseover = false"
      >
        <b-icon v-if="!negative.mouseover && !negative.selected" :icon="['fal', 'thumbs-down']" />
        <b-icon v-else :icon="['fas', 'thumbs-down']" @click="selectFeedback('negative')" />
      </a>
    </b-col>
  </b-row>
</template>

<script>
export default {
  props: {
    options: {
      type: Array,
      required: false
    }
  },
  data() {
    return {
      positive: {
        mouseover: false,
        selected: false,
        id: this.options[0].id
      },
      negative: {
        mouseover: false,
        selected: false,
        id: this.options[1].id
      }
    }
  },
  methods: {
    selectFeedback(direction) {
      this.positive.selected = direction === 'positive' ? !this.positive.selected : false
      this.negative.selected = direction === 'negative' ? !this.negative.selected : false
      this.$emit('selected', direction, this[direction].id, this[direction].selected)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/utils';

.thumb {
  height: 4.375rem;
  width: 4.375rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50rem;

  &__positive.active,
  &__positive:hover {
    color: white !important;
    @extend .bg-green;
  }

  &__negative.active,
  &__negative:hover {
    color: white !important;
    @extend .bg-red;
  }
}
</style>
