<template>
  <b-card border-variant="transparent"
          class="overflow-hidden h-100 transition-base border"
          no-body
          footer-tag="footer"
  >
    <b-card-body class="d-flex flex-column p-2 m-1 m-md-0 p-md-3" data-test-id="text-listing-item__content">
      <div class="d-flex justify-content-between">
        <div class="mb-2">
          <b-card-text class="text-listing-item-title mb-1" data-test-id="text-listing-item-title" :title-tag="item.htag || 'h5'">
            <a :href="item.url" class="" v-html="item.title" />
          </b-card-text>
        </div>
        <div v-if="item.image" class="d-md-none">
          <ImageSet :src="imageUrl"
                    :alt="imageAlt"
                    :title="imageTitle"
                    :img-width="imageWidth"
                    :img-height="imageHeight"
                    :sizes="[THUMB_SIZES.mobile]"
                    :aspect-ratio="3 / 2"
                    :options="{width: THUMB_SIZES.mobile.w, height: THUMB_SIZES.mobile.h}"
          />
        </div>
      </div>

      <div class="row">
        <div class="col-12 d-flex">
          <div v-if="item.image" class="d-none d-md-flex mr-3">
            <ImageSet :src="imageUrl"
                      :alt="imageAlt"
                      :title="imageTitle"
                      :img-width="imageWidth"
                      :img-height="imageHeight"
                      :sizes="[THUMB_SIZES.default]"
                      :aspect-ratio="3 / 2"
                      :options="{width: THUMB_SIZES.default.w, height: THUMB_SIZES.default.h}"
            />
          </div>
          <div>
            <b-card-text v-if="item.pageType || item.breadcrumbs" text-tag="div">
              <b-badge
                v-if="item.pageType"
                class="text-listing-item-badge font-weight-light text-gray-darker small mr-1 mb-2 p-2"
                data-test-id="text-listing-item-badge"
                variant="light"
              >
                {{ item.pageType }}
              </b-badge>
              <span v-if="item.breadcrumbs" class="text-gray-darker small font-weight-light" data-test-id="text-listing-item-breadcrumb">{{ item.breadcrumbs }}</span>
            </b-card-text>
            <b-card-text v-if="item.content" data-test-id="text-listing-item-description" v-html="item.content" />
          </div>
        </div>
      </div>
    </b-card-body>
  </b-card>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      required: true
    },
    labels: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props) {
    const {item} = props

    const THUMB_SIZES = {
      mobile: {w: 72, h: 48},
      default: {w: 126, h: 84}
    }

    const imageUrl = item.image && item.image.src
    const imageAlt = item.image?.imageAlt || ''
    const imageTitle = item.image?.imageTitle || ''
    const imageWidth = item.image?.imageWidth || null
    const imageHeight = item.image?.imageHeight || null

    return {
      THUMB_SIZES,
      imageUrl,
      imageAlt,
      imageTitle,
      imageWidth,
      imageHeight
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/colors';

.text-listing-item-badge{
  font-size: $font-size-sm;
}

.text-listing-item-title {
  text-decoration: underline;
  text-decoration-color: $primary;
}
</style>
