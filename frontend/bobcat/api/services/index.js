import buildQuoteService from './buildandquote.service'
import bobcatapiService from './bobcatapi.service'
import contentService from './content.service'
import productsService from './products.service'
import dealerInventoryService from './dealerinventory.service'
import dealerlocatorService from './dealerlocator.service'
import externalService from './external.service'

export default ($axios, $regions, $utils, $config, redirect) => ({
  buildandquote: buildQuoteService($axios, $config),
  bobcatapi: bobcatapiService($axios, $regions, $config, redirect),
  content: contentService($axios, $regions, $config),
  products: productsService($axios, $regions, $utils, $config),
  dealerinventory: dealerInventoryService($axios, $regions, $config),
  dealerlocator: dealerlocatorService($axios, $regions, $config),
  external: externalService($axios)
})
