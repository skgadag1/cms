import type {TTLCache} from './TTLCache'

const STORAGE_KEY_GEOCODING_CACHE = 'geocodingCache'

export async function setUpPersistence(cache: TTLCache): Promise<void> {
  // initialize cache from storage
  const storedData = localStorage.getItem(STORAGE_KEY_GEOCODING_CACHE)

  if (storedData) {
    cache.setSerializedData(storedData)
  }

  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
      // save cache
      if (cache.isModified()) {
        const data = cache.getSerializedData()

        localStorage.setItem(
          STORAGE_KEY_GEOCODING_CACHE,
          data
        )

        cache.resetModified()
      }
    }
  })
}
