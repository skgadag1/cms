<template>
  <div ref="rootElement"
       class="overflow-auto specification-compare-table__container"
       data-test-id="evolution-compare-spec-rows"
  >
    <div class="table-shadow position-absolute z-index-2" />
    <div :class="`specification-compare-table__items-${keys.length}`">
      <div v-for="(entry, idx) in propertyEntries"
           :key="`evolution-compare-spec-row__${entry[0]}`"
           class="row row-scrolled no-gutters align-items-stretch"
           :class="{
             'rounded-bottom': idx === propertyEntries.length - 1,
             'pt-1 pt-md-2': idx > 0,
             'pb-1 pb-md-2': true,
             'evolution-compare-spec-row--has-diff': rowHasDifferences(entry[1])
           }"
           :data-test-id="`evolution-compare-spec-row__${entry[0]}`"
      >
        <template v-if="keys.length === 1 && !entry[1].label && entry[1][keys[0]] && entry[1][keys[0]].type === 'table'">
          <b-table :fields="entry[1][keys[0]].value.fields"
                   :items="entry[1][keys[0]].value.items"
                   class="mb-0"
                   thead-tr-class="bg-gray-lighter"
                   responsive
          />
        </template>
        <template v-else>
          <div class="col-12">
            <span data-spec-label
                  class="d-block small text-uppercase mb-1 mb-md-2 pl-3 pl-md-4"
            >{{ entry[1].label }}</span>
          </div>
          <div class="col-12 ec-compare-hscroll-wrap p-0 pl-3 pl-md-4"
               data-ec-compare-hscroll-wrap
          >
            <div class="evolution-compare-spec-row__values-scroll"
                 data-ec-compare-hscroll
            >
              <div class="evolution-compare-spec-row__values-row d-flex flex-nowrap flex-lg-wrap no-gutters w-100 mx-0">
                <div v-for="j in colNum"
                     :key="`evolution-compare-spec-row-col__${entry[0]}-${j}`"
                     class="col evolution-compare-spec-row__value-col"
                     :data-test-id="`evolution-compare-spec-row-col__${entry[0]}-${j}`"
                >
                  <div class="compare-cell d-flex flex-column justify-content-center h-100 text-break bg-white p-0"
                       :data-key-spec="isKeyRow(entry[1]) ? 'true' : undefined"
                       :data-first-key-spec="isKeyRow(entry[1]) && idx === 0 ? 'true' : undefined"
                  >
                    <template v-if="isCellEmpty(entry[1], keys[j - 1])">
                      <div class="d-flex align-items-center w-100 h-100 text-muted"
                           data-test-id="evolution-compare-spec-empty-cell"
                      >
                        <span class="user-select-none">-</span>
                      </div>
                    </template>
                    <template v-else-if="typeof entry[1][keys[j - 1]] === 'object'">
                      <div v-if="Array.isArray(entry[1][keys[j - 1]].value)" class="row no-gutters w-100">
                        <div v-for="(x, k) in entry[1][keys[j - 1]].value"
                             :key="`evolution-compare-spec-subval__${k}`"
                             class="col-12 col-md"
                             :class="{'pr-1' : k !== entry[1][keys[j - 1]].value.length - 1}"
                        >
                          <span class="d-block text-dark"
                                :class="rowHasDifferences(entry[1]) ? 'font-weight-bold' : 'font-weight-normal'"
                          >
                            <span v-html="x.value" /><template v-if="entry[1].unit || x.unit">&nbsp;{{ entry[1].unit || x.unit }}</template>
                          </span>
                        </div>
                      </div>
                      <template v-else-if="isKeyRow(entry[1])">
                        <span :class="['d-inline', 'mb-0', 'text-dark', idx === 0 ? 'evolution-compare-spec-key-title mb-1 mb-md-2' : '', rowHasDifferences(entry[1]) ? 'font-weight-bold' : 'font-weight-normal']">
                          <span v-html="entry[1][keys[j - 1]].value" /><template v-if="entry[1][keys[j - 1]].unit || entry[1].unit">&nbsp;{{ entry[1][keys[j - 1]].unit || entry[1].unit }}</template>
                        </span>
                      </template>
                      <template v-else>
                        <span class="d-block text-dark"
                              :class="rowHasDifferences(entry[1]) ? 'font-weight-bold' : 'font-weight-normal'"
                        >
                          <span v-html="entry[1][keys[j - 1]].value" /><template v-if="entry[1][keys[j - 1]].unit || entry[1].unit">&nbsp;{{ entry[1][keys[j - 1]].unit || entry[1].unit }}</template>
                        </span>
                      </template>
                    </template>
                    <div v-else
                         class="text-dark"
                         :class="rowHasDifferences(entry[1]) ? 'font-weight-bold' : 'font-weight-normal'"
                    >
                      <span v-html="String(entry[1][keys[j - 1]])" /><template v-if="entry[1].unit">&nbsp;{{ entry[1].unit }}</template>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import {computed, ref} from '@nuxtjs/composition-api'
