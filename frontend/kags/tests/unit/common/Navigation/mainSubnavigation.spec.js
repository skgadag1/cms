import {mount} from '@vue/test-utils'
import MainSubnavigation from '@/components/common/Navigation/MainSubnavigation'
import {BDropdownItem} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'
import sitemap from '../../../../public/mocks/sitemap.json'
import {$_disabledItemsNonAuth} from './inPageNavigationDesktop.spec'
import {$_disabledItemsSubsNonAuth} from './inPageNavigationDesktop.spec'
import {$_disabledItemsSubsNonConnected} from './inPageNavigationDesktop.spec'

let $_wrapper
const $_sitemapItemExtraContent = {
  title: 'Homepage',
  url: '/kags',
  extraContent: {
    image: 'https://res.cloudinary.com/doosan-bobcat-dev/image/upload/{transformation}/v1566924414/sample',
    alt: 'Some sample image',
    text: 'Some extra content text'
  },
  items: [
    {
      title: 'My machine',
      url: '/machine/machine.html',
      extraContent: {},
      icon: 'loader',
      items: [
        {
          title: 'Troubleshooting',
          url: '/machine/troubleshooting.html',
          icon: 'tools',
          extraContent: {}
        }]
    }]
}

const $_testMenuItem = sitemap.items[0]

const $propsData = {
  counter: 1,
  item: $_testMenuItem
}

beforeEach(() => {
  $_wrapper = mount(MainSubnavigation, {
    mixin: config(Vue),
    propsData: $propsData,
    store: __createMocks().store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Navigation/MainSubnavigation.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering default state', () => {
    const _mi = $_wrapper.findAllComponents(BDropdownItem)

    for (const m in $propsData.item.items) expect(_mi.at(m).text()).toEqual($propsData.item.items[m].title)
    expect(_mi.length).toBe($propsData.item.items.length)
  })

  it('proper rendering of id', async () => {
    await $_wrapper.setProps({
      counter: 123
    })
    expect($_wrapper.findComponent(BDropdownItem).vm.$vnode.key).toEqual('primarymenu_123_subnavigation_0')
  })

  it('proper rendering extraContent', async () => {
    await $_wrapper.setProps({counter: 3, item: $_sitemapItemExtraContent})
    expect($_wrapper.find('.subnavigation__2cols > .col-6').exists()).toBe(true)
  })

  it('proper rendering disabled', () => {
    $_wrapper = mount(MainSubnavigation, {
      mixin: config(Vue),
      propsData: {
        counter: 2,
        item: $_testMenuItem
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(false),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false),
          isCurrentMachineConnected: jest.fn().mockReturnValue(true)
        }
      }).store
    })

    expect($_wrapper.findAllComponents(BDropdownItem).length).toBe($_testMenuItem.items.length)
    expect($_wrapper.findAll('.disabled').length).toEqual($_disabledItemsNonAuth.length)

    $_wrapper = mount(MainSubnavigation, {
      mixin: config(Vue),
      propsData: {
        item: $_testMenuItem
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(false)
        }
      }).store
    })

    expect($_wrapper.findAll('.disabled').length).toEqual($_disabledItemsSubsNonAuth.length)

    $_wrapper = mount(MainSubnavigation, {
      mixin: config(Vue),
      propsData: {
        item: $_testMenuItem
      },
      store: __createMocks({
        getters: {
          isCurrentMachineAuthorized: jest.fn().mockReturnValue(true),
          isCurrentMachineValidSubscription: jest.fn().mockReturnValue(true),
          isCurrentMachineConnected: jest.fn().mockReturnValue(false)
        }
      }).store
    })

    expect($_wrapper.findAll('.disabled').length).toEqual($_disabledItemsSubsNonConnected.length)
  })
})
