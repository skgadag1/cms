import FaultCodesComponent from '@/components/common/FaultCodes/FaultCodesComponent'
import FaultCodesTable from '@/components/common/FaultCodes/FaultCodesTable'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import {shallowMount} from '@vue/test-utils'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import $codesData from '../../../../public/mocks/iot-api/faultcodes/ALJ812464.json'
import Vue from 'vue'

let $_wrapper

const $_methods = {
  loadCodes: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(FaultCodesComponent, {
    mixin: config(Vue),
    methods: $_methods,
    store: __createMocks({
      getters: {
        getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
        isFaultCodesAuthorized: jest.fn().mockReturnValue(true),
        isCurrentMachineValidSubscription: jest.fn().mockReturnValue(true)
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FaultCodes/FaultCodesComponent.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.findAllComponents(OverlayWrapper).length).toBe(1)
    expect($_methods.loadCodes).toHaveBeenCalled()
  })

  it('renders disabled state', async () => {
    await $_wrapper.setProps({disabled: true})
    await $_wrapper.setData({loading: false})
    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(true)
  })

  it('renders error on fault codes not authorized', async () => {
    $_wrapper = shallowMount(FaultCodesComponent, {
      mixin: config(Vue),
      methods: $_methods,
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
          isFaultCodesAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false)
        }
      }).store
    })
    await $_wrapper.setData({loading: false, items: $codesData})
    expect($_wrapper.findComponent(FaultCodesTable).exists()).toBe(false)
    expect($_wrapper.find('.tests-error').exists()).toBe(true)
  })

  it('renders error on subscription missing', async () => {
    $_wrapper = shallowMount(FaultCodesComponent, {
      mixin: config(Vue),
      methods: $_methods,
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
          isFaultCodesAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false)
        }
      }).store
    })
    await $_wrapper.setData({loading: false, items: $codesData})
    expect($_wrapper.findComponent(FaultCodesTable).exists()).toBe(false)
    expect($_wrapper.find('.tests-error').exists()).toBe(true)
  })
})
