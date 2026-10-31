<template>
  <div class="position-relative">
    <div v-if="disabled" class="mask disabled zindex-3 rounded" />
    <GoogleMap v-show="!loading"
               ref="gmap"
               class="gmap"
               :api-key="apiKey"
               :center="center"
               :zoom="zoom"
               v-bind="goolgeMapOptions"
               @zoom_changed="handleZoomChange"
    >
      <template #default="{ ready }">
        <template v-if="!disabled && ready">
          <InfoWindow v-if="infoWinOpen && currentItem"
                      v-model="infoWinOpen"
                      :options="infoOptions"
                      @update:model-value="onInfoWindowUpdate"
          >
            <div class="info-window-content">
              <div class="item__dateTime">
                <h2 class="m-0">{{ translate('dateTimeLabel') }}</h2>
                <span>{{ formatDate(currentItem.startTs) }}</span><br>
                <span class="text-small">{{ formatTime(currentItem.startTs) }}</span>
              </div>
              <div class="mt-2">
                <h2 class="m-0">{{ translate('locationLabel') }}</h2>
                <b-spinner v-if="currentItem.reversedGeolocation === true" variant="black" small class="me-2" />
                <div v-else>
                  {{ currentItem.reversedGeolocation }}
                </div>
                <div class="text-small">
                  {{ currentItem.municipality }}
                  {{ currentItem.subRegion }}
                  {{ currentItem.region }}
                  {{ currentItem.country }}
                </div>
              </div>
              <div class="mt-2 text-small">{{ currentItem.latitude }} {{ currentItem.longitude }}</div>
            </div>
          </InfoWindow>

          <GMapMarker v-if="infoWinOpen && currentItem"
                      :options="{
                        position: infoOptions.position,
                        icon: itemNow.startTs === currentItem.startTs ? nowIconClicked : locationIconClicked,
                        zIndex: 9999
                      }"
          />

          <GMapMarker v-for="(item, index) in items"
                      :key="'marker_' + index"
                      :options="{
                        position: {lat: item.latitude, lng: item.longitude},
                        clickable: true,
                        icon: highlighted === item.startTs ? locationIconHighlighted : locationIcon,
                        zIndex: index
                      }"
                      @click="toggleInfoWindow(item)"
                      @mouseover="$emit('hover', item.startTs, true)"
                      @mouseout="$emit('hover', item.startTs, false)"
          />

          <GMapMarker v-if="itemNow && itemNow.latitude && itemNow.longitude"
                      :options="{
                        position: {lat: itemNow.latitude, lng: itemNow.longitude},
                        clickable: true,
                        icon: nowHighlighted ? nowIconHighlighted : nowIcon,
                        zIndex: 99999
                      }"
                      @click="toggleInfoWindow(itemNow)"
                      @mouseover="() => {$emit('hover', itemNow.startTs, true); nowHighlighted = true;}"
                      @mouseout="() => {$emit('hover', itemNow.startTs, false); nowHighlighted = false;}"
          />

          <GMapPolyline v-for="(arrow, i) in arrows"
                        :key="'arrow_' + i"
                        :options="{ path: arrow.path, ...arrow.options }"
          />
        </template>
      </template>
    </GoogleMap>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import {markRaw} from 'vue'
import {GoogleMap, Marker, InfoWindow, Polyline} from 'vue3-google-map'
import {geocodingService} from '@/services/google-maps-geocoding'

