export default function useInventorySorting() {
  const sortByDistance = (a, b, defaultValue = Number.MAX_SAFE_INTEGER) => parseFloat(a.dealer?.distance || defaultValue) - parseFloat(b.dealer?.distance || defaultValue)
  const sortByName = (a, b) => (a.label).localeCompare(b.label)
  const sortByPrice = (a, b, defaultValue = Number.MAX_SAFE_INTEGER, priceSelector = 0) => parseFloat(a.pricing?.estimatedNetPrices?.[priceSelector]?.value || defaultValue) - parseFloat(b.pricing?.estimatedNetPrices?.[priceSelector]?.value || defaultValue)
  const sortByAPR = (a, b) => (b.dealer?.recommended ? 1 : 0) - (a.dealer?.recommended ? 1 : 0)

  const sortProducts = (_products = [], sortingObject = null) => {
    /*
 NGB-3904
 possible options to sort:
  - distance asc/desc
  - price high/low
  - model name asc/desc
 chain priority: distance->price->name, price->distance->name, name->distance->price
 AV-1101
 possible options to sort:
  - distance asc/desc
  - price high/low
  - model name asc/desc
  - recommended
 chain priority: recommended->distance->price->name, recommended->price->distance->name, recommended->name->distance->price
*/
    const _firstProductWithPricing = _products?.find(prod => prod?.pricing?.estimatedNetPrices?.length)
    const _priceSelector = _firstProductWithPricing?.dealer?.shippingAddress?.country === 'US'
      ? _firstProductWithPricing?.pricing?.estimatedNetPrices?.findIndex(x => x.currency === 'USD')
      : _firstProductWithPricing?.pricing?.estimatedNetPrices?.findIndex(x => x.currency === 'CAD') || 0

    switch (sortingObject) {
    // 'recommended' is default
    case 'distance:asc':
      _products.sort((a, b) => {
        // distance first
        if (parseFloat(a.dealer?.distance || 0) === parseFloat(b.dealer?.distance || 0)) {
          // then price
          if (parseFloat(a.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER) === parseFloat(b.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER)) {
            // then name
            return sortByName(a, b)
          }
          return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
        }
        return sortByDistance(a, b)
      })
      break
      // 'distance:asc' is default
    case 'distance:desc':
      _products.sort((a, b) => {
        // distance first
        if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
          // then price
          if (parseFloat(a.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER) === parseFloat(b.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER)) {
            // then name
            return sortByName(a, b)
          }
          return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
        }
        return sortByDistance(b, a)
      })
      break
    case 'price:asc':
      _products.sort((a, b) => {
        // price first
        if (parseFloat(a.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER) === parseFloat(b.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER)) {
          // then distance
          if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
            // then name
            return sortByName(a, b)
          }
          return sortByDistance(a, b)
        }
        return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
      })
      break
    case 'price:desc':
      _products.sort((a, b) => {
        // price first
        if (parseFloat(a.pricing?.estimatedNetPrices?.[_priceSelector]?.value || 0) === parseFloat(b.pricing?.estimatedNetPrices?.[_priceSelector]?.value || 0)) {
          // then distance
          if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
            // then name
            return sortByName(a, b)
          }
          return sortByDistance(a, b)
        }
        return sortByPrice(b, a, 0, _priceSelector)
      })
      break
    case 'name:asc':
      _products.sort((a, b) => {
        // name first
        if (a.label === b.label) {
          // then distance
          if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
            // then price
            return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
          }
          return sortByDistance(a, b)
        }
        return sortByName(a, b)
      })
      break
    case 'name:desc':
      _products.sort((a, b) => {
        // name first
        if (a.label === b.label) {
          // then distance
          if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
            // then price
            return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
          }
          return sortByDistance(a, b)
        }
        return sortByName(b, a)
      })
      break
    default:
      // recommended
      _products.sort((a, b) => {
        // recommended first
        if (b.dealer?.recommended === a.dealer?.recommended) {
          // then distance
          if (parseFloat(b.dealer?.distance || 0) === parseFloat(a.dealer?.distance || 0)) {
            // then price
            if (parseFloat(a.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER) === parseFloat(b.pricing?.estimatedNetPrices?.[_priceSelector]?.value || Number.MAX_SAFE_INTEGER)) {
              // then name
              return sortByName(a, b)
            }
            return sortByPrice(a, b, Number.MAX_SAFE_INTEGER, _priceSelector)
          }
          return sortByDistance(a, b)
        }
        return sortByAPR(a, b)
      })
      break
    }
  }

  return {
    sortProducts
  }
}
