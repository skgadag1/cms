export default function anchorScrollPlugin({app}) {
  if (process.client) {
    const scrollToAnchor = (hash) => {
      const anchorId = decodeURIComponent(hash.replace('#', ''))
      const anchorElement = document.getElementById(anchorId)

      if (anchorElement) {
        setTimeout(() => {
          anchorElement.scrollIntoView({behavior: 'smooth'})
        }, 400)
      }
    }

    window.addEventListener('load', () => {
      if (window.location.hash) {
        scrollToAnchor(window.location.hash)
      }
    })

    app.router.afterEach((to) => {
      if (to.hash) {
        scrollToAnchor(to.hash)
      }
    })
  }
}
