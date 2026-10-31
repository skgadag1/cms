import {shallowMount} from '@vue/test-utils'
import LocationHistoryComponent from '@/components/common/LocationHistory/LocationHistoryComponent'
import LocationHistoryTableItem from '@/components/common/LocationHistory/LocationHistoryTableItem'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import BasicGridTable from '@/components/common/BasicGridTable'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $locationData from '../../../../public/mocks/iot-api/location/ALJ812466.json'
import {__createMocks} from '@/store/mocks'
import OverlayWrapper from '@/components/common/OverlayWrapper'

let $_wrapper

beforeEach(() => {
  $_wrapper = shallowMount(LocationHistoryComponent, {
    stubs: ['date-picker'],
    mixins: [config(Vue)],
    propsData: {
      disabled: false,
      config: {
        googleMapsAPIKey: 'dummyOne'
      }
    },
    store: __createMocks({
      getters: {
        isCurrentMachineAuthorized: jest.fn().mockReturnValue(true)
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/LocationHistory/LocationHistoryComponent.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering empty widget when component is disabled', async () => {
    await $_wrapper.setProps({disabled: true})
    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(true)
    expect($_wrapper.findAllComponents(ListEmptyWidget).length).toBe(1)
  })

  it('shows spinner when component is created', () => {
    expect($_wrapper.findComponent(OverlayWrapper).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
  })

  it('properly renders table', async () => {
    await $_wrapper.setData({items: {all: $locationData}})
    expect($_wrapper.findComponent(BasicGridTable).exists()).toBe(true)
    expect($_wrapper.findComponent(BasicGridTable).vm.disabled).toEqual($_wrapper.vm.disabled)
    expect($_wrapper.findAllComponents(LocationHistoryTableItem).length).toBe($locationData.length)
  })
})
