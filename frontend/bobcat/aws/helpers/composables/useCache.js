const LRU = require('fast-lru')

const _defaultOptions = {
  maximumSize: 50
}

const _cache = new LRU(_defaultOptions)

const useCache = () => {
  const cacheLRU = _cache

  return {
    cacheLRU
  }
}

module.exports = useCache
