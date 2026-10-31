import {shallowMount} from '@vue/test-utils'
import {BButton, BSpinner} from 'bootstrap-vue'
import HistorySessionDetailsModal from '@/components/common/MachineHistory/HistorySessionDetailsModal'
import store from '@/store'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import * as _details from '../../../../public/mocks/product-api/troubleshooting/session-summary.json'
import * as _detailsPaused from '../../../../public/mocks/product-api/troubleshooting/session-summary-paused.json'

let $_wrapper
const $_props = {
  details: _details.default,
  loading: false
}

beforeEach(() => {
  $_wrapper = shallowMount(HistorySessionDetailsModal, {
    store: store,
    mixin: config(Vue),
    propsData: $_props
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/MachineHistory/HistorySessionDetailsModal.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('checking if download button is present', () => {
    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    expect($_wrapper.findAllComponents(BButton).length).toEqual(1)
  })

  it('checking if download and resume buttons are present for paused session', async () => {
    await $_wrapper.setProps({details: _detailsPaused.default, loading: false})
    expect($_wrapper.findAllComponents(BButton).length).toEqual(2)
  })

  it('checking if spinner is present when session resuming', async () => {
    await $_wrapper.setProps({details: _detailsPaused.default, loading: false})
    $_wrapper.vm.session.resuming = true
    await $_wrapper.vm.$nextTick()
    expect($_wrapper.findComponent(BSpinner).exists()).toBe(true)

    await $_wrapper.setProps({details: _details.default})
    expect($_wrapper.findComponent(BSpinner).exists()).toBe(false)
  })
})
