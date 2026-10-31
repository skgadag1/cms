import {mount} from '@vue/test-utils'
import Machine from '@/components/Machine/Machine'
import store from '@/store'
import {SERIAL_ADD} from '@/store/mutations'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import MachineDetails from '@/components/common/Machine/MachineDetails'
import MachineDetailsNewDashboard from '@/components/common/Machine/MachineDetailsNewDashboard'
import InPageNavigationDesktop from '@/components/common/Navigation/InPageNavigationDesktop'
import {__createMocks} from '@/store/mocks'

let $_wrapper
Vue.prototype.config = {}
Vue.prototype.assetServer = ''

const $data = {
  errorInputUrl: 'url',
  historyPageUrl: 'historyPageUrl',
  bannerUrl: 'bannerUrl'
}

beforeEach(() => {
  $_wrapper = mount(Machine, {
    store: __createMocks({
      getters: {
        isEmpty: jest.fn().mockReturnValue(true),
        getCurrentSerial: jest.fn().mockReturnValue('123'),
        getCurrentAbbreviation: jest.fn().mockReturnValue(null)
      }
    }).store,
    mixin: config(Vue),
    data() {
      return {componentConfig: $data}
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Machine/Machine.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is checking state: empty', () => {
    expect($_wrapper.vm.isEmpty).toBe(true)
    expect($_wrapper.findComponent(MachineDetails).vm.disabled).toBe(true)
    expect($_wrapper.findComponent(InPageNavigationDesktop).vm.disabled).toBe(true)
    // expect($_wrapper.contains('.modal-backdrop__partial')).toBe(true)
  })

  it('is checking state: with serial', () => {
    $_wrapper = mount(Machine, {
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(false),
          getCurrentSerial: jest.fn().mockReturnValue('123'),
          getCurrentAbbreviation: jest.fn().mockReturnValue(null)
        }
      }).store,
      mixin: config(Vue),
      data() {
        return {componentConfig: $data}
      }
    })

    Object.defineProperty(window, 'history', {
      writable: true,
      value: {}
    })

    store.commit(SERIAL_ADD, {serial_number: '123123'})

    expect($_wrapper.contains('.modal-backdrop__partial')).toBe(false)
    expect($_wrapper.findComponent(MachineDetails).vm.disabled).toBe(false)
    expect($_wrapper.findComponent(InPageNavigationDesktop).vm.disabled).toBe(false)
  })
})

describe('components/Machine/Machine.vue with switchToNewDashboard enabled', () => {
  const newDashboardStore = (extra = {}) => __createMocks({
    getters: {
      isEmpty: jest.fn().mockReturnValue(false),
      getCurrentSerial: jest.fn().mockReturnValue('ALJ812464'),
      getCurrentAbbreviation: jest.fn().mockReturnValue(null),
      // Machine.vue reads getFeatureFlag('switchToNewDashboard')
      getFeatureFlag: jest.fn().mockReturnValue(() => true),
      getFeatureFlagNewDashBoard: jest.fn().mockReturnValue(() => true),
      ...extra
    }
  }).store

  const mountMachine = (storeOverride = {}) => mount(Machine, {
    mixins: [config(Vue)],
    store: newDashboardStore(storeOverride),
    data() {
      return {componentConfig: {errorInputUrl: '', historyPageUrl: '', bannerUrl: ''}}
    }
  })

  it('selects the new dashboard branch when the flag is on', () => {
    const wrapper = mountMachine()
    expect(wrapper.vm.isNewDashboardEnabled).toBe(true)
    wrapper.destroy()
  })

  it('renders the new dashboard (and not the legacy view) when the flag is on', () => {
    const wrapper = mountMachine()

    expect(wrapper.findComponent(MachineDetailsNewDashboard).exists()).toBe(true)
    expect(wrapper.findComponent(MachineDetails).exists()).toBe(false)
    wrapper.destroy()
  })
})


describe('components/Machine/Machine.vue feature-flag gating', () => {
  it('renders neither details view while flags are unresolved for a selected serial', () => {
    const wrapper = mount(Machine, {
      mixins: [config(Vue)],
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(false),
          getCurrentSerial: jest.fn().mockReturnValue('ALJ812464'),
          getCurrentAbbreviation: jest.fn().mockReturnValue(null),
          areFeatureFlagsResolved: jest.fn().mockReturnValue(false)
        }
      }).store,
      data() {
        return {componentConfig: {errorInputUrl: '', historyPageUrl: '', bannerUrl: ''}}
      }
    })

    expect(wrapper.vm.detailsReady).toBe(false)
    expect(wrapper.findComponent(MachineDetails).exists()).toBe(false)
    expect(wrapper.findComponent(MachineDetailsNewDashboard).exists()).toBe(false)
    wrapper.destroy()
  })

  it('still renders the disabled details view for the no-serial (empty) state', () => {
    const wrapper = mount(Machine, {
      mixins: [config(Vue)],
      store: __createMocks({
        getters: {
          isEmpty: jest.fn().mockReturnValue(true),
          getCurrentSerial: jest.fn().mockReturnValue('123'),
          getCurrentAbbreviation: jest.fn().mockReturnValue(null),
          // flags never resolve without a real machine, but the empty state must still show
          areFeatureFlagsResolved: jest.fn().mockReturnValue(false)
        }
      }).store,
      data() {
        return {componentConfig: {errorInputUrl: '', historyPageUrl: '', bannerUrl: ''}}
      }
    })

    expect(wrapper.vm.detailsReady).toBe(true)
    expect(wrapper.findComponent(MachineDetails).vm.disabled).toBe(true)
    wrapper.destroy()
  })
})
