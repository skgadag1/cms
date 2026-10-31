export default defineNuxtRouteMiddleware(async () => {
    const { $config, $pinia } = useNuxtApp()
    const mainStore = useMainStore($pinia)

    if (!mainStore.getCurrentUser?.id) {
        console.debug('[auth middleware] user not set, setting default')
        const data = (await $fetch(`${$config.public.DEALERNET_API_HOST}/users?lang=en`))
        const users = data?.map(u => { return {...u, label: `${u.firstName} ${u.lastName}`, value: `${u.id}`} }) || []
        mainStore.setCurrentUser(users?.[0] || {})
        mainStore.setUsers(users)
    }
})