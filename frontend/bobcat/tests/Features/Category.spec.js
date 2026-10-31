import {shallowMount} from '@vue/test-utils'
import cloudinary from '../utils/cloudinary.mock'
import FeatureCategory from '@/components/elements/Features/FeatureCategory'
import Feature from '@/components/elements/Features/Feature'
import Accordion from '@/components/Accordion'
import mock from '~/server/responses/bobcat-api/pages/components/features'
import linkUtils from '~/plugins/utils/linkUtils'

const category = mock.model.features[0].Comfort
const features = category['@nodes'].map(e => category[e])
const categoryTitle = category['@name']

const wrapper = shallowMount(FeatureCategory, {
  propsData: {
    category: {...category, title: categoryTitle, features},
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
    Feature,
    'b-icon': true,
    'b-collapse': true,
    'b-card-text': true,
    CloudinaryGallery: true
  }
})

describe('Category component [data props]', () => {
  it('has accordion', () => {
    expect(wrapper.find('section[data-test-id="accordion"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find('h3').text()).toEqual(categoryTitle)
  })

  it('has mocked arrow icon', () => {
    expect(wrapper.find('b-icon-stub[icon="sort-down"]').exists()).toBe(true)
  })

  it('has features', () => {
    expect(wrapper.find('div[data-test-id="features-feature"]').exists()).toBe(true)
  })

  it('renders as many features as it has in category props', () => {
    expect(wrapper.findAll('div[data-test-id="features-feature"]').length).toEqual(features.length)
  })

  it('renders counter which has total features amount', () => {
    expect(wrapper.find('p[data-test-id="counter"]').text()).toEqual(features.length.toString())
  })
})

describe('Category component [styles]', () => {
  it('has dark grey background color', () => {
    expect(wrapper.find('section[data-test-id="accordion"]').classes()).toContain('bg-gray-darker')
  })

  it('has black title ', () => {
    expect(wrapper.find('div[aria-expanded]').classes()).toContain('text-white')
  })

  it('has start content alignment', () => {
    expect(wrapper.find('div[aria-expanded]').classes()).toContain('justify-content-start')
  })
})

describe('Category component [functionality]', () => {
  it('can be collapsed', async () => {
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    const events = wrapper.emitted('accordion-expanded')
    expect(events[events.length - 1]).toEqual([false, category['@id']])
  })

  it('can be expanded', async () => {
    await wrapper.setProps({isExpanded: false})
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    const events = wrapper.emitted('accordion-expanded')
    expect(events[events.length - 1]).toEqual([true, category['@id']])
  })
})
