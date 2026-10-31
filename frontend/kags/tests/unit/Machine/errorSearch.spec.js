import {mount} from '@vue/test-utils'
import ErrorSearch from '@/components/common/Machine/ErrorSearch'
import ErrorInput from '@/components/common/ErrorInput'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'

let $_wrapper
const $_props = {
  action: 'http://www.tbscg.com',
  clearOnInit: false,
  ajax: false
}
const $_methods = {
  saveKeyword: jest.fn(),
  submitForm: jest.fn(),
  emitSubmit: jest.fn()
}

describe('components/common/Machine/ErrorSearch.vue', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    $_wrapper = mount(ErrorSearch, {
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
  })

  it('clear keyword on init', () => {
    $_wrapper = mount(ErrorSearch, {
      mixin: config(Vue),
      store: __createMocks().store,
      propsData: {clearOnInit: true},
      methods: $_methods
    })
    expect($_methods.saveKeyword).toBeCalled()
  })
})
