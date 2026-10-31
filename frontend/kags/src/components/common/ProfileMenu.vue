<template>
  <b-col cols="auto"
         class="align-items-center d-flex h-100 bg-md-dark"
         :class="{ 'bg-dark' : isOpened }"
  >
    <b-col cols="12" tag="span" class="p-1">
      <div class="cursor-pointer" @click="toggleProfile">
        <div class="position-relative d-inline-block">
          <div class="rounded-circle bg-gray-100 d-inline-flex align-items-center justify-content-center" style="width: 30px; height: 30px;"><b-icon icon="user" class="text-gray-400" /></div>
          <!--<span class="badge text-bg-primary rounded-circle position-absolute">2</span>-->
        </div>
        <span class="d-none d-md-inline-flex align-items-center ms-3 text-gray-500 small">
          {{ user.fullName }}
          <b-icon v-if="!isOpened" icon="sort-down" class="ms-2 mb-1" />
          <b-icon v-else icon="sort-up" class="ms-2 mt-1 pt-1" />
        </span>
      </div>

      <div v-show="isOpened" id="menu-profile" class="menu-profile bg-gray-800 py-3">
        <b-col cols="12" tag="ul" class="list-unstyled font-condensed">
          <b-nav-item class="bg-xs-black position-relative"
                      :href="logoutUrl"
          >
            <b-icon :icon="['far', 'sign-out-alt']"
                    class="text-white"
                    flip="horizontal"
            />
            <span class="ms-2 text-white">{{ translate("logoutLabel", 'Header') }}</span>
          </b-nav-item>
        </b-col>
      </div>

      <transition v-if="!isMagnoliaEditMode" name="fade">
        <div v-show="isOpened"
             class="modal-backdrop"
             style="z-index: -1;"
             @click="toggleProfile"
        />
      </transition>
    </b-col>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
export default {
  conceptName: ['Header'],
  setup() { return useLabels() },
  props: {
    user: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      isOpened: false
    }
  },
  computed: {
    logoutUrl() {
      return window.location.href.split('?')[0].split('#')[0] + '?mgnlLogout=true'
    }
  },
  methods: {
    toggleProfile() {
      this.isOpened = !this.isOpened
      if (this.isOpened) document.getElementsByTagName('html')[0].classList.add('menu-profile_opened')
      else document.getElementsByTagName('html')[0].classList.remove('menu-profile_opened')
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';
@import '../../scss/grid';

.menu-profile {
  position: fixed;
  right: 0;
  left: 0;
  bottom: 0;
  top: 3.75rem;

  @include media-breakpoint-up('md') {
    left: auto;
    width: 18rem;
  }

  &_opened {
    body {
      height: 100vh;
      overflow: hidden;
    }
  }

  .nav-item {
    &:hover {
      &:after {
        content: "";
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 0.25rem;
        background-color: #ff3600;
        z-index: -1;
      }
    }
  }
}

@include media-breakpoint-down('md') {
  .bg-xs-black {
    background-color: $black;
  }
}
</style>
