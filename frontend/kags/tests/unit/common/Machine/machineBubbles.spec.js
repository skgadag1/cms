import {mount} from '@vue/test-utils'
import Vue from 'vue'
import flushPromises from 'flush-promises'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import {contentService} from '@/services'
import MachineBubbles from '@/components/common/Machine/MachineBubbles'
import MachineBubble from '@/components/common/Machine/MachineBubble'
import $currentMachine from '../../../../public/mocks/iot-api/serial-ALJ812464.json'
import $zeroValues from '../../../../public/mocks/iot-api/serial-ALJ812466.json'
import $machineDetailsLabels from '../../../../public/mocks/content-api/components/MachineDetails.json'

beforeEach(() => {
  // seed the MachineDetails concept translations (incl. the "electric" key) so
  // translatedLabel resolves real values instead of "{ MachineDetails.<key> }"
  contentService.fetchTranslations = jest.fn().mockResolvedValue({data: $machineDetailsLabels})
})

afterEach(() => {
  jest.restoreAllMocks()
})

function mountBubbles({machine = $currentMachine, connected = true} = {}) {
  return mount(MachineBubbles, {
    mixins: [config(Vue)],
    propsData: {assetServer: ''},
    store: __createMocks({
      getters: {
        getCurrentMachine: jest.fn().mockReturnValue(machine),
        isCurrentMachineConnected: jest.fn().mockReturnValue(connected)
      }
    }).store
  })
}

