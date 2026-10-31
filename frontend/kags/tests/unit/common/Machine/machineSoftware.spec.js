import {mount, shallowMount} from '@vue/test-utils'
import MachineSoftware from '@/components/common/Machine/MachineSoftware'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $currentMachine from '../../../../public/mocks/iot-api/serial-ALJ812464.json'
import $machineData from '../../../../public/mocks/product-api/serial-ALJ812466.json'
import $softwareData from '../../../../public/mocks/product-api/software/ALJ812466.json'
import {__createMocks} from '@/store/mocks'
import {bdService} from '@/services'
import flushPromises from 'flush-promises'

let $_wrapper

beforeEach(() => {
  $_wrapper = mount(MachineSoftware, {
    mixins: [config(Vue)],
    propsData: {
      serial: 'ALJ812466',
      assetServer: ''
    },
    store: __createMocks({
      getters: {
        getCurrentMachine: jest.fn(),
        getCurrentMachineError: jest.fn().mockReturnValue(true)
      }
    }).store,
    attrs: {config: {errorInputUrl: ''}}
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Machine/MachineSoftware.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('properly renders latest Software version', async () => {
    bdService.fetchProductSoftware = jest.fn().mockImplementation(() => Promise.resolve({data: $softwareData}))

    $_wrapper = shallowMount(MachineSoftware, {
      mixin: config(Vue),
      propsData: {
        serial: 'ALJ812466',
        assetServer: ''
      },
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
          getCurrentMachine: jest.fn().mockReturnValue($currentMachine)
        }
      }).store,
      attrs: {config: {errorInputUrl: ''}}
    })
    await $_wrapper.setData({machineData: $machineData, loading: false, loadingSoftware: false})
    await flushPromises()

    expect(bdService.fetchProductSoftware).toHaveBeenCalled()
    expect($_wrapper.vm.mostRecentSA.version).toEqual($softwareData[7].version)
    expect($_wrapper.vm.mostRecentEAECU.version).toEqual($softwareData[3].version)
    expect($_wrapper.vm.mostRecentEADCU.version).toEqual($softwareData[1].version)
    expect($_wrapper.vm.mostRecentEAV2.version).toEqual($softwareData[5].version)
  })
})
