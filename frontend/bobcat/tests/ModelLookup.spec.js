import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import ModelLookup from '@/components/ModelLookup'
import categoryMock from '~/server/responses/bobcat-api/products/productCategory'
import linkUtils from '~/plugins/utils/linkUtils'

const sampleCategory = categoryMock['Skid-Steer-Loaders']
const sampleModel = categoryMock['Skid-Steer-Loaders']['S570-T4']

global.process.client = true
global.setTimeout = jest.fn(cb => cb())

const options = {
  propsData: {
    title: 'test',
    productCategory: categoryMock['@id'],
    productSubcategory: sampleCategory['@id'],
    lifecycle: {
      field: 'current'
    }
  },
  mocks: {
    $fetchState: {
      pending: false
    },
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          ...linkUtils
        },
        $services: {
          bobcatapi: {
            getConcepts: () => {
              return {}
            },
            getCategories: () => {
              return [categoryMock]
            },
            getCategoryModels: () => {
              return [sampleModel]
            }
          },
          content: {
            getModelPage: () => ''
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
  },
  stubs: {
    'b-overlay': true,
    'b-button': true,
    'b-card': true,
    'b-col': true,
    'b-card-body': true,
    'b-form-select': true,
    LazyHydrate: true
  }
}

const getWrapper = () => shallowMount(ModelLookup, options)
const wrapper = getWrapper()

describe('ModelLookup component [data props]', () => {
  it('sets the default category', async () => {
    const categorySelect = wrapper.find('[data-test-id="model-lookup__category-select"]')
    expect(categorySelect.exists()).toBe(true)
    expect(wrapper.vm.selectedCategory).toBeTruthy()
    expect(wrapper.vm.selectedCategory).toBe(sampleCategory['@id'])
    expect(wrapper.find('[data-test-id="model-lookup__model-select"]').exists()).toBe(true)
  })

  it('has rendered model select but not button', async () => {
    await wrapper.setData({selectedCategory: sampleCategory['@id']})
    expect(wrapper.find('[data-test-id="model-lookup__model-select"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="model-lookup__button"]').exists()).toBe(false)
  })

  it('displays view details button', async () => {
    await wrapper.setData({selectedCategory: sampleCategory['@id'], selectedModel: sampleModel})
    expect(wrapper.find('[data-test-id="model-lookup__button"]').exists()).toBe(true)
  })

  it('filters out historical lifecycle items', () => {
    expect(wrapper.vm.selectedCategory).toBe(sampleCategory['@id'])
    expect(wrapper.vm.models.find(x => x.html === 'OnlyHistorical')).toBeFalsy()
  })
})
