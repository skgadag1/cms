import {shallowMount} from '@vue/test-utils'
import CTA from '@/components/CTA'
import mock from '~/server/responses/bobcat-api/pages/components/cta'
import linkUtils from '~/plugins/utils/linkUtils'

const ctaProps = mock.main['0']
const wrapper = shallowMount(CTA, {
  propsData: {
    title: ctaProps.title,
    htag: ctaProps.htag,
    paragraph: ctaProps.paragraph,
    link: ctaProps.link
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          getLinkField: linkUtils.getLinkField
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: {name: 'en'}
          }
        },
        $ga4: {
          events: {
            cta_banner_click: jest.fn()
          }
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true
  }
})

const title = wrapper.find('h4')
const paragraph = wrapper.find('.cta-paragraph')
const ctaButton = wrapper.find('[data-test-id="cta-button"]')

describe('CTA component [data props]', () => {
  it('has h4 title with mocked label', () => {
    expect(title.exists()).toBe(true)
    expect(title.text()).toEqual(ctaProps.title)
  })

  it('has paragraph with mocked content', () => {
    expect(paragraph.exists()).toBe(true)
    expect(paragraph.text()).toContain('COVID-19')
  })

  it('has CTA button (DynamicButton) with mocked label', () => {
    expect(ctaButton.exists()).toBe(true)
    expect(ctaButton.text()).toEqual(ctaProps.link.link.customLabel)
  })

  it('CTA button has mocked href attribute', () => {
    expect(ctaButton.attributes().href).toBe(ctaProps.link.link.customUrl)
  })

  it('CTA button has mocked target attribute', () => {
    expect(ctaButton.attributes().target).toBe(ctaProps.link.link.target)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})

describe('CTA component [style props]', () => {
  it('has text centred when textAlignment is center', () => {
    const contentWrapper = wrapper.find('.text-center')
    expect(contentWrapper.exists()).toBe(true)
  })

  it('has CTA button with classic variant for legacy link', () => {
    expect(ctaButton.exists()).toBe(true)
    expect(ctaButton.props('variant')).toBe('classic')
    expect(ctaButton.props('color')).toBe('white')
  })

  it('has primary background color when color is red (default)', () => {
    expect(wrapper.find('section.bg-primary').exists()).toBe(true)
  })

  it('has section with text-white for red color variant', () => {
    expect(wrapper.find('section').classes()).toContain('text-white')
  })
})
