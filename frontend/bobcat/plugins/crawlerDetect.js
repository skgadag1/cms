export default async (context, inject) => {
  if (context.__hasisBotEnabled__ !== true) {
    try {
      const isbotModule = () => import('isbot' /* webpackChunkName: 'isbot' */)
      let userAgent = ''
      let preload = false
      let isBot = false

      if (typeof context.req !== 'undefined') {
        userAgent = context.req.headers['user-agent'] || ''
        preload = context.req.headers['x-preload'] === 'true'
      } else if (typeof navigator !== 'undefined') {
        userAgent = navigator.userAgent
      }

      const isLighthouse = userAgent?.toLowerCase()?.includes('lighthouse')

      if (!isLighthouse) {
        const isBotModule = await isbotModule()
        isBot = await isBotModule?.default(userAgent)
      } else {
        preload = false
      }

      // eslint-disable-next-line
      console.info(`[Crawler Detect] SSR Request: ${context.req?.originalUrl}, UA: ${userAgent}, isBotByCF: ${preload}, isBotByUA: ${isBot}, isLighthouse: ${isLighthouse}`)

      context.$isBot = preload || isBot
      context.$isLighthouse = isLighthouse
      inject('isBot', preload || isBot)
      inject('isLighthouse', isLighthouse)
      context.__hasisBotEnabled__ = true
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(`[Crawler Detect] Unable to run isbot module ${e}`)
    }
  }
}
