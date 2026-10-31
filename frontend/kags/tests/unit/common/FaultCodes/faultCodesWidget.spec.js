import {mount} from '@vue/test-utils'
import FaultCodesWidget from '@/components/common/FaultCodes/FaultCodesWidget'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import {__createMocks} from '@/store/mocks'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $codesData from '../../../../public/mocks/iot-api/faultcodes/ALJ812464.json'
import FaultCodeItem from '@/components/common/FaultCodes/FaultCodeItem'

let $_wrapper

const $_methods = {
  fetchActiveCodes: jest.fn(),
  fetchIndicatorData: jest.fn()
}


beforeEach(() => {
  $_wrapper = mount(FaultCodesWidget, {
    mixin: config(Vue),
    methods: $_methods,
    store: __createMocks().store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FaultCodesWidget.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering ListEmptyWidget when component is disabled', async () => {
    const $props = {
      disabled: true
    }

    await $_wrapper.setProps($props)

    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(true)
  })

  it('shows spinner when component is created', () => {
    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(false)
    expect($_wrapper.findComponent(OverlayWrapper).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
  })

  it('is render fault code item ', async () => {
    await $_wrapper.setData({loading: false, codes: {all: $codesData.items, displayed: $codesData.items, critical: [], warning: [], info: []}})
    expect($_wrapper.findComponent(FaultCodeItem).exists()).toBe(true)
  })

  it('is displaying error on no subscription ', async () => {
    $_wrapper = mount(FaultCodesWidget, {
      mixin: config(Vue),
      methods: $_methods,
      store: __createMocks({
        getters: {
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false)
        }
      }).store
    })
    await $_wrapper.setData({loading: false, codes: {all: $codesData.items, displayed: $codesData.items, critical: [], warning: [], info: []}})
    expect($_wrapper.findComponent(FaultCodeItem).exists()).toBe(false)
    expect($_wrapper.find('.tests-error').exists()).toBe(true)
  })
})
