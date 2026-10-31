export const useLinks = () => {
    const { $config } = useNuxtApp()

    const removeBasename = (path) => path?.startsWith($config.public.NUXT_APP_MGNL_SITE_PATH) ? path?.replace($config.public.NUXT_APP_MGNL_SITE_PATH, '') : path

    return {
        removeBasename
    }
}