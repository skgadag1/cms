import {shallowMount} from '@vue/test-utils'
import RelatedLinks from '@/components/RelatedLinks'
import mock from '~/server/responses/bobcat-api/pages/components/relatedLinks'
import linkUtils from '~/plugins/utils/linkUtils'

const relatedProps = mock.main['0']

const wrapper = shallowMount(RelatedLinks, {
  propsData: {
    title: relatedProps.title,
    firstTitle: relatedProps.firstTitle,
    secondTitle: relatedProps.secondTitle,
    thirdTitle: relatedProps.thirdTitle,
    htag: relatedProps.htag,
    firstHtag: relatedProps.firstHtag,
    secondHtag: relatedProps.secondHtag,
    thirdHtag: relatedProps.thirdHtag,
    grayBackground: relatedProps.grayBackground,
    first: relatedProps.first,
    second: relatedProps.second,
    third: relatedProps.third
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
            language: 'en'
          }
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    WarningAlert: true,
    'b-icon': true
  }
})

const firstColLinks = relatedProps.first ? relatedProps.first['@nodes'].map(x => linkUtils.getLinkField(relatedProps.first[x]?.link, 'en')) : []
const secondColLinks = relatedProps.second ? relatedProps.second['@nodes'].map(x => linkUtils.getLinkField(relatedProps.second[x]?.link, 'en')) : []
const thirdColLinks = relatedProps.third ? relatedProps.third['@nodes'].map(x => linkUtils.getLinkField(relatedProps.third[x]?.link, 'en')) : []

describe('Related Links component', () => {
  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })

  it('has section wrapper', () => {
    expect(wrapper.find('section[data-test-id="related-links"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find(`${relatedProps.htag}[data-test-id="related-links-title"]`).text()).toEqual(relatedProps.title)
  })

  it('has first column', () => {
    expect(wrapper.find('div[data-test-id="related-links-first-col"]').exists()).toBe(true)
  })

  it('has first column title with mocked label', () => {
    expect(wrapper.find(`${relatedProps.firstHtag}[data-test-id="related-links-first-col-title"]`).text()).toEqual(relatedProps.firstTitle)
  })

  it('has first column which renders links', () => {
    expect(wrapper.findAll('div[data-test-id="related-links-first-col"] a').length).toEqual(firstColLinks.length)
  })

  it('first column link has title', () => {
    expect(wrapper.find('div[data-test-id="related-links-first-col"] a').text()).toEqual(firstColLinks[0].label)
  })

  it('first column link has href', () => {
    expect(wrapper.find('div[data-test-id="related-links-first-col"] a').attributes().href).toBe(firstColLinks[0].url)
  })

  it('first column link has target', () => {
    expect(wrapper.find('div[data-test-id="related-links-first-col"] a').attributes().target).toBe(firstColLinks[0].target)
  })

  it('first column has arrow icon', () => {
    expect(wrapper.find('div[data-test-id="related-links-first-col"] b-icon-stub').exists()).toBe(true)
  })

  it('has second column', () => {
    expect(wrapper.find('div[data-test-id="related-links-second-col"]').exists()).toBe(true)
  })

  it('has second column title with mocked label', () => {
    expect(wrapper.find(`${relatedProps.secondHtag}[data-test-id="related-links-second-col-title"]`).text()).toEqual(relatedProps.secondTitle)
  })

  it('has second column which renders links', () => {
    expect(wrapper.findAll('div[data-test-id="related-links-second-col"] a').length).toEqual(secondColLinks.length)
  })

  it('second column link has title', () => {
    expect(wrapper.find('div[data-test-id="related-links-second-col"] a').text()).toEqual(secondColLinks[0].label)
  })

  it('second column link has href', () => {
    expect(wrapper.find('div[data-test-id="related-links-second-col"] a').attributes().href).toBe(secondColLinks[0].url)
  })

  it('second column link has target', () => {
    expect(wrapper.find('div[data-test-id="related-links-second-col"] a').attributes().target).toBe(secondColLinks[0].target)
  })

  it('second column has arrow icon', () => {
    expect(wrapper.find('div[data-test-id="related-links-second-col"] b-icon-stub').exists()).toBe(true)
  })

  it('has third column', () => {
    expect(wrapper.find('div[data-test-id="related-links-third-col"]').exists()).toBe(true)
  })

  it('has third column title with mocked label', () => {
    expect(wrapper.find(`${relatedProps.thirdHtag}[data-test-id="related-links-third-col-title"]`).text()).toEqual(relatedProps.thirdTitle)
  })

  it('has third column which renders links', () => {
    expect(wrapper.findAll('div[data-test-id="related-links-third-col"] a').length).toEqual(thirdColLinks.length)
  })

  it('third column link has title', () => {
    expect(wrapper.find('div[data-test-id="related-links-third-col"] a').text()).toEqual(thirdColLinks[0].label)
  })

  it('third column link has href', () => {
    expect(wrapper.find('div[data-test-id="related-links-third-col"] a').attributes().href).toBe(thirdColLinks[0].url)
  })

  it('third column link has target', () => {
    expect(wrapper.find('div[data-test-id="related-links-third-col"] a').attributes().target).toBe(thirdColLinks[0].target)
  })

  it('third column has arrow icon', () => {
    expect(wrapper.find('div[data-test-id="related-links-third-col"] b-icon-stub').exists()).toBe(true)
  })
})
