<template>
  <div class="d-flex flex-column w-100" data-test-id="tabs">
    <div ref="tabsContainer" class="tabs-container position-relative w-100" @scroll="checkScrollEnd">
      <div class="tab-overlay position-absolute h-100 z-index-3"
           :class="isEndOfScroll ? 'opacity-0' : 'opacity-5'"
           :style="{right: -scrollPosition + 'px'}"
      />
      <ul class="p-0 m-0 d-flex flex-nowrap">
        <li v-for="(tab, index) in tabs"
            :key="`${index}-${tab.title}`"
            class="single-tab-title flex-grow-0 flex-shrink-0 list-unstyled transition-base position-relative"
            :class="[index === activeTab ? 'active bg-white' : 'bg-gray-lighter', index === tabs.length - 1 ? 'mr-0' : 'mr-1']"
        >
          <button class="p-2 bg-transparent border-0 h4 m-0 text-dark" @click="switchTab(index, tab.title)">
            {{ tab.title }}
          </button>
        </li>
      </ul>
    </div>
    <slot />
  </div>
</template>

<script>
import {nextTick, onBeforeUnmount, onMounted, ref} from '@nuxtjs/composition-api'

export default {
  props: {
    tabs: {
      type: Array,
      required: true
    }
  },
  setup(props, {emit, slots}) {
    const activeTab = ref(0)
    const tabsContainer = ref(null)
    const isEndOfScroll = ref(true)
    const scrollPosition = ref(0)
    const slotRef = ref(slots.default())

    const checkScrollEnd = () => {
      const difference = Math.abs(tabsContainer.value.scrollWidth - tabsContainer.value.scrollLeft - tabsContainer.value.clientWidth)
      isEndOfScroll.value = (Math.round(difference / 5) * 5) === 0
      scrollPosition.value = tabsContainer.value?.scrollLeft
    }

    onMounted(async () => {
      await nextTick(function () {
        window.addEventListener('resize', checkScrollEnd, {passive: true})
        checkScrollEnd()
      })
      slotRef.value?.[0]?.child?.setActive(true)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', checkScrollEnd)
    })

    const switchTab = (index, title) => {
      activeTab.value = index
      emit('tabSwitchCallback', title)
      slotRef.value.forEach((tab, tabIndex) => {
        tab.child.setActive(tabIndex === index)
      })
    }
    return {activeTab, tabsContainer, checkScrollEnd, isEndOfScroll, scrollPosition, switchTab}
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

div[data-test-id="tabs"] {
  .single-tab-title {
    border-radius: $border-radius $border-radius 0 0;
  }

  .tabs-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scroll-snap-type: x mandatory;

    ul {
      scroll-padding-right: 50px; // hack for adjusting padding to prevent snap blocking on iOS
    }

    li {
      cursor: pointer;
      scroll-snap-align: end;

      &.active {
        &:before {
          background: $primary;
          transform: scaleX(1);
          transform-origin: left;
          transition: transform 0.3s ease, background 0.3s ease;
        }
      }

      &:after {
        content: '';
        position: absolute;
        pointer-events: none;
        width: 100%;
        height: 100%;
        opacity: 0;
        left: 0;
        box-shadow: 0 2px 6px rgba(0,0,0,0.5);
        transition: opacity 0.3s ease-in-out;
      }

      &:before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 4px;
        background: transparent;
        transform: scaleX(0);
        transform-origin: right;
        transition: transform 0.3s ease, background 0.3s ease;
        border-radius: $border-radius $border-radius 0 0;
      }

      /* Transition to showing shadow on hover */
      &:hover:after {
        opacity: 1;
      }
    }
  }

  ul {
    &:before {
      display: none;
    }
  }

  .tab-overlay {
    transition: $transition-fade;
    top: 0;
    right: 0;
    width: 2.75rem;
    box-shadow: inset -14px 0rem 17px 9px #d9d9d9;;
    pointer-events: none;
  }

}
</style>
