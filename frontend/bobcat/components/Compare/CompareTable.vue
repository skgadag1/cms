<template>
  <div data-test-id="compare-table">
    <template v-if="tableData && tableData.sections">
      <div v-for="(s, i, index) in tableData.sections"
           :key="`specification-table-item__${i}`"
           :data-test-id="`compare-table-item__${i}`"
           class="row"
      >
        <div v-show="Object.keys(s.properties || {}).length" class="col-12">
          <component :is="showCategoryHeader ? 'Accordion' : 'Div'"
                     data-test-id="compare-table-header"
                     :is-expanded="expaneded"
                     color="black"
                     :title="s.title"
                     class="mb-2 collapse-position-relative"
                     :class="{'shown shadow': !showCategoryHeader}"
          >
            <div data-test-id="compare-wide-container" class="max-left" :class="{wide: isWide}">
              <div class="sticky-header z-index-3">
                <div class="scroll-buttons d-none z-index-3">
                  <div class="d-md-none d-inline-flex justify-content-end py-1 pr-2">
                    <div class="shadow-button left-button bg-white mr-2">
                      <b-button class="p-2 d-flex align-content-center" @click="scroll(index, 'left')">
                        <b-icon style="transform: rotate(180deg)" icon="chevron-right rot" />
                      </b-button>
                    </div>
                    <div class="shadow-button right-button bg-white">
                      <b-button class="p-2 d-flex align-content-center" @click="scroll(index, 'right')">
                        <b-icon icon="chevron-right" />
                      </b-button>
                    </div>
                  </div>
                </div>
                <div class="overflow-hidden">
                  <div class="table-shadow position-absolute" />
                  <CompareTableHeader :models="tableData.models" :data-test-id="`compare-table__header-${tableData.models.length}`" :class="`specification-compare-table__items-${tableData.models.length}`" />
                </div>
              </div>
              <ProductSpecItem ref="tableItems"
                               :properties="s.properties"
                               :keys="tableData.models.map(m => m.id)"
                               :first-column-class="FIRST_COLUMN_WIDTH_CLASS"
                               :no-bottom-margin="noBottomMargin"
              />
            </div>
          </component>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import {
  onBeforeMount,
  onBeforeUnmount,
  ref,
  toRef,
  useContext,
  watch
} from '@nuxtjs/composition-api'
import debounce from 'lodash/debounce'
import Accordion from '~/components/Accordion.vue'

