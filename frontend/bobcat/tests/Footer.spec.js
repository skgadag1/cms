import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import Footer from '@/components/Footer'
import mock from '~/server/responses/bobcat-api/pages/components/footer'
import pathUtils from '~/plugins/utils/path'
import linkUtils from '~/plugins/utils/linkUtils'

const props = mock.footer['0']
const options = {
  propsData: {
    oneTough: props.oneTough,
    social: props.social,
    links: props.links,
    languages: props.languages,
    rightSection: props.rightSection,
    logo: props.logo,
    disclaimer: props.disclaimer,
    ssrCurrentRegion: {
      name: 'en',
      label: 'english',
      language: {
        name: 'en'
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    NuxtLink: true,
    ImageSet: true,
    'b-icon': true
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary,
          ...pathUtils,
          ...linkUtils
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        },
        $services: {
          bobcatapi: {
            getConcepts: jest.fn().mockResolvedValue({})
          }
        }
      }
    },
    $regions: {
      current: {
        name: 'en',
        label: 'english',
        language: {
          name: 'en'
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(Footer, opt)
const wrapper = getWrapper()

describe('Footer component [data props]', () => {
  it('has h6 titles with mocked labels', () => {
    const headersNum = props.links['@nodes'].length + 1 // languages
    expect(wrapper.findAll('h3.h6').length).toEqual(headersNum)

    const headers = wrapper.findAll('[data-test-id="footer-links"] h3.h6')
    for (const i in wrapper.vm.footerLinks) { expect(headers.at(i).text()).toEqual(wrapper.vm.footerLinks[i].title) }
  })

  it('has disclaimer', () => {
    expect(wrapper.find('[data-test-id="footer-disclaimer"]').text()).toEqual(props.disclaimer)
  })

  it('has social links when present', () => {
    expect(wrapper.findAll('[data-test-id="social-links"] a').length).toEqual(props.social['@nodes'].length)
  })

  it('has no social links when empty', async () => {
    const wrapper = getWrapper({...options, propsData: {...options.propsData, social: null}})
    await wrapper.find('[data-test-id="social-links"]')
    expect(wrapper.find('[data-test-id="social-links"]').exists()).toBeFalsy()
  })

  it('renders proper rel="noopener"', () => {
    const rightLinkGroups = wrapper.findAll('[data-test-id="footer-rightLinkSection"]')
    expect(rightLinkGroups.at(0).find('a:nth-of-type(1)').attributes('rel')).toEqual('noopener nofollow')
    expect(rightLinkGroups.at(0).find('a:nth-of-type(2)').attributes('rel')).toBeFalsy()

    const linkGroups = wrapper.findAll('[data-test-id="footer-links"]')
    expect(linkGroups.at(0).find('a:nth-of-type(1)').attributes('rel')).toEqual('noopener nofollow')
    expect(linkGroups.at(0).find('a:nth-of-type(2)').attributes('rel')).toBeFalsy()
  })

  it('renders links properly', async () => {
    const wrapper = getWrapper({...options, propsData: {...options.propsData, rightSection: null}})
    expect(wrapper.find('[data-test-id="footer-rightLinkSection"]').exists()).toBeFalsy()
  })
})
