import {mount} from '@vue/test-utils'
import {BButton} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'
import {__createMocks} from '@/store/mocks'
import FeedbackWidgetFooter from '@/components/common/Feedback/FeedbackWidgetFooter'

let $_wrapper

const $_methods = {
  submit: jest.fn()
}

beforeEach(() => {
  $_wrapper = mount(FeedbackWidgetFooter, {
    mixin: config(Vue),
    store: __createMocks().store,
    methods: $_methods
  })
})

afterEach(() => {
  $_wrapper.destroy()
})

describe('components/Feedback/FeedbackWidgetFooter.vue', () => {
  it('is vue instance', () => {
    expect($_wrapper.vm).toBeTruthy()
  })

  it('should submit the data if button is clicked', async () => {
    expect($_wrapper.findComponent(BButton).exists()).toBe(true)
    await $_wrapper.findComponent(BButton).trigger('click')
    expect($_methods.submit).toHaveBeenCalled()
  })
})
