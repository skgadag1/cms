import {mount, shallowMount} from '@vue/test-utils'
import MobileSelect from '@/components/common/MobileSelect'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_methods = {
  close: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(MobileSelect, {
    mixin: config(Vue),
    methods: $_methods
  })
})

describe('components/common/MobileSelect.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('render correctly with default data', () => {
    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBe(false)
  })

  it('showing backdrop: isOpen', async () => {
    await $_wrapper.setData({
      isOpen: true
    })

    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBeTruthy()
  })

  it('showing backdrop: empty', async () => {
    await $_wrapper.setProps({
      empty: true
    })

    expect($_wrapper.find('.modal-backdrop__partial').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop__partial').isVisible()).toBeTruthy()
  })

  it('showing backdrop when clicking button', async () => {
    $_wrapper = mount(MobileSelect, {
      mixin: config(Vue)
    })

    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBe(false)
    await $_wrapper.findComponent(BButton).trigger('click')
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBeTruthy()
  })

  it('closing backdrop on click', async () => {
    await $_wrapper.setData({
      isOpen: true
    })

    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBeTruthy()
    await $_wrapper.findComponent('.modal-backdrop').trigger('click')
    expect($_methods.close).toHaveBeenCalled()
  })

  it('check proper prop rendering: title', async () => {
    const _testTitle = 'this is test title'
    await $_wrapper.setProps({
      title: _testTitle
    })
    expect($_wrapper.findComponent(BButton).exists()).toBeTruthy()
    expect($_wrapper.findComponent(BButton).text()).toEqual(_testTitle)
  })
})
