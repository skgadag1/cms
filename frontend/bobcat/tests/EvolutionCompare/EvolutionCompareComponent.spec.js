import {shallowMount} from '@vue/test-utils'
import EvolutionCompareComponent from '@/components/EvolutionCompareComponent'

const models = [
  {id: 'S66', '@id': 'uuid-s66', '@path': '/bobcat/na/Loaders/Skid-Steer-Loaders/S66', title: 'S66', parent: {parentTitle: 'Skid-Steer Loaders'}},
  {id: 'S76', '@id': 'uuid-s76', '@path': '/bobcat/na/Loaders/Skid-Steer-Loaders/S76', title: 'S76', parent: {parentTitle: 'Skid-Steer Loaders'}},
  {id: 'S650', '@id': 'uuid-s650', '@path': '/bobcat/na/Loaders/Skid-Steer-Loaders/S650', title: 'S650', parent: {parentTitle: 'Skid-Steer Loaders'}}
]

const subcategoryDataSource = {field: 'subcategory', productSubcategory: 'uuid-skid-steer'}

const flushPromises = () => new Promise(resolve => setTimeout(resolve))

const slotRenderingStub = tag => ({
  render(h) {
    return h(tag, this.$slots.default)
  }
})

const createServices = (overrides = {}) => ({
  bobcatapi: {
    getConcepts: jest.fn().mockResolvedValue({}),
    getProduct: jest.fn(id => Promise.resolve({...models.find(m => m['@id'] === id), image: {src: `${id}.jpg`}})),
    getCategories: jest.fn().mockResolvedValue([]),
    getCategoryModels: jest.fn(),
    ...overrides.bobcatapi
  },
  products: {
    getCompareModels: jest.fn().mockResolvedValue(models),
    ...overrides.products
  }
})

const mountCompare = async (propsData = {}, services = createServices()) => {
  const wrapper = shallowMount(EvolutionCompareComponent, {
    propsData: {nodeId: 'compare', dataSource: subcategoryDataSource, ...propsData},
    mocks: {
      $route: {query: {}, path: '/compare'},
      $nuxt: {
        context: {
          $utils: {
            getLinkField: () => null,
            isSmallMobile: () => false,
            isDesktopLG: () => true,
            isDesktopXL: () => false
          },
          $regions: {current: {name: 'na', language: {name: 'en'}}},
          $ga4: {events: {}},
          $services: services
        }
      }
    },
    stubs: {
      'b-overlay': slotRenderingStub('div'),
      'b-row': slotRenderingStub('div'),
      'b-col': slotRenderingStub('div'),
      'b-alert': slotRenderingStub('div'),
      'b-form-checkbox': slotRenderingStub('div'),
      DynamicButton: slotRenderingStub('button'),
      EvolutionCompareModelDropdown: true,
      EvolutionCompareTile: true,
      EvolutionCompareSpecifications: true,
      'b-icon': true
    }
  })
  await flushPromises()
  return wrapper
}

const selectSlots = async (wrapper, slots) => {
  wrapper.vm.compareSlots = slots
  await flushPromises()
}

describe('EvolutionCompareComponent slot controls', () => {
  it('hides the remove control while a single model is selected', async () => {
    const wrapper = await mountCompare()
    await selectSlots(wrapper, [models[0], null])

    expect(wrapper.find('[data-test-id="evolution-compare-slot-remove-0"]').exists()).toBe(false)
    expect(wrapper.find('[data-test-id="evolution-compare-add-model"]').exists()).toBe(true)
  })

  it('shows a remove control next to every selected model once two are compared', async () => {
    const wrapper = await mountCompare()
    await selectSlots(wrapper, [models[0], models[1]])

    expect(wrapper.find('[data-test-id="evolution-compare-slot-remove-0"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="evolution-compare-slot-remove-1"]').exists()).toBe(true)
  })

  it('keeps the add model button on one line and drops it at three slots', async () => {
    const wrapper = await mountCompare()
    await selectSlots(wrapper, [models[0], models[1]])

    expect(wrapper.find('[data-test-id="evolution-compare-add-model"]').classes()).toContain('text-nowrap')

    wrapper.vm.addModelSlot()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.compareSlots).toHaveLength(3)
    expect(wrapper.find('[data-test-id="evolution-compare-add-model"]').exists()).toBe(false)
  })

  it('removes a model and compacts the comparison back to two slots', async () => {
    const wrapper = await mountCompare()
    await selectSlots(wrapper, [models[0], models[1], models[2]])

    wrapper.vm.removeSlotModel(1)
    await flushPromises()

    expect(wrapper.vm.compareSlots).toEqual([models[0], models[2]])
    expect(wrapper.find('[data-test-id="evolution-compare-slot-remove-0"]').exists()).toBe(true)
  })
})

