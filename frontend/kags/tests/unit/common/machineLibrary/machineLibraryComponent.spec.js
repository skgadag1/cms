import {shallowMount, mount} from '@vue/test-utils'
import MachineLibraryComponent from '@/components/common/Machine/MachineLibraryComponent'
import GroupedManuals from '@/components/common/GroupedManuals'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import config from '@/mixins/commonMixin'
import {__createMocks} from '@/store/mocks'
import Vue from 'vue'
import $options from '../../../../public/mocks/product-api/documents/manuals.json'

let $_wrapper

const $_config = {
  'clearFilters': 'Clear all'
}

const $_methods = {
  fetchManuals: jest.fn()
}

beforeEach(() => {
  $_wrapper = shallowMount(MachineLibraryComponent, {
    mixin: config(Vue),
    propsData: {config: $_config, disabled: false},
    methods: $_methods,
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

describe('components/common/Machine/MachineLibraryComponent.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.findAllComponents(ListEmptyWidget).length).toBe(1)
    expect($_methods.fetchManuals).toHaveBeenCalled()
  })

  it('is rendering loading state', async () => {
    await $_wrapper.setProps({disabled: false})

    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
    expect($_wrapper.findAllComponents(OverlayWrapper)).toHaveLength(1)
  })

  it('is rendering items', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({loading: false, error: false, mData: $options})

    expect($_wrapper.findComponent(GroupedManuals).exists()).toBe(true)
  })

  it('is rendering no items message', async () => {
    await $_wrapper.setProps({disabled: false})
    await $_wrapper.setData({loading: false, error: true, mData: {}})

    expect($_wrapper.find('[data-test-id=machine-library-noitems]').exists()).toBe(true)
  })

  it('renders error', async () => {
    $_wrapper = mount(MachineLibraryComponent, {
      mixin: config(Vue),
      propsData: {
        config: $_config,
        disabled: false
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true)
        }
      }).store
    })
    await $_wrapper.setData({loading: false, error: true})

    expect($_wrapper.findComponent(GroupedManuals).exists()).toBe(false)
    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(false)
    expect($_wrapper.find('[data-test-id="machine-library-error"]').exists()).toBe(true)
    expect($_wrapper.findAll('[data-test-id="machine-library-error"]').length).toBe(1)
  })
})

