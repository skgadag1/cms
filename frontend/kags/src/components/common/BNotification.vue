<template>
  <b-alert show variant="white" class="border-2 text-black" body-class="d-flex flex-grow-1">
    <b-icon icon="info-circle" class="flex-grow-0 pe-3" />
    <div class="font-condensed">
      <slot />
    </div>
  </b-alert>
</template>
