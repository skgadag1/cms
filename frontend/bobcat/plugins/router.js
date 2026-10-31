export default ({app}) => {
  app.router.beforeEach((to, from, next) => {
    // prevent navigate on hash change only for cludo search page
    if (
      (from.hash.includes('cludoquery') && to.hash.includes('cludoquery')) || // pagination case
      (from.path === to.path && to.hash.includes('cludoquery')) // search on search page case
    ) { return }
    next()
  })
}
