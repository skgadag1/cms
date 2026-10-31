import {computed} from 'vue'
import {useStore} from 'vuex'

type MenuItem = {
  title: string
  disabled?: boolean
  forInternals?: boolean
  forSupertechs?: boolean
  forKeyAccounts?: Array<{id: string | number}>
  [key: string]: any
}

type MenuUser = {
  internal?: boolean
  expertLevel?: string
  companyId?: string | number
}

export function useMenu() {
  const store = useStore()
  const getCurrentUser = computed(() => store.getters.getCurrentUser as MenuUser)
  const isCurrentMachineAuthorized = computed(() => store.getters.isCurrentMachineAuthorized as boolean)
  const isCurrentMachineValidSubscription = computed(() => store.getters.isCurrentMachineValidSubscription as boolean)
  const isCurrentMachineConnected = computed(() => store.getters.isCurrentMachineConnected as boolean)

  const isAvailable = (item: MenuItem) => {
    let allowed: boolean | {id: string | number} | undefined = true
    if (getCurrentUser.value.internal) return true // allow for internals
    if ('forInternals' in item && item.forInternals) return false // disallowed for non-internals
    if ('forSupertechs' in item) allowed = item.forSupertechs ? getCurrentUser.value.expertLevel === 'supertech' : true
    if ('forKeyAccounts' in item && item.forKeyAccounts?.length) {
      allowed = Boolean(allowed) && item.forKeyAccounts.find(element => element.id === getCurrentUser.value.companyId)
    }
    return allowed
  }

  const isDisabled = (item: MenuItem) => item.disabled
    || (!isCurrentMachineAuthorized.value && ['faultcodes'].filter(x => item.title.replace(' ', '').toLowerCase().startsWith(x)).length > 0)
    || (!isCurrentMachineValidSubscription.value && ['faultcodes'].filter(x => item.title.replace(' ', '').toLowerCase().startsWith(x)).length > 0)
    || (!isCurrentMachineConnected.value && ['locationhistory', 'faultcodes'].filter(x => item.title.replace(' ', '').toLowerCase().startsWith(x)).length > 0)

  return {
    getCurrentUser,
    isCurrentMachineAuthorized,
    isCurrentMachineValidSubscription,
    isCurrentMachineConnected,
    isAvailable,
    isDisabled
  }
}
