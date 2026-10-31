// it's pure vanilla JS to handle custom logic
import axios from 'axios'

const bodyEl = document.getElementsByTagName('body')[0]
const apiToken = bodyEl.dataset.apiToken ? bodyEl.dataset.apiToken : false
// const awsTraceId = bodyEl.dataset.traceId ? bodyEl.dataset.traceId : false

const _config = {headers: {}}
if (apiToken) _config.headers['Authorization'] = 'Bearer ' + apiToken
// if (awsTraceId) _config.headers['x-amzn-trace-id'] = awsTraceId

const _http = axios.create(_config)

// handler for custom external links
document.addEventListener('DOMContentLoaded', () => {
  document.body.addEventListener('click', e => {
    if (e.target.hasAttribute('data-download-link')) {
      _http.get(e.target.dataset.downloadLink).then(r => {
        if (r.data.link) window.open(r.data.link)
      })
      e.preventDefault()
    }
  })
})
