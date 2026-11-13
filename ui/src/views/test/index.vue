<script setup>
import { aliases } from '@/assets/icons/icon.js'
</script>

<template>
  <div class="ma-10">
    <a class="mx-10" target="_blank" href="https://pictogrammers.com/library/mdi/">MDI</a>
    <a class="mx-10" target="_blank" href="https://fontawesome.com/icons/">Fontawesome</a>
  </div>
  <div class="d-flex">
    <div class="d-flex flex-column align-center justify-center ma-10" v-for="item in Object.keys(aliases)">
      <div><v-icon :icon="item" size="1.5rem"></v-icon></div>
      <div>{{ item }}</div>
    </div>

  </div>
</template>

<style scoped lang="scss">

</style>
