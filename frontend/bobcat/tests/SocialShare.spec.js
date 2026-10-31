import {shallowMount} from '@vue/test-utils'
import SocialShare from '@/components/SocialShare'

const wrapper = shallowMount(SocialShare, {
  propsData: {},
  data() {
    return {shareThisId: '12'} // mock fetch returned
  },
  stubs: {
    ImageSet: true,
    LazyHydrate: true
  },
  mocks: {
    $nuxt: {
      context: {
        req: {
          headers: {
            host: 'test'
          }
        },
        $services: {
          bobcatapi: {
            getConfig: () => { return {data: {test: 'test'}} }
          }
        }
      }
    }
  }
})

describe('SocialShare component', () => {
  it('is rendered with default prop values', () => {
    expect(wrapper.find('[data-test-id="socialshare-buttons"]').exists()).toEqual(true)
    expect(wrapper.find('[data-test-id="socialshare-buttons-wrapper"]').classes().includes('col-sm-8')).toEqual(false)
    expect(wrapper.find('.bg-gray-light').exists()).toEqual(false)
    expect(wrapper.vm.shareThisId).toEqual('12')
  })

  it('renders spacer', async () => {
    await wrapper.setProps({showSpacer: 'true'})
    expect(wrapper.find('.bg-gray-light').exists()).toEqual(true)
  })

  it('renders centered', async () => {
    await wrapper.setProps({centeredColumn: 'true'})
    expect(wrapper.find('[data-test-id="socialshare-buttons-wrapper"]').classes().includes('col-sm-8')).toEqual(true)
  })
})
