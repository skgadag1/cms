import {shallowMount} from '@vue/test-utils'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'

const factory = (values = {}) => {
  return shallowMount(BasicGridTableHeader, {
    propsData: {
      ...values
    }
  })
}

describe('components/common/BasicGridTableHeader.vue', () => {
  it('does not render arrow when header not sortable', () => {
    const $_wrapper = factory({name: 'head'})
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.find('.carret').exists()).toBe(false)
  })

  it('render arrow when sortable', () => {
    const $_wrapper = factory({name: 'head', sortable: true, sortingDir: '-'})
    expect($_wrapper.vm).toBeTruthy()
    expect($_wrapper.find('.carret').exists()).toBe(true)
  })
})
