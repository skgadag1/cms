<template>
    <div class="row">
        <div class="col-12">
            <h3 class="d-flex align-items-center justify-content-between">
                <div class="d-inline-flex align-items-center">{{title}} <span class="ms-2 rounded-circle badge bg-primary" v-if="counter">{{counter}}</span></div>
                <a :href="link.url" v-if="link">
                    <span class="h5 m-0" v-html="link.label" />
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-right mx-1" viewBox="0 0 16 16">
                        <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </a>
            </h3>
            <hr />
        </div>
    </div>
</template>

<script setup>
defineProps({
  title: String,
  link: Object,
  counter: [String, Number]
})
</script>