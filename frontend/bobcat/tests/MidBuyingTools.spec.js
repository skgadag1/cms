import {shallowMount} from '@vue/test-utils'
import MidBuyingTools from '@/components/MidBuyingTools'
import mock from '~/server/responses/bobcat-api/pages/components/midBuyingTools'
import linkUtils from '~/plugins/utils/linkUtils'

const props = mock.main['0']
const wrapper = shallowMount(MidBuyingTools, {
  propsData: {
    tools: props.tools
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          getLinkField: linkUtils.getLinkField
        },
        $regions: {
          current: {
            language: {
              name: 'en'
            },
            name: 'na'
          }
        }
      }
    }
  },
  stubs: {
    'b-icon': true,
    WarningAlert: true
  }
})

const mappedTools = props?.tools['@nodes']?.map(el => ({...props.tools[el], link: linkUtils.getLinkField(props.tools[el]?.link)}) || [])

describe('Mid-buying-tools component [elements]', () => {
  it('has wrapper mid-buying-tools', () => {
    expect(wrapper.find('aside[data-test-id="mid-buying-tools"]').exists()).toBe(true)
  })

  it('has static wrapper', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"]').exists()).toBe(true)
  })

  it('static wrapper renders midBuyingTools', () => {
    expect(wrapper.findAll('div[data-test-id="mid-buying-tools-static-wrapper"] ul li').length).toEqual(mappedTools.length)
  })

  it('midBuyingTool has <a/> tag with mocked label', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"] ul li a').text()).toEqual(mappedTools[0].link.label)
  })

  it('midBuyingTool <a/> tag has mocked href attribute', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"] ul li a').attributes().href).toEqual(mappedTools[0].link.url)
  })

  it('midBuyingTool <a/> tag has mocked target attribute', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"] ul li a').attributes().target).toEqual(mappedTools[0].link.target)
  })

  it('midBuyingTool <a/> tag has mocked rel attribute', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"] ul li a').attributes().rel).toEqual(mappedTools[0].link.target === '_blank' ? 'noopener nofollow' : null)
  })

  it('midBuyingTool has mocked icon', () => {
    expect(wrapper.find('div[data-test-id="mid-buying-tools-static-wrapper"] ul li b-icon-stub').attributes('icon')).toEqual(mappedTools[0].icon)
  })

  it('has sticky wrapper when sticky proper is true', async () => {
    await wrapper.setData({isSticky: true})
    expect(wrapper.find('div[data-test-id="mid-buying-tools-sticky-wrapper"]').exists()).toBe(true)
  })

  it('sticky wrapper when dropdown is hidden has arrow button', async () => {
    await wrapper.setData({midBuyingToolsInDropdown: ['one', 'two', 'three']})
    expect(wrapper.find('div[data-test-id="mid-buying-tools-sticky-wrapper"] button span b-icon-stub').attributes('icon')).toEqual('sort-down')
  })

  it('sticky wrapper when dropdown is not hidden has dropdown', async () => {
    await wrapper.setData({isDropdownHidden: false})
    expect(wrapper.find('.dropdown').exists()).toBe(true)
  })

  it('sticky wrapper has mobile list with 3 items and third one is arrow button', async () => {
    await wrapper.setData({midBuyingToolsInDropdown: ['one', 'two', 'three']})
    expect(wrapper.findAll('.mobile-list li').length).toEqual(3)
  })

  it('sticky wrapper when dropdown is not hidden has mobile overlay', () => {
    expect(wrapper.findAll('.overlay').exists()).toBe(true)
  })
})
