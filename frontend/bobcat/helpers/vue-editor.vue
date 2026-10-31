<script>
const hasWindow = typeof window !== 'undefined'
const inIframe = hasWindow ? window.self !== window.top : false
export const inMgnlIframe = inIframe && hasWindow && window?.location?.search?.includes('mgnlPreview')
export const inEditorEdit = hasWindow && (inIframe || window?.location?.search?.includes('editmode=on'))
export const inMgnlPreview = hasWindow && window?.location?.search?.includes('mgnlPreview=true')

export const insertComments = (node, commentOpen, commentClose) => {
  if (node) {
    node.parentNode.insertBefore(document.createComment(commentOpen), node)
    node.parentNode.insertBefore(document.createComment(commentClose), node.nextSibling)
  }
}

export const getProps = (node) => {
  const props = {}
  const metadata = {}

  Object.keys(node).forEach((key) => {
    if (key.match(/^(@|mgnl:|jcr:)/)) {
      metadata[key] = node[key]
    } else {
      props[key] = node[key]
    }
  })

  props.metadata = metadata

  return props
}

export default {}
</script>
