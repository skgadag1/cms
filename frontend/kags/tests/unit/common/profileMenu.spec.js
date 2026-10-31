import {shallowMount} from '@vue/test-utils'
import ProfileMenu from '@/components/common/ProfileMenu'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper
let $props = {
  user: {
    name: 'Your User',
    givenName: 'Your',
    familyName: 'User',
    fullName: 'Your User'
  }
}

beforeEach(() => {
  $_wrapper = shallowMount(ProfileMenu, {
    mixin: config(Vue),
    propsData: $props
  })
})

describe('components/common/ProfileMenu.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('render correctly with default data', () => {
    expect($_wrapper.find('#menu-profile').isVisible()).toBe(false)
    expect($_wrapper.find('[icon="sort-down"]').exists()).toBe(true)
    expect($_wrapper.find('.cursor-pointer').text()).toEqual($props.user.fullName)
  })

  it('showing profile data: isOpened', async () => {
    await $_wrapper.setData({
      isOpened: true
    })
    expect($_wrapper.find('#menu-profile').isVisible()).toBe(true)
    expect($_wrapper.find('[icon="sort-up"]').exists()).toBe(true)
  })

  it('opening profile works correctly', async () => {
    expect($_wrapper.find('#menu-profile').isVisible()).toBe(false)
    expect($_wrapper.find('[icon="sort-down"]').exists()).toBe(true)
    expect($_wrapper.find('.modal-backdrop').exists()).toBe(true)
    await $_wrapper.find('.cursor-pointer').trigger('click')
    expect($_wrapper.find('#menu-profile').isVisible()).toBe(true)
    expect($_wrapper.find('[icon="sort-up"]').exists()).toBe(true)
  })
})
