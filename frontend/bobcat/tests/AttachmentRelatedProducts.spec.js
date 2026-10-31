import {shallowMount} from '@vue/test-utils'
import AttachmentCategory from '@/components/elements/Attachments/AttachmentCategory'
import AttachmentRelatedProducts from '@/components/AttachmentRelatedProducts'
import Accordion from '@/components/Accordion'
import magnoliaMock from '~/server/responses/bobcat-api/pages/components/attachmentRelatedProducts'
import contentMock from '~/server/responses/content-api/attachmentRelatedProducts'

const wrapper = shallowMount(AttachmentRelatedProducts, {
  propsData: {
    title: magnoliaMock.title
  },
  provide: {
    model: magnoliaMock
  },
  mocks: {
    $nuxt: {
      context: {
        $regions: {
          current: {
            language: {
              name: 'en'
            },
            name: 'na'
          }
        },
        $services: {
          content: {
            getAttachmentsProductsAssociations: () => ({
              ...contentMock
            })
          }
        }
      }
    }
  },
  stubs: {
    Accordion,
    AttachmentCategory,
    'b-icon': true,
    'b-table': true,
    'b-collapse': true,
    'b-spinner': true,
    WarningAlert: true
  }
})

const categories = contentMock.selectorResult.subcategories

describe('AttachmentRelatedProducts component [data props]', () => {
  it('has accordion', async () => {
    expect(wrapper.find('section[data-test-id="accordion"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find('h2').text()).toEqual(magnoliaMock.title)
  })

  it('has mocked arrow icon', () => {
    expect(wrapper.find('b-icon-stub[icon="sort-down"]').exists()).toBe(true)
  })

  it('has at least one category group', () => {
    expect(
      wrapper.find('div[data-test-id="attachment-related-products-category-group"]').exists()
    ).toBe(true)
  })

  it('has table', () => {
    expect(wrapper.find('b-table-stub').exists()).toBe(true)
  })

  it('renders counter which has total products amount', () => {
    expect(parseInt(wrapper.find('p[data-test-id="counter"]').text())).toEqual(categories[0].products.length)
  })

  it('can be expanded', async () => {
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    expect(wrapper.find('div[aria-expanded="true"]').exists()).toBe(true)
  })

  it('can be collapsed', async () => {
    await wrapper.find('section[data-test-id="accordion"] > div').trigger('click')
    expect(wrapper.find('div[aria-expanded="false"]').exists()).toBe(true)
  })
})
