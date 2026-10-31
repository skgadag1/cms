<template>
  <div v-if="userDataLoaded && currentInternalNotification" class="notification-bar-component bg-white w-100">
    <div class="notification-container d-flex flex-column align-items-start mx-auto position-relative p-3">
      <h4 class="notification-title m-0 fw-bold text-start">{{ currentInternalNotification.localizedContent && currentInternalNotification.localizedContent.length > 0 ? currentInternalNotification.localizedContent[0].title : '' }}</h4>
      <div class="notification-content text-start w-100 mt-3" v-html="currentInternalNotification.localizedContent && currentInternalNotification.localizedContent.length > 0 ? currentInternalNotification.localizedContent[0].content : ''" />
      <b-button
        ref="closeButton"
        class="btn close-button bg-transparent border-0 align-self-end"
        @click="closeNotification"
      >
        <b-icon :icon="'plus-circle'" class="close-button-icon" font-scale="1" variant="warning" />
        {{ translatedConfig('closeButton') }}
      </b-button>
    </div>
  </div>
</template>
<script>
import { useLabels } from '@/composables/useLabels'

import {notificationsService} from '@/services/notifications.service'
import {mapGetters} from 'vuex'

export default {
  name: 'NotificationBar',
  conceptName: 'NotificationBar',
  setup() { return useLabels() },
  data() {
    return {
      componentConfig: this.config,
      userDataLoaded: false,
      selectedLanguage: 'en',
      currentInternalNotification: null,
      seenNotificationIds: [],
      rawAnnouncements: [],
      announcements: [],
      loading: false,
      error: null
    }
  },
  computed: {
    ...mapGetters({
      currentUser: 'getCurrentUser',
      currentUserKey: 'getCurrentUserKey',
      isUserInternal: 'getCurrentUserInternal'
    }),

    notificationAppIds() {
      const sharedAppId = import.meta.env.VUE_APP_NOTIFICATIONS_APP_ID_SHARED
      if (this.isUserInternal) {
        const internalAppId = import.meta.env.VUE_APP_NOTIFICATIONS_APP_ID_INTERNAL
        return [internalAppId, sharedAppId].filter(Boolean)
      }
      const externalAppId = import.meta.env.VUE_APP_NOTIFICATIONS_APP_ID_EXTERNAL
      return [externalAppId, sharedAppId].filter(Boolean)
    },
    currentLanguage() {
      if (this.currentUser && this.currentUser.language) {
        return this.currentUser.language.split('-')[0]
      }
      return this.selectedLanguage
    },
    currentRegion() {
      if (this.currentUser && this.currentUser.countryCode) {
        return this.currentUser.countryCode
      }
      return 'US'
    },
    localStorageKey() {
      const userId = this.currentUserKey || 'defaultUser'
      return `seenNotifications_kags_${userId}`
    },
    unreadNotifications() {
      if (!this.announcements || this.announcements.length === 0) {
        return []
      }
      return this.announcements.filter(
        (announcement) => !this.seenNotificationIds.includes(announcement.id)
      )
    }
  },
  watch: {
    unreadNotifications: {
      handler(newUnread) {
        if (newUnread.length > 0) {
          this.currentInternalNotification = newUnread[0]
        } else {
          this.currentInternalNotification = null
        }
      },
      immediate: true,
      deep: true
    },

    currentUser() {
      this.loadSeenNotifications()
    },
    isUserInternal() {
      this.fetchAnnouncements()
    }
  },
  mounted() {
    this.fetchAnnouncements()
    this.loadSeenNotifications()
  },
  methods: {
    prepareAnnouncements() {
      this.announcements = this.rawAnnouncements.map(item => ({
        id: item.id,
        localizedContent: item.contents && item.contents[this.selectedLanguage] ? [
          {
            language: [this.selectedLanguage],
            title: item.contents[this.selectedLanguage].title,
            content: item.contents[this.selectedLanguage].content
          }
        ] : []
      }))
    },
    async fetchAnnouncements() {
      this.loading = true
      this.error = null

      if (!this.currentUserKey && !this.currentRegion) {
        return
      }

      try {
        const appIds = this.notificationAppIds
        const fetchPromises = appIds.map(appId =>
          notificationsService.fetchActiveNotifications(appId)
        )

        const responses = await Promise.all(fetchPromises)

        const allItems = []
        const seenIds = new Set()

        responses.forEach(response => {
          if (response && response.items) {
            response.items.forEach(item => {
              if (!seenIds.has(item.id)) {
                seenIds.add(item.id)
                allItems.push(item)
              }
            })
          }
        })

        this.rawAnnouncements = allItems
        this.prepareAnnouncements()
      } catch (err) {
        this.error = err
        this.rawAnnouncements = []
      }
      this.loading = false
    },
    async loadSeenNotifications() {
      try {
        const storedIds = localStorage.getItem(this.localStorageKey)
        this.seenNotificationIds = storedIds ? JSON.parse(storedIds) : []
        this.userDataLoaded = true
      } catch (e) {
        this.seenNotificationIds = []
        this.userDataLoaded = true
      }
    },
    saveSeenNotifications() {
      try {
        localStorage.setItem(this.localStorageKey, JSON.stringify(this.seenNotificationIds))
      } catch (e) {
        //Issue with local storage setItem action
      }
    },
    closeNotification() {
      if (this.currentInternalNotification) {
        const idToMark = this.currentInternalNotification.id
        if (!this.seenNotificationIds.includes(idToMark)) {
          this.seenNotificationIds.push(idToMark)
          this.saveSeenNotifications()
        }
      }
    }
  }
}
</script>
<style scoped lang="scss">
@import "../../scss/colors";

.notification-bar-component {
  z-index: 1000;
  box-shadow: 0px -3px 0px 0px $orange inset, 0px 8px 16px 0px $hover-default;
}

.notification-container {
  max-width: 40rem;
}

.notification-title {
  color: $gray-800;
}

.notification-content {
  color: $gray-700;
}

.close-button {
  color: $orange !important;
  cursor: pointer !important;
}

.close-button-icon {
  transform: rotate(45deg);
}
</style>