describe('components/common/Machine/MachineBubbles.vue', () => {
  it('is a vue instance', () => {
    const wrapper = mountBubbles()
    expect(wrapper.vm).toBeTruthy()
    wrapper.destroy()
  })

  describe('connected diesel machine with readings', () => {
    it('exposes rounded readings from the current machine', () => {
      const wrapper = mountBubbles()
      expect(wrapper.vm.engineHours).toEqual($currentMachine.engineHours)
      expect(wrapper.vm.fuelLevel).toEqual(Math.round($currentMachine.fuelLevel))
      expect(wrapper.vm.defLevel).toEqual(Math.round($currentMachine.defLevel))
      expect(wrapper.vm.electric).toBe(false)
      wrapper.destroy()
    })

    it('renders engine hours, fuel and DEF ring/icon bubbles (no electric)', () => {
      const wrapper = mountBubbles()
      const bubbles = wrapper.findAllComponents(MachineBubble)
      // engine hours (icon) + fuel (ring) + DEF (ring); battery is a plain div
      const types = bubbles.wrappers.map(b => b.props('type'))
      expect(types).toContain('icon')
      expect(types.filter(t => t === 'ring').length).toBe(2)
      expect(types).not.toContain('electric')
      wrapper.destroy()
    })

    it('shows the DEF bubble only for a positive reading', () => {
      const wrapper = mountBubbles()
      expect(wrapper.vm.showDef).toBe(true)
      wrapper.destroy()
    })

    it('uses the 2-column (2x2) layout for diesel with DEF', () => {
      // $currentMachine has defLevel = 50 -> engine + battery + fuel + DEF
      const wrapper = mountBubbles()
      expect(wrapper.vm.bubbleCount).toBe(4)
      expect(wrapper.find('.bubble-grid--two-col').exists()).toBe(true)
      wrapper.destroy()
    })

    it('uses the default 3-across layout for diesel without DEF', () => {
      const wrapper = mountBubbles({
        machine: {engineHours: 10, fuelLevel: 40, machineBatteryVoltage: 13, lastReported: '2023-10-27T11:35:56.000Z'}
      })
      expect(wrapper.vm.showDef).toBe(false)
      expect(wrapper.vm.bubbleCount).toBe(3)
      // 3 bubbles use the default sizing (no two-col modifier)
      expect(wrapper.find('.bubble-grid--two-col').exists()).toBe(false)
      wrapper.destroy()
    })
  })

  describe('electric machine', () => {
    const electricMachine = {
      equipmentType: 'electric',
      engineHours: 12,
      lastReported: '2023-10-27T11:35:56.000Z'
    }

    it('renders the electric bubble instead of fuel', () => {
      const wrapper = mountBubbles({machine: electricMachine})
      expect(wrapper.vm.electric).toBe(true)
      const types = wrapper.findAllComponents(MachineBubble).wrappers.map(b => b.props('type'))
      expect(types).toContain('electric')
      wrapper.destroy()
    })

    it('gives the electric bubble an accessible name from the electric concept', async () => {
      const wrapper = mountBubbles({machine: electricMachine})
      // let the concept translations load so labels resolve
      await flushPromises()

      const electricBubble = wrapper.findAllComponents(MachineBubble).wrappers
        .find(b => b.props('type') === 'electric')

      // resolves the real 'electric' concept value, not a missing-key placeholder
      expect(wrapper.vm.translatedLabel('electric', 'MachineDetails')).toBe('Electric')

      const ariaLabel = electricBubble.props('ariaLabel')
      expect(ariaLabel).toBe('Electric')
      expect(ariaLabel).not.toContain('{ MachineDetails.electric }')
      // the child renders it as an image role with that accessible name
      expect(electricBubble.attributes('role')).toBe('img')
      expect(electricBubble.attributes('aria-label')).toBe('Electric')
      wrapper.destroy()
    })

    it('does not render a fuel ring bubble for electric (electric replaces fuel)', () => {
      const wrapper = mountBubbles({machine: electricMachine})
      const types = wrapper.findAllComponents(MachineBubble).wrappers.map(b => b.props('type'))
      // no DEF here, so no ring bubbles at all
      expect(types.filter(t => t === 'ring').length).toBe(0)
      wrapper.destroy()
    })

    it('uses the default 3-across layout for electric without DEF (electric does not add a bubble)', () => {
      const wrapper = mountBubbles({machine: electricMachine})
      expect(wrapper.vm.bubbleCount).toBe(3)
      // 3 bubbles use the default sizing (no two-col modifier)
      expect(wrapper.find('.bubble-grid--two-col').exists()).toBe(false)
      wrapper.destroy()
    })

    it('uses the 2-column layout for electric with DEF', () => {
      const wrapper = mountBubbles({
        machine: {equipmentType: 'electric', engineHours: 12, defLevel: 60, lastReported: '2023-10-27T11:35:56.000Z'}
      })
      expect(wrapper.vm.electric).toBe(true)
      expect(wrapper.vm.showDef).toBe(true)
      expect(wrapper.vm.bubbleCount).toBe(4)
      expect(wrapper.find('.bubble-grid--two-col').exists()).toBe(true)
      // engine + battery + electric + DEF(ring) = one ring bubble present
      const types = wrapper.findAllComponents(MachineBubble).wrappers.map(b => b.props('type'))
      expect(types).toContain('electric')
      expect(types.filter(t => t === 'ring').length).toBe(1)
      wrapper.destroy()
    })
  })

  describe('zero readings', () => {
    it('treats zero engine hours / fuel as valid (enabled) values', () => {
      const wrapper = mountBubbles({machine: $zeroValues})
      expect(wrapper.vm.engineHours).toEqual(0)
      expect(wrapper.vm.fuelLevel).toEqual(0)
      // DEF is visible if value is 0
      expect(wrapper.vm.showDef).toBe(true)
      wrapper.destroy()
    })

    it('marks a zero fuel bubble as having a value (not disabled)', () => {
      const wrapper = mountBubbles({machine: $zeroValues})
      const fuel = wrapper.findAllComponents(MachineBubble).wrappers
        .find(b => b.props('type') === 'ring')
      expect(fuel.vm.hasValue).toBe(true)
      wrapper.destroy()
    })
  })

  describe('disconnected machine', () => {
    it('returns false for all readings when not connected', () => {
      const wrapper = mountBubbles({connected: false})
      expect(wrapper.vm.engineHours).toBe(false)
      expect(wrapper.vm.fuelLevel).toBe(false)
      expect(wrapper.vm.defLevel).toBe(false)
      expect(wrapper.vm.machineBatteryVoltage).toBe(false)
      expect(wrapper.vm.electric).toBe(false)
      wrapper.destroy()
    })

    it('uses the 2-column layout when disconnected (4 bubbles)', () => {
      const wrapper = mountBubbles({connected: false})
      expect(wrapper.vm.bubbleCount).toBe(4)
      expect(wrapper.find('.bubble-grid--two-col').exists()).toBe(true)
      wrapper.destroy()
    })

    it('disables the reading bubbles (no numeric value)', () => {
      const wrapper = mountBubbles({connected: false})
      const rings = wrapper.findAllComponents(MachineBubble).wrappers
      rings.forEach(b => {
        if (b.props('type') !== 'electric') {
          expect(b.vm.hasValue).toBe(false)
        }
      })
      wrapper.destroy()
    })

    it('falls back to the null battery icon when voltage is unavailable', () => {
      const wrapper = mountBubbles({connected: false})
      expect(wrapper.vm.batteryIcon).toContain('battery_null.svg')
      wrapper.destroy()
    })
  })

  describe('missing readings on a connected machine', () => {
    it('returns null for absent numeric readings', () => {
      const wrapper = mountBubbles({machine: {lastReported: '2023-10-27T11:35:56.000Z'}})
      expect(wrapper.vm.fuelLevel).toBeNull()
      expect(wrapper.vm.machineBatteryVoltage).toBeNull()
      wrapper.destroy()
    })
  })
})
