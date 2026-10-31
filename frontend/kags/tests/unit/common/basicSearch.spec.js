import {mount} from '@vue/test-utils'
import BasicSearch from '@/components/common/BasicSearch'
import ErrorInput from '@/components/common/ErrorInput'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'

let $_wrapper
const $_props = {
  action: 'http://www.tbscg.com',
  message: 'this text should be here',
  clearOnInit: false,
  ajax: false
}
const $_methods = {
  saveKeyword: jest.fn(),
  submitForm: jest.fn(),
  emitSubmit: jest.fn()
}

describe('components/common/BasicSearch.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    $_wrapper = mount(BasicSearch, {
      mixin: config(Vue),
      store: __createMocks().store,
      propsData: $_props
    })
  })

  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('default rendering', () => {
    expect($_wrapper.find('[data-test-id="troubleshooting-btn"]').exists()).toBeFalsy()
    expect($_wrapper.findAllComponents(ErrorInput).length).toBe(1)
    expect($_wrapper.findAllComponents(BButton).length).toBe(0)
    expect($_wrapper.find('h2').text()).toEqual($_props.message)
  })

  it('ErrorInput submit handled', () => {
    $_wrapper.setMethods($_methods)
    $_wrapper.findComponent(ErrorInput).vm.$emit('submit')
    expect($_methods.submitForm).toBeCalled()
  })

  it('form submit handler', async () => {
    $_wrapper.setMethods($_methods)
    await $_wrapper.find('form').trigger('submit')
    expect($_methods.emitSubmit).toBeCalled()
  })

  it('ajax submit', async () => {
    const _fakeEvent = {preventDefault: jest.fn()}
    await $_wrapper.setProps({ajax: true})
    $_wrapper.vm.emitSubmit(_fakeEvent)
    expect(_fakeEvent.preventDefault).toBeCalled()
  })
})
