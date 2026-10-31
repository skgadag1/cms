export default function ({route, redirect}) {
  const shouldRedirect = route.path.length > 2 && route?.path.endsWith('/')

  if (shouldRedirect) {
    const _newPath = route.fullPath.replace(route.path, route.path.slice(0, -1))
    // eslint-disable-next-line no-console
    console.info(`[TrailingRedirect middleware] will redirect now ${route.fullPath} to new path without trailing slash ${_newPath}`)
    return redirect(_newPath)
  }
}
