import {shallowMount} from '@vue/test-utils'
import {BCardBody, BCardTitle} from 'bootstrap-vue'
import cloudinary from './utils/cloudinary.mock'
import PreviouslyViewed from '@/components/PreviouslyViewed'
import mock from '~/server/responses/bobcat-api/pages/components/previouslyViewed'

const productsProps = mock.main['0']

const options = {
  propsData: {
    title: productsProps.title,
    htag: productsProps.htag
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils
        },
        $regions: {
          current: {
            name: 'na',
            label: 'english',
            language: 'en'
          }
        },
        $cookies: {
          get: () => false
        }
      }
    }
  },
  stubs: {
    'b-row': true,
    'b-col': true,
    'b-container': true,
    BCardBody,
    BCardTitle,
    ImageSet: true
  }
}

const getWrapper = (opt = options) => shallowMount(PreviouslyViewed, {...options, ...opt})
const wrapper = getWrapper()

describe('Previously Viewed component', () => {
  it('has title rendered', () => {
    expect(wrapper.find('[data-test-id="previously-viewed"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="previously-viewed__title"]').exists()).toBe(true)
  })

  it('has no title rendered', async () => {
    const _wrapper = await getWrapper({propsData: {}})
    expect(_wrapper.find('[data-test-id="previously-viewed"]').exists()).toBe(true)
    expect(_wrapper.find('[data-test-id="previously-viewed__title"]').exists()).toBe(false)
  })

  it('has no items rendered', () => {
    expect(wrapper.findAll('[data-test-id="previously-viewed__item"]').length).toBe(0)
  })

  it('has no items rendered if present for different region', async () => {
    global.window.localStorage.setItem('viewed', JSON.stringify({pages: {eu: [mock]}}))
    global.process.client = true

    const _wrapper = await getWrapper()
    expect(_wrapper.findAll('[data-test-id="previously-viewed__item"]').length).toBe(0)
  })

  it('has items rendered if present for region', async () => {
    global.window.localStorage.setItem('viewed', JSON.stringify({pages: {na: [mock]}}))
    global.process.client = true

    const _wrapper = await getWrapper()
    expect(_wrapper.findAll('[data-test-id="previously-viewed__item"]').length).toBe(1)
    expect(_wrapper.find('[data-test-id="previously-viewed__item"]').attributes('href')).toEqual(mock.pageUrl)
    expect(_wrapper.find('.card-title[data-test-id="previously-viewed__item-title"]').text()).toEqual(mock.title)
  })
})
