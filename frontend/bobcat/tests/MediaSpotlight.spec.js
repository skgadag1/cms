import {shallowMount} from '@vue/test-utils'
import MediaSpotlight from '@/components/MediaSpotlight'
import linkUtils from '~/plugins/utils/linkUtils'

const mountMediaSpotlight = propsData => shallowMount(MediaSpotlight, {
  propsData: {
    variant: 'left',
    ...propsData
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          getLinkField: linkUtils.getLinkField
        },
        $regions: {
          current: {
            language: {name: 'en'}
          }
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        },
        $ga4: {
          events: {
            media_spotlight_cta_click: jest.fn()
          }
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    'b-button': true,
    DynamicButton: true,
    ImageSet: true,
    GTMVideo: true,
    YoutubeIframe: true
  }
})

describe('MediaSpotlight primary link', () => {
  it('resolves a localAnchor link', () => {
    const wrapper = mountMediaSpotlight({
      title: 'Models',
      link: {
        field: 'localAnchor',
        '@nodes': ['link'],
        link: {
          anchorID: 'models',
          localAnchorLabel: 'See models',
          target: '_self'
        }
      }
    })

    expect(wrapper.vm.linkField).toMatchObject({
      url: '#models',
      label: 'See models',
      target: '_self',
      type: 'localAnchor'
    })
  })
})

describe('MediaSpotlight headings', () => {
  it.each(['left', 'right'])('renders one legacy heading for the %s media variant', (variant) => {
    const wrapper = mountMediaSpotlight({
      title: 'Legacy heading',
      subtitle: 'Legacy subtitle',
      htag: 'h3',
      variant
    })

    expect(wrapper.findAll('h3').length).toBe(1)
    expect(wrapper.find('h3').text()).toBe('Legacy heading')
    expect(wrapper.findAll('p.h4').length).toBe(1)
    expect(wrapper.find('p.h4').text()).toBe('Legacy subtitle')
  })

  it.each(['left', 'right'])('renders one rich-text heading for the %s media variant', (variant) => {
    const wrapper = mountMediaSpotlight({
      evolutionTitle: '<h2><span>Rich-text heading</span></h2>',
      evolutionSubtitle: '<p>Rich-text subtitle</p>',
      variant
    })

    expect(wrapper.findAll('.evolution-title').length).toBe(1)
    expect(wrapper.findAll('.evolution-title h2').length).toBe(1)
    expect(wrapper.find('.evolution-title h2').text()).toBe('Rich-text heading')
    expect(wrapper.findAll('.media-spotlight-heading > .mb-0').length).toBe(1)
    expect(wrapper.find('.media-spotlight-heading > .mb-0').text()).toBe('Rich-text subtitle')
  })
})
