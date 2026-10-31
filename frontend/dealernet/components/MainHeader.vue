<template>
    <header>
        <nav class="navbar navbar-expand-lg bg-dark text-white" data-bs-theme="dark">
            <div class="container-fluid px-0">
                <img src="~/assets/img/dealernet-workspace-img.svg" />
                <a class="navbar-brand bg-primary px-3 d-flex align-items-center" href="#">
                    <img :src="logoUrl" height="52" />
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse">
                    <ul class="navbar-nav">
                        <li v-for="(item, i) in snavItems" :key="`navigation-item__${i}`" class="nav-item">
                            <a :href="item.url" v-if="item.type==='linkMenu'" class="nav-link">{{item.label}}</a>
                            <Dropdown v-else-if="item.type==='listMenu'" :options="item.items" :modelValue="item" />
                        </li>
                    </ul>
                </div>

                <div class="flex-grow-1" />

                <div style="min-width: 10rem; max-width: 14rem">
                    <div class="input-group search-input">
                        <span class="bg-white input-group-text rounded-0">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search" viewBox="0 0 16 16">
                              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
                            </svg>
                        </span>
                        <input type="text" class="form-control rounded-0" id="search" placeholder="search">
                    </div>
                </div>

                <div class="d-flex">
                    <div class="collapse navbar-collapse">
                        <ul class="navbar-nav">
                            <li class="nav-item dropdown me-3">
                                <Dropdown :options="getUsers" :modelValue="getCurrentUser" @update:modelValue="userChanged">
                                    <div class="row d-inline-flex align-items-center justify-content-center" v-if="getCurrentUser.firstName" style="min-width: 230px">
                                        <div class="col-auto">
                                            <div class="rounded-circle bg-black d-flex align-items-center justify-content-center" style="height: 40px; width: 40px;">{{getCurrentUser.firstName[0]}}{{getCurrentUser.lastName[0]}}</div>
                                        </div>
                                        <div class="col">
                                            {{getCurrentUser.label}}
                                            <br/>
                                            <span class="small">{{getCurrentUser.authority.contractType}} | {{getCurrentUser.authority.contractCode}} | {{getCurrentUser.authority.dealerRole}}</span>
                                        </div>
                                    </div>
                                </Dropdown>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </nav>
    </header>
</template>

<script setup>
import { EditorContextHelper } from '@magnolia/vue-editor'
const inEditorMode = EditorContextHelper.inEditorPreview() || EditorContextHelper.inIframe()

const props = defineProps({
  logo: Object
})

const mainStore = useMainStore()
const {getCurrentUser, getUsers} = mainStore
const { $cloudinary, $config } = useNuxtApp()
const snavItems = ref([])
const fullPath = useRoute().path
const logoUrl = $cloudinary.generateUrl(props.logo.src)
const linkUtils = useLinks()

const userChanged = (user) => {
  if (user.id !== getCurrentUser?.id) {
    mainStore.setCurrentUser(user)
    reloadNuxtApp({force: true})
  }
}

const _region = (fullPath.startsWith('/dealernet') ? linkUtils.removeBasename(fullPath) : fullPath)?.split('/')?.filter(x => x?.length)?.[0]
const {data} = await useFetch(`${$config.public.DEALERNET_API_HOST}/navigation`, {
  params: {
    lang: 'en',
    region: _region,
    instance: 'author',
    ...usePersonalized().getParams()
  }
})

snavItems.value = (await data?.value || []).map(i => {
  return {
    ...i,
    label: i.label,
    url: linkUtils.removeBasename(i?.link?.url),
    ...(i.items?.length && {items: i.items.map(itm => { return {...itm, url: linkUtils.removeBasename(itm?.link?.url)}})})
  }
})
</script>

<style lang="scss">
header .navbar {
    min-height: 4rem;
}
</style>

<style lang="scss" scoped>
.search-input {
    .input-group-text,
    input {
        height: 64px;
        border-color: white;
        background-color: white;
    }
}
</style>