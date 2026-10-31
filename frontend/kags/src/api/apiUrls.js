import prod from './apiUrls.prod'
import dev from './apiUrls.dev'

const urls = import.meta.env.VUE_APP_MODE === 'preview' ? Object.assign({}, prod, dev) : prod

export default urls
