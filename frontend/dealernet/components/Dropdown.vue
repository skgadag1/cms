<template>
    <div class="dropdown">
        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            <slot>
                {{modelValue.label}}
            </slot>
        </a>
        <ul class="dropdown-menu dropdown-menu-end">
            <li v-for="(u, i) in options" :key="`users__${u.id}_${i}`" @click="$emit('update:modelValue', u)"><a class="dropdown-item text-white" :href="u.url || '#'">{{ u.label }}</a></li>
        </ul>
    </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: [Object, null],
    default: () => { return {}}
  },
  options: Array
})
defineEmits(['update:modelValue'])
</script>

