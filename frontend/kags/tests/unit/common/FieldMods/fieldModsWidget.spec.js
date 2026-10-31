import {mount} from '@vue/test-utils'
import FieldModsWidget from '@/components/common/FieldMods/FieldModsWidget'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import OverlayWrapper from '@/components/common/OverlayWrapper'
import {__createMocks} from '@/store/mocks'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {BLink, BCol} from 'bootstrap-vue'

let $_wrapper

beforeEach(() => {
  $_wrapper = mount(FieldModsWidget, {
    store: __createMocks({
      getters: {}
    }).store,
    mixin: config(Vue)
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/common/FieldModsWidget.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering ListEmptyWidget when component is disabled', async () => {
    const $props = {
      disabled: true
    }

    await $_wrapper.setProps($props)

    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(true)
  })

  it('shows spinner when component is created', async () => {
    const $props = {
      fieldModsPageUrl: 'http://www.google.com'
    }

    await $_wrapper.setProps($props)

    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).exists()).toBe(true)
    expect($_wrapper.findComponent(OverlayWrapper).vm.show).toBe(true)
  })

  it('page url is correctly passed from props', async () => {
    const $props = {
      fieldModsPageUrl: 'http://www.google.com'
    }

    await $_wrapper.setProps($props)
    await $_wrapper.setData({loading: false})

    expect($_wrapper.findComponent(ListEmptyWidget).exists()).toBe(false)
    expect($_wrapper.findComponent(BLink).exists()).toBe(true)
    expect($_wrapper.find('h2 > a').attributes('href')).toBe('http://www.google.com')
  })

  it('full width', async () => {
    const $props = {
      fullWidth: true
    }

    await $_wrapper.setProps($props)
    await $_wrapper.setData({loading: false})

    expect($_wrapper.findComponent(BCol).exists()).toBe(true)
    // To be improved, find a way to check the md property in the component and make sure it's 12.
    expect($_wrapper.find('div[data-test-id=field-mods-widget]').exists()).toBe(true)
    expect($_wrapper.find('div[data-test-id=field-mods-widget]').classes()).toContain('col-md-12')
  })

  it('half width', async () => {
    const $props = {
      fullWidth: false
    }

    await $_wrapper.setProps($props)
    await $_wrapper.setData({loading: false})

    expect($_wrapper.findComponent(BCol).exists()).toBe(true)
    // To be improved, find a way to check the md property in the component and make sure that it's 6.
    expect($_wrapper.find('div[data-test-id=field-mods-widget]').exists()).toBe(true)
    expect($_wrapper.find('div[data-test-id=field-mods-widget]').classes()).toContain('col-md-6')
  })
})
