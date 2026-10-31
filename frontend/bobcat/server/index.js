import {createServer} from 'miragejs'
import contentApi from './content.api'
// import magnoliaApi from './magnolia.api'

// this is used only for mocks. If you will remove this, please remove also all in /api (not /api/services!)

export default function mockServer({environment = 'development'} = {}) {
  return createServer({
    environment,
    routes() {
      if (!process.env.MOCK_MAGNOLIA) { this.passthrough(`${process.env.MAGNOLIA_SERVER}/**`) }
      contentApi(this)
      // magnoliaApi(this)
      this.passthrough(`${process.env.BUILDQUOTE_API_SERVER}/**`)
    }
  })
}
