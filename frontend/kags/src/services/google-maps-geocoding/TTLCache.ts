/* eslint-disable no-restricted-syntax */

type CacheItem = {value: any; timestamp: number}

export class TTLCache {
  capacity;

  maxAge;

  cache: Record<string, CacheItem> = {};

  #isModified;

  constructor(capacity: number, maxAge: number) {
    this.capacity = capacity
    this.maxAge = maxAge
    this.#isModified = false

    setInterval(() => {
      this.removeExpiredCacheItems()
    }, 12 * 60 * 60 * 1000) // 12 hours in milliseconds
  }

  get size() {
    return Object.keys(this.cache).length
  }

  get(
    key,
    keyComparator: ((key: string, cachedKey: string) => boolean) | null = null
  ) {
    let cacheKey
    let cacheItem

    if (!keyComparator) {
      cacheKey = key
      cacheItem = this.cache[key]
    } else {
      for (const cachedKey in this.cache) {
        if (keyComparator(key, cachedKey)) {
          cacheKey = key
          cacheItem = this.cache[key]
          break
        }
      }
    }

    if (!cacheItem) {
      return undefined
    }
    if (this.isExpired(cacheItem) && cacheKey) {
      this.removeCacheItem(cacheKey)
      return undefined
    }

    return cacheItem.value
  }

  set(key: string, value: any): void {
    const cacheItem = this.cache[key]
    if (cacheItem) {
      cacheItem.value = value
      cacheItem.timestamp = Date.now()
    } else {
      // Add a new cache item
      this.cache[key] = {value, timestamp: Date.now()}
      this.#isModified = true
      // Remove the oldest cache item if the cache has exceeded its maximum size
      if (Object.keys(this.cache).length > this.capacity) {
        this.removeOldestCacheItem()
      }
    }
  }

  clear() {
    this.cache = {}
    this.#isModified = true
  }

  isExpired(cacheItem: CacheItem): boolean {
    return Date.now() - cacheItem.timestamp > this.maxAge
  }

  removeCacheItem(key: string): void {
    delete this.cache[key]
    this.#isModified = true
  }

  removeOldestCacheItem() {
    let oldestCacheItem
    let oldestKey

    for (const key in this.cache) {
      if (Object.hasOwn(this.cache, key)) {
        const cacheItem = this.cache[key]
        if (!oldestCacheItem || cacheItem.timestamp < oldestCacheItem.timestamp) {
          oldestKey = key
          oldestCacheItem = cacheItem
        }
      }
    }
    if (oldestKey) {
      this.removeCacheItem(oldestKey)
    }
  }

  removeExpiredCacheItems() {
    const now = Date.now()
    for (const key in this.cache) {
      if (Object.hasOwn(this.cache, key)) {
        const cacheItem = this.cache[key]
        if (now - cacheItem.timestamp > this.maxAge) {
          this.removeCacheItem(key)
        }
      }
    }
  }

  isModified() {
    return this.#isModified
  }

  resetModified() {
    this.#isModified = false
  }

  getSerializedData() {
    return JSON.stringify(this.cache)
  }

  setSerializedData(data: string): void {
    this.cache = JSON.parse(data)
    this.#isModified = false

    setTimeout(() => {
      this.removeExpiredCacheItems()
    }, 0)
  }
}
