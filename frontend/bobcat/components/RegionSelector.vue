<template>
  <LazyHydrate never>
    <section data-test-id="region-selector">
      <div data-test-id="region-selector-nav" class="region-selector-nav pt-3 position-relative bg-white shadow">
        <div class="container">
          <div class="row">
            <div class="col-12 d-flex flex-column align-items-center">
              <img v-if="logoObj.src"
                   height="38"
                   width="140"
                   class="w-auto"
                   data-test-id="region-selector-nav-logo"
                   :alt="logoObj.alt"
                   :title="logoObj.title"
                   :src="logoObj.src"
                   loading="lazy"
                   decoding="async"
              >
              <component :is="htag" v-if="title" data-test-id="region-selector-nav-title">{{ title }}</component>
            </div>
          </div>
        </div>
      </div>
      <div class="gray-container d-flex flex-column justify-content-between bg-gray-lighter pb-3 pt-5">
        <div class="container" data-test-id="region-selector-grid">
          <div class="row">
            <div class="col-12">
              <div v-if="caption" class="d-flex justify-content-center">
                <component :is="htagCaption" data-test-id="region-selector-caption">{{ caption }}</component>
              </div>
            </div>
            <div class="card-columns w-100 px-3">
              <div v-for="region in allRegions" :key="region['@id']" :style="`background-image: url('${region.asset}');`" class="bg-image bg-white mt-1 card shadow">
                <div class="justify-content-end d-flex" data-test-id="region-selector-grid-item">
                  <div class="py-4 pl-4 col-6" />
                  <div class="py-4 pr-4 col-6 col-overlay">
                    <h4>{{ region.title }}</h4>
                    <span v-for="link in region.links" :key="link['@id']" class="d-flex align-items-center mb-3">
                      <a class="text-black grid-item-link mr-2" :rel="link.target === '_blank' ? 'noopener nofollow' : null" :href="link.url" :target="link.target === '_blank_follow' ? '_blank' : link.target">{{ link.label }}</a>
                      <span class="arr" />
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="footerContent" data-test-id="region-selector-footer" class="container py-4">
          <div class="row">
            <div class="col-12 d-flex justify-content-center" v-html="footerContent" />
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {useContext, ref} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'

export default {
  name: 'RegionSelector',
  serverCacheKey: () => 'global-regions',
  props: {
    nodeId: {
      type: String
    },
    footerContent: {
      type: String
    },
    title: {
      type: String
    },
    caption: {
      type: String
    },
    logo: {
      type: Object
    },
    regions: {
      type: Object,
      required: true
    },
    htag: {
      type: String,
      default: 'h6'
    },
    htagCaption: {
      type: String,
      default: 'h6'
    }
  },
  setup(props) {
    const {$utils, $regions} = useContext()
    const {logo, title} = props
    const logoObj = ref({
      src: $utils.getCloudinaryAsset(logo?.src, true, {height: 38}),
      title: logo?.imageTitle || title,
      alt: logo?.imageAlt
    })
    const currentLanguage = ref($regions?.current?.language?.name)
    useMounted(props.nodeId)

    const allRegions = ref([])

    allRegions.value = props.regions
      ? props.regions['@nodes']?.map(x => ({
        ...props.regions[x],
        asset: $utils.getCloudinaryAsset(props.regions[x]?.asset, true, {width: 382}),
        links: props.regions[x]?.links
          ? props.regions[x]?.links['@nodes']?.map(link => $utils.getLinkField(props.regions[x]?.links[link].link, currentLanguage.value))
          : []
      }))
      : []

    return {
      logoObj,
      allRegions
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~/assets/sass/utils';

.card-columns {
  column-count: 1;

  .card {
    border: none;

    .col-overlay {
      background-color: rgba(255, 255, 255, 0.5);
    }

    a {
      &:hover {
        text-shadow: 0 1rem 1.5rem rgba(12, 24, 30, 0.2);
        font-weight: 700;
      }
    }
  }

  @include media-breakpoint-up(md) {
    column-count: 2;
  }

  @include media-breakpoint-up(lg) {
    column-count: 3;
  }
}

.gray-container {
  min-height: calc(100vh - 4.875rem);
  .bg-image {
    background-repeat: no-repeat;
    background-position: left center;
    background-size: cover;
  }
}

.region-selector-nav {
  button {
    border: none;
    right: 1rem;
    top: 1rem;
    cursor: pointer;
  }
}

.arr {
  width: .55rem;
  height: .55rem;
  border: solid $primary;
  border-width: 0 3px 3px 0;
  transform: rotate(-45deg);
  -webkit-transform: rotate(-45deg);
}

.grid-item-link {
  &:hover {
    color: $primary;
  }
}
</style>
