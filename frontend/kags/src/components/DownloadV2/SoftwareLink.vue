<template>
  <overlay-wrapper :show="loading">
    <template v-if="loading">
      <div v-for="software in [1,2]" :key="software" class="software-link p-3 border1 bg-white mb-2">
        <div class="d-flex">
          <div class="me-2 d-flex align-items-center">
            <img :src="softwareIcons['DealerServiceAnalyzer']" width="34" height="34">
          </div>
          <div class="software-link-title font-condensed">
            {{ link.softwareLabel
              .replace('[software]', chosenSoftware)
              .replace('[version]', '~')
              .replace('[release]', '~') }}
          </div>
        </div>
      </div>
    </template>

    <div v-for="software in softwares" :key="software.version" class="software-link cursor-pointer p-3 border1 bg-white mb-2" @click="downloadFile(software.download_link)">
      <div class="d-flex">
        <div class="me-2 d-flex align-items-center">
          <img :src="softwareIcons[chosenSoftware]" width="34" height="34">
        </div>
        <div class="software-link-title font-condensed">
          {{ link.softwareLabel
            .replace('[software]', chosenSoftware)
            .replace('[version]', software.version)
            .replace('[release]', software.releaseDate) }}
        </div>
      </div>
    </div>
  </overlay-wrapper>
</template>

<script>
import {inMgnl} from '@/helpers/magnolia-helper'
import {bdService} from '@/services'

export default {
  name: 'SoftwareLink',
  props: ['link'],
  emits: ['releaseNotes', 'error'],
  data() {
    return {
      softwares: [],
      loading: false,
      inMgnl: inMgnl
    }
  },
  computed: {
    chosenSoftware() {
      if (this.link.softwareCategory) {
        return this.link.softwareCategory
      } else {
        return this.link.chosenSoftware
      }
    },
    softwareIcons() {
      const icons = {}
      icons[this.chosenSoftware] = this.assetServer + '/assets/img/icons/bobcathead.png'
      icons['ServiceAnalyzer'] = this.assetServer + '/assets/img/icons/service-analyzer.svg'
      icons['DealerServiceAnalyzer'] = this.assetServer + '/assets/img/icons/service-analyzer-red.svg'
      const softwareConceptMapping = {
        ServiceAnalyzer: 'nationalServiceAnalyzer',
        DealerServiceAnalyzer: 'serviceAnalyzer',
        [this.chosenSoftware]: this.chosenSoftware.charAt(0).toLowerCase() + this.chosenSoftware.slice(1)
      }
      let softwareConcepts = this.$store.state.concepts.en && this.$store.state.concepts.en['mymachine_Software'] || {}
      if (softwareConcepts) {
        const link = /(https?:\/\/[^" ]*)/i
        for (const software of Object.keys(icons)) {
          const match = Object.keys(softwareConcepts).find(label => label === softwareConceptMapping[software] + 'Icon')
          if (match) {
            const imageSrc = softwareConcepts[match].match(link)
            if (Array.isArray(imageSrc)) icons[software] = imageSrc[0]
          }
        }
      }
      return icons
    }
  },
  created() {
    if (inMgnl) {
      if (this.link.releaseNotesLabel !== '') {
        this.$emit('releaseNotes', {
          label: this.link.releaseNotesLabel
            .replace('[software]', this.chosenSoftware)
            .replace('[version]', '1.01')
            .replace('[release]', this.moment('2022-02-22T22:22:22Z').toDate().toLocaleDateString()),
          url: 'https://bobcat.com'
        })
      }
      this.softwares.push({
        'software_type': this.chosenSoftware,
        'name': this.chosenSoftware + ' 1.01 Installer.exe',
        'brand': 'Bobcat NA',
        'version': '1.01',
        'instructions': 'No change to file extension',
        'id': '01I4V2QH7FO6AFEPJG4ZDYBXGHT2WMW6F6',
        'release_date': '2022-02-22T22:22:22Z',
        'releaseDate': this.moment('2022-02-22T22:22:22Z').toDate().toLocaleDateString(),
        'locale': 'en-US',
        'type': 'software',
        'download_link': 'https://bobcat.com'
      })
    } else {
      this.loading = true
      bdService.fetchSoftware(this.chosenSoftware, 'en-US').then(response => {
        for (const item of response.data) {
          const releaseDate = item.release_date ? this.moment(item.release_date).toDate().toLocaleDateString() : ''
          if (item.type === 'releasenotes') {
            if (this.link.releaseNotesLabel !== '') {
              this.$emit('releaseNotes', {
                label: this.link.releaseNotesLabel
                  .replace('[software]', item.software_type)
                  .replace('[version]', item.version)
                  .replace('[release]', releaseDate),
                url: item.download_link
              })
            }
          } else {
            item.releaseDate = releaseDate
            this.softwares.push(item)
          }
        }
        this.softwares.sort((a, b) => this.compareVersion(a.version, b.version))
      }).catch(() => {
        this.$emit('error')
      }).finally(() => {
        this.loading = false
      })
    }
  },
  methods: {
    compareVersion(v1, v2) {
      if (typeof v1 !== 'string' || typeof v2 !== 'string') return false
      v1 = v1.split('.')
      v2 = v2.split('.')
      const k = Math.min(v1.length, v2.length)
      for (let i = 0; i < k; ++ i) {
        v1[i] = parseInt(v1[i], 10)
        v2[i] = parseInt(v2[i], 10)
        if (v1[i] > v2[i]) return -1
        if (v1[i] < v2[i]) return 1
      }
      return v1.length === v2.length ? 0 : (v1.length < v2.length ? 1 : -1)
    },
    downloadFile(url) {
      const link = document.createElement('a')
      link.href = url
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      link.parentNode.removeChild(link)
    }
  }
}
</script>

<style lang="scss">
@import '../../scss/colors';

.software-link {
  border: 2px solid $gray-400;
  transition: .15s all;

  &:hover {
    border: 2px solid $secondary;
    background-color: $hover-default;
  }

  &-title {
    font-size: 1.125rem;
    margin-top: .2rem;
  }
}
</style>
