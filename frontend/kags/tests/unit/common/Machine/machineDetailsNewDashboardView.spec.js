import {mount} from '@vue/test-utils'
import Vue from 'vue'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import {bdService} from '@/services'
import MachineDetailsNewDashboard from '@/components/common/Machine/MachineDetailsNewDashboard'
import MachineBubbles from '@/components/common/Machine/MachineBubbles'
import {BSpinner} from 'bootstrap-vue'
import $currentMachine from '../../../../public/mocks/iot-api/serial-ALJ812464.json'
import $machineData from '../../../../public/mocks/product-api/serial-ALJ812466.json'

const $config = {
  errorInputUrl: '',
  locationHistoryPageUrl: '/machine/locationhistory.html',
  softwareForKeyAccounts: 'false'
}

function mountView({
  serial = 'ALJ812464',
  machine = $currentMachine,
  connected = true,
  extraGetters = {},
  attrsConfig = $config
} = {}) {
  return mount(MachineDetailsNewDashboard, {
    mixins: [config(Vue)],
    propsData: {serial, assetServer: '', disabled: false},
    store: __createMocks({
      getters: {
        getCurrentSerial: jest.fn().mockReturnValue(serial),
        getCurrentMachine: jest.fn().mockReturnValue(machine),
        getCurrentAbbreviation: jest.fn().mockReturnValue(null),
        isCurrentMachineConnected: jest.fn().mockReturnValue(connected),
        getMachinePassword: jest.fn(),
        ...extraGetters
      }
    }).store,
    attrs: {config: attrsConfig}
  })
}

beforeEach(() => {
  Vue.prototype.assetServer = ''
  bdService.fetchDetails = jest.fn().mockResolvedValue({data: $machineData})
  bdService.fetchProductSoftware = jest.fn().mockResolvedValue({data: []})
})

afterEach(() => {
  jest.restoreAllMocks()
})

describe('components/common/Machine/MachineDetailsNewDashboard.vue', () => {
  it('is a vue instance', () => {
    const wrapper = mountView()
    expect(wrapper.vm).toBeTruthy()
    wrapper.destroy()
  })

  it('shows the no-serial error when disabled', async () => {
    const wrapper = mountView()
    await wrapper.setProps({disabled: true, loading: false})
    expect(wrapper.find('[data-test-id="error-no-serial"]').exists()).toBe(true)
    wrapper.destroy()
  })

  it('shows a spinner while loading', () => {
    const wrapper = mountView()
    expect(wrapper.findComponent(BSpinner).exists()).toBe(true)
    wrapper.destroy()
  })

  it('renders the SERIAL-MODEL title and bubbles once data is loaded', async () => {
    const wrapper = mountView()
    await wrapper.setData({machineData: $machineData, loading: false})
    expect(wrapper.find('.machine-title').text()).toContain('ALJ812464')
    expect(wrapper.findComponent(MachineBubbles).exists()).toBe(true)
    wrapper.destroy()
  })

  describe('disconnected state', () => {
    it('shows the "not connected" danger badge', async () => {
      const wrapper = mountView({connected: false})
      await wrapper.setData({machineData: $machineData, loading: false})
      // the disconnected branch renders the connection-status danger badge
      expect(wrapper.find('.badge-danger').exists()).toBe(true)
      wrapper.destroy()
    })

    it('does not show the danger badge when connected', async () => {
      const wrapper = mountView({connected: true})
      await wrapper.setData({machineData: $machineData, loading: false})
      expect(wrapper.find('.badge-danger').exists()).toBe(false)
      wrapper.destroy()
    })

    it('disables the View on Map link when not connected', async () => {
      const wrapper = mountView({connected: false})
      await wrapper.setData({machineData: $machineData, loading: false})
      const mapLink = wrapper.find('.view-map-link')
      expect(mapLink.classes()).toContain('view-map-link--disabled')
      // b-link renders the disabled state on an anchor (aria-disabled, no real destination)
      expect(mapLink.attributes('aria-disabled')).toBe('true')
      expect(mapLink.attributes('href')).not.toBe($config.locationHistoryPageUrl)
      wrapper.destroy()
    })

    it('keeps the View on Map link enabled and navigable when connected', async () => {
      const wrapper = mountView({connected: true})
      await wrapper.setData({machineData: $machineData, loading: false})
      const mapLink = wrapper.find('.view-map-link')
      expect(mapLink.classes()).not.toContain('view-map-link--disabled')
      expect(mapLink.attributes('href')).toBe($config.locationHistoryPageUrl)
      wrapper.destroy()
    })
  })

  describe('timestamp', () => {
    it('derives lastSeen date and time from the machine lastReported value', () => {
      const wrapper = mountView()
      expect(typeof wrapper.vm.lastSeen).toBe('string')
      expect(wrapper.vm.lastSeen.length).toBeGreaterThan(0)
      expect(typeof wrapper.vm.lastSeenTime).toBe('string')
      wrapper.destroy()
    })

    it('returns false for both when there is no lastReported', () => {
      const wrapper = mountView({machine: {model: 'X'}})
      expect(wrapper.vm.lastSeen).toBe(false)
      expect(wrapper.vm.lastSeenTime).toBe(false)
      wrapper.destroy()
    })
  })

  describe('address', () => {
    it('renders the address block when the machine has an address', async () => {
      const wrapper = mountView()
      await wrapper.setData({machineData: $machineData, loading: false})
      expect(wrapper.find('[data-test-id="bubble-location"]').exists()).toBe(true)
      expect(wrapper.find('[data-test-id="bubble-location"]').text())
        .toContain($currentMachine.location.address.municipality)
      wrapper.destroy()
    })

    it('falls back to the no-address message when address is missing', async () => {
      const wrapper = mountView({machine: {model: 'X', lastReported: '2023-10-27T11:35:56.000Z'}})
      await wrapper.setData({machineData: $machineData, loading: false})
      await wrapper.setData({componentLabels: {MachineDetails: {noMachineAddressDesc: 'No Machine Address'}}})
      // the location block container is always rendered; when the address is missing
      // it shows the fallback message instead of address lines
      expect(wrapper.find('[data-test-id="bubble-location"]').exists()).toBe(true)
      expect(wrapper.find('[data-test-id="bubble-location"]').text()).toContain('No Machine Address')
      expect(wrapper.find('.information-summary-card').text()).toContain('No Machine Address')
      wrapper.destroy()
    })
  })

  describe('software section (key accounts flag)', () => {
    it('hides the software section when softwareForKeyAccounts is not "true"', async () => {
      const wrapper = mountView()
      await wrapper.setData({machineData: $machineData, loading: false})
      expect(wrapper.find('.software-section').exists()).toBe(false)
      wrapper.destroy()
    })

    it('shows the software section when softwareForKeyAccounts is "true"', async () => {
      const wrapper = mountView({attrsConfig: {...$config, softwareForKeyAccounts: 'true'}})
      await wrapper.setData({machineData: $machineData, loading: false})
      expect(wrapper.find('.software-section').exists()).toBe(true)
      wrapper.destroy()
    })
  })
})
