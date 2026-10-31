import {shallowMount} from '@vue/test-utils'
import Header from '@/components/Header/Header'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_methods = {
  updateCurrentUser: jest.fn(),
  getMenuItems: jest.fn()
}

const $data = {
  user: {
    name: 'name surname',
    givenName: 'name',
    familyName: 'surname',
    fullName: 'full name',
    email: 'my@example.com'
  }
}

beforeEach(() => {
  $_wrapper = shallowMount(Header, {
    mixins: [config(Vue)],
    methods: $_methods,
    data() {
      return {componentConfig: $data}
    }
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/Header/Header.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('proper rendering', () => {
    expect($_methods.updateCurrentUser).toHaveBeenCalled()
    expect($_methods.getMenuItems).toHaveBeenCalled()
  })
})
