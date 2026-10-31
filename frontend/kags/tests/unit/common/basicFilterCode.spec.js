import {shallowMount, mount} from '@vue/test-utils'
import BasicFilterCode from '@/components/common/BasicFilterCode'
import {BInputGroup} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import $_codes from '../../../public/mocks/product-api/faultCodes/B10.json'

let $_wrapper

const $_props = {
  title: 'Code-title-test',
  placeholder: 'placeholder-test',
  codes: $_codes
}

beforeEach(() => {
  $_wrapper = shallowMount(BasicFilterCode, {
    stubs: ['b-popover'],
    mixin: config(Vue),
    propsData: $_props
  })
})

afterEach(() => {
  jest.clearAllMocks()
})

describe('components/common/BasicFilterCode.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm.title).toEqual($_props.title)
    expect($_wrapper.vm).toBeTruthy()
  })

  it('is rendering opened state', async () => {
    $_wrapper = mount(BasicFilterCode, {
      mixin: config(Vue),
      propsData: $_props
    })

    await $_wrapper.find('.dropdown-toggle').trigger('click')

    expect($_wrapper.find('[id=popover-code-autocomplete]').exists()).toBe(true)
    expect($_wrapper.findComponent(BInputGroup).exists()).toBe(true)
  })

  it('emits updated with proper keyword', async () => {
    await $_wrapper.setData({keyword: 'B1001-96', codes: $_codes})

    expect($_wrapper.emitted().updated).toBeFalsy()
    $_wrapper.vm.handleHide()
    expect($_wrapper.emitted().updated.length).toBe(1)
  })

  it('do not emits updated when wrong code', async () => {
    const _emitted = $_wrapper.emitted()
    await $_wrapper.setData({keyword: 'wrong', codes: $_codes})

    await $_wrapper.vm.$nextTick()
    expect(_emitted.updated).toBeFalsy()
    $_wrapper.vm.handleHide()
    await $_wrapper.vm.$nextTick()
    expect(_emitted.updated).toBeFalsy()
  })

  it('emits updated event when keyword changes', async () => {
    const _emitted = $_wrapper.emitted()
    await $_wrapper.setData({keyword: '', codes: $_codes})
    expect(_emitted.updated).toBeFalsy()

    $_wrapper.vm.handleChange('wrong1')
    await $_wrapper.vm.$nextTick()
    expect(_emitted.updated).toBeFalsy()

    $_wrapper.vm.handleChange($_codes[1].code)
    await $_wrapper.vm.$nextTick()
    expect(_emitted.updated.length).toBe(1)
  })
})
