import {shallowMount} from '@vue/test-utils'
import {BFormInput, BInputGroup, BInputGroupText} from 'bootstrap-vue'
import cloudinary from './utils/cloudinary.mock'
import PartsFinder from '@/components/PartsFinder'

const _props = {
  title: 'Test title here',
  subtitle: 'custom subtitle',
  partsUrl: {
    url: 'https://test.com/test',
    target: '_self',
    label: 'test',
    placeholder: 'placeholder test here'
  }
}

const _opt = {
  propsData: _props,
  stubs: {
    LazyHydrate: true,
    'b-icon': true,
    'b-button': true,
    BFormInput,
    BInputGroup,
    BInputGroupText
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils
        }
      }
    }
  }
}

const getWrapper = (opt = {}) => shallowMount(PartsFinder, opt)

describe('[PartsFinder] component', () => {
  const wrapper = getWrapper(_opt)

  it('is rendered with default prop values (form)', () => {
    expect(wrapper.find('[data-test-id="parts-finder-form"]').exists()).toEqual(true)
    expect(wrapper.find('[data-test-id="parts-finder-form"]').attributes('action')).toEqual(_props.partsUrl.url)
    expect(wrapper.find('[data-test-id="parts-finder-form"]').attributes('target')).toEqual(_props.partsUrl.target)
    expect(wrapper.find('[data-test-id="parts-finder-form"] input').attributes('placeholder')).toEqual(_props.partsUrl.placeholder)
    expect(wrapper.find('[data-test-id="parts-finder-form"] input').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('[data-test-id="parts-finder-form__button"]').attributes('disabled')).toBeUndefined()
    expect(wrapper.find('h3').text()).toEqual(_props.title)
    expect(wrapper.find('[data-test-id="parts-finder-link"]').exists()).toEqual(false)
  })

  it('has LazyHydrate component', () => {
    expect(wrapper.find('lazyhydrate-stub').exists()).toBe(true)
  })

  it('has external icon', () => {
    expect(wrapper.find('b-icon-stub[icon="external"]').exists()).toBe(true)
  })

  it('renders disabled in editor', async () => {
    let _wrapper = getWrapper({..._opt, propsData: {..._props, showForm: 'false'}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="parts-finder-link"]').exists()).toEqual(true)
    expect(_wrapper.find('[data-test-id="parts-finder-link"] a').attributes('url')).toBeUndefined()

    _wrapper = getWrapper(_opt)
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="parts-finder-form"]').exists()).toEqual(true)
    expect(_wrapper.find('[data-test-id="parts-finder-form"] input').attributes('disabled')).toBeTruthy()
    expect(_wrapper.find('[data-test-id="parts-finder-form__button"]').attributes('disabled')).toBeTruthy()
  })

  it('renders with bg variants', async () => {
    await wrapper.setData({backgroundUrl: undefined})
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-gray-lighter')).toBe(true)
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-white')).toBe(false)

    await wrapper.setData({backgroundUrl: _props.partsUrl.url})
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-gray-lighter')).toBe(true)
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-white')).toBe(false)

    await wrapper.setProps({variant: 'white'})
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-gray-lighter')).toBe(false)
    expect(wrapper.find('[data-test-id="parts-finder"]').classes('bg-white')).toBe(true)
  })
})

describe('[PartsFinder] component link', () => {
  const wrapper = getWrapper({..._opt, propsData: {..._props, showForm: 'false'}})

  it('renders link', () => {
    const wrapper = getWrapper({..._opt, propsData: {..._props, showForm: 'false'}})
    expect(wrapper.find('[data-test-id="parts-finder-form"]').exists()).toEqual(false)
    expect(wrapper.find('[data-test-id="parts-finder-link"]').exists()).toEqual(true)
    expect(wrapper.find('[data-test-id="parts-finder-link"] a').attributes('target')).toEqual(_props.partsUrl.target)
    expect(wrapper.find('[data-test-id="parts-finder-link"] a').attributes('href')).toEqual(_props.partsUrl.url)
    expect(wrapper.find('[data-test-id="parts-finder-link"] a').attributes('rel')).toBeFalsy()
  })

  it('has external icon', () => {
    expect(wrapper.find('a b-icon-stub[icon="external"]').exists()).toBe(true)
  })
})
