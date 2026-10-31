import {mount, shallowMount} from '@vue/test-utils'
import Vue from 'vue'
import flushPromises from 'flush-promises'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import {bdService} from '@/services'
import Machine from '@/components/Machine/Machine'
import MachineDetailsNewDashboard from '@/components/common/Machine/MachineDetailsNewDashboard'
import MachineBubbles from '@/components/common/Machine/MachineBubbles'
import $currentMachine from '../../../public/mocks/iot-api/serial-ALJ812464.json'

const $config = {
  errorInputUrl: 'url',
  historyPageUrl: 'historyPageUrl',
  bannerUrl: 'bannerUrl',
  locationHistoryPageUrl: '/machine/locationhistory.html',
  softwareForKeyAccounts: 'false'
}

// enable the new dashboard feature flag (Machine.vue reads getFeatureFlag('switchToNewDashboard'))
const newDashboardGetters = {
  getFeatureFlag: jest.fn().mockReturnValue(() => true),
  getFeatureFlagNewDashBoard: jest.fn().mockReturnValue(() => true)
}

beforeEach(() => {
  Vue.prototype.config = {}
  Vue.prototype.assetServer = ''
  bdService.fetchDetails = jest.fn().mockResolvedValue({data: {model_id: 'S550 T4'}})
  bdService.fetchProductSoftware = jest.fn().mockResolvedValue({data: []})
})

afterEach(() => {
  jest.restoreAllMocks()
})

function mountNewDashboard(extraGetters = {}) {
  return shallowMount(MachineDetailsNewDashboard, {
    mixins: [config(Vue)],
    propsData: {
      serial: 'ALJ812464',
      assetServer: '',
      disabled: false
    },
    store: __createMocks({
      getters: {
        getCurrentSerial: jest.fn().mockReturnValue('ALJ812464'),
        getCurrentMachine: jest.fn().mockReturnValue($currentMachine),
        getCurrentAbbreviation: jest.fn().mockReturnValue(null),
        ...newDashboardGetters,
        ...extraGetters
      }
    }).store,
    attrs: {config: $config}
  })
}

describe('components/common/Machine/MachineDetailsNewDashboard.vue', () => {
  it('is a vue instance', () => {
    const wrapper = mountNewDashboard()
    expect(wrapper.vm).toBeTruthy()
    wrapper.destroy()
  })

  it('renders the machine summary card with SERIAL-MODEL title once data is loaded', async () => {
    const wrapper = mountNewDashboard()
    await wrapper.setData({machineData: {model_id: 'S550 T4'}, loading: false})
    await flushPromises()

    expect(wrapper.find('.machine-summary-card').exists()).toBe(true)
    expect(wrapper.find('.machine-title').text()).toContain('ALJ812464-S550 T4')
    wrapper.destroy()
  })

  it('renders the MachineBubbles component in the summary card', async () => {
    const wrapper = mountNewDashboard()
    await wrapper.setData({machineData: {model_id: 'S550 T4'}, loading: false})
    await flushPromises()

    expect(wrapper.findComponent(MachineBubbles).exists()).toBe(true)
    wrapper.destroy()
  })

  it('shows the "Not connected" badge when the machine is not connected', async () => {
    const wrapper = mountNewDashboard({
      isCurrentMachineConnected: jest.fn().mockReturnValue(false)
    })
    await wrapper.setData({machineData: {model_id: 'S550 T4'}, loading: false})
    await flushPromises()

    expect(wrapper.find('.machine-media-row .text-center.text-md-left').exists()).toBe(true)
    wrapper.destroy()
  })

  it('formats lastSeen date and time from the machine lastReported value', () => {
    const wrapper = mountNewDashboard()
    // lastSeen is the formatted date portion; lastSeenTime is the time portion
    expect(typeof wrapper.vm.lastSeen).toBe('string')
    expect(wrapper.vm.lastSeen.length).toBeGreaterThan(0)
    expect(typeof wrapper.vm.lastSeenTime).toBe('string')
    wrapper.destroy()
  })

  it('fetches machine details on creation', () => {
    const wrapper = mountNewDashboard()
    expect(bdService.fetchDetails).toHaveBeenCalledWith('ALJ812464')
    wrapper.destroy()
  })
})

