<template>
  <input
    v-if="dynamicValue"
    :id="id"
    :name="name"
    class="d-none"
    type="hidden"
    :value="dynamicValue"
    data-test-id="dynamic-hidden-field"
    :data-dynamic-value="dynamicValue"
  >
</template>

<script>
import {onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import {PRODUCT_NODE_TYPES} from '../../../constants/magnolia'
import useModels from '@/helpers/composables/useModels'
import useDealerLocator from '~/helpers/composables/useDealerLocator'
import {HIDDEN_FIELDS_NAMES, HIDDEN_FIELDS_VALUES} from '~/constants/hiddenFields'
import HARDCODED_PRODUCT_TYPES from '~/constants/hardcodedProductTypes'

export default {
  inject: ['model'],
  props: {
    name: {
      type: String
    },
    id: {
      type: String
    },
    field: {
      type: Object
    },
    onChange: {
      type: Function
    }
  },
  setup(props) {
    const {model} = useModels()
    const {name, onChange, field} = props
    const dynamicValue = ref('')
    const {route} = useContext()
    const currency = ref(route.value.query?.currency || 'USD')
    const type = field?.dataSource?.type || field?.type
    const {dealerLocatorData} = useDealerLocator()
    const {$regions, $cookies} = useContext()

    const updateDynamicValue = async () => {
      if (type === HIDDEN_FIELDS_NAMES.MODEL) {
        dynamicValue.value = model?.name
      } else if (type === HIDDEN_FIELDS_NAMES.MODEL_ID) {
        dynamicValue.value =
          model?.id ||
          model?.progressiveFormId ||
          ([PRODUCT_NODE_TYPES.ATTACHMENT_CATEGORY, PRODUCT_NODE_TYPES.ATTACHMENT_GROUP, PRODUCT_NODE_TYPES.ATTACHMENT].includes(model?.['@nodeType'])
            ? model?.['@name']
            : 'no_id')
      } else if (type === HIDDEN_FIELDS_NAMES.MODEL_TITLE) {
        dynamicValue.value = model?.title || model?.['@name']
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_AR) {
        dynamicValue.value = model?.dealer?.arNumber
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_NAME) {
        dynamicValue.value = model?.dealer?.name
      } else if (type === HIDDEN_FIELDS_NAMES.TYPE) {
        const {ZERO_TURN_MOWERS, WALK_BEHIND_MOWERS, ZERO_STAND_ON_MOWER} = HARDCODED_PRODUCT_TYPES
        if (model?.productTypeLabel?.toLowerCase() === ZERO_TURN_MOWERS.title.toLowerCase()) {
          dynamicValue.value = ZERO_TURN_MOWERS.value
        } else if (model?.productTypeLabel?.toLowerCase() === WALK_BEHIND_MOWERS.title.toLowerCase()) {
          dynamicValue.value = WALK_BEHIND_MOWERS.value
        } else if (model?.productTypeLabel?.toLowerCase() === ZERO_STAND_ON_MOWER.title.toLowerCase()) {
          dynamicValue.value = ZERO_STAND_ON_MOWER.value
        } else {
          dynamicValue.value = model?.productTypeLabel
        }
      } else if (type === HIDDEN_FIELDS_NAMES.SERIAL) {
        dynamicValue.value = model?.serial
      } else if (type === HIDDEN_FIELDS_NAMES.PRICE) {
        const eNP = currency.value === 'USD'
          ? model?.pricing?.estimatedNetPrices?.find(x => x.currency === 'USD')
          : model.pricing?.estimatedNetPrices?.find(x => x.currency === 'CAD')
        dynamicValue.value = eNP?.value && eNP?.currency ? `${eNP.value} ${eNP.currency}` : ''
      } else if (type === HIDDEN_FIELDS_NAMES.EST_PAYMENT) {
        const eP = currency.value === 'USD'
          ? model?.pricing?.estimatedPayment?.find(x => x.currency === 'USD')
          : model.pricing?.estimatedPayment?.find(x => x.currency === 'CAD')
        dynamicValue.value = eP?.value && eP?.currency ? `${eP.value} ${eP.currency}` : ''
      } else if (type === HIDDEN_FIELDS_NAMES.MANUFACTURING) {
        dynamicValue.value = model?.pricing?.manufacturerFinancing
      } else if (type === HIDDEN_FIELDS_NAMES.REBATE) {
        const rebate = currency.value === 'USD'
          ? model?.pricing?.cashRebate?.find(x => x.currency === 'USD')
          : model.pricing?.cashRebate?.find(x => x.currency === 'CAD')
        dynamicValue.value = rebate?.value && rebate?.currency ? `${rebate.value} ${rebate.currency}` : ''
      } else if (type === HIDDEN_FIELDS_NAMES.URL) {
        dynamicValue.value = window.location.href
      } else if (type === HIDDEN_FIELDS_NAMES.CATEGORY) {
        const irwName = model?.IRWModelName || model?.productTypeLabel
        const contractCode = model?.IRWContractCode || model?.contractCode
        const parts = [irwName, contractCode].filter(Boolean)
        dynamicValue.value = parts.join('|') || irwName || ''
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_DEALER_NAME) {
        dynamicValue.value = dealerLocatorData[HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_DEALER_NAME] || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_MDM_ID) {
        dynamicValue.value = dealerLocatorData[HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_MDM_ID] || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_OPT_IN) {
        dynamicValue.value = dealerLocatorData[HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_OPT_IN] || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_CUSTOMER_NUMBER) {
        dynamicValue.value = dealerLocatorData[HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_CUSTOMER_NUMBER] || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_SERVICE) {
        dynamicValue.value = localStorage.getItem(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_SERVICE) || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.DEALER_LOCATOR_INDUSTRY) {
        dynamicValue.value = localStorage.getItem(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_INDUSTRY) || ' '
      } else if (type === HIDDEN_FIELDS_NAMES.SUBCATEGORY) {
        let subcategoryValue = ''
        if (model?.sfId) {
          subcategoryValue = model.sfId
        } else if (model?.parent) {
          subcategoryValue = model?.parent?.parentName || ''
        } else if (!subcategoryValue && process.client && window.location.href) {
          const url = new URL(window.location.href)
          const pathSegments = url.pathname.split('/').filter(Boolean)

          const equipmentIndex = pathSegments.findIndex(seg => seg === 'equipment')
          if (equipmentIndex !== -1 && pathSegments.length > equipmentIndex + 2) {
            const subcategorySegment = pathSegments[equipmentIndex + 2]
            subcategoryValue = subcategorySegment
              .split('-')
              .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
              .join(' ')
          }
        }

        dynamicValue.value = subcategoryValue
      } else if (type === HIDDEN_FIELDS_NAMES.PARENT_TITLE) {
        dynamicValue.value = model?.parent?.parentTitle || ''
      } else if (type === HIDDEN_FIELDS_NAMES.PARENT_SFID) {
        dynamicValue.value = model?.parent?.parentSfId || ''
      } else if (type === HIDDEN_FIELDS_NAMES.SITE_LANGUAGE) {
        dynamicValue.value = $regions.current?.language?.name ?? 'en'
      } else if (type === HIDDEN_FIELDS_NAMES.GOOGLEID) {
        const gaCookie = $cookies.get('_ga') || ''
        // GA4 format: GA1.1.xxxxxxxx.xxxxxxxxxx → sxxxxxxxx.xxxxxxxxxx
        const gaMatch = gaCookie.match(/^GA\d+\.\d+\.(.+)$/)
        dynamicValue.value = gaMatch ? `s${gaMatch[1]}` : gaCookie
      }

      if (onChange) {
        onChange(dynamicValue.value, name, true)
      }
    }

    watch(dealerLocatorData, updateDynamicValue)

    onMounted(() => {
      updateDynamicValue()
    })

    return {
      dynamicValue
    }
  }
}
</script>
