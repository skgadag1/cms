<template>
  <div v-click-outside="hideDropdown"
       class="d-block d-md-inline-block position-relative filter-item"
       :data-test-id="'filter-item__'+title.trim().replace(' ','')"
       :class="{ 'filter-item__active' : show }"
  >
    <b-button class="dropdown-toggle text-start bg-white smallest"
              variant="outline-light"
              :class="{ 'active': show }"
              :style="'min-width: '+filterWidth+'px'"
              @click="toggleDropdown"
    >
      {{ title }}
    </b-button>
    <div v-show="show"
         ref="filterOptions"
         class="filter-item__options list-unstyled smallest"
         data-test-id="filter-item__options"
         :class="{ 'w-100' : shouldStretch }"
    >
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      required: true
    },
    options: {
      type: Array,
      required: false,
      default() { return [] }
    }
  },
  data() {
    return {
      show: true,
      filterWidth: 'auto',
      shouldStretch: false,
      internalOptions: []
    }
  },
  watch: {
    show(_v) {
      if (_v && this.options) {
        this.internalOptions = this.options.map(x => x.value)
        this.$emit('show')
      } else if (this.internalOptions.length && this.options.length) {
        // compare changes
        if (this.options.filter((x, i) => this.internalOptions[i] !== x.value).length) this.$emit('updated')
        this.$emit('hide')
      } else {
        this.$emit('hide')
      }
    }
  },
  mounted() {
    this.filterWidth = this.$refs.filterOptions.clientWidth + 2
    this.shouldStretch = this.filterWidth < this.$el.clientWidth
    this.show = false
  },
  methods: {
    hideDropdown() {
      this.show = false
    },
    showDropdown() {
      this.show = true
    },
    toggleDropdown() {
      this.show = !this.show
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/grid';
@import '../../scss/utils';
@import '../../scss/z-index';

.filter-item {
  margin: .25rem 0;

  @include media-breakpoint-up('md') {
    margin: .5rem .5rem 0 0;
  }

  // Above Bootstrap pagination (.page-item.active .page-link has z-index: 3), which otherwise
  // paints over the open options panel when a filter sits just above a table's pager.
  &__active {
    z-index: $zindex-dropdown;
  }

  &__options {
    position: absolute;
    top: 0;
    left: 0;
    padding-top: 2.2rem;
    box-shadow: 0 .15rem .5rem rgba($black, .35);

    &> div,
    &> li {
      @extend .bg-white;
    }

    @include media-breakpoint-down('md') {
      right: 0;
    }
  }

  button.dropdown-toggle {
    padding-right: 1.675rem;
    background: $gray-100;
    border-color: $gray-400;
    color: $gray-800;

    @include media-breakpoint-down('md') {
      width: 100%;
    }

    &:after {
      position: absolute;
      top: calc(50% - .255rem);
      right: .75rem;
      transition: .2s all;
    }

    &.active {
      z-index: 1;

      &:active,
      &:focus,
      & {
        box-shadow: none !important;
        outline: none;
        border-color: transparent;
        border-bottom-color: $gray-200;
        background: transparent !important;
      }

      &:after {
        transform: rotate(180deg);
      }
    }
  }
}
</style>
