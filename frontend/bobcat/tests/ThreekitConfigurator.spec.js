import {shallowMount} from '@vue/test-utils'
import ThreekitConfigurator, {buildIframeSrc, getTrustedOrigin} from '@/components/ThreekitConfigurator'

const THREEKIT_SRC = 'https://bobcat-cpq.dev.3kit.ai/configurator'
const THREEKIT_ORIGIN = 'https://bobcat-cpq.dev.3kit.ai'
const UNTRUSTED_SRC = 'https://evil.example.com/configurator'
const UNTRUSTED_ORIGIN = 'https://evil.example.com'

describe('buildIframeSrc', () => {
  it('returns empty string when no src', () => {
    expect(buildIframeSrc('')).toBe('')
  })

  it('returns the raw src when it is not a valid URL', () => {
    expect(buildIframeSrc('not a url', '?cat=loaders')).toBe('not a url')
  })

  it('always forwards cat/subcat/model when no additional params are set', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&subcat=compact-track-loaders&model=t450'))
    expect(result.searchParams.get('cat')).toBe('loaders')
    expect(result.searchParams.get('subcat')).toBe('compact-track-loaders')
    expect(result.searchParams.get('model')).toBe('t450')
  })

  it('drops other page params when no additional params are set', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&utm_source=x'))
    expect(result.searchParams.get('cat')).toBe('loaders')
    expect(result.searchParams.has('utm_source')).toBe(false)
  })

  it('forwards additional params when they are listed', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&utm_source=x', 'utm_source'))
    expect(result.searchParams.get('utm_source')).toBe('x')
  })

  it('only forwards allowlisted params when an allowlist is set', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&utm=x', 'cat, model'))
    expect(result.searchParams.get('cat')).toBe('loaders')
    expect(result.searchParams.has('utm')).toBe(false)
  })

  it('always forwards the share-config param "b" even when it is not allowlisted', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&b=abc123', 'cat'))
    expect(result.searchParams.get('b')).toBe('abc123')
  })

  it('always forwards the legacy share-config param "build" even when it is not allowlisted', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=loaders&build=abc123'))
    expect(result.searchParams.get('build')).toBe('abc123')
  })

  it('drops empty-valued params', () => {
    const result = new URL(buildIframeSrc(THREEKIT_SRC, '?cat=&model=t450'))
    expect(result.searchParams.has('cat')).toBe(false)
    expect(result.searchParams.get('model')).toBe('t450')
  })
})

describe('getTrustedOrigin', () => {
  it.each([
    ['https://bobcat-cpq.dev.3kit.ai/configurator', 'https://bobcat-cpq.dev.3kit.ai'],
    ['https://bobcat-cpq.3kit.ai/', 'https://bobcat-cpq.3kit.ai'],
    ['  https://bobcat-cpq.qa.3kit.ai/?x=1  ', 'https://bobcat-cpq.qa.3kit.ai']
  ])('trusts %s', (src, origin) => {
    expect(getTrustedOrigin(src)).toBe(origin)
  })

  it.each([
    ['an unrelated host', 'https://evil.example.com/'],
    ['a look-alike host', 'https://evil3kit.ai/'],
    ['3kit.ai as a subdomain of another host', 'https://bobcat-cpq.3kit.ai.evil.com/'],
    ['the bare 3kit.ai domain', 'https://3kit.ai/'],
    ['http', 'http://bobcat-cpq.dev.3kit.ai/'],
    ['an explicit port', 'https://bobcat-cpq.dev.3kit.ai:8443/'],
    ['an invalid URL', 'not a url'],
    ['an empty src', '']
  ])('rejects %s', (_label, src) => {
    expect(getTrustedOrigin(src)).toBeNull()
  })
})

