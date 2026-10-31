const LRU = require('fast-lru')

const _defaultOptions = {
  maximumSize: process.env.IS_DEV ? 2 : 50
}

const _cache = new LRU(_defaultOptions)

const useCache = () => {
  const cacheLRU = _cache
  cacheLRU.del = () => {} // hack for not throwing cache errors locally

  return {
    cacheLRU
  }
}

module.exports = useCache
