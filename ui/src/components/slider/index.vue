<template>
  <div class="slider-wrap">
    <div class="slider-track-fill" :style="`width: ${position}%`"></div>
    <input type="range" class="slider-input" v-model="value" :step="props.step" :min="props.min" :max="props.max" />
    <div class="slider-thumb" :style="`--v-slider-thumb-position: ${position}%;`">
      <div class="slider-thumb-surface" />
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue'
const value = defineModel()
const props = defineProps({
  min: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 100
  },
  step: {
    type: Number,
    default: 1
  }
})

const position = computed(() => {
  return (value.value - props.min) / (props.max - props.min) * 100
})
</script>

<style scoped lang="less">
@import "./index.less";

</style>