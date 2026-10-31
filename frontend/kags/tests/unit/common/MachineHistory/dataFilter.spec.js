import {shallowMount} from '@vue/test-utils'
import DataFilter from '@/components/common/MachineHistory/DataFilter'
import {BFormCheckboxGroup, BFormCheckbox} from 'bootstrap-vue'
import {__createMocks} from '@/store/mocks'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper
let $props = {
  filters: ['In Progress', 'Completed', 'Paused']
}

describe('components/common/MachineHistory/DataFilter.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    $_wrapper = shallowMount(DataFilter, {
      mixin: config(Vue),
      store: __createMocks().store,
      propsData: $props
    })
  })

  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('checking if selected attributes are correct', async () => {
    await $_wrapper.setData({selected: ['In Progress', 'Completed', 'Paused']})
    expect($_wrapper.findComponent(BFormCheckboxGroup).exists()).toBe(true)
    expect($_wrapper.findComponent(BFormCheckboxGroup).attributes('checked')).toBe('In Progress,Completed,Paused')
    expect($_wrapper.findComponent(BFormCheckbox).attributes('checked')).toBe('In Progress,Completed,Paused')

    await $_wrapper.setData({selected: ['Paused']})
    expect($_wrapper.findComponent(BFormCheckboxGroup).attributes('checked')).toBe('Paused')
    expect($_wrapper.findComponent(BFormCheckbox).attributes('checked')).toBe('Paused')
  })
})
