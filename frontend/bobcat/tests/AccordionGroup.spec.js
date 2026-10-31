import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import AccordionGroup from '@/components/AccordionGroup'
import Accordion from '@/components/Accordion'
import SingleAccordion from '@/components/elements/SingleAccordion'
import mock from '~/server/responses/bobcat-api/pages/components/accordionGroup'
import linkUtils from '~/plugins/utils/linkUtils'
import downloadUtils from '~/plugins/utils/download'
import {COLORS} from '~/constants/accordion'

const groupProps = mock.main['0']

const wrapper = shallowMount(AccordionGroup, {
  propsData: {
    accordions: groupProps.accordions,
    title: groupProps.title,
    htag: groupProps.htag,
    isExpanded: groupProps.isExpanded,
    expandLabel: groupProps.expandLabel,
    collapseLabel: groupProps.collapseLabel
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          getLinkField: linkUtils.getLinkField,
          downloadFile: downloadUtils.downloadFile
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        },
        $services: {
          bobcatapi: {
            getConcepts: () => ({})
          }
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        },
        $ga4: {
          events: {
            accordion_click: jest.fn()
          }
        }
      }
    },
    $utils: {
      ...cloudinary.$utils
    }
  },
  stubs: {
    LazyHydrate: true,
    'b-icon': true,
    'b-collapse': true,
    Accordion,
    SingleAccordion,
    CloudinaryGallery: true
  }
})

const accordions = groupProps.accordions
  ? groupProps.accordions['@nodes'].map((x) => {
    const _sItem = groupProps.accordions[x]
    const assets =
            _sItem?.assets && _sItem?.assets['@nodes']
              ? _sItem?.assets['@nodes']
                .filter(
                  y =>
                    _sItem?.assets[y]?.asset?.img?.src ||
                            _sItem?.assets[y]?.asset?.video?.src ||
                            null
                )
                .map(
                  x =>
                    _sItem?.assets[x]?.asset?.img ||
                            _sItem?.assets[x]?.asset?.video ||
                            null
                )
                .filter(z => z) || []
              : []

    const accordion = {
      '@id': _sItem['@id'],
      assets,
      brochure: _sItem.brochure?.field === 'none' ? null : _sItem?.brochure,
      color: _sItem.colors?.field || COLORS.BLACK,
      isExpanded: _sItem.isExpanded === 'true' || false,
      link: linkUtils.getLinkField(_sItem.link, 'en'),
      title: _sItem.title,
      description: _sItem.description
    }

    return accordion
  })
  : []

describe('Accordion group component', () => {
  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })

  it('has section wrapper', () => {
    expect(wrapper.find('section[data-test-id="accordion-group"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find(groupProps.htag).text()).toEqual(groupProps.title)
  })

  it('has expand and collapse button', () => {
    expect(wrapper.find('button[data-test-id="expand-button"]').exists()).toBe(true)
  })

  it('has list of accordions', () => {
    expect(wrapper.findAll('div[data-test-id="accordion-standalone"]').length).toEqual(accordions.length)
  })
})

describe('Accordion has ', () => {
  it('has <a/> tag with mocked label', () => {
    expect(wrapper.find('a[data-test-id="cta-button"]').text()).toEqual(groupProps.accordions.accordions0.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(wrapper.find('a[data-test-id="cta-button"]').attributes().href).toBe(groupProps.accordions.accordions0.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(wrapper.find('a[data-test-id="cta-button"]').attributes().target).toBe(groupProps.accordions.accordions0.link.link.target)
  })

  it('has brochure button', () => {
    expect(wrapper.find('a[data-test-id="brochure-button"]').exists()).toBe(true)
  })

  it('has description', () => {
    expect(wrapper.find('div[data-test-id="accordion-description"]').text()).toEqual(groupProps.accordions.accordions0.description)
  })
})
