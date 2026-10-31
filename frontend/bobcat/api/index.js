const dev = require('./apiUrls.dev')
const prod = require('./apiUrls.prod')
const qa = require('./apiUrls.qa')
const int = require('./apiUrls.int')

let urls = {}

switch (process.env.NODE_ENV) {
case 'qa': urls = qa; break
case 'prod': urls = prod; break
case 'int' : urls = int; break
default: urls = dev; break
}

export default {urls}
