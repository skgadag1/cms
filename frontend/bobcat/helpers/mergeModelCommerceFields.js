/**
 * Merge commerce display fields: API payload overrides PC / Magnolia fields.
 * @param {object} pcFields from fetchProductPricingOffers
 * @param {object|null} apiPayload optional API (e.g. Apex) keyed like pcFields
 * @param {object} manual optional dialog-level overrides (unused until dialog fields exist)
 */
export function mergeModelCommerceFields(pcFields, apiPayload = null, manual = {}) {
  const pick = key => (apiPayload && apiPayload[key]) || manual[key] || pcFields[key]
  return {
    specialOfferDetails: pick('specialOfferDetails'),
    specialOfferDetails2: pick('specialOfferDetails2'),
    specialOffersDetailsAdditionalInfo: pick('specialOffersDetailsAdditionalInfo'),
    suggestedPrice: pick('suggestedPrice'),
    suggestedPrice2: pick('suggestedPrice2'),
    suggestedPriceAdditionalInfo: pick('suggestedPriceAdditionalInfo')
  }
}
