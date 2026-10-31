<template>
  <div ref="rootElement" class="overflow-auto specification-compare-table__container" data-test-id="product-spec-item">
    <div class="table-shadow position-absolute z-index-2" />
    <div :class="`specification-compare-table__items-${keys.length}`">
      <div v-for="(s, i) in properties"
           :key="`product-spec-item__${i}`"
           class="row row-scrolled no-gutters d-flex align-items-stretch transition-base"
           :data-test-id="`product-spec-item__${i}`"
           :class="{'rounded-bottom' : i === keys.length - 1}"
      >
        <template v-if="keys.length === 1 && !s.label && s[keys[0]].type === 'table'">
          <b-table :fields="s[keys[0]].value.fields"
                   :items="s[keys[0]].value.items"
                   class="mb-0"
                   thead-tr-class="bg-gray-lighter"
                   responsive
          />
        </template>
        <template v-else>
          <div :class="firstColumnClass" class="transition-base">
            <div class="compare-cell pb-4 pb-md-5 box-shadow-inset d-flex align-items-center h-100 text-break">{{ s.label }}</div>
          </div>
          <div v-for="j in colNum"
               :key="`product-spec-item-col__${i}-${j}`"
               :data-test-id="`product-spec-item-col__${i}-${j}`"
               class="col"
          >
            <div class="compare-cell pb-4 pb-md-5 bg-white font-weight-bold box-shadow-inset d-flex align-items-center h-100 text-break">
              <template v-if="typeof s[keys[j - 1]] === 'object'">
                <div v-if="Array.isArray(s[keys[j - 1]].value)" class="row no-gutters w-100">
                  <div v-for="(x, k) in s[keys[j - 1]].value" :key="`test__${k}`" class="col-12 col-md" :class="{'pr-1' : k !== s[keys[j - 1]].value.length - 1}">
                    <span v-html="x.value" /> <span v-if="s.unit || x.unit" class="small">{{ s.unit || x.unit }}</span>
                  </div>
                </div>
                <span v-else v-html="s[keys[j - 1]].value" />&nbsp;<span v-if="s[keys[j - 1]].unit || s.unit" class="small">{{ s[keys[j - 1]].unit || s.unit }}</span>
              </template>
              <div v-else-if="s[keys[j - 1]]">
                <span v-html="s[keys[j - 1]]" /><span v-if="s.unit" class="small">&nbsp;{{ s.unit }}</span>
              </div>
            </div>
          </div>
          <div class="col scroll-space d-none bg-white" />
        </template>
      </div>
    </div>
  </div>
</template>

<script>
/*
 IMPORTANT! format used for properties:
 properties: {
  propertyUniqueKey: {
    label: '',
    model1Id: {
      id... // here property item
    },
    model1Id: {
      id... // here property item
    },
    ...
  }
 }
  */

import {onMounted, onUnmounted, ref} from '@nuxtjs/composition-api'

export default {
  props: {
    properties: {
      type: Object,
      required: true
    },
    opened: {
      type: Boolean,
      required: false,
      default: false
    },
    keys: {
      type: Array,
      required: true
    },
    firstColumnClass: {
      type: String,
      required: false,
      default: 'col-4 col-lg'
    },
    noBottomMargin: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const rootElement = ref(null) // scrollable element
    const colNum = props.keys?.length || 1

    const horizontalScrollHandler = (e) => {
      e.target.previousElementSibling.children[1].scrollLeft = e.target.scrollLeft // copy scroll
      e.target.parentElement.classList.toggle('max-left', e.target.scrollLeft === 0)
      e.target.parentElement.classList.toggle('max-right', e.target.scrollLeft >= e.target.scrollWidth - e.target.offsetWidth - 1)
    }

    if (process.client) {
      onMounted(() => {
        rootElement.value?.addEventListener('scroll', horizontalScrollHandler, {passive: true})
      })
      onUnmounted(() => {
        rootElement.value?.removeEventListener('scroll', horizontalScrollHandler)
      })
    }

    return {
      rootElement,
      colNum
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/assets/sass/tables';
@import '~/assets/sass/_colors';

.box-shadow-inset {
  box-shadow: inset -1px 0px 0px $tertiary, inset 0px 1px 0px $tertiary;
}
</style>
