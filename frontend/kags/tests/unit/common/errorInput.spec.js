import {mount} from '@vue/test-utils'
import ErrorInput from '@/components/common/ErrorInput'
import {BButton, BInputGroupAppend, BInputGroup} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'

let $_wrapper

const $_props = {
  searchLabel: 'Search-label-test',
  emptyLabel: 'Empty-label-test',
  initialKeyword: 'test'
}

const $_methods = {
  setOpened: jest.fn(),
  emitSubmit: jest.fn(),
  resetKeyword: jest.fn()
}

describe('components/common/ErrorInput.vue', () => {
  beforeEach(() => {
    $_wrapper = mount(ErrorInput, {
      mixin: config(Vue),
      store: __createMocks().store,
      propsData: $_props,
      methods: $_methods
    })
  })

  afterEach(() => jest.clearAllMocks())

  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is default rendered', () => {
    expect($_wrapper.find('.modal-backdrop').isVisible()).toBeFalsy()
    expect($_wrapper.findComponent(BInputGroup).exists()).toBe(true)
    expect($_wrapper.vm.opened).toBe(false)
    expect($_wrapper.findComponent(BInputGroupAppend).exists()).toBe(false)
  })

  it('is rendering opened state', async () => {
    await $_wrapper.setData({opened: true})

    expect($_wrapper.find('.modal-backdrop').isVisible()).toBe(true)
    expect($_wrapper.find('.modal-backdrop').exists()).toBe(true)
    expect($_wrapper.findAllComponents(BInputGroupAppend).isVisible()).toBe(true)
  })

  it('is closing backdrop by click', async () => {
    await $_wrapper.setData({opened: true})

    const _backdrop = $_wrapper.find('.modal-backdrop')
    expect(_backdrop.isVisible()).toBe(true)
    await _backdrop.trigger('click')
    expect($_methods.setOpened).toBeCalled()
  })

  it('is displaying empty label', async () => {
    await $_wrapper.setData({opened: true, keyword: ''})
    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    expect($_wrapper.findComponent(BButton).text()).toEqual($_props.emptyLabel)
  })

  it('is displaying search label', async () => {
    await $_wrapper.setData({opened: true})
    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    expect($_wrapper.findComponent(BButton).text()).toEqual($_props.searchLabel)
  })

  it('emit proper event on submit', async () => {
    $_wrapper = mount(ErrorInput, {
      mixin: config(Vue),
      propsData: $_props,
      store: __createMocks({
        getters: {
          getCurrentKeyword: jest.fn().mockReturnValue('test'),
          getTroubleshootingKeyword: jest.fn().mockReturnValue('test')
        }
      }).store
    })
    await $_wrapper.setData({opened: true})

    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    await $_wrapper.findComponent(BButton).trigger('click')
    expect($_wrapper.emitted('submit')).toBeTruthy()

    $_wrapper.setMethods($_methods)
    await $_wrapper.findComponent(BButton).trigger('click')
    expect($_methods.emitSubmit).toBeCalled()
  })
})
