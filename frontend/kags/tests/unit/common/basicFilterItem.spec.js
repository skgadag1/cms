import {shallowMount, mount} from '@vue/test-utils'
import BasicFilterItem from '@/components/common/BasicFilterItem'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_props = {
  title: 'Sample-title-test'
}

const $_methods = {
  hideDropdown: jest.fn(),
  toggleDropdown: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(BasicFilterItem, {
    mixin: config(Vue),
    propsData: $_props,
    methods: $_methods
  })
})

afterEach(() => {
  jest.clearAllMocks()
})

describe('components/common/BasicFilterItem.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm.title).toEqual($_props.title)
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering opened state', async () => {
    await $_wrapper.setData({show: true})

    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="filter-item__options"]').isVisible()).toBe(true)
  })

  it('opens/close via button', async () => {
    $_wrapper = mount(BasicFilterItem, {
      mixin: config(Vue),
      propsData: $_props,
      methods: $_methods
    })

    await $_wrapper.findComponent(BButton).trigger('click')
    await $_wrapper.setData({show: true})

    expect($_methods.toggleDropdown).toHaveBeenCalled()
    expect($_wrapper.find('.filter-item__active').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="filter-item__options"]').isVisible()).toBe(true)

    await $_wrapper.findComponent(BButton).trigger('click')
    await $_wrapper.setData({show: false})

    expect($_methods.toggleDropdown).toHaveBeenCalled()
    expect($_wrapper.find('.filter-item__active').exists()).toBe(false)
    expect($_wrapper.find('[data-test-id="filter-item__options"]').isVisible()).toBe(false)
  })
})
