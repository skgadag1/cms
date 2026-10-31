import {shallowMount} from '@vue/test-utils'
import {BFormInput, BInputGroup, BInputGroupText} from 'bootstrap-vue'
import FAQ from '@/components/FAQ'
import Accordion from '@/components/Accordion'
import mock from '~/server/responses/bobcat-api/pages/components/faq'

const FAQProps = mock.main['0']
const options = {
  propsData: {
    title: FAQProps.title,
    htag: FAQProps.htag,
    placeholder: FAQProps.placeholder,
    faqSections: FAQProps.faqSections,
    labels: {test: 'test'}
  },
  mocks: {
    $nuxt: {
      context: {
        $services: {
          bobcatapi: {
            getConcepts: () => {
              return {data: {test: 'test'}}
            }
          }
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        }
      }
    }
  },
  stubs: {
    'b-row': true,
    'b-col': true,
    'b-icon': true,
    'b-collapse': true,
    BInputGroupText,
    BInputGroup,
    BFormInput,
    Accordion,
    LazyHydrate: true
  }
}

const getWrapper = (opt = options) => shallowMount(FAQ, opt)
const wrapper = getWrapper()

const title = wrapper.find('h2')

describe('FAQ component [data props]', () => {
  it('has title with mocked label', () => {
    expect(title.text()).toEqual(FAQProps.title)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})

describe('FAQ component [style props]', () => {
  it('has none sections rendered', async () => {
    const _wrapper = getWrapper({...options, propsData: {...options.propsData, faqSections: null}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="faq-sections"]').exists()).toBe(false)
    expect(_wrapper.findAll('.faq-section').length).toBe(0)
    expect(_wrapper.find('[data-test-id="faq-no-sections"]').exists()).toBe(true)
  })

  it('has all sections rendered and visible', () => {
    expect(wrapper.find('[data-test-id="faq-sections"]').exists()).toBe(true)
    expect(wrapper.findAll('.faq-section:not(.faq-section__hidden)').length).toBe(FAQProps.faqSections['@nodes'].map(x => FAQProps.faqSections[x]).length)
  })

  it('filters by keyword', async () => {
    await wrapper.find('input[data-test-id="faq-keyword-input"]').setValue('test')
    expect(wrapper.findAll('.faq-section:not(.faq-section__hidden)').length).toBe(2)
    expect(wrapper.find('[data-test-id="faq-no-results"]').isVisible()).toBe(false)
  })

  it('no results error', async () => {
    await wrapper.find('input[data-test-id="faq-keyword-input"]').setValue('testing')
    expect(wrapper.findAll('.faq-section:not(.faq-section__hidden)').length).toBe(0)
    expect(wrapper.find('[data-test-id="faq-no-results"]').isVisible()).toBe(true)
  })

  it('has all results again', async () => {
    await wrapper.find('input[data-test-id="faq-keyword-input"]').setValue('te')
    expect(wrapper.find('[data-test-id="faq-sections"]').exists()).toBe(true)
    expect(wrapper.findAll('.faq-section:not(.faq-section__hidden)').length).toBe(FAQProps.faqSections['@nodes'].map(x => FAQProps.faqSections[x]).length)
  })

  it('clear heyword by clicking icon', async () => {
    await wrapper.find('input[data-test-id="faq-keyword-input"]').setValue('testing')
    expect(wrapper.findAll('.faq-section:not(.faq-section__hidden)').length).toBe(0)
    expect(wrapper.find('[data-test-id="faq-no-results"]').isVisible()).toBe(true)
    await wrapper.find('[data-test-id="faq-keyword-clear"]').trigger('click')
    expect(wrapper.find('[data-test-id="faq-no-results"]').isVisible()).toBe(false)
  })
})
