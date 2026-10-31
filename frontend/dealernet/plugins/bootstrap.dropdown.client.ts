import { Dropdown } from "bootstrap"

export default defineNuxtPlugin(() => ({
    provide: {
        bootstrap: {
            Dropdown,
        },
    },
}))