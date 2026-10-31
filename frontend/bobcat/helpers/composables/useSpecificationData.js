import {ref, useContext} from '@nuxtjs/composition-api'

export default function useSpecificationData(props, globalOptions = {}, shouldSort = false) {
  const {$services, $utils, $regions} = useContext()
  const options = {...props.config, ...globalOptions}

  const CURRENT_LANGUAGE = $regions?.current?.language?.name || process.env.NUXT_DEFAULT_LANGUAGE

  const tableData = ref(null)
  const containerClass = ref(options.containerClass || '')
  const modelSpecifications = ref({})
  const error = ref(false)
  const FIRST_COLUMN_WIDTH_CLASS = (props.models?.length === 1 || !(props.models && props.config?.multipleModels)) ? 'col-6 col-lg-7' : 'col-sticky col-md-4 col-xl'

  const specificationFetch = async (specLevel = props.specificationLevel, mType = props.modelType, labels = {}) => {
    error.value = false
    tableData.value = {}
    modelSpecifications.value = {}

    const inProgress = []
    const requests = props.models.map((m) => {
      if (!inProgress.includes(m.id) && !modelSpecifications.value[m.id]) {
        inProgress.push(m.id)
        return $services.products.getModelSpecifications(m, props.uom.value, specLevel, mType).then(({data}) => {
          modelSpecifications.value[m.id] = {model: m, data}
        }).catch((e) => {
          // eslint-disable-next-line no-console
          console.error(`[CompareTable] Error for model: ${m['@name']}, ${e}`)
          throw e
        })
      }

      return Promise.resolve()
    })

    // allSettled: one hung/failed model no longer hangs the spinner forever or blanks the
    // whole table. Render specs for the models that succeeded; only flag an error when none did.
    const results = await Promise.allSettled(requests)
    const failures = results.filter(r => r.status === 'rejected')
    if (failures.length) {
      // eslint-disable-next-line no-console
      console.error(`[CompareTable] ${failures.length} of ${results.length} models cannot be fetched`)
    }

    try {
      // Keep every selected model as a column, preserving the requested order. Models whose
      // request failed get a placeholder with no data so their comparison column is retained
      // (rendered as empty cells) and subsequent columns stay aligned with the tile/dropdown
      // grid instead of shifting left.
      const orderedSpecs = (props.models || [])
        .map(m => modelSpecifications.value[m.id] || {model: m, data: null})

      if (results.every(r => r.status === 'rejected')) {
        // Nothing usable came back - surface the all-or-nothing failure.
        error.value = true
        tableData.value = {}
        return
      }

      tableData.value = $utils.products.structurizeSpecsForCompareTable(orderedSpecs,
        options.filterSections,
        options.filterProps,
        labels,
        options.filterPropsId,
        CURRENT_LANGUAGE,
        shouldSort
      )
      if (options.customGroupHeader) { tableData.value = $utils.products.groupSpecsForCompareTable(tableData.value, options.customGroupHeader, labels) }
      containerClass.value = `specification-compare-table__container-${tableData.value.models.length}`
    } catch (e) {
      error.value = true
      // eslint-disable-next-line no-console
      console.error(`[CompareTable] Error while structurizing specs for compare table: ${e}`)
    }
  }

  return {
    FIRST_COLUMN_WIDTH_CLASS,
    specificationFetch,
    error,
    tableData,
    containerClass
  }
}
