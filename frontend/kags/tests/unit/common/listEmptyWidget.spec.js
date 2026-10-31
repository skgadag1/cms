import {mount} from '@vue/test-utils'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $props = {
  range: 6
}

beforeEach(() => {
  $_wrapper = mount(ListEmptyWidget, {
    store: store,
    mixin: config(Vue),
    propsData: $props
  })
})

describe('components/common/ListEmptyWidget.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering proper number of empty widgets', () => {
    expect($_wrapper.findAll('[data-test-id]').length).toBe($props.range)
  })
})
