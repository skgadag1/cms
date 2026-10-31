export const useVisibility = () => {
    return {
        getVisibleContent: (node) => {
            if (!node) return
            return node?.[node?.field] || node
        }
    }
}