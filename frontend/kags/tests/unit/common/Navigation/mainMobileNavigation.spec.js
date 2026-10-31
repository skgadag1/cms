import {mount} from '@vue/test-utils'
import MainMobileNavigation from '@/components/common/Navigation/MainMobileNavigation'
import {BNavItem} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'
import sitemap from '../../../../public/mocks/sitemap.json'
import {$_disabledItemsSubsNonConnected} from './inPageNavigationDesktop.spec'

let $_wrapper

const _testMenuItems = sitemap.items

beforeEach(() => {
  $_wrapper = mount(MainMobileNavigation, {
    mixin: config(Vue),
    propsData: {
      menuitems: _testMenuItems
    },
    store: __createMocks({
      getters: {
        isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false),
        isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
        isCurrentMachineConnected: jest.fn().mockReturnValue(false)
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Navigation/MainMobileNavigation.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering main menu items', () => {
    expect($_wrapper.findAll('#bobcat-nav__mobile > ul > li').length).toBe(_testMenuItems.length)
  })

  it('proper rendering subnavigations', () => {
    expect($_wrapper.findAll('.subnavigation').length).toBe(_testMenuItems.filter(x => x.items).length)
  })

  it('proper rendering number of subnavigation items', () => {
    const _sItems = _testMenuItems.filter(x => x.items)[0].items
    expect($_wrapper.find('.subnavigation').findAllComponents(BNavItem).length).toBe(_sItems.length)
  })

  it('proper rendering subnavigation disabled items', () => {
    expect($_wrapper.findAll('.disabled').length).toEqual($_disabledItemsSubsNonConnected.length)
  })
})
