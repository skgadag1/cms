export const getURLwithQuery = (baseURL, query) => {
  if (!query || Object.keys(query).length === 0) { return baseURL }

  const queryString = Object.entries(query)
    .map(a => a[0] + '=' + a[1])
    .join('&')

  return `${baseURL}?${queryString}`
}
