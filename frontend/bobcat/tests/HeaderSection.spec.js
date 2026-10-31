import {shallowMount} from '@vue/test-utils'
import HeaderSection from '@/components/HeaderSection'
import mock from '~/server/responses/bobcat-api/pages/components/headerSection'

const props = mock.main['0']

const wrapper = shallowMount(HeaderSection, {
  propsData: {
    title: props.title,
    htag: props.htag,
    postDate: props.postDate,
    beforePostDate: props.beforePostDate,
    description: props.description
  },
  stubs: {
    LazyHydrate: true
  },
  mocks: {
    $nuxt: {
      context: {
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            locale: 'en-us',
            language: {
              name: 'en'
            }
          }
        }
      }
    }
  }
})

const title = wrapper.find('h2')
const postDate = wrapper.find('p[data-test-id="header-section-post-date"]')
const description = wrapper.find('div[data-test-id="header-section-description"]')

describe('HeaderSection component [data props]', () => {
  it('has h2 title with mocked label', () => {
    expect(title.text()).toEqual(props.title)
  })

  it('has paragraph post date with mocked label', () => {
    expect(postDate.attributes()['data-iso-date']).toContain(new Date(props.postDate).toISOString().split('T')[0])
  })

  it('has label before post date', () => {
    expect(postDate.text()).toContain(props.beforePostDate)
  })

  it('has paragraph description with mocked label', () => {
    expect(description.text()).toEqual(props.description)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})
