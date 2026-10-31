/* eslint-disable class-methods-use-this */
import {TTLCache} from './TTLCache'
import {setUpPersistence} from './persistence.helper'

const TTL_30_DAYS_MS = 86400e3 * 30 // 30 days in ms

type StreetData = {name: string; number?: string}
type DistanceMethod = (lat1: number, lng1: number, lat2: number, lng2: number) => number
type GeocoderResult = {
  types: string[]
  address_components: Array<{types: string[]; long_name: string; short_name: string}>
}
type MapsApi = {
  GeocoderStatus: {OK: string}
  Geocoder: new() => {geocode: (request: {location: unknown}, callback: (results: GeocoderResult[] | null, status: string) => void) => void}
  LatLng: new(latitude: number, longitude: number) => unknown
}
type GeocodingOptions = {
  cacheActive?: boolean
  cacheCapacity?: number
  distanceMethod?: 'Euclidean' | 'Haversine'
  maxDistanceThreshold?: number
  cachePersistence?: boolean
}

export class GeocodingService {
  cacheActive: boolean

  maxDistanceThreshold: number // in meters

  distanceMethod: 'Euclidean' | 'Haversine'

  cacheCapacity: number

  distanceMethodReference: DistanceMethod

  cache: TTLCache

  /**
   * Creates a new instance of the `GeocodingService` class.
   * @constructor
   * @param {GeocodingOptions} options - Options object for Geocoding class
   * @param {boolean} [options.cacheActive=true] - Flag to activate or deactivate caching
   * @param {number} [options.cacheCapacity=2000] - Maximum number of entries in cache
   * @param {string} [options.distanceMethod="Euclidean"] - Method used to calculate distances
   * @param {number} [options.maxDistanceThreshold=10] - Maximum distance threshold for search results
   */
  constructor(options: GeocodingOptions | null = null) {
    this.cacheActive = options ? options.cacheActive : true
    this.cacheCapacity = options ? options.cacheCapacity : 2000
    this.distanceMethod = options ? options.distanceMethod : 'Euclidean'
    this.maxDistanceThreshold = options ? options.maxDistanceThreshold : 10

    this.distanceMethodReference = this.distanceMethod === 'Euclidean'
      ? this.calculateEuclideanDistance
      : this.calculateHaversineDistance

    this.cache = new TTLCache(this.cacheCapacity, TTL_30_DAYS_MS)

    const hasCachePersistence = options && options.cachePersistence
    if (this.cacheActive && (hasCachePersistence)) {
      setUpPersistence(this.cache)
    }
  }

  /**
   * Concatenates the name and number properties of a StreetData object.
   * @param {StreetData} data - The StreetData object to concatenate.
   * @returns {string} A string containing the concatenated name and number properties, separated by a space.
   */
  static ConcatStreetData(data: StreetData): string {
    const address = []

    if (data.number) {
      address.push(data.number)
    }

    address.push(data.name)

    return address.join(' ')
  }

  /**
   * Returns the nearest street as a string for the given latitude and longitude coordinates.
   * @param {number} latitude - The latitude coordinate of the location.
   * @param {number} longitude - The longitude coordinate of the location.
   * @returns {Promise<string>} A promise that resolves to a string containing the nearest street,
   * or undefined if no street was found.
   */
  async getNearestStreetAsString(
    latitude: number,
    longitude: number
  ): Promise<string | undefined> {
    const streetData = await this.getNearestStreet(latitude, longitude)

    if (streetData) {
      return GeocodingService.ConcatStreetData(streetData)
    }

    return undefined
  }

