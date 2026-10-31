import {shallowMount, mount} from '@vue/test-utils'
import BuiltOptionsComponent from '@/components/common/BuiltOptions/BuiltOptionsComponent'
import BuiltOptionsTable from '@/components/common/BuiltOptions/BuiltOptionsTable'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import Vue from 'vue'
import $options from '../../../../public/mocks/equipment-api/options/options-ALJ812464.json'

let $_wrapper

const $_config = {
  'clearFilters': 'Clear all'
}

const $_methods = {
  fetchOptions: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(BuiltOptionsComponent, {
    mixin: config(Vue),
    propsData: {config: $_config},
    methods: $_methods,
    store: __createMocks({
      getters: {
        isCurrentMachineAuthorized: jest.fn().mockReturnValue(true)
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/BuiltOptions/BuiltOptionsComponent.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.findAllComponents(ListEmptyWidget).length).toBe(1)
    expect($_methods.fetchOptions).toHaveBeenCalled()
  })

  it('is rendering loading state', async () => {
    await $_wrapper.setProps({disabled: false})

    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
    expect($_wrapper.findAllComponents(OverlayWrapper)).toHaveLength(1)
  })

  it('is rendering items', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({loading: false, error: {present: false}, items: $options})

    expect($_wrapper.findComponent(BuiltOptionsTable).exists()).toBe(true)
  })

  it('is rendering no items message', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({loading: false, error: {present: false}, items: []})

    expect($_wrapper.find('[data-test-id=built-options-noitems]').exists()).toBe(true)
  })

  it('renders error', async () => {
    $_wrapper = mount(BuiltOptionsComponent, {
      mixin: config(Vue),
      propsData: {
        config: $_config,
        disabled: false
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true)
        }
      }).store
    })
    await $_wrapper.setData({loading: false, error: {present: true}})

    expect($_wrapper.findComponent(BuiltOptionsTable).exists()).toBe(false)
    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(false)
    expect($_wrapper.find('[data-test-id=built-options-error] .box.bg-gray-400').exists()).toBe(false)
    expect($_wrapper.findAll('[data-test-id=built-options-error] .box').length).toBe(1)
  })
})
