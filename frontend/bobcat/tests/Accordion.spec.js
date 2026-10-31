import {shallowMount} from '@vue/test-utils'
import Accordion from '@/components/Accordion'
import mock from '~/server/responses/bobcat-api/pages/components/accordion'

const accordionProps = mock.main['0']
const wrapper = shallowMount(Accordion, {
  propsData: {
    title: accordionProps.title
  },
  stubs: {
    'b-icon': true,
    'b-card': true,
    'b-collapse': true
  }
})

const title = wrapper.find('h3')

describe('Accordion component [data props]', () => {
  it('has h5 title with mocked label', () => {
    expect(title.text()).toEqual(accordionProps.title)
  })

  it('has by default white background color property', () => {
    expect(wrapper.vm.selectedColorVariant.cardBackground).toEqual('bg-white')
  })

  it('has by default black text property', () => {
    expect(wrapper.vm.selectedColorVariant.cardText).toEqual('text-black')
  })

  it('has mocked arrow icon', () => {
    expect(wrapper.find('b-icon-stub[icon="sort-down"]').exists()).toBe(true)
  })
})

describe('Accordion component [functionality]', () => {
  it('can be opened', async () => {
    await wrapper.setData({isVisible: true})
    expect(wrapper.find('div.collapsed').exists()).toBe(false)
  })

  it('can be closed', async () => {
    await wrapper.setData({isVisible: false})
    expect(wrapper.find('div.collapsed').exists()).toBe(true)
  })
})
