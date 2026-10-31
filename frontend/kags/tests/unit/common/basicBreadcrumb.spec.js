import {mount} from '@vue/test-utils'
import BasicBreadcrumb from '@/components/common/BasicBreadcrumb'
import store from '@/store'
import {BLink} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const _testMenuItems = [
  {
    title: 'Machine page',
    url: '#machine',
    icon: 'tools',
    extraContent: {}
  }, {
    title: 'Parts',
    icon: 'tools',
    url: '#parts',
    active: true,
    extraContent: {},
    items: [{
      title: 'Open cases',
      url: '#one',
      icon: 'files',
      extraContent: {}
    }, {
      title: 'Machine IQ',
      url: '#two',
      icon: 'wifi',
      active: true,
      extraContent: {}
    }, {
      title: 'Schematics Library',
      url: '#tree',
      icon: '/assets/img/icons/blitz.svg',
      extraContent: {}
    }, {
      title: 'Tools & softwares',
      url: '#four',
      icon: '/assets/img/icons/download.svg',
      extraContent: {}
    }]
  }, {
    title: 'Third',
    url: '#parts',
    icon: 'tools',
    extraContent: {},
    active: false,
    items: [{
      title: 'Open cases',
      url: '#one',
      icon: 'files',
      extraContent: {}
    }]
  }
]

const $data = {
  loading: false,
  menuitems: _testMenuItems
}

const $_methods = {
  getMenuItems: jest.fn()
}

beforeEach(() => {
  $_wrapper = mount(BasicBreadcrumb, {
    store: store,
    mixin: config(Vue),
    methods: $_methods,
    data() {
      return $data
    }
  })
})

describe('components/common/BasicBreadcrumb.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering menu breadcrumbs', () => {
    const _mi = $_wrapper.findAllComponents(BLink)
    const selectedBreadcrumbs = ['Parts', 'Machine IQ']
    for (const m in selectedBreadcrumbs) expect(_mi.at(m).text()).toEqual(selectedBreadcrumbs[m])
    expect(_mi.length).toBe(selectedBreadcrumbs.length)
  })
})
