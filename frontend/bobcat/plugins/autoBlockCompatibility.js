export default async (context) => {
  if (context.__auto_block_compatibility__ !== true) {
    try {
      const srcPrefix = new URL(
        ['cdnURL', 'assetsPath', 'basePath'].map(k => context.$config._app[k]).filter(v => v && v !== '/').join(''),
        window.location.origin
      ).toString()

      const observer = new MutationObserver((records) => {
        for (const record of records) {
          for (const addedNode of record.addedNodes) {
            const shouldUnblock = addedNode instanceof HTMLScriptElement && !('otIgnore' in addedNode.dataset) && addedNode.src &&
                                    (addedNode.src.startsWith(srcPrefix) || addedNode.src.startsWith('/'))
            if (shouldUnblock) {
              addedNode.dataset.otIgnore = ''
              // eslint-disable-next-line no-console
              console.log(`[AutoBlock Compatibility] Unblocked script ${addedNode.src}`)
            }
          }
        }
      })

      observer.observe(document.head, {childList: true})

      context.__auto_block_compatibility__ = true
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(`[AutoBlock Compatibility] Unable to listen for scripts ${e}`)
    }
  }
}
