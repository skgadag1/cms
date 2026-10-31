import {mount} from '@vue/test-utils'
import AddSerialControl from '@/components/common/Machine/AddSerialControl'
import store from '@/store'
import {BBadge, BFormInput, BInputGroupPrepend} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_methods = {
  addSerial: jest.fn(),
  enableEditMode: jest.fn()
}

beforeEach(() => {
  $_wrapper = mount(AddSerialControl, {
    store: store,
    mixin: config(Vue),
    methods: $_methods
  })
})

describe('components/common/Machine/AddSerialControl.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('renders exposed', async () => {
    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    await $_wrapper.setData({
      editMode: false,
      input: {
        invalidSerial: true,
        internalSerial: '',
        serialData: null,
        errorMessage: 'Wrong serial number format'
      }
    })
    await $_wrapper.setProps({
      disabled: true,
      empty: true
    })
    expect($_wrapper.find('.modal-backdrop__partial').exists()).toBeTruthy()
    expect($_wrapper.find('.add-serial-control.disabled').exists()).toBeTruthy()
    expect($_wrapper.findComponent(BBadge).exists()).toBeTruthy()
  })

  it('shows link by default', async () => {
    expect($_wrapper.find('[data-test-id="add-serial-link__desktop"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="add-serial-input__inactive"]').exists()).toBe(false)

    await $_wrapper.find('[data-test-id="add-serial-link__desktop"]').trigger('click')
    expect($_methods.enableEditMode).toBeCalled()
  })

  it('renders input mode properly', async () => {
    await $_wrapper.setProps({inputMode: true})
    expect($_wrapper.find('[data-test-id="add-serial-input__inactive"]').exists()).toBe(true)
    expect($_wrapper.find('[data-test-id="add-serial-link__desktop"]').exists()).toBe(false)
    await $_wrapper.find('[data-test-id="add-serial-input__inactive"]').trigger('click')
    expect($_methods.enableEditMode).toBeCalled()
  })

  it('checks proper state: edit', async () => {
    await $_wrapper.setData({
      editMode: true
    })
    expect($_wrapper.find('.modal-backdrop').exists()).toBeTruthy()
    expect($_wrapper.find('.modal-backdrop__partial').exists()).toBe(false)
    expect($_wrapper.find('.exposed').exists()).toBeTruthy()
    expect($_wrapper.findComponent(BFormInput).exists()).toBeTruthy()
  })

  it('is adding serial', async () => {
    await $_wrapper.setData({
      editMode: true
    })
    $_wrapper.findComponent(BFormInput).setValue('ABCD789')
    expect($_wrapper.vm.$data.input.internalSerial).toEqual('ABCD789')
    await $_wrapper.findComponent(BInputGroupPrepend).trigger('click')
    expect($_methods.addSerial).toHaveBeenCalled()
  })
})
