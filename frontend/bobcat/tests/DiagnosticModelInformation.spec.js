import {shallowMount} from '@vue/test-utils'
import DiagnosticModelInformation from '@/components/DiagnosticModelInformation'
import mock from '~/server/responses/bobcat-api/pages/components/diagnosticModelInformation'

const options = {
  propsData: {
    codeLabel: mock.codeLabel,
    descriptionLabel: mock.descriptionLabel,
    title: mock.title,
    htag: mock.htag,
    serialNumberLabel: mock.serialNumberLabel,
    diagnosticCodeLabel: mock.diagnosticCodeLabel,
    checkCodeButton: mock.checkCodeButton,
    whereToFindLink: {
      label: mock.whereToFindLinkText,
      url: mock.whereToFindLinkUrl
    }
  },
  mocks: {
    $nuxt: {
      context: {
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        },
        $utils: {
          getLinkField: arg => arg,
          getCloudinaryAsset: src => src
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    ImageSet: true,
    'b-form-input': true,
    'b-row': true,
    'b-col': true,
    'b-icon': true,
    'b-spinner': true,
    WarningAlert: true
  }
}
const getWrapper = (opt = options) => shallowMount(DiagnosticModelInformation, opt)
const wrapper = getWrapper()

const header = wrapper.find('h2')
const serialInputLabel = wrapper.find('label[for="serialNumber"]')
const codeInputLabel = wrapper.find('label[for="diagnosticCode"]')
const whereToFindLink = wrapper.find('a[data-test-id="where-to-find-link"]')
const submitButton = wrapper.find('a[data-test-id="check-code-button"]')

describe('DiagnosticModelInformation [data props]', () => {
  it('has h2 with mocked label', () => {
    expect(header.text()).toEqual(mock.title)
  })

  it('has serial input with mocked label', () => {
    expect(serialInputLabel.text()).toEqual(mock.serialNumberLabel)
  })

  it('has code input with mocked label', () => {
    expect(codeInputLabel.text()).toEqual(mock.diagnosticCodeLabel)
  })

  it('has where to find link with mocked text', () => {
    expect(whereToFindLink.text()).toEqual(mock.whereToFindLinkText)
  })

  it('hide where to find link when text or url is not present', () => {
    let wrapper = getWrapper({...options, propsData: {whereToFindLinkText: ''}})
    expect(wrapper.find('a[data-test-id="where-to-find-link"]').exists()).toBe(false)
    wrapper = getWrapper({...options, propsData: {whereToFindLinkUrl: ''}})
    expect(wrapper.find('a[data-test-id="where-to-find-link"]').exists()).toBe(false)
  })

  it('has LazyHydrate component', () => {
    expect(wrapper.find('lazyhydrate-stub').exists()).toBe(true)
  })
})

describe('DiagnosticModelInformation component [functionality]', () => {
  it('show disabled submit button when inputs are not filled', async () => {
    expect(submitButton.classes('disabled')).toBe(true)
  })

  it('enable submit button when inputs are filled', async () => {
    await wrapper.setData({serialNumberInput: '123', faultCodeInput: '456'})
    expect(submitButton.classes('disabled')).toBe(false)
  })
})
