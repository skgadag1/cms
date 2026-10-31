<template>
    <div class="container py-5">
        <ComponentHeader title="Updated Cases" :counter="2" :link="{label: 'View all Cases', url: '#'}" />
    </div>
</template>

<script setup>
useHead({
  script: [{ src: 'https://doosan-nao--qa1.sandbox.lightning.force.com/lightning/lightning.out.js' }]
})
</script>