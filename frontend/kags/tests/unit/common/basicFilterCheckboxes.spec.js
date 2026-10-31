import {shallowMount, mount} from '@vue/test-utils'
import BasicFilterCheckboxes from '@/components/common/BasicFilterCheckboxes'
import {BDropdownForm} from 'bootstrap-vue'
import config from '@/mixins/commonMixin'
import Vue from 'vue'

let $_wrapper

const $_props = {
  title: 'Sample-title-test'
}

const $_options = [
  {label: 'First yes', value: true},
  {label: 'First no', value: false}
]

beforeEach(() => {
  $_wrapper = shallowMount(BasicFilterCheckboxes, {
    mixin: config(Vue),
    propsData: $_props
  })
})

describe('components/common/BasicFilterCheckboxes.vue', () => {
  it('is properly rendered (default)', () => {
    expect($_wrapper.vm.title).toEqual($_props.title)
    expect($_wrapper.vm).toBeTruthy()
  })

  it('renders options', async () => {
    $_wrapper = mount(BasicFilterCheckboxes, {
      mixin: config(Vue),
      propsData: $_props
    })

    await $_wrapper.setProps({options: $_options})

    expect($_wrapper.findAllComponents(BDropdownForm).length).toBe($_options.length)
  })
})
