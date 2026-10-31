<template>
    <div class="container py-5">
        <h1>{{error.statusCode}} - {{ error.message }}</h1>
    </div>
</template>

<script setup>
const error = useError()
</script>