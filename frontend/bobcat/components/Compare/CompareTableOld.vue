<template>
  <div data-test-id="compare-table-old">
    <div v-if="showHeader && tableData && tableData.models"
         ref="tableHeader"
         class="row row-scrolled no-gutters align-items-stretch position-relative transition-base"
         data-test-id="compare-table-old-header"
    >
      <div ref="shadowGradient"
           class="bg-gradient-light d-print-none d-md-none transition-base"
           data-test-id="compare-table-old-first-column"
           :class="FIRST_COLUMN_WIDTH_CLASS"
      />
      <LazyHydrate never>
        <div class="d-none d-md-flex transition-base" :class="FIRST_COLUMN_WIDTH_CLASS" />
      </LazyHydrate>

      <div v-for="model in tableData.models"
           :key="`specification-table-compare-header__${model.id}`"
           :data-test-id="`compare-table-old-header__${model.id}`"
           class="col"
      >
        <LazyHydrate never>
          <h5 class="compare-cell bg-black text-white rounded-top-8 border border-light mb-0 h-100 d-flex align-items-center">
            {{ model.title || model.id || model['@name'] }}
            <span v-if="model.lifecycle && model.lifecycle.toLowerCase() === 'historical' && model.yearEnd" class="ml-1">({{ model.yearEnd }})</span>
          </h5>
        </LazyHydrate>
      </div>
    </div>

    <template v-if="tableData && tableData.sections">
      <div v-for="(s, i) in tableData.sections" :key="`specification-table-item__${i}`" :data-test-id="`compare-table-old-item__${i}`" class="row">
        <div v-show="Object.keys(s.properties || {}).length" class="col-12">
          <ProductSpecItemOld :opened="expaneded"
                              :title="s.title"
                              :properties="s.properties"
                              :keys="tableData.models.map(m => m.id)"
                              :first-column-class="FIRST_COLUMN_WIDTH_CLASS"
                              :show-header="showCategoryHeader"
                              :hydrate="showCategoryHeader"
                              :no-bottom-margin="noBottomMargin"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import {onMounted, ref} from '@nuxtjs/composition-api'

export default {
  props: {
    expaneded: {
      type: Boolean,
      default: true
    },
    tableData: {
      type: Object,
      required: true
    },
    showHeader: {
      type: Boolean,
      default: false
    },
    showCategoryHeader: {
      type: Boolean,
      default: true
    },
    noBottomMargin: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const {tableData, showHeader} = props
    const tableHeader = ref()
    const shadowGradient = ref()
    const FIRST_COLUMN_WIDTH_CLASS = ref(tableData?.models?.length === 1 ? 'col-6 col-lg-7' : 'col-sticky col-md-2 col-lg-3 col-xl')

    if (process.client && showHeader && shadowGradient.value) {
      onMounted(() => {
        shadowGradient.value.style.height = `${tableHeader.value.offsetHeight}px`
      })
    }

    return {
      tableHeader,
      shadowGradient,
      FIRST_COLUMN_WIDTH_CLASS
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/tables';

$mobileFirstColWidthSqueezed: 25vw;
$mobileFirstColWidth: 35vw;
$lgColWidth: 185px;

.specification-compare-table-old {
  .bg-gradient-light {
    background: linear-gradient(to right, $white 10%, rgba(255, 255, 255, 0));

    .bg-gray-lighter & {
      background: linear-gradient(to right, $gray-100 10%, rgba(255, 255, 255, 0));
    }
  }

  &.wide {
    overflow: auto;

    &.squeezed .col-sticky {
      max-width: $mobileFirstColWidthSqueezed;
      font-size: $small-font-size;
    }

    .col-sticky {
      max-width: $mobileFirstColWidth;
      position: sticky;
      left: 0;
      top: 0;
      z-index: 1;
      margin-left: -$mobileFirstColWidth;

      @include media-breakpoint-up(md) {
        max-width: $mobileFirstColWidth;
      }
    }

    .compare-sort-down,
    .compare-header {
      position: sticky;
      left: 0;
      top: 0;
      z-index: 1;
    }

    .compare-sort-down {
      left: unset;
      right: 0;
    }

    .col-sticky,
    .compare-header {
      &:before {
        content: '';
        position: absolute;
        left: - ($grid-gutter-width);
        top: 0;
        bottom: 0;
        width: $grid-gutter-width;
        background: $white;

        .bg-gray-lighter & {
          background:$gray-100;
        }
      }
    }

    .specification-compare-table__container {
      @for $i from 2 through 48 {
        &-#{$i} {
          min-width: ($i+1)*$mobileFirstColWidth;

          @include media-breakpoint-up(md) {
            min-width: ($i+1)*$mobileFirstColWidth*.675;
          }

          @include media-breakpoint-up(lg) {
            min-width: ($i+1)*$lgColWidth;
          }
        }
      }
    }

    .row-scrolled {
      .col-sticky {
        @include media-breakpoint-up(md) {
          width: 0;
        }
      }
      margin-left: $mobileFirstColWidth;
    }
  }

  .rounded-top-8 {
    border-top-left-radius: 8px !important;
    border-top-right-radius: 8px !important;
  }
}
</style>
