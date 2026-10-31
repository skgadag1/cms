const inIframe: boolean =
  typeof window !== 'undefined' ? window.self !== window.top : false
export const inMgnlIframe =
  inIframe &&
  typeof window !== 'undefined' &&
  window.location.search.includes('mgnlPreview')
export const inEditorEdit =
  inIframe && window.location.search.includes('mgnlPreview=false')

export const inMgnl = inMgnlIframe

export default {}
