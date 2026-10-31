/* eslint-disable no-undef */
import {useContext} from '@nuxtjs/composition-api'
import {GREY_COLOR_MAP} from '~/constants/googleMapStyles'

const autocompleteFields = [
  'address_components', 'adr_address', 'formatted_address', 'geometry',
  'name', 'type', 'vicinity'
]

export default function useMarketingTags() {
  const {$services} = useContext()

  const getUserLocation = () => {
    return new Promise((resolve, reject) => {
      if (navigator?.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            resolve({
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            })
          },
          error => reject(error)
        )
      } else {
        reject(new Error('Geolocation is not supported by this browser.'))
      }
    })
  }

  const getCoordinates = async (address, googleApiKey, metaData) => {
    const data = await $services.external.getLocationCoordinates(address, googleApiKey)
    const location = data?.results[0]?.geometry?.location
    return {
      latitude: location?.lat || metaData?.location?.lat,
      longitude: location?.lng || metaData?.location?.lng
    }
  }

  const getAddressFromCoordinates = async (latitude, longitude, googleApiKey) => {
    try {
      const data = await $services.external.getAddressFromCoordinates(latitude, longitude, googleApiKey)

      if (data?.status === 'OK' && data.results[0]) {
        return data.results[0].formatted_address
      }
    } catch (error) {
      throw new Error(error)
    }
  }

  const calculateDistance = (coord1, coord2, unit) => {
    const R = unit === 'miles' ? 3958.8 : 6371

    const dLat = (coord2.latitude - coord1.latitude) * (Math.PI / 180)
    const dLon = (coord2.longitude - coord1.longitude) * (Math.PI / 180)

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(coord1.latitude * (Math.PI / 180)) * Math.cos(coord2.latitude * (Math.PI / 180)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2)

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
  }

  const getDistanceToAddress = async (address, googleApiKey) => {
    try {
      const userLocation = await getUserLocation()
      const destinationCoordinates = await getCoordinates(address, googleApiKey)
      const distance = calculateDistance(userLocation, destinationCoordinates)

      return distance.toFixed(1)
    } catch (error) {
      throw new Error(error)
    }
  }

  const convertDistance = (distance, unit) => {
    const conversionFactors = {
      km: 1 / 1000,
      mi: 1 / 1609
    }

    return (distance * conversionFactors[unit]).toFixed(1)
  }

  const loadGoogleMapsScript = async (apiKey) => {
    if (typeof google !== 'undefined') {
      return google
    }

    return new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places`
      script.async = true
      script.defer = true

      script.onload = () => resolve(google)
      script.onerror = () => reject(new Error('Failed to load Google Maps script'))

      document.head.appendChild(script)
    })
  }

  const initializeMap = (mapRef, center, zoom = 16) => {
    return new google.maps.Map(mapRef.value, {
      center,
      zoom,
      styles: GREY_COLOR_MAP
    })
  }

  const addCustomMarker = (map, location, iconUrl) => {
    return new google.maps.Marker({
      position: location,
      map,
      icon: iconUrl
    })
  }

  const initMapWithCustomMarker = async (mapRef, address, googleApiKey, iconUrl, latitude, longitude) => {
    if (!mapRef.value) {
      throw new Error('Map reference is not defined.')
    }

    try {
      await loadGoogleMapsScript(googleApiKey)

      const map = initializeMap(mapRef, {lat: 0, lng: 0})

      if (latitude != null && longitude != null) {
        const location = {
          lat: Number(latitude),
          lng: Number(longitude)
        }

        map.setCenter(location)
        addCustomMarker(map, location, iconUrl)
        return
      }
      const geocoder = new google.maps.Geocoder()

      geocoder.geocode({address}, (results, status) => {
        if (status === 'OK' && results[0]) {
          const location = results[0].geometry.location
          map.setCenter(location)
          addCustomMarker(map, location, iconUrl)
        } else {
          throw new Error(`Geocode failed due to: ${status}`)
        }
      })
    } catch (error) {
      throw new Error(error)
    }
  }

  const loadGoogleMapApiScript = (apiKey) => {
    if (window?.google?.maps) {
      return Promise.resolve(true)
    }

    return new Promise((resolve, reject) => {
      const existingScript = document.querySelector('script[src*="maps.googleapis.com"]')
      if (existingScript) {
        existingScript.addEventListener('load', () => resolve(true))
        existingScript.addEventListener('error', () => reject(new Error('Failed to load Google Maps API')))
        return
      }

      const scriptElement = document.createElement('script')
      scriptElement.async = true
      scriptElement.defer = true
      scriptElement.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&callback=gmapLoaded`

      window.gmapLoaded = () => resolve(true)

      scriptElement.addEventListener('error', () => reject(new Error('Failed to load Google Maps API')))
      document.body.appendChild(scriptElement)
    })
  }

  const getCountryCodeFromGoogle = (countryName) => {
    return new Promise((resolve, reject) => {
      if (!window.google || !window.google.maps) {
        return reject(new Error('Google Maps not available'))
      }

      const geocoder = new window.google.maps.Geocoder()

      geocoder.geocode({address: countryName}, (results, status) => {
        if (status !== 'OK' || !results || !results.length) {
          return resolve(null)
        }

        const component = results[0].address_components.find(c => c.types.includes('country'))
        resolve(component?.short_name || null)
      })
    })
  }

  return {
    getUserLocation,
    getDistanceToAddress,
    initMapWithCustomMarker,
    getAddressFromCoordinates,
    getCoordinates,
    convertDistance,
    loadGoogleMapApiScript,
    getCountryCodeFromGoogle,
    autocompleteFields
  }
}
