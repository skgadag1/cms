// main is the name of the store. It is unique across your application
// and will appear in devtools
export const useMainStore = defineStore('main', {
  state: () => ({
    user: {},
    users: []
  }),
  getters: {
    getCurrentUser: (state) => state.user?.id ? state.user : state.users?.[0] || {},
    getUsers: (state) => state.users?.length ? state.users : state.setUsers()
  },
  actions: {
    setCurrentUser(u) { this.user = u },
    async setUsers(u) {
      if (!this.users?.length) {
        if (u?.length) {
          this.users = u
        } else {
          const { $config } = useNuxtApp()
          const data = await $fetch(`${$config.public.DEALERNET_API_HOST}/users?lang=en`)
          this.users?.push(...data?.map(u => { return {...u, label: `${u.firstName} ${u.lastName}`, value: `${u.id}`} }))
        }
      }
      return this.users
    }
  },
  persist: true
})