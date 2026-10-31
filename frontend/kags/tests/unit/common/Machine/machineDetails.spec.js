import {mount} from '@vue/test-utils'
import MachineDetails from '@/components/common/Machine/MachineDetails'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $zeroValues from '../../../../public/mocks/iot-api/serial-ALJ812466.json'
import $currentMachine from '../../../../public/mocks/iot-api/serial-ALJ812464.json'
import $machineData from '../../../../public/mocks/product-api/serial-ALJ812466.json'
import {__createMocks} from '@/store/mocks'
import {BSpinner} from 'bootstrap-vue'

let $_wrapper

beforeEach(() => {
  $_wrapper = mount(MachineDetails, {
    mixins: [config(Vue)],
    propsData: {
      serial: 'ALJ812466',
      assetServer: ''
    },
    store: __createMocks({
      getters: {
        getCurrentMachine: jest.fn(),
        getCurrentMachineError: jest.fn().mockReturnValue(true),
        isCurrentMachineConnected: jest.fn().mockReturnValue(true),
        getMachinePassword: jest.fn()
      }
    }).store,
    attrs: {config: {errorInputUrl: ''}}
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Machine/MachineDetails.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering error when component is disabled', async () => {
    await $_wrapper.setProps({disabled: true, loading: false})
    expect($_wrapper.find('[data-test-id="error-no-serial"]').exists()).toBe(true)
  })

  it('shows spinner when component is created', () => {
    expect($_wrapper.findComponent(BSpinner).exists()).toBe(true)
  })

  it('properly renders Machine IQ bubbles disabled state', async () => {
    await $_wrapper.setData({machineData: $machineData, loading: false})
    expect($_wrapper.find('div[data-test-id="bubble-engineHours"]').vm.disabled).toBe(true)
    expect($_wrapper.find('div[data-test-id="bubble-fuelLevel"]').vm.disabled).toBe(true)
    expect($_wrapper.find('div[data-test-id="bubble-defLevel"]').exists()).toBe(false)
    expect($_wrapper.find('div[data-test-id="bubble-location"]').exists()).toBe(false)
  })

  it('properly renders Machine IQ bubbles', async () => {
    $_wrapper = mount(MachineDetails, {
      mixins: [config(Vue)],
      propsData: {
        serial: 'ALJ812464',
        assetServer: ''
      },
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812467'),
          getCurrentMachine: jest.fn().mockReturnValue($currentMachine),
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineConnected: jest.fn().mockReturnValue(true),
          getMachinePassword: jest.fn()
        }
      }).store,
      attrs: {config: {errorInputUrl: ''}}
    })
    await $_wrapper.setData({machineData: $machineData, loading: false})

    expect($_wrapper.find('div[data-test-id="bubble-engineHours"]').vm.value).toEqual($currentMachine.engineHours)
    expect($_wrapper.find('div[data-test-id="bubble-fuelLevel"]').vm.value).toEqual(Math.round($currentMachine.fuelLevel))
    expect($_wrapper.find('div[data-test-id="bubble-defLevel"]').vm.value).toEqual(Math.round($currentMachine.defLevel))
    expect($_wrapper.find('div[data-test-id="bubble-location"]').text()).toEqual(
      $currentMachine.location.address.municipality + ' ' +
      $currentMachine.location.address.subRegion + ' ' +
      $currentMachine.location.address.region + ' ' +
      $currentMachine.location.address.country)})

  it('properly renders Machine IQ bubbles zero values', async () => {
    $_wrapper = mount(MachineDetails, {
      mixins: [config(Vue)],
      propsData: {
        serial: 'ALJ812466',
        assetServer: ''
      },
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
          getCurrentMachine: jest.fn().mockReturnValue($zeroValues),
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          getMachinePassword: jest.fn()
        }
      }).store,
      attrs: {config: {errorInputUrl: ''}}
    })
    await $_wrapper.setData({machineData: $machineData, loading: false})

    expect($_wrapper.find('div[data-test-id="bubble-engineHours"]').vm.value).toEqual(0)
    expect($_wrapper.find('div[data-test-id="bubble-fuelLevel"]').vm.disabled).toBe(false)
    expect($_wrapper.find('div[data-test-id="bubble-defLevel"]').exists()).toBe(false)
  })
})
