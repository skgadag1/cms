import {shallowMount} from '@vue/test-utils'
import MaintenanceScheduleComponent from '@/components/common/MaintenanceScheduleComponent'
import BasicGridTable from '@/components/common/BasicGridTable'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import $_items from '../../../public/mocks/product-api/service-details.json'
import Vue from 'vue'
import OverlayWrapper from '@/components/common/OverlayWrapper'

let $_wrapper

const $_methods = {
  loadData: jest.fn(),
  downloadServiceSchedulePdf: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(MaintenanceScheduleComponent, {
    store: __createMocks({
      getters: {
        getCurrentSerial: jest.fn().mockReturnValue('123'),
        isCurrentMachineAuthorized: jest.fn().mockReturnValue(true)
      }
    }).store,
    mixin: config(Vue),
    propsData: {
      items: $_items
    },
    methods: $_methods
  })
})

describe('components/common/MaintenanceScheduleComponent.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('properly renders disabled state', async () => {
    await $_wrapper.setData({loading: false})
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toBe(true)
    expect($_wrapper.find('[data-test-id="maintenance-disabled"]').exists()).toBe(true)
  })

  it('properly renders normal state', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({loading: false})
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(false)
    expect($_methods.loadData).toHaveBeenCalled()
  })

  it('properly renders download link', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({
      loading: false,
      items: $_items,
      error: {
        present: false
      }
    })
    expect($_wrapper.find('[data-test-id="maintenance-download-section"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="maintenance-download-link"]').exists()).toBe(true)
    await $_wrapper.find('[data-test-id="maintenance-download-link"]').trigger('click')
    expect($_methods.downloadServiceSchedulePdf).toHaveBeenCalled()
  })

  it('properly renders loading state', async () => {
    await $_wrapper.setProps({disabled: false})
    expect($_wrapper.findComponent(OverlayWrapper).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
  })

  it('properly renders ajax error state', async () => {
    await $_wrapper.setData({loading: false})
    await $_wrapper.setProps({disabled: false})
    expect($_wrapper.find('[data-test-id="maintenance-no-results"]').exists()).toBe(true)
  })

  it('properly renders general error state', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({
      loading: false,
      items: $_items,
      error: {
        present: true
      }
    })
    expect($_wrapper.find('[data-test-id="maintenance-error"]').exists()).toBe(true)
  })

  it('properly renders items', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({
      loading: false,
      items: $_items,
      error: {
        present: false
      }
    })

    expect($_wrapper.vm.items.length).toEqual($_items.length)
    expect($_wrapper.find('[data-test-id="maintenance-intervals-table"]').exists()).toBe(true)
    expect($_wrapper.findAll('.grid-table-row').length).toEqual($_items.length)
  })
})