  /**
   * Gets the nearest street address to the given latitude and longitude.
   * @param latitude The latitude of the location to search.
   * @param longitude The longitude of the location to search.
   * @returns A promise that resolves to the nearest street address to the given location or undefined
   * if an street name is not available.
   */
  async getNearestStreet(
    latitude: number,
    longitude: number
  ): Promise<StreetData | null | undefined> {
    // Check if there is a result in the cache that is within 'maxDistanceThreshold' meters of distance
    if (this.cacheActive) {
      const cachedResult = this.getClosestAddressFromCache(latitude, longitude)
      if (cachedResult !== undefined) {
        return cachedResult
      }
    }

    const maps = (window as unknown as {google: {maps: MapsApi}}).google.maps
    const geocoder = new maps.Geocoder()
    const geolocation = new maps.LatLng(latitude, longitude)

    return new Promise<StreetData | undefined>((resolve, reject) => {
      geocoder.geocode({location: geolocation}, (results, status) => {
        if (status === maps.GeocoderStatus.OK && results) {
          let route: string | undefined
          let streetNumber: string | undefined

          const record = results
            .find(r => r.types.some(t => ['street_address', 'route', 'postal_code'].includes(t)))

          if (record) {
            record.address_components.forEach((ac) => {
              if (ac.types.includes('route')) {
                route = ac.long_name
              } else if (ac.types.includes('street_number')) {
                streetNumber = ac.short_name
              }
            })
          }

          const nearestStreet = route
            ? {name: route, number: streetNumber}
            : undefined

          // if the request was ok and the goglemaps response is undefined, we register this
          // as null in the cache, to not repeat the goglemaps request again.
          this.cache.set(`${latitude},${longitude}`, nearestStreet || null)

          resolve(nearestStreet)
        } else {
          reject(status)
        }
      })
    })
  }

  keyComparator = (key1, key2) => {
    const [lat1, lng1] = key1.split(',')
    const [lat2, lng2] = key2.split(',')
    const distance = this.distanceMethodReference(
      Number(lat1),
      Number(lng1),
      Number(lat2),
      Number(lng2)
    )

    return distance <= this.maxDistanceThreshold
  }

  /**
   * Gets the closest address to the given coordinates from the cache,
   * if it is within the specified distance threshold.
   * @param lat The latitude of the coordinates.
   * @param lng The longitude of the coordinates.
   * @param threshold The maximum distance threshold in meters.
   * @returns The closest address value from the cache, or undefined if
   * no address is found within the specified distance threshold.
   */
  getClosestAddressFromCache(
    lat: number,
    lng: number
  ): StreetData | null | undefined {
    const cacheKey = `${lat},${lng}`

    let cachedResult = this.cache.get(cacheKey)
    if (cachedResult !== undefined) {
      return cachedResult
    }

    cachedResult = this.cache.get(cacheKey, this.keyComparator)
    if (cachedResult !== undefined) {
      return cachedResult
    }

    return undefined
  }

  /**
   * Calculates the Haversine distance between two points specified
   * by their latitude and longitude coordinates.
   * @param lat1 The latitude of the first point.
   * @param lng1 The longitude of the first point.
   * @param lat2 The latitude of the second point.
   * @param lng2 The longitude of the second point.
   * @returns The distance between the two points in meters.
   */
  calculateHaversineDistance(
    lat1: number,
    lng1: number,
    lat2: number,
    lng2: number
  ): number {
    const earthRadius = 6371e3 // metres
    const phi1 = (lat1 * Math.PI) / 180
    const phi2 = (lat2 * Math.PI) / 180
    const deltaPhi = ((lat2 - lat1) * Math.PI) / 180
    const deltaLambda = ((lng2 - lng1) * Math.PI) / 180

    const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
      + Math.cos(phi1)
        * Math.cos(phi2)
        * Math.sin(deltaLambda / 2)
        * Math.sin(deltaLambda / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
  }

  /**
   * Calculates the Euclidean distance between two points specified
   * by their latitude and longitude coordinates.
   * @param lat1 The latitude of the first point.
   * @param lng1 The longitude of the first point.
   * @param lat2 The latitude of the second point.
   * @param lng2 The longitude of the second point.
   * @returns The distance between the two points in meters.
   */
  calculateEuclideanDistance(
    lat1: number,
    lng1: number,
    lat2: number,
    lng2: number
  ): number {
    const earthCircumference = 40075000 // in meters
    const x = (lng2 - lng1) * Math.cos((lat1 + lat2) / 2)
    const y = lat2 - lat1
    return (Math.sqrt(x * x + y * y) * earthCircumference) / 360
  }
}