describe('components/Machine/Machine.vue new dashboard rendering path', () => {
  it('renders the new dashboard (not the legacy one) when the feature flag is enabled', async () => {
    const wrapper = mount(Machine, {
      mixins: [config(Vue)],
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(false),
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812464'),
          getCurrentAbbreviation: jest.fn().mockReturnValue(null),
          getCurrentMachine: jest.fn().mockReturnValue($currentMachine),
          ...newDashboardGetters
        }
      }).store,
      data() {
        return {componentConfig: $config}
      }
    })

    // the flag-driven computed selects the new dashboard
    expect(wrapper.vm.isNewDashboardEnabled).toBe(true)

    // dashboards are async components; let them resolve, then assert the new
    // dashboard's test-id is present and the legacy one is not
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-test-id="mike-machinedetails-newDashboard"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="mike-machinedetails"]').exists()).toBe(false)
    wrapper.destroy()
  })
})

describe('MachineDetailsNewDashboard.vue stale response guard (A -> B -> A)', () => {
  // Build a controllable fetchDetails: each call returns a pending promise whose
  // resolver is captured so tests can settle requests out of order.
  function deferredFetchDetails() {
    const calls = []
    const fn = jest.fn((serial) => {
      let resolve, reject
      const promise = new Promise((res, rej) => { resolve = res; reject = rej })
      calls.push({serial, resolve, reject})
      return promise
    })
    return {fn, calls}
  }

  function mountWithDeferred(fetchImpl) {
    bdService.fetchDetails = fetchImpl
    return shallowMount(MachineDetailsNewDashboard, {
      mixins: [config(Vue)],
      propsData: {serial: 'A', assetServer: '', disabled: false},
      store: __createMocks({
        getters: {
          getCurrentSerial: jest.fn().mockReturnValue('A'),
          getCurrentMachine: jest.fn().mockReturnValue($currentMachine),
          getCurrentAbbreviation: jest.fn().mockReturnValue(null),
          ...newDashboardGetters
        }
      }).store,
      attrs: {config: $config}
    })
  }

  it('ignores an older A response that resolves after a newer A request is pending', async () => {
    const {fn, calls} = deferredFetchDetails()
    const wrapper = mountWithDeferred(fn)

    // created() fired request #1 for A
    expect(calls.length).toBe(1)
    expect(calls[0].serial).toBe('A')

    // switch A -> B (request #2 for B), clears machineData
    await wrapper.setProps({serial: 'B'})
    // switch B -> A (request #3 for A, the latest)
    await wrapper.setProps({serial: 'A'})

    expect(calls.length).toBe(3)
    expect(calls.map(c => c.serial)).toEqual(['A', 'B', 'A'])
    expect(wrapper.vm.loading).toBe(true)

    // resolve the FIRST (stale) A request while the latest A request is still pending
    calls[0].resolve({data: {model_id: 'OLD-MODEL'}})
    await flushPromises()

    // the stale result must NOT be applied, and loading must stay true
    expect(wrapper.vm.machineData).toBeNull()
    expect(wrapper.vm.loading).toBe(true)

    // now resolve the LATEST A request
    calls[2].resolve({data: {model_id: 'NEW-MODEL'}})
    await flushPromises()

    // the current model is applied and loading stops
    expect(wrapper.vm.machineData).toEqual({model_id: 'NEW-MODEL'})
    expect(wrapper.vm.loading).toBe(false)

    wrapper.destroy()
  })

  it('a stale in-flight B response does not overwrite the current A data', async () => {
    const {fn, calls} = deferredFetchDetails()
    const wrapper = mountWithDeferred(fn)

    // A (req#1) -> B (req#2) -> A (req#3)
    await wrapper.setProps({serial: 'B'})
    await wrapper.setProps({serial: 'A'})

    // resolve the latest A request first
    calls[2].resolve({data: {model_id: 'A-MODEL'}})
    await flushPromises()
    expect(wrapper.vm.machineData).toEqual({model_id: 'A-MODEL'})
    expect(wrapper.vm.loading).toBe(false)

    // a late B response arrives — must be ignored (serial no longer B)
    calls[1].resolve({data: {model_id: 'B-MODEL'}})
    await flushPromises()
    expect(wrapper.vm.machineData).toEqual({model_id: 'A-MODEL'})
    expect(wrapper.vm.loading).toBe(false)

    wrapper.destroy()
  })
})
