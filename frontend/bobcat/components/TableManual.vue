<template>
  <section v-cloak class="transition-base py-4" data-test-id="manual-table">
    <div class="container">
      <div class="row">
        <LazyHydrate never>
          <div v-if="title" class="col-12">
            <div class="border-bottom mb-4">
              <component :is="htag" :class="htag" data-test-id="manual-table-title">{{ title }}</component>
            </div>
          </div>
        </LazyHydrate>

        <CompareTableWrapperOld v-if="sections && sections.length"
                                data-test-id="manual-table-spec-table"
                                class="col-12 mt-4 manual-table-spec"
                                :models="columns"
                                :table-specification-data="manualTableData"
                                :config="config"
                                expanded
        />
        <WarningAlert v-else-if="inEditorEdit" class="col-12" data-test-id="manual-table-alert__nodata">
          No data specified for table
        </WarningAlert>
      </div>
    </div>
  </section>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String,
      required: true
    },
    htag: {
      type: String,
      default: 'h2'
    },
    showHeaders: {
      type: String
    },
    manualTable: {
      type: Object
    }
  },
  setup(props) {
    const {showHeaders, manualTable} = props
    const {$utils, $regions} = useContext()
    const sections = manualTable?.['@nodes'].map(x => manualTable[x]) || []
    useMounted(props.nodeId)

    // Filter out columns with no rows or columns whose rows have no values
    const columns = sections?.[0]?.['@nodes'].map(y => sections[0][y]).filter((col) => {
      const rows = col?.rows?.['@nodes']?.map(p => col?.rows[p]).filter(row => row?.txt?.trim()) || []
      return (col?.header?.length || rows.length > 0) // show column if it has header or non-empty rows
    }) || []

    const getPropertyValue = (prop) => {
      // map value to proper string
      let val = null

      switch (prop.field) {
      case 'assets':
        val = `<a href="${$utils.getCloudinaryDocument(prop[prop.field])}" target="_blank">${prop.linkLabel || $utils.getCloudinaryAssetMetadata(prop[prop.field])?.metadata?.filename}</a>`
        break
      case 'urls': {
        const linkField = $utils.getLinkField(prop[prop.field], $regions.current.language.name)
        val = `<a href="${linkField?.url}" target="${linkField?.target}">${linkField?.label}</a>`
        break
      }
      default:
        val = prop[prop.field]
      }

      return {
        type: 'text',
        value: val
      }
    }

    const manualTableTmpData = []

    // structurize manual data into data similar to "real specs"
    columns?.forEach((column) => {
      const columnName = column?.['@name']
      manualTableTmpData.push({
        model: {
          id: columnName,
          title: column?.header || ' '
        },
        data: {
          sections: sections?.map((s) => {
            return {
              title: s.sectionTitle,
              properties: s?.[columnName]?.rows?.['@nodes']?.map((p) => {
                const prop = s?.[columnName]?.rows[p]
                const propValue = getPropertyValue(prop)

                return {
                  id: prop.key,
                  label: prop.key,
                  ...propValue
                }
              }) || []
            }
          })
        }
      })
    })

    const manualTableData = $utils.products.structurizeSpecsForCompareTable(manualTableTmpData)

    const config = ref({
      alwaysShowHeader: showHeaders !== 'false',
      containerClass: `specification-compare-table__container-${columns?.length}`,
      multipleModels: columns?.length > 1
    })

    return {
      config,
      columns,
      manualTableData,
      sections,
      inEditorEdit
    }
  }
}
</script>
