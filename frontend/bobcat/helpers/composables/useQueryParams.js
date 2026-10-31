import {ref, useRoute} from '@nuxtjs/composition-api'

export default function useQueryParams() {
  const route = useRoute()
  const queryParams = ref(route?.value?.query)

  const updateQueryParams = (newParams = {}) => {
    if (!process.client) { return }

    queryParams.value = {
      ...queryParams.value,
      ...newParams
    }

    queryParams.value = Object.keys(queryParams.value)
      .filter(key => !!queryParams.value[key])
      .reduce((obj, key) => {
        return {
          ...obj,
          [key]: queryParams.value[key]
        }
      }, {})
    const urlParams = new URLSearchParams(queryParams.value).toString()
    const newPath = route.value.path + (urlParams !== '' ? `?${urlParams}` : '')
    route?.value && history.pushState({}, null, newPath)
  }

  return {
    queryParams,
    updateQueryParams
  }
}