export default {
  conceptName: ['MachineDetails'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    GoogleMap,
    GMapMarker: Marker,
    InfoWindow,
    GMapPolyline: Polyline
  },
  props: {
    assetServer: {
      required: true
    },
    apiKey: {
      type: String,
      required: true
    },
    disabled: {
      type: Boolean,
      required: true
    },
    highlighted: {
      required: true
    },
    arrowDir: {
      type: String,
      default: '-',
      validator(value) {
        return ['-', '+'].indexOf(value) !== -1
      },
      required: false
    },
    items: {
      type: Array,
      required: true
    },
    itemNow: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      locationIcon: {
        path: 'M5 0C5 2.76 2.76 5 0 5C-2.76 5 -5 2.76 -5 0C-5 -2.76 -2.76 -5 0 -5C2.76 -5 5 -2.76 5 0Z',
        fillColor: '#333',
        fillOpacity: 1,
        strokeOpacity: 0
      },
      locationIconHighlighted: {
        path: 'M5 0C5 2.76 2.76 5 0 5C-2.76 5 -5 2.76 -5 0C-5 -2.76 -2.76 -5 0 -5C2.76 -5 5 -2.76 5 0Z',
        fillColor: '#333',
        fillOpacity: 1,
        strokeOpacity: 1,
        strokeWeight: 2,
        strokeColor: '#FFF'
      },
      locationIconClicked: {
        url: this.assetServer + '/assets/img/map/marker-black.svg',
        anchor: {x: 10, y: 35}
      },
      nowIcon: {
        path: 'M5 0C5 2.76 2.76 5 0 5C-2.76 5 -5 2.76 -5 0C-5 -2.76 -2.76 -5 0 -5C2.76 -5 5 -2.76 5 0Z',
        fillColor: '#ff3600',
        fillOpacity: 1,
        strokeOpacity: 0
      },
      nowIconHighlighted: {
        url: this.assetServer + '/assets/img/map/now-icon.svg',
        anchor: {x: 15, y: 15},
        scaledSize: {width: 29, height: 29}
      },
      nowIconClicked: {
        url: this.assetServer + '/assets/img/map/marker-red.svg',
        anchor: {x: 14, y: 50},
        scaledSize: {width: 29, height: 59}
      },
      nowHighlighted: false,
      isNowClicked: false,
      arrowOptions: {
        clickable: false,
        icons: [{
          icon: {
            path: 1,
            fillColor: '#333',
            fillOpacity: 1
          },
          offset: '100%'
        }],
        strokeColor: '#333',
        strokeOpacity: 1
      },
      arrows: [],
      google: null,
      center: {lat: 38, lng: -99},
      zoom: 4,
      loading: true,
      infoWinOpen: false,
      infoOptions: {
        position: null,
        pixelOffset: {
          width: 140,
          height: 100
        }
      },
      currentItem: null,
      currentItemZIndex: 9999
    }
  },
  computed: {
    goolgeMapOptions() {
      return {
        streetViewControl: false,
        mapTypeControl: true,
        fullscreenControl: false,
        disableDoubleClickZoom: true,
        maxZoom: 22,
        // mapTypeControlOptions is passed to Google Maps as-is, so it needs the ControlPosition enum
        // (available once the API is loaded); GoogleMap re-applies options when this recomputes.
        mapTypeControlOptions: {
          position: this.google ? this.google.maps.ControlPosition.LEFT_TOP : undefined,
          mapTypeIds: ['roadmap', 'satellite']
        },
        // vue3-google-map converts these position names to the enum itself.
        zoomControlPosition: 'LEFT_BOTTOM',
        rotateControlPosition: 'LEFT_CENTER',
        styles: [
          {
            featureType: 'poi.business',
            stylers: [{visibility: 'off'}]
          },
          {
            featureType: 'transit',
            elementType: 'labels.icon',
            stylers: [{visibility: 'off'}]
          }
        ]
      }
    }
  },
  watch: {
    items(newItems) {
      this.fitToItems(newItems)
      this.generateArrows(newItems)
    },
    itemNow: {
      deep: true,
      handler(newItemNow) {
        if (newItemNow && newItemNow.latitude && newItemNow.longitude) {
          this.fitToItems(this.items)
        }
      }
    }
  },
  async mounted() {
    // vue3-google-map has no $mapPromise (vue2-google-maps API); it flips `ready` once the
    // Maps script is loaded and the map instance exists. Until then window.google.maps is only
    // the loader's importLibrary stub, so constructors like LatLngBounds are undefined.
    const gmap = this.$refs.gmap
    if (!gmap.ready) {
      await new Promise(resolve => {
        const stop = this.$watch(() => gmap.ready, ready => {
          if (ready) {
            stop()
            resolve()
          }
        })
      })
    }
    await window.google.maps.importLibrary('core')
    this.loading = false
    this.google = markRaw(window.google)
    this.fitToItems(this.items)
  },
  methods: {
    handleZoomChange(z) {
      if (this.zoom != z) {
        this.zoom = z
        this.generateArrows(this.items)
      }
    },
    closeInfoWindow() {
      this.infoWinOpen = false
      this.$emit('clicked', null)
    },
    onInfoWindowUpdate(val) {
      if (!val) this.closeInfoWindow()
    },
    async toggleInfoWindow(item) {
      this.infoOptions.position = {lat: item.latitude, lng: item.longitude}
      this.currentItem = item
      this.infoWinOpen = true
      this.$emit('clicked', item.startTs)
      if (!item.reversedGeolocation) {
        item.reversedGeolocation = true
        try {
          item.reversedGeolocation = await geocodingService.getNearestStreetAsString(item.latitude, item.longitude)
        } catch (e) {
          // no street name (e.g. geocoding denied): stop the spinner, retry on the next click
          item.reversedGeolocation = ''
        }
      }
    },
    async generateArrows(items) {
      if (!this.google) return
      if (!items || !items.length) {
        this.arrows = []
        return
      }

      const arrows = []
      const map = this.$refs.gmap.map
      const offset = 10 // offset of the arrow in px

      let startLatLng = false
      let endLatLng = false
      for (let item of items) {
        if (item.latitude && item.longitude) {
          if (!startLatLng) {
            startLatLng = {lat: item.latitude, lng: item.longitude} //first point
          } else {
            endLatLng = {lat: item.latitude, lng: item.longitude}
            const startPoint = this.fromLatLngToPixel(new this.google.maps.LatLng(startLatLng.lat, startLatLng.lng), map)
            const endPoint = this.fromLatLngToPixel(new this.google.maps.LatLng(endLatLng.lat, endLatLng.lng), map)
            if (startPoint && endPoint) {
              const length = Math.round(Math.sqrt(Math.pow(Math.abs(startPoint.x - endPoint.x), 2) + Math.pow(Math.abs(startPoint.y - endPoint.y), 2)))

              if (length > 8 + (offset * 2)) { //line is at least 8 pixel long not including offset
                const ratio = offset / length
                const offsetStartPoint = {
                  x: Math.round((1 - ratio) * startPoint.x + ratio * endPoint.x),
                  y: Math.round((1 - ratio) * startPoint.y + ratio * endPoint.y)}
                const offsetEndPoint = {
                  x: Math.round((1 - ratio) * endPoint.x + ratio * startPoint.x),
                  y: Math.round((1 - ratio) * endPoint.y + ratio * startPoint.y)}

                arrows.push({
                  path: this.arrowDir === '-' ? [this.fromPixelToLatLng(offsetEndPoint, map), this.fromPixelToLatLng(offsetStartPoint, map)] : [this.fromPixelToLatLng(offsetStartPoint, map), this.fromPixelToLatLng(offsetEndPoint, map)],
                  options: this.arrowOptions
                })
              }
            }
            startLatLng = endLatLng
          }
        }
      }
      this.arrows = arrows
    },
    fromLatLngToPixel(position, map) {
      const scale = Math.pow(2, map.getZoom())
      const proj = map.getProjection()
      const bounds = map.getBounds()

      if (!proj) return

      const nw = proj.fromLatLngToPoint(
        new this.google.maps.LatLng(
          bounds.getNorthEast().lat(),
          bounds.getSouthWest().lng()
        ))
      const point = proj.fromLatLngToPoint(position)

      return {x: Math.floor((point.x - nw.x) * scale), y: Math.floor((point.y - nw.y) * scale)}
    },
    fromPixelToLatLng(pixel, map) {
      const scale = Math.pow(2, map.getZoom())
      const proj = map.getProjection()
      const bounds = map.getBounds()

      if (!proj) return

      const nw = proj.fromLatLngToPoint(
        new this.google.maps.LatLng(
          bounds.getNorthEast().lat(),
          bounds.getSouthWest().lng()
        ))

      return proj.fromPointToLatLng({x: pixel.x / scale + nw.x, y: pixel.y / scale + nw.y})
    },
    fitToItems: async function (items) {
      if (!this.google) return

      const map = this.$refs.gmap.map
      const bounds = new this.google.maps.LatLngBounds()
      for (let item of items) {
        item.latitude && item.longitude && bounds.extend(new this.google.maps.LatLng(item.latitude, item.longitude))
      }
      if (this.itemNow && this.itemNow.latitude && this.itemNow.longitude) {
        bounds.extend(new this.google.maps.LatLng(this.itemNow.latitude, this.itemNow.longitude))
      }
      if (bounds.isEmpty()) return
      map.fitBounds(bounds)
      if (this.zoom > 16) this.zoom = 16
    }
  }
}
</script>

