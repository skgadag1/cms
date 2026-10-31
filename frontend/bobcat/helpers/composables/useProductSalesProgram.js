import {onMounted, onServerPrefetch, useContext, useFetch, ref, inject} from '@nuxtjs/composition-api'
import {fetchProductPricingOffers} from '~/helpers/fetchProductPricingOffers'

export default function useProductSalesProgram(historicalContext) {
  const {$services, $utils} = useContext()
  const loaded = ref(false)
  const model = inject('model')

  const specialOfferDetails = ref(model?.specialOfferDetails)
  const specialOfferDetails2 = ref(model?.specialOfferDetails2)
  const specialOffersDetailsAdditionalInfo = ref(model?.specialOffersDetailsAdditionalInfo)
  const suggestedPriceAdditionalInfo = ref(model?.suggestedPriceAdditionalInfo)
  const suggestedPrice = ref(null)
  const suggestedPrice2 = ref(null)

  const {fetch} = useFetch(async () => {
    if (!model || historicalContext.value) { return }

    const fields = await fetchProductPricingOffers(model, {$services, $utils})
    specialOfferDetails.value = fields.specialOfferDetails
    specialOfferDetails2.value = fields.specialOfferDetails2
    specialOffersDetailsAdditionalInfo.value = fields.specialOffersDetailsAdditionalInfo
    suggestedPrice.value = fields.suggestedPrice
    suggestedPrice2.value = fields.suggestedPrice2
    suggestedPriceAdditionalInfo.value = fields.suggestedPriceAdditionalInfo

    loaded.value = true
  })

  onServerPrefetch(async () => {
    await fetch()
  })

  onMounted(() => {
    if (!loaded.value) {
      fetch()
    }
  })

  return {
    specialOfferDetails,
    specialOfferDetails2,
    specialOffersDetailsAdditionalInfo,
    suggestedPrice,
    suggestedPrice2,
    suggestedPriceAdditionalInfo
  }
}
