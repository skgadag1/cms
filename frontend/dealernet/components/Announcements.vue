<template>
    <div class="container py-5">
        <div class="row">
            <div class="col-12">
                <h2>{{ mainTitle }}</h2>
            </div>

            <div class="col-12">
                <div class="accordion shadow" :id="accordionId">
                    <div class="accordion-item bg-white">
                        <h4 class="accordion-header text-uppercase" id="headingOne">
                            <button class="accordion-button text-uppercase" type="button" data-bs-toggle="collapse" data-bs-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                                {{ announcementsTitle }} <span class="ms-2 rounded-circle badge bg-primary">{{announcements.length}}</span>
                            </button>
                        </h4>
                        <div id="collapseOne" class="accordion-collapse collapse show" aria-labelledby="headingOne" data-bs-parent="#accordionId">
                            <div class="accordion-body" :class="{ 'px-0': announcements.length}">
                                <div class="row" v-if="announcements && announcements.length">
                                    <div class="col-12 col-md-8 offset-md-2 py-2">
                                        <h4 class="px-3">{{ announcements[currentAnnouncement].title }}</h4>
                                    </div>
                                    <div class="col-12"><div class="border-bottom"></div></div>
                                    <div class="col-12 col-md-8 offset-md-2 py-3">
                                        <div v-html="announcements[currentAnnouncement].content" class="px-3" />
                                    </div>
                                    <div class="col-12 col-md-8 offset-md-2 pb-3" v-if="announcements.length > 1">
                                        <div class="d-flex flex-row px-3">
                                            <button class="btn btn-primary" :disabled="currentAnnouncement === 0" @click.prevent="() => currentAnnouncement--">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-left" viewBox="0 0 16 16">
                                                    <path fill-rule="evenodd" d="M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0z"/>
                                                </svg>
                                                Preview
                                            </button>
                                            <div class="flex-grow-1" />
                                            <button class="btn btn-primary" :disabled="currentAnnouncement === announcements.length - 1" @click.prevent="() => currentAnnouncement++">
                                                Next
                                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-right" viewBox="0 0 16 16">
                                                    <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="row" v-else>
                                    <div class="col-12">
                                        <div class="alert alert-warning">No annoucements.</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
  title: [String, Object],
  announcementsTitle: String,
})
const {$config} = useNuxtApp()
const mainStore = useMainStore()
const {getCurrentUser} = mainStore

const accordionId = Math.random().toString(16).substring(2,8)
const mainTitle = computed(() => useVisibility().getVisibleContent(props.title)?.replace('%USER%', getCurrentUser.firstName || getCurrentUser.label)?.replace('%USER_ROLE%', getCurrentUser.authority?.dealerRole))

const announcements = ref([])
const currentAnnouncement = ref(null)

const {data} = await useFetch(`${$config.public.DEALERNET_API_HOST}/announcements`, {
  params: {...usePersonalized().getParams()}
})

announcements.value = data?.value
currentAnnouncement.value = 0
</script>

<style lang="scss">
.accordion p {
    font-family: inherit !important;
    font-size: inherit !important;
}
</style>