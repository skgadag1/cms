import {shallowMount} from '@vue/test-utils'
import HistoryWidget from '@/components/common/MachineHistory/HistoryWidget'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import http from '@/utils'
import flushPromises from 'flush-promises'
import sessions from '../../../../public/mocks/product-api/troubleshooting/sessions.json'

let $_wrapper

const originalItems = [{}, {}, {}, {}, {}, {}, {}, {}, {}]
const $_methods = {
  getHistory: jest.fn()
}

beforeEach(() => {
  jest.clearAllMocks()
  $_wrapper = shallowMount(HistoryWidget, {
    store: store,
    mixin: config(Vue),
    methods: $_methods
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/MachineHistory/HistoryWidget.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('checking state: default', async () => {
    await $_wrapper.setData({items: {displayed: originalItems}})
    await $_wrapper.setProps({widgetMode: false})

    expect($_methods.getHistory).toHaveBeenCalled()
    expect(originalItems.length).toEqual($_wrapper.vm.items.displayed.length)
    expect($_wrapper.find('[data-test-id="show-all-link"]').exists()).toBe(false)
  })

  it('checking state: widgetHistory', async () => {
    http.get = jest.fn().mockImplementation(() => Promise.resolve({data: sessions}))
    $_wrapper = shallowMount(HistoryWidget, {
      store: store,
      mixin: config(Vue)
    })
    await $_wrapper.setData({
      widgetMaxElements: 3
    })
    await $_wrapper.setProps({widgetMode: true})

    await flushPromises()
    expect(http.get).toHaveBeenCalled()
    expect($_wrapper.find('[data-test-id="show-all-link"]').exists()).toBe(true)
    expect($_wrapper.vm.items.displayed.length).toEqual(3)
  })
})
