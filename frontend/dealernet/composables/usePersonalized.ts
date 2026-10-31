export const usePersonalized = () => {
    const mainStore = useMainStore()
    const {getCurrentUser} = mainStore

    const getParams = () => ({
        contractType: getCurrentUser?.authority?.contractType,
        contractCode: getCurrentUser?.authority?.contractCode,
        dealerRole: getCurrentUser?.authority?.dealerRole
    })

    return {
        getParams
    }
}