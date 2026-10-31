export const PRODUCT_SPECS_REMOVED_PROPERTIES = ['references']
export const PRODUCT_SPECS_REMOVED_SECTIONS = ['general', 'refill capacities', 'core', 'machine', 'comprehensive properties', 'machine ratings', 'compatibility']
export const ATTACHMENT_SPECS_REMOVED_PROPERTIES = ['references']
export const ATTACHMENT_SPECS_REMOVED_PROPERTIES_ID = ['attachment-combined-compatibility_property']
export const ATTACHMENT_SPECS_REMOVED_SECTIONS = ['core', 'comprehensive properties']
export const MODEL_TYPE = {
  ATTACHMENT: 'attachment',
  PRODUCT: 'product',
  PRODUCT_GROUP: 'productGroup'
}

export const MODEL_LIFECYCLE = {
  HISTORICAL: 'historical',
  CURRENT: 'current'
}

// object to translate values while using concepts [NGB-2271]
// hack for limitation with JCR name allowed chars
export const SPECS_VALUES_DICTIONARY = {
  'n/a': 'n-a'
}