export default {
  components: {
    Accordion
  },
  props: {
    expaneded: {
      type: Boolean,
      default: false
    },
    tableData: {
      type: Object,
      required: true
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
    const {expaneded} = props
    const {$utils} = useContext()
    const tableItems = ref(null) // scrollable element
    const isWide = ref(false)
    const tableHeader = ref()
    const tableExpanded = ref(expaneded)
    const FIRST_COLUMN_WIDTH_CLASS = ref('col-sticky col-md')
    const tableDataRef = toRef(props, 'tableData')

    if (process.client) {
      const checkSize = debounce(() => {
        if ($utils.isMobile()) {
          isWide.value = tableDataRef.value.models?.length > 2
        } else if ($utils.isTablet()) {
          isWide.value = tableDataRef.value.models?.length > 3
        } else {
          isWide.value = tableDataRef.value.models?.length > 5
        }
      }, 500)

      onBeforeMount(() => {
        window.addEventListener('resize', checkSize, {passive: true})
      })

      onBeforeUnmount(() => {
        window.removeEventListener('resize', checkSize)
      })

      checkSize()
      watch(tableDataRef, () => checkSize())
    }

    const scroll = (table, direction) => {
      const columnWidth = ((window.innerWidth * 0.3) * (tableDataRef.value?.models?.length + 1) - 128) / tableDataRef.value?.models?.length
      const tableElement = tableItems.value?.[table].$el
      if (direction === 'right') {
        const moveBy = columnWidth - tableElement.scrollLeft % columnWidth
        tableElement.scrollTo({behavior: 'smooth', left: tableElement.scrollLeft + (moveBy < 1 ? columnWidth : moveBy)})
      } else {
        const moveBy = tableElement.scrollLeft % columnWidth
        tableElement.scrollTo({behavior: 'smooth', left: tableElement.scrollLeft - (moveBy < 1 ? columnWidth : moveBy)})
      }
    }

    return {
      tableExpanded,
      tableHeader,
      FIRST_COLUMN_WIDTH_CLASS,
      scroll,
      tableItems,
      isWide
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/tables';
@import '~/assets/sass/_colors';
@import '{.d-block} from ~bootstrap/scss/bootstrap';

$mobileFirstColWidth: 30vw;
$lgColWidth: 185px;

$transparent: rgba(0, 0, 0, 0);
$background: $white;
$shadow: black;

body {
  .sticky-header {
    top: 7.5rem;
    transition: $transition-base;
  }

  &.tablet {
    .sticky-header {
      top: 3.5rem;
    }
  }

  &.tablet.region-visible {
    .sticky-header {
      top: 6.5rem;
    }
  }

  &.mobile {
    .sticky-header {
      top: 6.5rem;
    }
  }

  &.mobile.region-visible {
    .sticky-header {
      top: 9.5rem;
    }
  }
}

.product-listing-compare-table,
.manual-table-spec,
.content-limiter.opened {
  .shown {
    .sticky-header {
      position: sticky;

    }
  }
}

.collapse-position-relative {
  .collapse {
    position: relative;
  }
}

.specification-compare-table {

  .col-sticky {
    position: sticky;
    left: 0;
    top: 0;
    z-index: 1;

    @include media-breakpoint-down(sm) {
      background: $white;
      font-size: $small-font-size;
      color: $black;
      font-family: $font-family-base;
    }

    @include media-breakpoint-up(md) {
      background: $white;
    }

    > .compare-cell {
      max-width: calc(100vw - #{$grid-gutter-width});
      position: sticky;
      left: 0;
      top: 0;
      z-index: 1;
    }
  }

  .scroll-space {
    flex-basis: 8rem;
    flex-grow: 0;
  }

  .wide {
    .col-sticky {
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
          background: $gray-100;
        }
      }
    }

    @media screen {
      .table-shadow {
        top: 0;
        width: 6rem;
        bottom: 0;
        right: 0;
        opacity: 1;
        pointer-events: none;
        transition: $transition-base;
        background: linear-gradient(to left, $white 0%, rgba(255, 255, 255, 0));
      }
    }

    .specification-compare-table {
      &__header {
        display: flex !important;
        position: sticky;
      }

      &__container {
        .table-shadow {
          @include media-breakpoint-up(md) {
            bottom: 1.75rem;
          }
        }
      }

      &__items {
        @for $i from 2 through 48 {
          @media screen {
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
      }
    }

    .row-scrolled {
      .col-sticky {
        @include media-breakpoint-up(md) {
          width: 0;
        }
      }

    }

    .scroll-buttons {
      @extend .d-block;
      position: absolute;
      right: 0;

      .btn {
        min-width: auto;
      }

      .shadow-button {
        transition: $transition-base;
        opacity: 1;
        box-shadow: 0 .5rem 1rem 0 rgba(0, 0, 0, 0.2);
        z-index: 1;
      }

      i.bobcat-icon-chevron-right {
        font-size: 1.5rem;
      }

      @media print {
        display: none!important;
      }
    }

    .scroll-space {
      @media screen {
        @include media-breakpoint-down(sm) {
          display: block!important;
        }
      }
    }

    &.max-left {
      .shadow-button.left-button {
        opacity: 0;
      }
    }

    &.max-right {
      .table-shadow {
        opacity: 0;
      }

      .shadow-button.right-button {
        opacity: 0;
      }
    }
  }

  &__container {
    background: white;
  }
}
</style>
