import {ref} from '@nuxtjs/composition-api'

export default function useCompare(_modelsArray = [], minimumModelSize = 0) {
  const showCompareStripe = ref(false)
  const showCompareTable = ref(false)
  const modelsToCompare = ref([..._modelsArray])

  const addToCompare = (_m) => {
    showCompareStripe.value = true
    showCompareTable.value = false
    modelsToCompare.value.push(_m)
  }

  const enableCompareTable = () => {
    showCompareTable.value = true
  }

  const removeFromCompare = (_id) => {
    modelsToCompare.value = modelsToCompare.value.filter(x => (x?.['@id'] || x.id) !== _id)
    showCompareTable.value = false
  }

  const clearAll = () => {
    const mid = _modelsArray.length ? _modelsArray?.[0]?.['@id'] || _modelsArray?.[0]?.id : null
    if (mid && modelsToCompare.value.length > minimumModelSize) { modelsToCompare.value = modelsToCompare.value.filter(x => x['@id'] === mid) } else { modelsToCompare.value = [..._modelsArray] }
    showCompareTable.value = false
  }

  return {
    showCompareStripe,
    modelsToCompare,
    addToCompare,
    removeFromCompare,
    clearAll,
    showCompareTable,
    enableCompareTable
  }
}