describe('ThreekitConfigurator messaging', () => {
  let wrappers = []

  const COOKIES = {_ga: 'GA1.1.123456789.1700000000', Bobcat_ref: 'utm_source=newsletter'}

  const mount = (props = {}, cookies = COOKIES) => {
    const wrapper = shallowMount(ThreekitConfigurator, {
      propsData: {src: THREEKIT_SRC, ...props},
      mocks: {
        $nuxt: {
          context: {
            $cookies: {get: jest.fn(name => cookies[name])}
          }
        }
      }
    })
    wrappers.push(wrapper)
    return wrapper
  }

  // Mount, wait for the client-only iframe to render, and stub its
  // contentWindow (jsdom only creates one for iframes attached to the document).
  // `child` stands in for the iframe's window: messages must come from it.
  const mountWithIframe = async (props, cookies) => {
    const wrapper = mount(props, cookies)
    await wrapper.vm.$nextTick()
    const postMessage = jest.fn()
    const child = {postMessage}
    Object.defineProperty(wrapper.vm.$refs.iframe, 'contentWindow', {value: child})
    return {wrapper, postMessage, child}
  }

  const parentUrlMessage = (ga = COOKIES._ga, ref = COOKIES.Bobcat_ref) => ({
    type: 'bobcat-cpq:parent-url',
    href: window.location.href,
    ga,
    ref
  })

  // jsdom's MessageEvent only accepts real windows as `source`, so set it
  // after construction.
  const fireMessage = (origin, data, source = null) => {
    const event = new MessageEvent('message', {origin, data})
    Object.defineProperty(event, 'source', {value: source})
    window.dispatchEvent(event)
  }

  const location = () => new URL(window.location.href).searchParams

  // jsdom serves pages from its own origin (http://localhost); replaceState
  // only permits same-origin navigation, so seed the URL with a relative path.
  const PAGE_PATH = '/na/en/buying-tools/build-price'

  beforeEach(() => {
    window.history.replaceState(null, '', PAGE_PATH)
  })

  afterEach(() => {
    // Tear down every mounted component so its window message listener is
    // removed and does not leak into the next test.
    wrappers.forEach(wrapper => wrapper.destroy())
    wrappers = []
    jest.restoreAllMocks()
  })

  describe('location messages', () => {
    it('mirrors cat/subcat/model from a child location message into the parent URL', async () => {
      const {child} = await mountWithIframe()
      fireMessage(THREEKIT_ORIGIN, {
        type: 'bobcat-cpq:location',
        cat: 'loaders',
        subcat: 'compact-track-loaders',
        model: 't450'
      }, child)
      expect(location().get('cat')).toBe('loaders')
      expect(location().get('subcat')).toBe('compact-track-loaders')
      expect(location().get('model')).toBe('t450')
    })

    it('removes a synced param when the child omits it', async () => {
      window.history.replaceState(null, '', `${PAGE_PATH}?cat=loaders&model=t450`)
      const {child} = await mountWithIframe()
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', cat: 'loaders'}, child)
      expect(location().get('cat')).toBe('loaders')
      expect(location().has('model')).toBe(false)
    })

    it('removes share-config params (b, build) on every location message', async () => {
      window.history.replaceState(null, '', `${PAGE_PATH}?cat=loaders&model=t450&b=abc123&build=old123&utm_source=x`)
      const {child} = await mountWithIframe()
      // Same screen as the share link, and a b field the child should not set.
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', cat: 'loaders', model: 't450', b: 'new456'}, child)
      expect(location().has('b')).toBe(false)
      expect(location().has('build')).toBe(false)
      expect(location().get('model')).toBe('t450')
      expect(location().get('utm_source')).toBe('x')
    })

    it('ignores invalid (non-slug) values from the child', async () => {
      const {child} = await mountWithIframe()
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', model: 'T 450!!'}, child)
      expect(location().has('model')).toBe(false)
    })

    it('ignores unrelated message types', async () => {
      const {child} = await mountWithIframe()
      fireMessage(THREEKIT_ORIGIN, {type: 'something-else', model: 't450'}, child)
      expect(location().has('model')).toBe(false)
    })

    it('ignores messages from a foreign origin', async () => {
      const {child} = await mountWithIframe()
      fireMessage(UNTRUSTED_ORIGIN, {type: 'bobcat-cpq:location', model: 't450'}, child)
      expect(location().has('model')).toBe(false)
    })

    it('ignores messages from the trusted origin that do not come from its iframe', async () => {
      await mountWithIframe()
      const otherWindow = {postMessage: jest.fn()}
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', model: 't450'}, otherWindow)
      expect(location().has('model')).toBe(false)
    })

    it('removes the message listener on unmount', async () => {
      const {wrapper, child} = await mountWithIframe()
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', model: 't450'}, child)
      expect(location().get('model')).toBe('t450')

      wrapper.destroy()
      window.history.replaceState(null, '', PAGE_PATH)
      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:location', model: 's650'}, child)
      expect(location().has('model')).toBe(false)
    })
  })

  describe('parent-url messages', () => {
    it('posts the parent url and cookies into the iframe when the child reports ready', async () => {
      const {postMessage, child} = await mountWithIframe()

      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:ready'}, child)

      expect(postMessage).toHaveBeenCalledWith(parentUrlMessage(), THREEKIT_ORIGIN)
    })

    it('posts the parent url and cookies into the iframe when it loads', async () => {
      const {wrapper, postMessage} = await mountWithIframe()

      await wrapper.find('iframe').trigger('load')

      expect(postMessage).toHaveBeenCalledWith(parentUrlMessage(), THREEKIT_ORIGIN)
    })

    it('sends null for cookies that are not set', async () => {
      const {wrapper, postMessage} = await mountWithIframe({}, {})

      await wrapper.find('iframe').trigger('load')

      expect(postMessage).toHaveBeenCalledWith(parentUrlMessage(null, null), THREEKIT_ORIGIN)
    })

    it('reads cookies as raw strings (no JSON parsing)', async () => {
      const {wrapper} = await mountWithIframe()

      await wrapper.find('iframe').trigger('load')

      const {get} = wrapper.vm.$nuxt.context.$cookies
      expect(get).toHaveBeenCalledWith('_ga', {parseJSON: false})
      expect(get).toHaveBeenCalledWith('Bobcat_ref', {parseJSON: false})
    })

    it('does not respond to ready from a foreign origin', async () => {
      const {postMessage, child} = await mountWithIframe()

      fireMessage(UNTRUSTED_ORIGIN, {type: 'bobcat-cpq:ready'}, child)

      expect(postMessage).not.toHaveBeenCalled()
    })

    it('does not respond to ready from another window on the trusted origin', async () => {
      const {postMessage} = await mountWithIframe()
      const otherWindow = {postMessage: jest.fn()}

      fireMessage(THREEKIT_ORIGIN, {type: 'bobcat-cpq:ready'}, otherWindow)

      expect(postMessage).not.toHaveBeenCalled()
      expect(otherWindow.postMessage).not.toHaveBeenCalled()
    })
  })

  describe('untrusted configurator URL (fail soft)', () => {
    let warn
    const mountUntrusted = () => {
      warn = jest.spyOn(global.console, 'warn').mockImplementation(() => {})
      return mountWithIframe({src: UNTRUSTED_SRC})
    }

    it('still renders the iframe', async () => {
      const {wrapper} = await mountUntrusted()
      expect(wrapper.find('iframe').attributes('src')).toContain(UNTRUSTED_ORIGIN)
    })

    it('sends no page URL or cookies on load or ready', async () => {
      const {wrapper, postMessage, child} = await mountUntrusted()

      await wrapper.find('iframe').trigger('load')
      fireMessage(UNTRUSTED_ORIGIN, {type: 'bobcat-cpq:ready'}, child)

      expect(postMessage).not.toHaveBeenCalled()
      expect(wrapper.vm.$nuxt.context.$cookies.get).not.toHaveBeenCalled()
    })

    it('ignores location messages from the configured origin', async () => {
      const {child} = await mountUntrusted()
      fireMessage(UNTRUSTED_ORIGIN, {type: 'bobcat-cpq:location', model: 't450'}, child)
      expect(location().has('model')).toBe(false)
    })

    it('warns that URL sync and Share Config are disabled', async () => {
      await mountUntrusted()
      expect(warn).toHaveBeenCalledWith(expect.stringContaining(UNTRUSTED_SRC))
    })
  })
})