describe('EvolutionCompareComponent data loading', () => {
  it('loads the model list from the compare endpoint by data source id', async () => {
    const services = createServices()
    const wrapper = await mountCompare({}, services)

    expect(services.products.getCompareModels).toHaveBeenCalledWith({id: 'uuid-skid-steer'})
    expect(services.bobcatapi.getCategoryModels).not.toHaveBeenCalled()
    expect(services.bobcatapi.getCategories).not.toHaveBeenCalled()
    expect(wrapper.vm.flatModels.map(m => m.id)).toEqual(['S650', 'S66', 'S76'])
  })

  it('uses the category id when the data source is a category', async () => {
    const services = createServices()
    await mountCompare({dataSource: {field: 'category', productCategory: {'@id': 'uuid-loaders'}}}, services)

    expect(services.products.getCompareModels).toHaveBeenCalledWith({id: 'uuid-loaders'})
  })

  it('removes excluded models from the list', async () => {
    const wrapper = await mountCompare({excludedModels: [{'@id': 'uuid-s76', '@nodeType': 'dxp:product'}]})

    expect(wrapper.vm.flatModels.map(m => m.id)).toEqual(['S650', 'S66'])
  })

  it('fills slots from configured default models and loads details only for them', async () => {
    const services = createServices()
    const wrapper = await mountCompare({defaultModel1: 'uuid-s66', defaultModel2: 'uuid-s650'}, services)

    expect(wrapper.vm.compareSlots.map(m => m?.id)).toEqual(['S66', 'S650'])
    expect(services.bobcatapi.getProduct).toHaveBeenCalledTimes(2)
    expect(services.bobcatapi.getProduct).toHaveBeenCalledWith('uuid-s66', null)
    expect(services.bobcatapi.getProduct).toHaveBeenCalledWith('uuid-s650', null)
    expect(wrapper.vm.detailedSlots.map(m => m.image.src)).toEqual(['uuid-s66.jpg', 'uuid-s650.jpg'])
    expect(wrapper.vm.compareSpecModels.map(m => m.id)).toEqual(['S66', 'S650'])
  })

  it('falls back to the list model when product details cannot be loaded', async () => {
    const services = createServices({bobcatapi: {getProduct: jest.fn().mockRejectedValue(new Error('boom'))}})
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {})
    const wrapper = await mountCompare({defaultModel1: 'uuid-s66', defaultModel2: 'uuid-s76'}, services)

    expect(wrapper.vm.compareSpecModels).toEqual([models[0], models[1]])
    consoleError.mockRestore()
  })

  it('shows the error alert when the compare endpoint fails', async () => {
    const error = Object.assign(new Error('Not found'), {response: {status: 404}})
    const services = createServices({products: {getCompareModels: jest.fn().mockRejectedValue(error)}})
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {})
    const wrapper = await mountCompare({}, services)

    expect(wrapper.find('[data-test-id="evolution-compare-error"]').exists()).toBe(true)
    consoleError.mockRestore()
  })

  it('shows the error alert when no data source is configured', async () => {
    const services = createServices()
    const wrapper = await mountCompare({dataSource: null}, services)

    expect(services.products.getCompareModels).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test-id="evolution-compare-error"]').exists()).toBe(true)
  })

  it('shows the empty state when the category has no current models', async () => {
    const services = createServices({products: {getCompareModels: jest.fn().mockResolvedValue([])}})
    const wrapper = await mountCompare({}, services)

    expect(wrapper.find('[data-test-id="evolution-compare-no-models"]').exists()).toBe(true)
  })
})
