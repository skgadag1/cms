import {shallowMount, mount} from '@vue/test-utils'
import InPageNavigationMobile from '@/components/common/Navigation/InPageNavigationMobile'
import {BNavItem} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import sitemap from '../../../../public/mocks/sitemap.json'
import {__createMocks} from '@/store/mocks'
import {$_disabledItemsAuthConnected, $_disabledItemsSubsNonAuth} from './inPageNavigationDesktop.spec'
import {$_disabledItemsSubsNonConnected} from './inPageNavigationDesktop.spec'

let $_wrapper
const $_testMenuItems = sitemap.items[0].items

beforeEach(() => {
  $_wrapper = shallowMount(InPageNavigationMobile, {
    mixin: config(Vue),
    propsData: {
      loading: false,
      items: sitemap.items
    },
    store: __createMocks().store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Navigation/InPageNavigationMobile.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('properly renders disabled state', async () => {
    await $_wrapper.setProps({disabled: true})
    expect($_wrapper.find('.mask.disabled').exists()).toBe(true)
  })

  it('properly renders menu items', () => {
    $_wrapper = mount(InPageNavigationMobile, {
      mixin: config(Vue),
      propsData: {
        loading: false,
        items: $_testMenuItems
      },
      store: __createMocks().store
    })
    const _mi = $_wrapper.findAllComponents(BNavItem)
    for (const i in $_testMenuItems) expect(_mi.at(i).text()).toEqual($_testMenuItems[i].title)
    expect(_mi.length).toBe($_testMenuItems.length)
  })

  it('properly renders disabled items', () => {
    $_wrapper = mount(InPageNavigationMobile, {
      mixin: config(Vue),
      propsData: {
        loading: false,
        items: $_testMenuItems
      },
      store: __createMocks({
        getters: {
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false),
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineConnected: jest.fn().mockReturnValue(true)
        }
      }).store
    })

    expect($_wrapper.findAllComponents(BNavItem).length).toBe($_testMenuItems.length)
    expect($_wrapper.findAll('.nav-link').length).toBe($_testMenuItems.length)
    expect($_wrapper.findAll('.disabled').length).toBe($_disabledItemsAuthConnected.length)

    $_wrapper = mount(InPageNavigationMobile, {
      mixin: config(Vue),
      propsData: {
        loading: false,
        items: $_testMenuItems
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false)
        }
      }).store
    })

    expect($_wrapper.findAll('.disabled').length).toBe($_disabledItemsSubsNonAuth.length)

    $_wrapper = mount(InPageNavigationMobile, {
      mixin: config(Vue),
      propsData: {
        loading: false,
        items: $_testMenuItems
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(true),
          isCurrentMachineConnected: jest.fn().mockReturnValue(false)
        }
      }).store
    })

    expect($_wrapper.findAll('.disabled').length).toBe($_disabledItemsSubsNonConnected.length)
  })
})
