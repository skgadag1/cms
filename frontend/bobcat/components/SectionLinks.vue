<template>
  <LazyHydrate never>
    <section v-if="allLinks && allLinks.length" class="container" data-test-id="section-links">
      <div class="row">
        <div class="col-12">
          <div class="d-flex flex-column flex-md-row justify-content-center align-items-center">
            <div v-for="(link, i) in allLinks"
                 :key="`${link.label}_${i}`"
                 class="link-wrapper text-primary h5 d-flex flex-md-row flex-column w-md-auto align-items-center m-0"
            >
              <div class="d-flex align-items-center py-md-0 py-5">
                <b-icon v-if="link.icon" :icon="link.icon" class="mr-1" />
                <a
                  :href="link.url"
                  :target="link.target === '_blank_follow' ? '_blank' : link.target"
                  :rel="link.target === '_blank' ? 'noopener nofollow' : null"
                  data-test-id="error-link"
                >
                  {{ link.label }}
                </a>
              </div>
              <span v-if="i !== allLinks.length - 1" class="separator mx-md-6 bg-gray-light" />
            </div>
          </div>
        </div>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'

export default {
  props: {
    links: {
      type: Object
    }
  },
  setup(props) {
    const {$regions, $utils} = useContext()
    const {links} = props
    const allLinks = ref([])

    allLinks.value = links
      ? links['@nodes']?.map(x => ({
        ...$utils.getLinkField(links[x].link, $regions.current.language.name),
        icon: links[x]?.icon
      }
      ))
      : []

    return {
      allLinks
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

section[data-test-id='section-links'] {
  .separator {
    width: 1px;
    height: 1.5rem;
  }

  @include media-breakpoint-down(sm) {
    .separator {
      width: 100%;
      height: 1px;
    }

    .link-wrapper {
      width: 100%;
    }
  }
}
</style>
