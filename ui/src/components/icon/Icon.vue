<template>
  <svg aria-hidden="true" :width="size" :height="size" class="icon">
    <use :xlink:href="symbolId" :fill="color" />
  </svg>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
    name: {
      type: String,
      required: true,
    },
    color: String,
    size: String
})

const symbolId = computed(() => `#icon-${props.name}`)
</script>