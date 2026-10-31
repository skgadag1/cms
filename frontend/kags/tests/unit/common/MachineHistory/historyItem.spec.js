import {shallowMount} from '@vue/test-utils'
import {BBadge} from 'bootstrap-vue'
import HistoryItem from '@/components/common/MachineHistory/HistoryItem'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const _item = {
  session_id: '5df37a3abebe9c471cfa6c10',
  title: 'M3507',
  created_at: '2019-12-13T11:47:06.562Z',
  description: 'Hyraulic Fan Solenoid Open Circuit',
  state: 'In Progress'
}

const $_props = {
  item: _item
}
const $_methods = {
  resumeSession: jest.fn(),
  downloadFile: jest.fn()
}

describe('components/common/MachineHistory/HistoryItem.vue', () => {
  beforeEach(() => {
    $_wrapper = shallowMount(HistoryItem, {
      store: store,
      mixin: config(Vue),
      propsData: $_props,
      methods: $_methods,
      directives: {'b-tooltip': jest.fn()}
    })
  })

  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('checking resume option for item in progress', () => {
    expect($_wrapper.find('[data-test-id="history-item-desktop__resume__5df37a3abebe9c471cfa6c10"]').exists()).toBe(true)
    expect($_wrapper.findComponent(BBadge).exists()).toBe(true)
    expect($_wrapper.findComponent(BBadge).text()).toEqual(_item.state.toUpperCase())
  })
})
