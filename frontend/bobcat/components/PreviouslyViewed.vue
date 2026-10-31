<template>
  <aside class="bg-gray-lighter" data-test-id="previously-viewed">
    <b-container v-show="hasCookie || viewedPages.length" class="py-5">
      <transition name="fade" appear>
        <b-row>
          <b-col v-if="title" cols="12" data-test-id="previously-viewed__title">
            <div class="border-bottom mb-4">
              <component :is="htag">{{ title }}</component>
            </div>
          </b-col>
        </b-row>
      </transition>

      <div v-if="hasCookie || viewedPages.length" class="previously-viewed__container py-3 py-lg-0 mt-n3 mt-lg-0">
        <b-row align-v="stretch">
          <client-only>
            <b-col v-for="(page, idx) in viewedPages" :key="`previously-viewed-page__${page.id}`" cols="3">
              <transition name="fade-zoom" appear>
                <a :href="page.url"
                   :title="page.title"
                   :style="{ transitionDelay: `${idx * 75}ms` }"
                   class="card text-dark h-100"
                   data-test-id="previously-viewed__item"
                >
                  <ImageSet :alt="page.image.alt || page.image.title || page.title"
                            :title="page.image.title || page.image.alt || page.title"
                            :src="page.image.src"
                            width="300"
                            height="200"
                  />

                  <b-card-body body-class="p-3">
                    <b-card-title title-tag="div" :title="page.title" class="mb-0 h6" data-test-id="previously-viewed__item-title" />
                  </b-card-body>
                </a>
              </transition>
            </b-col>
          </client-only>

          <b-col v-if="hasCookie" key="previously-viewed-page__invisible" class="invisible" cols="3">
            <div class="card text-dark h-100">
              <ImageSet />

              <b-card-body body-class="p-3">
                <b-card-title title-tag="div" class="mb-0 h6">&nbsp;<br>&nbsp;</b-card-title>
              </b-card-body>
            </div>
          </b-col>
        </b-row>
      </div>
    </b-container>
  </aside>
</template>

<script>
import {onMounted, ref, useContext} from '@nuxtjs/composition-api'
import * as useViewedPagesModule from '~/helpers/composables/useViewedPages'
import useMounted from '~/helpers/composables/useMounted'

export default {
  name: 'PreviouslyViewed',
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const viewedPages = ref([])
    const {$utils, $regions, $cookies} = useContext()
    const hasCookie = ref($cookies?.get(`${useViewedPagesModule.PREVIOUSLY_VISITED_COOKIE}`) === $regions?.current?.name)
    useMounted(props.nodeId)

    if (process.client) {
      onMounted(() => {
        const {
          getPagesForRegion
        } = useViewedPagesModule.default()

        viewedPages.value = getPagesForRegion($regions?.current?.name).map((p) => {
          return {
            id: p['@id'] || p['@name'],
            title: p.title || p['@name'],
            url: p.pageUrl,
            image: {
              src: $utils.getCloudinaryAsset(p?.image?.src || p?.primaryPageImage?.src, true, {crop: 'fill', width: 300, height: 200}),
              alt: p?.image?.alt || p?.primaryPageImage?.alt,
              title: p?.image?.title || p?.primaryPageImage?.title
            }
          }
        })
        hasCookie.value = false
      })
    }

    return {
      viewedPages,
      hasCookie
    }
  }
}
</script>

<style lang="scss" scoped>
@use 'sass:math';
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.previously-viewed__container {
  @include media-breakpoint-down(md) {
    overflow-x: scroll;
    margin-right: - math.div($grid-gutter-width, 2);
    margin-left: - math.div($grid-gutter-width, 2);
    padding-left: math.div($grid-gutter-width, 2);

    & > .row {
      min-width: 150vw;
      width: 150vw;
    }
  }

  @include media-breakpoint-down(sm) {
    & > .row {
      min-width: 275vw;
      width: 275vw;
    }
  }
}
</style>
