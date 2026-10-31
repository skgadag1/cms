import WarrantyComponent from '@/components/common/Warranty/WarrantyComponent'
import WarrantyTableItem from '@/components/common/Warranty/WarrantyTableItem'
import BasicGridTable from '@/components/common/BasicGridTable'
import {shallowMount} from '@vue/test-utils'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import $warrantyData from '../../../../public/mocks/equipment-api/warranty/plans.json'
import Vue from 'vue'

let $_wrapper

const $_methods = {
  fetchWarranties: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(WarrantyComponent, {
    mixin: config(Vue),
    methods: $_methods,
    store: __createMocks({
      getters: {
        getCurrentSerial: jest.fn().mockReturnValue('ALJ812466'),
        isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
        isCurrentMachineValidSubscription: jest.fn().mockReturnValue(true)
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Warranty/WarrantyComponent.vue', () => {
  it('is properly rendered (default)', async () => {
    expect($_wrapper.vm).toBeTruthy()
    expect($_methods.fetchWarranties).toHaveBeenCalled()
  })

  it('renders disabled state', async () => {
    await $_wrapper.setProps({disabled: true})
    await $_wrapper.setData({loading: false})
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
  })

  it('renders all items', async () => {
    await $_wrapper.setData({loading: false, items: $warrantyData})
    expect($_wrapper.findAllComponents(BasicGridTable).length).toBe(1)
    expect($_wrapper.findAllComponents(WarrantyTableItem).length).toBe($warrantyData.length)
  })
})
