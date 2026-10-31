import {shallowMount} from '@vue/test-utils'
import MainDesktopNavigation from '@/components/common/Navigation/MainDesktopNavigation'
import MainSubnavigation from '@/components/common/Navigation/MainSubnavigation'
import {BNavItem} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'

let $_wrapper

const _testMenuItems = [
  {
    'title': 'Parts',
    'icon': 'tools',
    'url': '/kags/parts',
    'extraContent': {
      'image': 'https://res.cloudinary.com/doosan-bobcat-dev/image/upload/{transformation}/v1566924414/sample',
      'alt': 'Some sample image',
      'text': 'Some extra content text'
    },
    'items': [
      {
        'title': 'Parts',
        'icon': 'tools',
        'url': '/kags/parts',
        'extraContent': {
          'image': 'https://res.cloudinary.com/doosan-bobcat-dev/image/upload/{transformation}/v1566924414/sample',
          'alt': 'Some sample image',
          'text': 'Some extra content text'
        },
        'items': null,
        'active': false
      },
      {
        'title': 'Field mods',
        'icon': 'comments',
        'url': '/kags/parts/field_mods',
        'extraContent': {
          'image': 'https://res.cloudinary.com/doosan-bobcat-dev/image/upload/{transformation}/v1561141943/success_quote'
        }
      },
      {
        'title': 'Reman program',
        'url': '/kags/parts/reaman_program',
        'extraContent': {}
      },
      {
        'title': 'Policy & Procedures',
        'icon': 'file-pdf',
        'url': '/kags/parts/policy',
        'extraContent': {}
      }
    ]
  }
]

beforeEach(() => {
  $_wrapper = shallowMount(MainDesktopNavigation, {
    mixin: config(Vue),
    propsData: {
      menuitems: _testMenuItems
    },
    store: __createMocks({
      getters: {
      }
    }).store
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/Navigation/MainDesktopNavigation.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering main menu items', () => {
    const _mi = $_wrapper.findAllComponents(BNavItem)

    for (const m in _testMenuItems) expect(_mi.at(m).text()).toEqual(_testMenuItems[m].title)
    expect(_mi.length).toBe(_testMenuItems.length)
  })

  it('proper rendering subnavigations', () => {
    expect($_wrapper.findAllComponents(MainSubnavigation).length).toBe(_testMenuItems.filter(x => x.items).length)
  })
})
