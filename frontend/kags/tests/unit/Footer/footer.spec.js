import {shallowMount} from '@vue/test-utils'
import Footer from '@/components/Footer/Footer.vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

beforeEach(() => {
  $_wrapper = shallowMount(Footer, {
    stubs: ['b-img'],
    mixin: config(Vue),
    mocks: {
      config: {
        contactUrl: 'url',
        privacyPolicyUrl: 'privacyPolicyUrl',
        termOfUseUrl: 'termOfUseUrl'
      }
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/Footer.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm).toBeTruthy()
  })
})
