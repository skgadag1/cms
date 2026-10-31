<template>
  <div class="bg-gray-100 box-shadow rounded font-condensed position-relative overflow-hidden d-lg-none mb-2">
    <overlay-wrapper :show="loading">
      <div v-show="isSomethingBefore"
           class="inpage-navigation inpage-before box-shadow rounded-start"
           @click="scrollBack"
      >
        <div class="icon-wrapper d-flex align-items-center">
          <span class="icon" />
        </div>
      </div>

      <b-nav ref="navigationList" class="nav-vertical nav-inpage d-md-flex flex-nowrap text-center" data-test-id="nav-vertical-mobile">
        <template v-for="(itm, i) in items" :key="'page_navigation_item_'+i">
          <b-nav-item v-if="isAvailable(itm)"
                      :href="itm.url"
                      :data-test-id="'nav-inpage-item_'+i"
                      :active="itm.active"
                      :disabled="isDisabled(itm)"
                      class="small d-inline-flex align-items-center justify-content-center flex-column"
          >
            <b-icon v-if="itm.icon && itm.icon !== 'null'" class="h2" :icon="itm.icon" /><br>
            <div>{{ itm.title }}</div>
          </b-nav-item>
        </template>

        <b-nav-item v-if="!items.length" class="small d-inline-flex align-items-center justify-content-center flex-column invisible">
          <b-icon class="h2" icon="clock" /><br>Empty
        </b-nav-item>
      </b-nav>

      <div v-show="isSomethingAfter"
           class="inpage-navigation inpage-next box-shadow rounded-end"
           @click="scrollForth"
      >
        <div class="icon-wrapper d-flex align-items-center">
          <span class="icon" />
        </div>
      </div>

      <div v-if="disabled" class="mask disabled" />
    </overlay-wrapper>
  </div>
</template>

<script>
import InPageNavigationDesktop from './InPageNavigationDesktop'
import {useMenu} from '@/composables/useMenu'

export default {
  extends: InPageNavigationDesktop,
  setup() { return useMenu() },
  data() {
    return {
      scrollPosition: 0,
      screenWidth: 0,
      navigationWidth: 0
    }
  },
  computed: {
    isSomethingBefore() {
      return this.scrollPosition > 0
    },
    isSomethingAfter() {
      if (this.scrollPosition == 0 && this.navigationWidth == this.screenWidth) return 1
      return Math.floor(this.navigationWidth - this.screenWidth - this.scrollPosition) > 1
    }
  },
  watch: {
    loading(n) {
      if (!n && this.navigationEl()) this.$nextTick(() => this.scrollToActive())
    }
  },
  mounted() {
    this.scrollEl = this.navigationEl()
    if (this.scrollEl) {
      this.handleScroll()
      this.scrollEl.addEventListener('scroll', this.handleScroll)
    }
  },
  beforeDestroy() {
    if (this.scrollEl) this.scrollEl.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    // The ref is the <b-nav> component; the scrollable <ul> is its $el
    navigationEl() {
      return this.$refs.navigationList ? this.$refs.navigationList.$el : null
    },
    handleScroll() {
      const el = this.navigationEl()
      this.navigationWidth = el.scrollWidth
      this.screenWidth = el.offsetWidth
      this.scrollPosition = el.scrollLeft
    },
    scrollBack() {
      this.navigationEl().scrollLeft -= this.screenWidth / 4
    },
    scrollForth() {
      this.navigationEl().scrollLeft += this.screenWidth / 4
    },
    scrollToActive() {
      const firstItem = this.navigationEl().querySelector('li')
      if (!firstItem) return
      const itemWidth = firstItem.offsetWidth
      const maxItemsVisible = Math.floor(this.screenWidth / itemWidth)
      const currentActive = this.items.map(x => x.active).indexOf(true) + 1
      let scrollOffset = currentActive - maxItemsVisible
      this.$nextTick(() => {
        this.navigationEl().scrollLeft += scrollOffset * itemWidth
      })
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';

.nav-inpage {
  overflow-x: auto;
  scroll-behavior: smooth;

  .nav-item {
    min-width: 6.5rem;
    min-height: 5rem;
    width: 25%;

    .nav-link {
      height: 100%;
      width: 100%;
      padding: 1rem 0.25rem;
      line-height: 1rem;
      flex-flow: row wrap;
      display: flex;
      align-items: center;
      justify-content: center;


      bicon {
        font-size: 2rem;
      }

      span.d-inline-block.h2 {
        margin: 0;
        width: 100%;
      }
    }
  }
}

.inpage-navigation {
  cursor: pointer;
  background: $white;
  border-radius: .5rem;
  height: 100%;
  position: absolute;
  text-align: center;
  top: 0;
  width: 1.125rem;

  .icon-wrapper {
    height: 100%;
    padding: .25rem;
  }

  .icon {
    border: solid $orange;
    border-width: 0 3px 3px 0;
    display: inline-block;
    padding: 3px;
  }

  &.inpage {
    &-next,
    &-before {
      z-index: 1;
    }

    &-before {
      left: 0;

      .icon {
        transform: rotate(135deg);
      }
    }

    &-next {
      right: 0;

      .icon {
        transform: rotate(-45deg);
      }
    }
  }
}
</style>