import {cellToComparableString, rowEntirelySameAcrossModels} from '~/helpers/evolutionCompareSpecsFilter'

export default {
  props: {
    properties: {
      type: Object,
      required: true
    },
    keys: {
      type: Array,
      required: true
    }
  },
  setup(props) {
    const rootElement = ref(null)
    const colNum = computed(() => props.keys?.length || 1)

    const propertyEntries = computed(() => Object.entries(props.properties || {}))

    const isKeyRow = (row) => {
      const sample = props.keys.map(k => row[k]).find(c => c && typeof c === 'object')
      return sample?.type === 'number'
    }

    const rowHasDifferences = (row) => {
      const ids = props.keys || []
      if (ids.length < 2) {
        return false
      }
      return !rowEntirelySameAcrossModels(row, ids)
    }

    const isCellEmpty = (row, modelId) => {
      const cell = row[modelId]
      if (cell === undefined || cell === null) {
        return true
      }
      if (typeof cell === 'object') {
        return !String(cellToComparableString(cell, row.unit) || '').trim()
      }
      if (typeof cell === 'string') {
        return cell.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ').trim() === ''
      }
      return false
    }

    return {
      rootElement,
      colNum,
      propertyEntries,
      isKeyRow,
      rowHasDifferences,
      isCellEmpty
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/tables';
@import '~/assets/sass/_colors';

[data-test-id='evolution-compare-spec-rows'] > div > .row.row-scrolled {
  border-bottom: 1px solid $tertiary;
}

[data-test-id='evolution-compare-spec-rows'] > div > .row.row-scrolled.evolution-compare-spec-row--has-diff {
  border-left: 4px solid $primary;
}

[data-test-id='evolution-compare-spec-rows'] .evolution-compare-spec-key-title {
  font-size: 1.5rem;
  line-height: 1.2;

  @include media-breakpoint-up(md) {
    font-size: 1.75rem;
  }
}

@include media-breakpoint-down(lg) {
  [data-test-id='evolution-compare-spec-rows'].specification-compare-table__container {
    overflow-x: hidden;
  }

  [data-test-id='evolution-compare-spec-rows'] .evolution-compare-spec-row__values-scroll {
    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;
  }

  [data-test-id='evolution-compare-spec-rows'] .evolution-compare-spec-row__values-row {
    flex-wrap: nowrap !important;
    column-gap: 0.25rem;
  }

  [data-test-id='evolution-compare-spec-rows'] .evolution-compare-spec-row__value-col.col {
    flex: 0 0 auto;
    width: auto;
    max-width: none;
    min-width: min(68vw, 33.333333%);
  }
}

@include media-breakpoint-up(lg) {
  [data-test-id='evolution-compare-spec-rows'] .evolution-compare-spec-row__values-scroll {
    overflow-x: visible;
  }
}
</style>
