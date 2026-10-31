import {NODE_ATTRIBUTES, INHERITANCE_KEYS, PRODUCT_NODE_TYPES} from '~/constants/magnolia'
import {MODEL_LIFECYCLE} from '~/constants'

const inheritanceKeyToFieldsMapping = {
  [INHERITANCE_KEYS.SPECIAL_OFFER]: ['specialOfferDetails', 'specialOfferDetails2', 'specialOffersDetailsAdditionalInfo'],
  [INHERITANCE_KEYS.SUGGESTED_PRICING]: ['suggestedPrice', 'suggestedPrice2', 'suggestedPriceAdditionalInfo']
}

const getInheritanceDisabled = (currentModel, key) => {
  const isDisabled = currentModel[key]
  if (typeof (isDisabled) === 'undefined' && key === INHERITANCE_KEYS.SUGGESTED_PRICING) {
    return getInheritanceDisabled(currentModel, INHERITANCE_KEYS.SPECIAL_OFFER)
  }
  return isDisabled === 'true'
}

const productInheritanceFlow = {
  [PRODUCT_NODE_TYPES.PRODUCT]: (currentModel, key, utils) => ({
    canInheritFromThis: () => true,
    nextModel: async () => {
      if (getInheritanceDisabled(currentModel, key)) {
        return
      }
      const currentPath = currentModel[NODE_ATTRIBUTES.PATH]
      if (!utils.pathValidator(currentPath)) {
        return
      }
      const parentPath = currentPath.split('/').slice(0, -1).join('/')
      return await utils.getModel(parentPath)
    }
  }),
  [PRODUCT_NODE_TYPES.PRODUCT_GROUP]: (currentModel, key) => ({
    canInheritFromThis: () => !getInheritanceDisabled(currentModel, key),
    nextModel: () => null
  })
}

const productGroupInheritanceFlow = {
  [PRODUCT_NODE_TYPES.PRODUCT]: (currentModel, key, utils) => ({
    canInheritFromThis: () => true,
    nextModel: async () => {
      if (getInheritanceDisabled(currentModel, key)) {
        return
      }
      const inheritableModels = utils.getNextProductsFromGroup(currentModel[NODE_ATTRIBUTES.PATH])
      return inheritableModels.find(product => getInheritanceDisabled(product, key))
    }
  }),
  [PRODUCT_NODE_TYPES.PRODUCT_GROUP]: (currentModel, key, utils) => {
    const inheritableModels = utils.getNextProductsFromGroup()
    const forcedModel = inheritableModels.find(product => getInheritanceDisabled(product, key))
    const isGroupInheritanceDisabled = getInheritanceDisabled(currentModel, key) && !forcedModel
    return {
      canInheritFromThis: () => !isGroupInheritanceDisabled && !forcedModel,
      nextModel: () => {
        if (forcedModel) {
          return forcedModel
        } else if (isGroupInheritanceDisabled) {
          return inheritableModels[0]
        }
      }
    }
  }
}

const inheritancesFlows = {
  [PRODUCT_NODE_TYPES.PRODUCT]: (currentModel, key, utils) => productInheritanceFlow[currentModel?.[NODE_ATTRIBUTES.TYPE]]?.(currentModel, key, utils),
  [PRODUCT_NODE_TYPES.PRODUCT_GROUP]: (currentModel, key, utils) => productGroupInheritanceFlow[currentModel?.[NODE_ATTRIBUTES.TYPE]]?.(currentModel, key, utils)
}

const handleInheritance = async (inheritableModel, key, utils) => {
  const inheritanceFields = inheritanceKeyToFieldsMapping[key]
  const values = {}
  const baseFlow = inheritancesFlows[inheritableModel?.[NODE_ATTRIBUTES.TYPE]]
  let model = inheritableModel
  while (model) {
    const currentFlow = baseFlow?.(model, key, utils)
    if (!currentFlow || currentFlow.canInheritFromThis()) {
      for (const k of inheritanceFields) {
        if (model[k]) {
          values[k] = model[k]
        }
      }
    }
    model = await currentFlow?.nextModel()
  }
  return values
}

/**
 * Loads suggested pricing and special offer fields for any product node (with PC inheritance rules).
 */
export async function fetchProductPricingOffers(model, {$services, $utils}) {
  if (!model || model.lifecycle?.toLowerCase() === MODEL_LIFECYCLE.HISTORICAL) {
    return {
      specialOfferDetails: null,
      specialOfferDetails2: null,
      specialOffersDetailsAdditionalInfo: null,
      suggestedPrice: null,
      suggestedPrice2: null,
      suggestedPriceAdditionalInfo: null
    }
  }

  const modelCache = {}
  const utils = {
    getModel: async (path) => {
      if (path in modelCache) {
        return modelCache[path]
      }
      const loaded = await $services.bobcatapi.getProduct(null, path)
      modelCache[path] = loaded
      return loaded
    },
    pathValidator: (currentPath) => {
      const path = $utils.pathWithoutBaseName(currentPath.replace(`/${currentPath.split('/').pop()}`, '')).split('/').filter(Boolean)
      if (path.length <= 2) {
        return false
      }
      return true
    },
    getNextProductsFromGroup: (currentModelPath) => {
      const allModels = model['@nodes']
        ? model['@nodes']
          .map((k) => JSON.parse(JSON.stringify(model[k])))
          .filter((node) => node[NODE_ATTRIBUTES.TYPE] === PRODUCT_NODE_TYPES.PRODUCT)
          .filter((node) => node.lifecycle?.toLowerCase() !== MODEL_LIFECYCLE.HISTORICAL)
        : []
      const fromIndex = currentModelPath ? allModels.findIndex((node) => node[NODE_ATTRIBUTES.PATH] === currentModelPath) : -1
      return allModels.slice(fromIndex + 1)
    }
  }

  if (model?.[NODE_ATTRIBUTES.PATH]) {
    modelCache[model[NODE_ATTRIBUTES.PATH]] = model
  }

  const offerFields = await handleInheritance(model, INHERITANCE_KEYS.SPECIAL_OFFER, utils)
  const pricingFields = await handleInheritance(model, INHERITANCE_KEYS.SUGGESTED_PRICING, utils)

  return {
    specialOfferDetails: offerFields.specialOfferDetails,
    specialOfferDetails2: offerFields.specialOfferDetails2,
    specialOffersDetailsAdditionalInfo: offerFields.specialOffersDetailsAdditionalInfo,
    suggestedPrice: pricingFields.suggestedPrice,
    suggestedPrice2: pricingFields.suggestedPrice2,
    suggestedPriceAdditionalInfo: pricingFields.suggestedPriceAdditionalInfo
  }
}
