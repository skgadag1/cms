<template>
  <header class="bg-black d-flex align-items-center main-header flex-shrink-0">
    <b-col cols="12">
      <b-row>
        <b-navbar toggleable="md" :container="false" class="navbar-dark text-white w-100">
          <b-navbar-brand :href="mainPage" class="col-auto align-items-center px-4 bg-primary d-none d-md-flex h-100">
            <b-img alt="Bobcat logo"
                   height="35"
                   data-test-id="header-brand-desktop"
                   :src="assetServer + '/assets/img/logo.png'"
            />
          </b-navbar-brand>

          <b-navbar-toggle target="bobcat-nav__mobile" data-test-id="header-nav-toggle" />

          <MainDesktopNavigation :menuitems="menuitems" />

          <b-navbar-brand href="/"
                          class="col align-items-center px-4 d-flex d-md-none justify-content-center"
                          data-test-id="header-brand-mobile"
          >
            <b-img alt="Bobcat logo" :src="assetServer + '/assets/img/logo.png'" height="35" />
          </b-navbar-brand>

          <div class="col-auto col-md align-items-center d-none d-md-flex justify-content-end invisible">
            <b-icon icon="search" class="text-gray-500" />
          </div>

          <ProfileMenu v-if="translatedConfig('user')" :user="translatedConfig('user')" />
        </b-navbar>

        <MainMobileNavigation :menuitems="menuitems" />
      </b-row>
    </b-col>
  </header>
</template>

<script>
import ProfileMenu from '@/components/common/ProfileMenu'
import MainDesktopNavigation from '@/components/common/Navigation/MainDesktopNavigation'
import MainMobileNavigation from '@/components/common/Navigation/MainMobileNavigation'
import {getMainNavigationStructure} from '@/utils'
import {USER_SAVE, SERIAL_REMOVE} from '@/store/mutations'
import {contentService} from '@/services'
import {useLabels} from '@/composables/useLabels'

export default {
  name: 'Header',
  setup() {
    return useLabels()
  },
  components: {
    ProfileMenu,
    MainMobileNavigation,
    MainDesktopNavigation
  },
  data() {
    return {
      componentConfig: this.config,
      menuitems: []
    }
  },
  computed: {
    mainPage() {
      return this.componentConfig.mainPage || '/'
    }
  },
  created() {
    this.getMenuItems()
  },
  beforeMount() {
    this.updateCurrentUser()
  },
  methods: {
    getMenuItems() {
      contentService.fetchSitemap().then(r => {
        this.menuitems = getMainNavigationStructure(r.data)
      })
    },
    updateCurrentUser() {
      if (this.componentConfig && this.componentConfig.user) {
        this.$store.commit(USER_SAVE, this.componentConfig.user)
        this.$store.commit(SERIAL_REMOVE, {serial_number: null})
      }
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';
@import '../../scss/grid';
@import '../../scss/z-index';

header.main-header {
  position: relative;
  background: $black;
  z-index: $zindex-modal-backdrop - 5;

  input {
    transition: .25s all ease-in-out !important;
    opacity: 1;

    &.hidden {
      width: 0 !important;
      opacity: 0;
      position: absolute;
      z-index: -1;
    }
  }

  .badge {
    top: -.25rem;
    right: -.25rem;
  }
}

@include media-breakpoint-up('md') {
  .bg-md-dark {
    background-color: $gray-800;
  }
}
</style>
