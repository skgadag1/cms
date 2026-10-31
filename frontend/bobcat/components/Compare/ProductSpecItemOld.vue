<template>
  <div class="transition-slide shadow" :class="isOpened && noBottomMargin ? 'mb-0' : 'mb-3'" data-test-id="product-spec-item-old">
    <LazyHydrate :on-interaction="['click', 'touchstart']" :trigger-hydration="hydrate">
      <div v-if="showHeader"
           class="rounded-top d-flex align-items-center justify-content-between bg-gray-darker text-white cursor-pointer user-select-none"
           :class="{'rounded-bottom' : !isOpened}"
           @click="isOpened = !isOpened"
      >
        <div class="compare-header p-3">
          <h5 class="m-0 d-inline-flex position-relative flex-grow-1 sticky"
              data-test-id="product-spec-item-old-title"
          >
            {{ title }}
          </h5>
        </div>
        <span class="compare-sort-down d-inline-flex align-items-center justify-content-center px-3 m-0 transition-base"
              :style="isOpened ? 'transform: rotate(180deg)' : null"
        ><b-icon icon="sort-down" class="dropdown" /></span>
      </div>
    </LazyHydrate>
    <b-collapse v-model="isOpened">
      <div v-for="(s, i) in properties"
           :key="`product-spec-item__${i}`"
           class="row row-scrolled no-gutters d-flex align-items-stretch position-relative transition-base"
           :data-test-id="`product-spec-item-old__${i}`"
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
          <div :class="compareMode ? firstColumnClass : 'col-6 col-lg-7'" class="transition-base">
            <div class="compare-cell bg-light text-gray-darker box-shadow-inset d-flex align-items-center h-100 text-break">{{ s.label }}</div>
          </div>
          <div v-for="j in colNum"
               :key="`product-spec-item-col__${i}-${j}`"
               :data-test-id="`product-spec-item-old-col__${i}-${j}`"
               :class="[`col`, !compareMode ? 'col-lg-5' : null]"
          >
            <div class="compare-cell bg-white box-shadow-inset d-flex align-items-center h-100 text-break">
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
        </template>
      </div>
    </b-collapse>
  </div>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'

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

export default {
  props: {
    title: {
      type: String,
      required: true
    },
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
    showHeader: {
      type: Boolean,
      default: true
    },
    hydrate: {
      type: Boolean,
      default: false
    },
    noBottomMargin: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const isOpened = ref(props.opened)
    const colNum = props.keys?.length || 1
    const compareMode = colNum > 1

    return {
      isOpened,
      colNum,
      compareMode
    }
  }
}
</script>

<style lang="scss" scoped>
.box-shadow-inset {
  box-shadow: inset 0px -3px 0px #D5DEE3;
}
</style>
