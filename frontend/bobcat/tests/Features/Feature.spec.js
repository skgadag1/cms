import {shallowMount} from '@vue/test-utils'
import cloudinary from '../utils/cloudinary.mock'
import Feature from '@/components/elements/Features/Feature'
import Accordion from '@/components/Accordion'
import mock from '~/server/responses/bobcat-api/pages/components/features'
import linkUtils from '~/plugins/utils/linkUtils'

const featureProps = mock.model.features[0].Comfort['Premium-Cab']
const featureTitle = featureProps['@name']

const wrapper = shallowMount(Feature, {
  propsData: {
    feature: {...featureProps, title: featureTitle},
    isExpanded: true
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary,
          ...cloudinary.$utils,
          ...linkUtils
        },
        $regions: {
          current: {
            language: {
              name: 'en'
            },
            name: 'na'
          }
        },
        $ga4: {
          events: {
            product_features_click() {}
          }
        },
        $config: {}
      }
    }
  },
  stubs: {
    Accordion,
    'b-icon': true,
    'b-collapse': true,
    'b-card-text': true,
    CloudinaryGallery: true
  }
})

const link = wrapper.find('b-collapse-stub a')

describe('Feature component [data props]', () => {
  it('has accordion', () => {
    expect(wrapper.find('section[data-test-id="accordion"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find('h4').text()).toEqual(featureTitle)
  })

  it('has description with mocked label', () => {
    expect(wrapper.find('b-collapse-stub [data-test-id="feature-description"]').text()).toEqual(featureProps.description)
  })

  it('has <a/> tag with mocked label', () => {
    expect(link.text()).toEqual(featureProps.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(link.attributes().href).toBe(featureProps.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(link.attributes().target).toBe(featureProps.link.link.target)
  })

  it('has asset gallery', () => {
    expect(wrapper.find('cloudinarygallery-stub').exists()).toBe(true)
  })

  it('has mocked arrow icon', () => {
    expect(wrapper.find('b-icon-stub[icon="sort-down"]').exists()).toBe(true)
  })
})

describe('Feature component [styles]', () => {
  it('has white background color', () => {
    expect(wrapper.find('section[data-test-id="accordion"]').classes()).toContain('bg-white')
  })

  it('has black title ', () => {
    expect(wrapper.find('div[aria-expanded]').classes()).toContain('text-black')
  })

  it('has start content alignment', () => {
    expect(wrapper.find('div[aria-expanded]').classes()).toContain('justify-content-start')
  })
})

describe('Feature component [functionality]', () => {
  it('can be collapsed', async () => {
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    expect(wrapper.find('div[aria-expanded="false"]').exists()).toBe(true)
  })

  it('can be expanded', async () => {
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    expect(wrapper.find('div[aria-expanded="true"]').exists()).toBe(true)
  })
})
