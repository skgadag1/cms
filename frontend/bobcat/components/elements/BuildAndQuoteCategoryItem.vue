<template>
  <div class="category-item mb-3" data-test-id="build-quote-category-item">
    <template v-if="subcategories.length">
      <div class="bg-white category-item__header rounded" data-test-id="build-quote-category-item__header">
        <div v-b-toggle="`build-quote-category-item__${category['@id']}`"
             role="button"
             class="row px-4 align-items-center"
             data-test-id="build-quote-category-item__header-div"
        >
          <LazyHydrate never>
            <h3 class="col-10 col-md-7 py-4 mb-0 text-uppercase" data-test-id="build-quote-category-item__title">
              {{ category.title || category['@name'] }}
            </h3>
          </LazyHydrate>
          <LazyHydrate never>
            <div class="d-none d-md-flex col-md justify-content-end justify-content-md-center align-items-center">
              <ImageSet v-if="categoryImage.src || !inEditorEdit"
                        :src="categoryImage.src"
                        :options="{}"
                        :img-width="categoryImage.width"
                        :img-height="categoryImage.height"
                        class="category-item__image"
                        data-test-id="category-item__image"
              />
              <WarningAlert v-else-if="inEditorEdit">No <strong>image</strong> defined for category</WarningAlert>
            </div>
          </LazyHydrate>
          <div
            class="col col-md-auto d-flex align-items-center justify-content-end justify-content-md-start dropdown-toggle"
          />
        </div>
      </div>

      <b-collapse :id="`build-quote-category-item__${category['@id']}`"
                  class="category-item__collapse bg-white shadow-sm p-4 rounded"
      >
        <div class="row align-items-stretch">
          <div v-for="s in subcategories" :key="`subcategory__${s['@id']}`" class="col-6 col-md-4 col-lg-3 mb-3">
            <b-card no-header
                    class="h-100 text-decoration-none"
                    tag="a"
                    :href="s.pageUrl.url"
                    :target="s.pageUrl.target === '_blank_follow' ? '_blank' : s.pageUrl.target"
                    :rel="s.pageUrl.target === '_blank' ? 'noopener nofollow' : null"
                    :data-test-id="`build-quote-subcategory-item__${s['@id']}`"
                    @click="() => ga4click(s.title || s['@name'])"
            >
              <b-card-header v-if="inEditorEdit && !s.pageUrl.url" data-test-id="subcategory-error_nourl">
                <WarningAlert>No <strong>url</strong> configured for subcategory</WarningAlert>
              </b-card-header>

              <ImageSet v-if="s.image.src"
                        :src="s.image.src"
                        :img-width="s.image.width"
                        :img-height="s.image.height"
                        class="card-img-top"
                        data-test-id="subcategory-item__image"
              />
              <WarningAlert v-else-if="inEditorEdit" data-test-id="subcategory-error_noimage">
                No <strong>image</strong>
                defined for subcategory
              </WarningAlert>

              <template #footer>
                <span class="font-weight-bold text-dark" v-html="s.title || s['@name']" />
              </template>
            </b-card>
          </div>
        </div>
      </b-collapse>
    </template>

    <div v-else class="bg-white category-item__header rounded" data-test-id="build-quote-category-item__header">
      <a :href="categoryUrl"
         :target="categoryUrlTarget === '_blank_follow' ? '_blank' : categoryUrlTarget"
         :rel="categoryUrlTarget === '_blank' ? 'noopener nofollow' : null"
         role="button"
         class="row px-4 align-items-center text-decoration-none"
         data-test-id="build-quote-category-item__header-link"
         @click="() => ga4click(category.title || category['@name'])"
      >
        <h3 class="col-10 col-md-7 py-4 mb-0 text-uppercase text-dark" data-test-id="build-quote-category-item__title">
          {{ category.title || category['@name'] }}</h3>
        <div class="d-none d-md-flex col-md justify-content-end justify-content-md-center align-items-center">
          <ImageSet v-if="categoryImage.src || !inEditorEdit"
                    :src="categoryImage.src"
                    :options="{}"
                    class="category-item__image"
                    data-test-id="category-item__image"
          />
          <WarningAlert v-else-if="inEditorEdit">No <strong>image</strong> defined for category</WarningAlert>
        </div>
        <div
          class="col col-md-auto d-flex align-items-center justify-content-end justify-content-md-start dropdown-toggle text-dark"
        />
      </a>
    </div>
  </div>
</template>

<script>
import {onMounted, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  props: {
    category: {
      type: Object,
      required: true
    },
    url: {
      type: String
    }
  },
  setup(props) {
    const {category, url} = props
    const {$utils, $regions, $ga4} = useContext()
    const categoryImage = {
      src: category.image?.src,
      width: category.image?.imageWidth || null,
      height: category.image?.imageHeight || null
    }
    const categoryUrl = url && category?.id ? url.replace('%ID%', category.id) : (category.pageLink ? $utils.getPageUrl(category, $regions.current.language.name) : null)
    const categoryUrlTarget = url && category?.id ? '_blank' : category?.pageLink?.link?.target || '_self'

    const subcategories = category['@nodes']?.filter(x => !!category[x].id).map((x) => {
      const subcategoryPageLink = category[x].pageLink ? $utils.getLinkField(category[x].pageLink, $regions.current.language.name) : null
      const subcategoryUrlObject = url
        ? {
          url: category[x].id ? url.replace('%ID%', category[x].id) : null,
          target: '_blank'
        }
        : null

      return {
        ...category[x],
        image: {
          src: category[x].image?.src,
          width: category[x].image?.imageWidth || null,
          height: category[x].image?.imageHeight || null
        },
        pageUrl: subcategoryUrlObject || subcategoryPageLink || {}
      }
    }).filter(c => c['@nodeType'] === 'dxp:subcategory') || []

    const ga4click = (itemName) => {
      $ga4?.events?.form_submission_attempt('Build and Quote')
      $ga4?.events?.form_step('Build and Quote', 'Select Equipment Type', itemName)
    }

    onMounted(() => {
      if (!categoryImage?.src) {
        $ga4?.events?.form_submission_failure('Build and Quote', 'No image defined for category')
      }

      if (subcategories?.find(s => !s.pageUrl.url)) {
        $ga4?.events?.form_submission_failure('Build and Quote', 'No url configured for one of subcategory')
      }
    })

    return {
      subcategories,
      categoryImage,
      categoryUrl,
      categoryUrlTarget,
      inEditorEdit,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '{.shadow-sm, .shadow, .text-primary, .d-flex .flex-column, .align-items-center, .justify-content-center} from ~bootstrap/scss/bootstrap';

.category-item {
  &__header {
    @extend .shadow-sm;

    &:hover {
      @extend .shadow;
      @extend .text-primary;

      & > a > * {
        color: $primary !important;
      }
    }
  }

  &__image {
    height: 150px;
    visibility: hidden;

    &[src] {
      visibility: visible;
    }

    @include media-breakpoint-down('md') {
      height: 100px;
    }
  }

  &__collapse {
    margin-top: 1px;
  }

  .card {
    &-footer {
      border-top: 0;
    }

    &-body {
      padding: 0;
      @extend .d-flex;
      @extend .flex-column;
      @extend .align-items-center;
      @extend .justify-content-center;
    }
  }
}
</style>
