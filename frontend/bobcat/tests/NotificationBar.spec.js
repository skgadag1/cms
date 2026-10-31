import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import NotificationBar from '@/components/NotificationBar'
import mock from '~/server/responses/bobcat-api/pages/components/notificationBar'
import linkUtils from '~/plugins/utils/linkUtils'

const notificationBarProps = mock.main['0']
const options = {
  propsData: {
    paragraph: notificationBarProps.paragraph,
    link: notificationBarProps.link,
    icon: notificationBarProps.icon
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils,
          getLinkField: linkUtils.getLinkField
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
    'b-icon': true
  }
}

const getWrapper = (opt = options) => shallowMount(NotificationBar, opt)
const wrapper = getWrapper()

const paragraph = wrapper.find('div[data-test-id="notification-bar-content"] p')
const link = wrapper.find('a[data-test-id="notification-bar-btn"]')
const closeBtn = wrapper.find('[data-test-id="notification-bar-close-btn"]')

describe('Notification Bar component [data props]', () => {
  it('has paragraph with mocked label', () => {
    expect(paragraph.text()).toEqual(notificationBarProps.paragraph)
  })

  it('has mocked left icon', () => {
    expect(wrapper.find(`b-icon-stub[icon="${notificationBarProps.icon}"]`).exists()).toBe(true)
  })

  it('has mocked close icon', () => {
    expect(wrapper.find('b-icon-stub[icon="close"]').exists()).toBe(true)
  })

  it('has close button', () => {
    expect(closeBtn.exists()).toBe(true)
  })

  it('has <a/> tag with mocked label', () => {
    expect(link.text()).toEqual(notificationBarProps.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(link.attributes().href).toBe(notificationBarProps.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(link.attributes().target).toBe(notificationBarProps.link.link.target)
  })

  it('<a/> is not displaying self when not present', async () => {
    await wrapper.setData({linkField: undefined})
    expect(wrapper.find('a').exists()).toBe(false)
  })
})

describe('Notification Bar component [style props]', () => {
  it('has text centred', () => {
    expect(paragraph.classes('text-center')).toBe(true)
  })

  it('has link centred', () => {
    expect(link.classes('text-center')).toBe(true)
  })

  it('has white text color', () => {
    expect(wrapper.find('div[data-test-id="notification-bar-content-container"]').classes('text-white')).toBe(true)
  })

  it('has dark background color', () => {
    expect(wrapper.find('div[data-test-id="notification-bar-content"]').classes('bg-dark')).toBe(true)
  })

  it('is fixed to the top of the screen', () => {
    expect(wrapper.find('#notificationBar').classes('fixed-top')).toBe(true)
  })

  it('it\'s not fixed to the top of the screen when editor mode is on', async () => {
    await wrapper.setData({inEditorEdit: true})
    await wrapper.vm.$forceUpdate()
    expect(wrapper.find('#notificationBar').classes('fixed-top')).toBe(false)
  })
})

describe('Notification Bar component [functionality]', () => {
  it('isHidden property on default is false', () => {
    expect(wrapper.vm.isHidden).toBe(false)
  })

  it('on click notification bar disappears', async () => {
    expect(!wrapper.vm.isHidden && (wrapper.find('div[data-test-id="notification-bar-content"]').exists())).toBe(true)
    await closeBtn.trigger('click')
    expect(wrapper.vm.isHidden && !(wrapper.find('div[data-test-id="notification-bar-content"]').exists())).toBe(true)
  })

  it('on click notification bar DOM is not present', async () => {
    await closeBtn.trigger('click')
    expect(wrapper.vm.isHidden && !(wrapper.find('div[data-test-id="notification-bar-content"]').exists())).toBe(true)
  })

  it('has setHeaderClientHeight function related to header component', () => {
    expect(wrapper.vm.setHeaderClientHeight).toBeDefined()
  })
})
