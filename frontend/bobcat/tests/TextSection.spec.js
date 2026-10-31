import {shallowMount} from '@vue/test-utils'
import TextSection from '@/components/TextSection'
import mock from '~/server/responses/bobcat-api/pages/components/textSection'

const props = mock.main['0']

const wrapper = shallowMount(TextSection, {
  propsData: {
    text: props.text
  },
  stubs: {
    LazyHydrate: true
  }
})

const htmlWrapper = wrapper.find('div[data-test-id="article-text-section-wrapper"]')

describe('TextSection component [data props]', () => {
  it('has injected html', () => {
    expect(htmlWrapper.html()).toContain(props.text)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})
