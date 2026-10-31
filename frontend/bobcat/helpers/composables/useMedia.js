export default function useMedia() {
  const updateMediaElements = (container, selector) => {
    const elements = container.querySelectorAll(selector)
    elements.forEach((element) => {
      if (!element.hasAttribute('data-src')) {
        const src = element.getAttribute('src')
        if (src) {
          element.setAttribute('data-src', src)
        }
      }
    })
  }

  return {
    updateMediaElements
  }
}
